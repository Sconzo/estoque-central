package com.estoquecentral.marketplace.application;

import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.domain.Inventory;
import com.estoquecentral.marketplace.adapter.out.*;
import com.estoquecentral.marketplace.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service for synchronizing stock to marketplaces
 * Story 5.4: Stock Synchronization to Mercado Livre
 */
@Service
public class MarketplaceStockSyncService {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceStockSyncService.class);
    private static final int BATCH_SIZE = 10;

    private final MarketplaceSyncQueueRepository queueRepository;
    private final MarketplaceSyncLogRepository logRepository;
    private final MarketplaceListingRepository listingRepository;
    private final InventoryRepository inventoryRepository;
    private final MercadoLivreApiClient apiClient;

    public MarketplaceStockSyncService(
        MarketplaceSyncQueueRepository queueRepository,
        MarketplaceSyncLogRepository logRepository,
        MarketplaceListingRepository listingRepository,
        InventoryRepository inventoryRepository,
        MercadoLivreApiClient apiClient
    ) {
        this.queueRepository = queueRepository;
        this.logRepository = logRepository;
        this.listingRepository = listingRepository;
        this.inventoryRepository = inventoryRepository;
        this.apiClient = apiClient;
    }

    /**
     * AC1: Enqueue product for sync (with deduplication)
     */
    @Transactional
    public void enqueueStockSync(UUID tenantId, UUID productId, UUID variantId, Marketplace marketplace) {
        // Check if already in queue (deduplication)
        var existing = queueRepository.findExistingQueueItem(
            tenantId, productId, variantId,
            marketplace.name(), SyncType.STOCK.name()
        );

        if (existing.isPresent()) {
            log.debug("Product {} already in sync queue, skipping", productId);
            return;
        }

        // Create new queue item
        MarketplaceSyncQueue queueItem = new MarketplaceSyncQueue(tenantId, productId, variantId, marketplace);
        queueRepository.save(queueItem);

        log.info("Enqueued product {} for stock sync to {}", productId, marketplace);
    }

    /**
     * AC3: Manual sync - force immediate sync with high priority
     */
    @Transactional
    public void syncStockManually(UUID tenantId, UUID productId) {
        log.info("Manual stock sync requested for product: {}", productId);

        // Find listings for this product
        List<MarketplaceListing> listings = listingRepository.findByProductId(productId);

        if (listings.isEmpty()) {
            log.warn("No marketplace listings found for product: {}", productId);
            return;
        }

        // Create high-priority queue items
        for (MarketplaceListing listing : listings) {
            MarketplaceSyncQueue queueItem = new MarketplaceSyncQueue(
                tenantId, productId, listing.getVariantId(), listing.getMarketplace()
            );
            queueItem.setPriority(1); // High priority
            queueRepository.save(queueItem);
        }

        log.info("Created {} high-priority sync queue items for manual sync", listings.size());
    }

    /**
     * AC2: Process sync queue (called by scheduled worker)
     */
    @Transactional
    public void processSyncQueue() {
        List<MarketplaceSyncQueue> pendingItems = queueRepository.findPendingItems(BATCH_SIZE);

        if (pendingItems.isEmpty()) {
            return;
        }

        log.info("Processing {} items from sync queue", pendingItems.size());

        for (MarketplaceSyncQueue item : pendingItems) {
            processSyncItem(item);
        }
    }

    /**
     * Process a single sync queue item with retry logic
     * AC5: Tratamento de Erros
     */
    private void processSyncItem(MarketplaceSyncQueue item) {
        item.markAsProcessing();
        queueRepository.save(item);

        try {
            // Sync the stock
            syncStockToMarketplace(item);

            // Mark as completed
            item.markAsCompleted();
            queueRepository.save(item);

            log.info("Successfully synced product {} to {}", item.getProductId(), item.getMarketplace());

        } catch (Exception e) {
            log.error("Error syncing product {} to {}: {}",
                item.getProductId(), item.getMarketplace(), e.getMessage());

            // AC5: Retry logic with backoff
            item.markAsFailed(e.getMessage());
            queueRepository.save(item);

            // If max retries exceeded, log final failure
            if (!item.canRetry()) {
                log.error("Max retries exceeded for product {}. Marking as FAILED.", item.getProductId());
                createSyncLog(item, SyncStatus.FAILED, null, null, e.getMessage());
            }
        }
    }

    /**
     * AC2: Sync stock to marketplace
     */
    private void syncStockToMarketplace(MarketplaceSyncQueue item) {
        // Find listing for this product/variant
        MarketplaceListing listing;
        if (item.getVariantId() != null) {
            listing = listingRepository.findByVariantId(item.getVariantId())
                .orElseThrow(() -> new IllegalStateException("Listing not found for variant: " + item.getVariantId()));
        } else {
            List<MarketplaceListing> listings = listingRepository.findByProductId(item.getProductId());
            if (listings.isEmpty()) {
                throw new IllegalStateException("No listing found for product: " + item.getProductId());
            }
            listing = listings.get(0);
        }

        // Calculate available stock
        // AC2: quantity_available - quantity_reserved
        BigDecimal availableStock = calculateAvailableStock(item.getTenantId(), item.getProductId(), item.getVariantId());

        // Apply safety margin (Story 5.7 - for now, use 100% = no margin)
        BigDecimal syncQuantity = availableStock;

        BigDecimal oldQuantity = listing.getQuantity() != null ? BigDecimal.valueOf(listing.getQuantity()) : BigDecimal.ZERO;

        // Only sync if quantity changed
        if (syncQuantity.compareTo(oldQuantity) == 0) {
            log.debug("Stock unchanged for product {}, skipping sync", item.getProductId());
            createSyncLog(item, SyncStatus.SUCCESS, oldQuantity, syncQuantity, "Stock unchanged, skipped");
            return;
        }

        // AC2.3: PUT /items/{listing_id} to update stock in ML
        updateStockInMarketplace(item.getTenantId(), listing, syncQuantity);

        // Update local listing
        listing.setQuantity(syncQuantity.intValue());
        listingRepository.save(listing);

        // Create success log
        createSyncLog(item, SyncStatus.SUCCESS, oldQuantity, syncQuantity, null);

        log.info("Synced stock for product {}: {} -> {}", item.getProductId(), oldQuantity, syncQuantity);
    }

    /**
     * Calculate available stock for a product/variant
     * Aggregates inventory across all locations
     * AC2: Sum (quantityAvailable - reservedQuantity) for all locations
     */
    private BigDecimal calculateAvailableStock(UUID tenantId, UUID productId, UUID variantId) {
        List<Inventory> inventories;

        if (variantId != null) {
            // For variants, aggregate inventory by variantId across all locations
            inventories = inventoryRepository.findAllByTenantIdAndVariantId(tenantId, variantId);
        } else {
            // For simple products, aggregate inventory by productId across all locations
            inventories = inventoryRepository.findAllByTenantIdAndProductId(tenantId, productId);
        }

        // Sum quantity for sale (quantityAvailable - reserved) across all locations
        BigDecimal totalAvailable = BigDecimal.ZERO;
        for (Inventory inventory : inventories) {
            // Use the computed quantity for sale from the domain entity
            BigDecimal forSale = inventory.getComputedQuantityForSale();
            totalAvailable = totalAvailable.add(forSale);
        }

        // Never negative
        return totalAvailable.max(BigDecimal.ZERO);
    }

    /**
     * Update stock in marketplace via API
     */
    private void updateStockInMarketplace(UUID tenantId, MarketplaceListing listing, BigDecimal quantity) {
        if (listing.getMarketplace() == Marketplace.MERCADO_LIVRE) {
            // Build update payload
            var updatePayload = new java.util.HashMap<String, Object>();
            updatePayload.put("available_quantity", quantity.intValue());

            // PUT /items/{listing_id}
            String endpoint = "/items/" + listing.getListingIdMarketplace();

            try {
                apiClient.put(endpoint, updatePayload, Object.class, tenantId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update stock in Mercado Livre: " + e.getMessage(), e);
            }
        }
    }

    /**
     * AC4: Create sync log entry
     */
    private void createSyncLog(MarketplaceSyncQueue item, SyncStatus status,
                               BigDecimal oldValue, BigDecimal newValue, String errorMessage) {
        MarketplaceSyncLog log = new MarketplaceSyncLog(
            item.getTenantId(),
            item.getProductId(),
            item.getVariantId(),
            item.getMarketplace(),
            item.getSyncType(),
            oldValue,
            newValue,
            status
        );

        log.setErrorMessage(errorMessage);
        log.setRetryCount(item.getRetryCount());

        logRepository.save(log);
    }
}
