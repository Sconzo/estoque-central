package com.estoquecentral.sales.application;

import com.estoquecentral.sales.adapter.out.FiscalEventRepository;
import com.estoquecentral.sales.domain.FiscalEvent;
import com.estoquecentral.sales.domain.FiscalEventType;
import com.estoquecentral.sales.domain.Sale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * NotificationService - Handles notifications for NFCe failures
 * Story 4.4: NFCe Retry Queue and Failure Management
 *
 * <p>Currently logs errors for permanent failures.
 * Future enhancement: integrate with email service (SendGrid, AWS SES, etc.)
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final FiscalEventRepository fiscalEventRepository;

    public NotificationService(FiscalEventRepository fiscalEventRepository) {
        this.fiscalEventRepository = fiscalEventRepository;
    }

    /**
     * Notify about permanent NFCe failure
     * Currently logs error and creates NFCE_FAILED fiscal event
     *
     * TODO: Integrate with email service for actual notifications
     * - Send email to admin/manager with sale details
     * - Include error message, sale number, customer info
     * - Provide link to retry or cancel sale
     *
     * @param sale sale that permanently failed
     * @param errorMessage error description
     */
    public void notifyPermanentFailure(Sale sale, String errorMessage) {
        logger.error("PERMANENT NFCE FAILURE - Sale ID: {}, Sale Number: {}, Error: {}",
                sale.getId(), sale.getSaleNumber(), errorMessage);

        // Create fiscal event for permanent failure
        FiscalEvent failureEvent = new FiscalEvent();
        failureEvent.setTenantId(sale.getTenantId());
        failureEvent.setSaleId(sale.getId());
        failureEvent.setEventType(FiscalEventType.NFCE_FAILED);
        failureEvent.setErrorMessage(errorMessage);
        failureEvent.setUserId(sale.getCreatedByUserId());
        failureEvent.setTimestamp(LocalDateTime.now());

        fiscalEventRepository.save(failureEvent);

        // TODO: Send email notification
        // Example:
        // emailService.send(
        //     to: getTenantAdminEmail(sale.getTenantId()),
        //     subject: "NFCe Emission Failed - Sale " + sale.getSaleNumber(),
        //     body: buildFailureEmailBody(sale, errorMessage)
        // );

        logger.info("Permanent failure notification logged for sale {}", sale.getId());
    }

    /**
     * Notify about manual intervention required
     * Used when sale needs human review
     *
     * @param sale sale requiring intervention
     * @param reason reason for intervention
     */
    public void notifyManualInterventionRequired(Sale sale, String reason) {
        logger.warn("MANUAL INTERVENTION REQUIRED - Sale ID: {}, Sale Number: {}, Reason: {}",
                sale.getId(), sale.getSaleNumber(), reason);

        // TODO: Send notification to operations team
        // - Could be Slack, email, or internal task system
        // - Include sale details and recommended actions
    }

    /**
     * Notify about successful retry after failures
     * Informational notification for monitoring
     *
     * @param sale sale that succeeded after retries
     * @param attemptCount number of attempts before success
     */
    public void notifyRetrySuccess(Sale sale, int attemptCount) {
        if (attemptCount > 1) {
            logger.info("NFCe emission succeeded after {} attempts for sale {}",
                    attemptCount, sale.getSaleNumber());

            // TODO: Could send success notification if multiple retries occurred
            // Useful for monitoring system health and identifying issues
        }
    }
}
