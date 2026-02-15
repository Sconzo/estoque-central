package com.estoquecentral.sales.application;

import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.adapter.out.StockMovementRepository;
import com.estoquecentral.inventory.domain.Inventory;
import com.estoquecentral.inventory.domain.MovementType;
import com.estoquecentral.inventory.domain.StockMovement;
import com.estoquecentral.marketplace.application.MarketplaceStockSyncService;
import com.estoquecentral.marketplace.adapter.out.MarketplaceListingRepository;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.sales.adapter.out.FiscalEventRepository;
import com.estoquecentral.sales.adapter.out.SaleItemRepository;
import com.estoquecentral.sales.adapter.out.SaleRepository;
import com.estoquecentral.sales.domain.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SaleService - Business logic for sale processing
 * Story 4.3: NFCe Emission and Stock Decrease
 *
 * <p>This service handles the complete sale flow:
 * <ol>
 *   <li>Validate stock availability for all items</li>
 *   <li>Create Sale entity with generated sale number</li>
 *   <li>Create SaleItems and decrease stock for each</li>
 *   <li>Try to emit NFCe (success: EMITTED, fail: PENDING)</li>
 *   <li>Create fiscal events for audit trail (NFR16)</li>
 * </ol>
 *
 * @see Sale
 * @see SaleItem
 * @see FiscalEvent
 */
