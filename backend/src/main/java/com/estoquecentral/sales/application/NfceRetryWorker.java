package com.estoquecentral.sales.application;

import com.estoquecentral.sales.adapter.out.FiscalEventRepository;
import com.estoquecentral.sales.adapter.out.SaleItemRepository;
import com.estoquecentral.sales.adapter.out.SaleRepository;
import com.estoquecentral.sales.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * NfceRetryWorker - Background worker for processing NFCe retries
 * Story 4.4: NFCe Retry Queue and Failure Management
 *
 * <p>Scheduled task that:
 * <ol>
 *   <li>Polls retry queue every 60 seconds</li>
 *   <li>Loads sale and attempts NFCe emission</li>
 *   <li>On success: marks as EMITTED, creates fiscal event</li>
 *   <li>On failure: reenqueues if attempts < 10, else marks as FAILED and notifies</li>
 * </ol>
 */
@Component
@EnableScheduling
public class NfceRetryWorker {

    private static final Logger logger = LoggerFactory.getLogger(NfceRetryWorker.class);

    private final RetryQueueService retryQueueService;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final NfceService nfceService;
    private final FiscalEventRepository fiscalEventRepository;
    private final NotificationService notificationService;

    private int retryAttempts = 0; // Track retry attempts for a sale

    public NfceRetryWorker(
            RetryQueueService retryQueueService,
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            NfceService nfceService,
            FiscalEventRepository fiscalEventRepository,
            NotificationService notificationService) {
        this.retryQueueService = retryQueueService;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.nfceService = nfceService;
        this.fiscalEventRepository = fiscalEventRepository;
        this.notificationService = notificationService;
    }

    /**
     * Process retries from queue every 60 seconds
     * Polls queue and processes one sale at a time
     */
    @Scheduled(fixedDelay = 60000) // 60 seconds
    @Transactional
    public void processRetries() {
        try {
            // Poll next sale from queue
            UUID saleId = retryQueueService.dequeue();

            if (saleId == null) {
                // Queue is empty, nothing to process
                return;
            }

            logger.info("Processing retry for sale: {}", saleId);
            processSaleRetry(saleId);

        } catch (Exception e) {
            logger.error("Error processing retry queue: {}", e.getMessage(), e);
        }
    }

    /**
     * Process retry for a single sale
     * Attempts NFCe emission and handles success/failure
     *
     * @param saleId sale ID to retry
     */
    private void processSaleRetry(UUID saleId) {
        // Load sale entity
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalStateException("Sale not found: " + saleId));

        // Only retry if status is PENDING
        if (!sale.isNfcePending()) {
            logger.warn("Sale {} is not in PENDING status (status: {}). Skipping retry.",
                    saleId, sale.getNfceStatus());
            return;
        }

        // Count previous retry attempts by looking at fiscal events
        retryAttempts = countRetryAttempts(saleId);

        // Check if max attempts reached
        if (retryAttempts >= RetryQueueService.MAX_ATTEMPTS) {
            handlePermanentFailure(sale);
            return;
        }

        // Load sale items for NFCe emission
        List<SaleItem> saleItems = saleItemRepository.findBySaleId(saleId);