@Service
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final FiscalEventRepository fiscalEventRepository;
    private final SaleNumberGenerator saleNumberGenerator;
    private final NfceService nfceService;
    @Nullable
    private final RetryQueueService retryQueueService;
    private final NotificationService notificationService;
    private final MarketplaceStockSyncService marketplaceStockSyncService;
    private final MarketplaceListingRepository marketplaceListingRepository;

    public SaleService(
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            FiscalEventRepository fiscalEventRepository,
            SaleNumberGenerator saleNumberGenerator,
            NfceService nfceService,
            @Nullable RetryQueueService retryQueueService,
            NotificationService notificationService,
            MarketplaceStockSyncService marketplaceStockSyncService,
            MarketplaceListingRepository marketplaceListingRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.fiscalEventRepository = fiscalEventRepository;
        this.saleNumberGenerator = saleNumberGenerator;
        this.nfceService = nfceService;
        this.retryQueueService = retryQueueService;
        this.notificationService = notificationService;
        this.marketplaceStockSyncService = marketplaceStockSyncService;
        this.marketplaceListingRepository = marketplaceListingRepository;
    }

    /**
     * Process a complete sale transaction
     * Story 4.3 - AC1, AC2, AC3, AC4
     *
     * @param request sale request with items
     * @return created sale with NFCe status
     * @throws InsufficientStockException if stock is insufficient for any item
     */
    @Transactional
    public Sale processSale(SaleRequest request) {
        // AC1: Validate stock availability for all items
        validateStockAvailability(request.tenantId(), request.stockLocationId(), request.items());

        // AC2: Create Sale entity with generated sale number
        Sale sale = createSale(request);
        sale = saleRepository.save(sale);

        // AC2: Create SaleItems and decrease stock for each
        List<SaleItem> saleItems = new ArrayList<>();
        for (ItemRequest itemRequest : request.items()) {
            SaleItem saleItem = createSaleItem(sale.getId(), itemRequest);
            saleItem = saleItemRepository.save(saleItem);
            saleItems.add(saleItem);

            // Decrease stock and create stock movement
            decreaseStock(
                    request.tenantId(),
                    request.stockLocationId(),
                    itemRequest.productId(),
                    itemRequest.variantId(),
                    itemRequest.quantity(),
                    sale.getId(),
                    request.userId()
            );

            // Story 5.4: Enqueue stock sync to marketplaces after sale
            enqueueMarketplaceStockSync(
                    request.tenantId(),
                    itemRequest.productId(),
                    itemRequest.variantId()
            );
        }

        // AC3: Try to emit NFCe
        try {
            NfceService.NfceResponse nfceResponse = nfceService.emitNfce(sale, saleItems);
            sale.markNfceAsEmitted(nfceResponse.nfceKey(), nfceResponse.xml());
            sale = saleRepository.save(sale);

            // AC4: Create fiscal event for successful emission
            createFiscalEvent(
                    request.tenantId(),
                    sale.getId(),
                    FiscalEventType.NFCE_EMITTED,
                    nfceResponse.nfceKey(),
                    nfceResponse.xml(),
                    null,
                    null,
                    request.userId()
            );

        } catch (NfceService.NfceException e) {
            // AC3: NFCe emission failed - mark as PENDING (not FAILED)
            // Sale is still valid, NFCe can be retried later
            sale.markNfceAsPending();
            sale = saleRepository.save(sale);

            // AC4: Create fiscal event for failed emission
            createFiscalEvent(
                    request.tenantId(),
                    sale.getId(),
                    FiscalEventType.NFCE_FAILED,
                    null,
                    null,
                    e.getMessage(),
                    null,
                    request.userId()
            );
        }

        return sale;
    }

    // ==================== Helper Methods ====================

    /**
     * Validates that sufficient stock is available for all items
     * AC1: Stock validation before processing sale
     *
     * @throws InsufficientStockException if stock is insufficient for any item
     */
    private void validateStockAvailability(UUID tenantId, UUID locationId, List<ItemRequest> items) {
        for (ItemRequest item : items) {
            Inventory inventory;

            if (item.variantId() != null) {
                // Variant product
                inventory = inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                        tenantId, item.variantId(), locationId
                ).orElseThrow(() -> new InsufficientStockException(
                        "No inventory found for variant: " + item.variantId()
                ));
            } else {
                // Simple/composite product
                inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                        tenantId, item.productId(), locationId
                ).orElseThrow(() -> new InsufficientStockException(
                        "No inventory found for product: " + item.productId()
                ));
            }

            BigDecimal availableForSale = inventory.getComputedQuantityForSale();
            if (availableForSale.compareTo(item.quantity()) < 0) {
                String itemIdentifier = item.variantId() != null
                        ? "variant " + item.variantId()
                        : "product " + item.productId();
                throw new InsufficientStockException(
                        String.format("Insufficient stock for %s. Available: %s, Requested: %s",
                                itemIdentifier, availableForSale, item.quantity())
                );
            }
        }
    }

    /**
     * Creates Sale entity with generated sale number and calculated totals
     * AC2: Create sale record
     */
    private Sale createSale(SaleRequest request) {
        Sale sale = new Sale();
        sale.setTenantId(request.tenantId());
        sale.setSaleNumber(saleNumberGenerator.generateSaleNumber(request.tenantId()));
        sale.setCustomerId(request.customerId());
        sale.setStockLocationId(request.stockLocationId());
        sale.setPaymentMethod(request.paymentMethod());
        sale.setPaymentAmountReceived(request.paymentAmountReceived());
        sale.setDiscount(request.discount() != null ? request.discount() : BigDecimal.ZERO);
        sale.setTotalAmount(calculateTotal(request.items(), sale.getDiscount()));
        sale.calculateChangeAmount();
        sale.setCreatedByUserId(request.userId());
        sale.setSaleDate(LocalDateTime.now());
        sale.setNfceStatus(NfceStatus.PENDING);

        return sale;
    }

    /**
     * Creates a SaleItem entity
     * AC2: Create sale item records
     */
    private SaleItem createSaleItem(UUID saleId, ItemRequest itemRequest) {
        SaleItem saleItem = new SaleItem();
        saleItem.setSaleId(saleId);
        saleItem.setProductId(itemRequest.productId());
        saleItem.setVariantId(itemRequest.variantId());
        saleItem.setQuantity(itemRequest.quantity());
        saleItem.setUnitPrice(itemRequest.unitPrice());
        saleItem.setDiscount(itemRequest.discount() != null ? itemRequest.discount() : BigDecimal.ZERO);
        saleItem.calculateTotalPrice();

        return saleItem;
    }

    /**
     * Decreases stock and creates stock movement record
     * AC2: Decrease stock for each item
     * AC5: Create stock movement audit trail
     */
    private void decreaseStock(
            UUID tenantId,
            UUID locationId,
            UUID productId,
            UUID variantId,
            BigDecimal quantity,
            UUID saleId,
            UUID userId) {

        Inventory inventory;

        if (variantId != null) {
            // Variant product
            inventory = inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                    tenantId, variantId, locationId
            ).orElseThrow(() -> new IllegalStateException(
                    "Inventory not found for variant: " + variantId
            ));
        } else {
            // Simple/composite product
            inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                    tenantId, productId, locationId
            ).orElseThrow(() -> new IllegalStateException(
                    "Inventory not found for product: " + productId
            ));
        }

        BigDecimal balanceBefore = inventory.getQuantityAvailable();
        inventory.removeQuantity(quantity);
        BigDecimal balanceAfter = inventory.getQuantityAvailable();

        inventoryRepository.save(inventory);

        // Create stock movement record (audit trail)
        StockMovement movement = new StockMovement(
                tenantId,
                productId,
                variantId,
                locationId,
                MovementType.SALE,
                quantity.negate(), // Negative quantity for exit
                balanceBefore,
                userId,
                "SALE",
                saleId,
                "Stock decreased by sale"
        );
        stockMovementRepository.save(movement);
    }

    /**
     * Creates a fiscal event for audit trail
     * AC4: Create fiscal events (NFR16 - 5-year retention)
     */
    private void createFiscalEvent(
            UUID tenantId,
            UUID saleId,
            FiscalEventType eventType,
            String nfceKey,
            String xmlSnapshot,
            String errorMessage,
            Integer httpStatusCode,
            UUID userId) {

        FiscalEvent event = new FiscalEvent();
        event.setTenantId(tenantId);
        event.setSaleId(saleId);
        event.setEventType(eventType);
        event.setNfceKey(nfceKey);
        event.setXmlSnapshot(xmlSnapshot);
        event.setErrorMessage(errorMessage);
        event.setHttpStatusCode(httpStatusCode);
        event.setUserId(userId);

        fiscalEventRepository.save(event);
    }

    /**
     * Calculates total amount for all items minus discount
     * AC2: Calculate sale total
     */
    private BigDecimal calculateTotal(List<ItemRequest> items, BigDecimal discount) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemRequest item : items) {
            BigDecimal itemTotal = item.quantity().multiply(item.unitPrice());
            if (item.discount() != null) {
                itemTotal = itemTotal.subtract(item.discount());
            }
            total = total.add(itemTotal);
        }

        if (discount != null) {
            total = total.subtract(discount);
        }

        return total;
    }


    // ==================== Story 4.4 Methods ====================

    /**
     * Manually retry NFCe emission for a PENDING or FAILED sale
     * Story 4.4: Manual retry functionality
     *
     * @param tenantId tenant ID
     * @param saleId sale ID to retry
     * @param userId user ID triggering retry
     * @return updated sale
     * @throws IllegalStateException if sale is already EMITTED or not found
     */
    @Transactional
    public Sale retrySale(UUID tenantId, UUID saleId, UUID userId) {
        // Load sale
        Sale sale = saleRepository.findByTenantIdAndId(tenantId, saleId)
                .orElseThrow(() -> new IllegalStateException("Sale not found: " + saleId));

        // Validate sale status
        if (sale.isNfceEmitted()) {
            throw new IllegalStateException("NFCe already emitted for sale: " + saleId);
        }

        // Load sale items
        List<SaleItem> saleItems = saleItemRepository.findBySaleId(saleId);

        try {
            // Attempt NFCe emission
            NfceService.NfceResponse nfceResponse = nfceService.emitNfce(sale, saleItems);

            // Success: update status and create fiscal event
            sale.markNfceAsEmitted(nfceResponse.nfceKey(), nfceResponse.xml());
            sale = saleRepository.save(sale);

            // Create fiscal event
            createFiscalEvent(
                    tenantId,
                    saleId,
                    FiscalEventType.NFCE_EMITTED,
                    nfceResponse.nfceKey(),
                    nfceResponse.xml(),
                    null,
                    null,
                    userId
            );

        } catch (NfceService.NfceException e) {
            // Failure: create fiscal event and keep as PENDING/FAILED
            createFiscalEvent(
                    tenantId,
                    saleId,
                    FiscalEventType.NFCE_FAILED,
                    null,
                    null,
                    e.getMessage(),
                    null,
                    userId
            );

            throw new IllegalStateException("NFCe emission failed: " + e.getMessage(), e);
        }

        return sale;
    }

    /**
     * Cancel sale and reverse stock movements (refund stock)
     * Story 4.4: Cancel sale with stock refund
     *
     * @param tenantId tenant ID
     * @param saleId sale ID to cancel
     * @param userId user ID triggering cancellation
     * @param justification reason for cancellation
     * @return cancelled sale
     * @throws IllegalStateException if sale is already EMITTED
     */
    @Transactional
    public Sale cancelSaleWithRefund(UUID tenantId, UUID saleId, UUID userId, String justification) {
        // Load sale
        Sale sale = saleRepository.findByTenantIdAndId(tenantId, saleId)
                .orElseThrow(() -> new IllegalStateException("Sale not found: " + saleId));

        // Validate sale status - cannot cancel if NFCe is already emitted
        if (sale.isNfceEmitted()) {
            throw new IllegalStateException(
                    "Cannot cancel sale with emitted NFCe. NFCe must be cancelled first via SEFAZ: " + saleId
            );
        }

        // Load sale items for stock reversal
        List<SaleItem> saleItems = saleItemRepository.findBySaleId(saleId);

        // Reverse stock movements (add stock back)
        for (SaleItem saleItem : saleItems) {
            reverseStock(
                    sale.getTenantId(),
                    sale.getStockLocationId(),
                    saleItem.getProductId(),
                    saleItem.getVariantId(),
                    saleItem.getQuantity(),
                    saleId,
                    userId,
                    justification
            );
        }

        // Update sale status to CANCELLED
        sale.setNfceStatus(NfceStatus.CANCELLED);
        sale.setDataAtualizacao(LocalDateTime.now());
        sale = saleRepository.save(sale);

        // Create fiscal event for cancellation
        createFiscalEvent(
                tenantId,
                saleId,
                FiscalEventType.NFCE_CANCELLED,
                null,
                null,
                "Sale cancelled: " + justification,
                null,
                userId
        );

        return sale;
    }

    /**
     * Reverse stock movement (add stock back)
     * Story 4.4: Stock refund on sale cancellation
     */
    private void reverseStock(
            UUID tenantId,
            UUID locationId,
            UUID productId,
            UUID variantId,
            BigDecimal quantity,
            UUID saleId,
            UUID userId,
            String justification) {

        Inventory inventory;

        if (variantId != null) {
            // Variant product
            inventory = inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                    tenantId, variantId, locationId
            ).orElseThrow(() -> new IllegalStateException(
                    "Inventory not found for variant: " + variantId
            ));
        } else {
            // Simple/composite product
            inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                    tenantId, productId, locationId
            ).orElseThrow(() -> new IllegalStateException(
                    "Inventory not found for product: " + productId
            ));
        }

        BigDecimal balanceBefore = inventory.getQuantityAvailable();
        inventory.addQuantity(quantity);
        BigDecimal balanceAfter = inventory.getQuantityAvailable();

        inventoryRepository.save(inventory);

        // Create stock movement record (reversal)
        StockMovement movement = new StockMovement(
                tenantId,
                productId,
                variantId,
                locationId,
                MovementType.SALE,
                quantity, // Positive quantity for refund
                balanceBefore,
                userId,
                "SALE_CANCELLATION",
                saleId,
                "Stock reversed due to sale cancellation: " + justification
        );
        stockMovementRepository.save(movement);
    }

    /**
     * Enqueue stock sync to marketplaces after sale
     * Story 5.4: Integration with marketplace stock sync
     */
    private void enqueueMarketplaceStockSync(UUID tenantId, UUID productId, UUID variantId) {
        try {
            // Check if product has marketplace listings
            var listings = marketplaceListingRepository.findByProductId(productId);

            if (!listings.isEmpty()) {
                // Enqueue sync for each marketplace
                for (var listing : listings) {
                    marketplaceStockSyncService.enqueueStockSync(
                            tenantId,
                            productId,
                            variantId,
                            listing.getMarketplace()
                    );
                }
            }
        } catch (Exception e) {
            // Log error but don't fail sale if sync enqueue fails
            // Sync can be triggered manually later if needed
            System.err.println("Error enqueuing marketplace stock sync: " + e.getMessage());
        }
    }

    // ==================== Inner Classes ====================

    /**
     * Sale request DTO
     * Contains all data needed to process a sale
     */
    public record SaleRequest(
            UUID tenantId,
            UUID customerId,
            UUID stockLocationId,
            PaymentMethod paymentMethod,
            BigDecimal paymentAmountReceived,
            BigDecimal discount,
            List<ItemRequest> items,
            UUID userId
    ) {
        public SaleRequest {
            if (tenantId == null) {
                throw new IllegalArgumentException("Tenant ID is required");
            }
            if (stockLocationId == null) {
                throw new IllegalArgumentException("Stock location ID is required");
            }
            if (paymentMethod == null) {
                throw new IllegalArgumentException("Payment method is required");
            }
            if (paymentAmountReceived == null || paymentAmountReceived.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Payment amount received must be positive");
            }
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Sale must have at least one item");
            }
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
        }
    }

    /**
     * Sale item request DTO
     * Contains data for a single item in the sale
     */
    public record ItemRequest(
            UUID productId,
            UUID variantId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discount
    ) {
        public ItemRequest {
            if (productId == null && variantId == null) {
                throw new IllegalArgumentException("Either product ID or variant ID must be provided");
            }
            if (productId != null && variantId != null) {
                throw new IllegalArgumentException("Cannot specify both product ID and variant ID");
            }
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }
        }
    }

    /**
     * Exception thrown when insufficient stock is available
     */
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