        try {
            // Attempt NFCe emission
            logger.info("Attempting NFCe emission for sale {} (attempt {}/{})",
                    saleId, retryAttempts + 1, RetryQueueService.MAX_ATTEMPTS);

            NfceService.NfceResponse nfceResponse = nfceService.emitNfce(sale, saleItems);

            // Success: update status and create fiscal event
            sale.markNfceAsEmitted(nfceResponse.nfceKey(), nfceResponse.xml());
            saleRepository.save(sale);

            createFiscalEvent(
                    sale.getTenantId(),
                    saleId,
                    FiscalEventType.NFCE_EMITTED,
                    nfceResponse.nfceKey(),
                    nfceResponse.xml(),
                    null,
                    sale.getCreatedByUserId()
            );

            logger.info("NFCe successfully emitted for sale {} after {} attempts",
                    saleId, retryAttempts + 1);

        } catch (NfceService.NfceException e) {
            // Failure: increment attempt and reenqueue or mark as failed
            handleRetryFailure(sale, e);
        }
    }

    /**
     * Handle retry failure
     * Reenqueues if attempts < MAX_ATTEMPTS, else marks as permanent failure
     *
     * @param sale sale entity
     * @param error NFCe exception
     */
    private void handleRetryFailure(Sale sale, NfceService.NfceException error) {
        retryAttempts++;

        logger.warn("NFCe emission failed for sale {} (attempt {}/{}): {}",
                sale.getId(), retryAttempts, RetryQueueService.MAX_ATTEMPTS, error.getMessage());

        // Create retry fiscal event
        FiscalEvent retryEvent = new FiscalEvent();
        retryEvent.setTenantId(sale.getTenantId());
        retryEvent.setSaleId(sale.getId());
        retryEvent.setEventType(FiscalEventType.NFCE_RETRY);
        retryEvent.setRetryCount(retryAttempts);
        retryEvent.setErrorMessage(error.getMessage());
        retryEvent.setUserId(sale.getCreatedByUserId());
        fiscalEventRepository.save(retryEvent);

        // Check if max attempts reached
        if (retryAttempts >= RetryQueueService.MAX_ATTEMPTS) {
            handlePermanentFailure(sale);
        } else {
            // Reenqueue with exponential backoff
            retryQueueService.enqueue(sale.getId(), retryAttempts);
            logger.info("Sale {} reenqueued for retry (attempt {}/{})",
                    sale.getId(), retryAttempts, RetryQueueService.MAX_ATTEMPTS);
        }
    }

    /**
     * Handle permanent failure after max attempts
     * Marks sale as FAILED and sends notification
     *
     * @param sale sale entity
     */
    private void handlePermanentFailure(Sale sale) {
        logger.error("NFCe emission permanently failed for sale {} after {} attempts",
                sale.getId(), RetryQueueService.MAX_ATTEMPTS);

        // Mark sale as FAILED
        String errorMessage = String.format("NFCe emission failed after %d retry attempts",
                RetryQueueService.MAX_ATTEMPTS);
        sale.markNfceAsFailed(errorMessage);
        saleRepository.save(sale);

        // Send notification
        notificationService.notifyPermanentFailure(sale, errorMessage);

        logger.info("Sale {} marked as FAILED and notification sent", sale.getId());
    }

    /**
     * Count retry attempts by looking at NFCE_RETRY fiscal events
     *
     * @param saleId sale ID
     * @return number of retry attempts
     */
    private int countRetryAttempts(UUID saleId) {
        List<FiscalEvent> events = fiscalEventRepository.findBySaleIdOrderByTimestampDesc(saleId);

        return (int) events.stream()
                .filter(e -> e.getEventType() == FiscalEventType.NFCE_RETRY)
                .count();
    }

    /**
     * Create fiscal event for audit trail
     *
     * @param tenantId tenant ID
     * @param saleId sale ID
     * @param eventType event type
     * @param nfceKey NFCe key
     * @param xmlSnapshot XML snapshot
     * @param errorMessage error message
     * @param userId user ID
     */
    private void createFiscalEvent(
            UUID tenantId,
            UUID saleId,
            FiscalEventType eventType,
            String nfceKey,
            String xmlSnapshot,
            String errorMessage,
            UUID userId) {

        FiscalEvent event = new FiscalEvent();
        event.setTenantId(tenantId);
        event.setSaleId(saleId);
        event.setEventType(eventType);
        event.setNfceKey(nfceKey);
        event.setXmlSnapshot(xmlSnapshot);
        event.setErrorMessage(errorMessage);
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());

        fiscalEventRepository.save(event);
    }
}
