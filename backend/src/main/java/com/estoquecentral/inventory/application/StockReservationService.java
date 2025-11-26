package com.estoquecentral.inventory.application;

import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.adapter.out.StockMovementRepository;
import com.estoquecentral.inventory.domain.Inventory;
import com.estoquecentral.inventory.domain.MovementType;
import com.estoquecentral.inventory.domain.StockMovement;
import com.estoquecentral.marketplace.application.MarketplaceStockSyncService;
import com.estoquecentral.marketplace.adapter.out.MarketplaceListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockReservationService - Manages stock reservations for sales orders
 * Story 4.6: Stock Reservation and Automatic Release
 *
 * <p>Handles reservation and release of stock for B2B sales orders.
 * Updates inventory.reserved_quantity and creates audit trail movements.
 *
 * <p><strong>Key Operations:</strong>
 * <ul>
 *   <li>reserve() - Reserves stock when confirming sales order</li>
 *   <li>release() - Releases reservation when cancelling/expiring order</li>
 *   <li>fulfill() - Fulfills reservation when invoicing order</li>
 * </ul>
 *
 * <p><strong>Formula:</strong>
 * <pre>
 * quantity_for_sale = quantity_available - reserved_quantity
 * </pre>
 */
@Service
public class StockReservationService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final MarketplaceStockSyncService marketplaceStockSyncService;
    private final MarketplaceListingRepository marketplaceListingRepository;

    public StockReservationService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            MarketplaceStockSyncService marketplaceStockSyncService,
            MarketplaceListingRepository marketplaceListingRepository) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.marketplaceStockSyncService = marketplaceStockSyncService;
        this.marketplaceListingRepository = marketplaceListingRepository;
    }

    /**
     * Reserves stock for a sales order item
     *
     * @param tenantId tenant ID
     * @param productId product ID (null for variants)
     * @param variantId variant ID (null for products)
     * @param locationId stock location ID
     * @param quantity quantity to reserve
     * @param orderId sales order ID
     * @param orderNumber sales order number
     * @param userId user performing the reservation
     * @throws IllegalArgumentException if stock is insufficient
     */
    @Transactional
    public void reserve(
            UUID tenantId,
            UUID productId,
            UUID variantId,
            UUID locationId,
            BigDecimal quantity,
            UUID orderId,
            String orderNumber,
            UUID userId) {

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Find inventory record
        Inventory inventory = findInventory(tenantId, productId, variantId, locationId);

        // Validate sufficient quantity for sale
        BigDecimal forSale = inventory.getComputedQuantityForSale();
        if (forSale.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock for sale. Available: %s, Requested: %s",
                    forSale, quantity));
        }

        // Reserve quantity
        BigDecimal balanceBefore = inventory.getReservedQuantity();
        inventory.reserve(quantity);
        inventoryRepository.save(inventory);

        // Create RESERVE movement (audit trail)
        StockMovement movement = new StockMovement(
            tenantId,
            productId,
            variantId,
            locationId,
            MovementType.RESERVE,
            quantity.negate(), // Negative because it reduces quantity_for_sale
            balanceBefore,
            userId,
            "SALES_ORDER",
            orderId,
            "Reserva OV " + orderNumber
        );
        stockMovementRepository.save(movement);
    }

    /**
     * Releases (unreserves) stock for a sales order item
     *
     * @param tenantId tenant ID
     * @param productId product ID (null for variants)
     * @param variantId variant ID (null for products)
     * @param locationId stock location ID
     * @param quantity quantity to release
     * @param orderId sales order ID
     * @param reason reason for release (e.g., "Cancelamento manual", "Liberação automática - 7 dias")
     * @param userId user performing the release
     */
    @Transactional
    public void release(
            UUID tenantId,
            UUID productId,
            UUID variantId,
            UUID locationId,
            BigDecimal quantity,
            UUID orderId,
            String reason,
            UUID userId) {

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Find inventory record
        Inventory inventory = findInventory(tenantId, productId, variantId, locationId);

        // Release reservation
        BigDecimal balanceBefore = inventory.getReservedQuantity();
        inventory.unreserve(quantity);
        inventoryRepository.save(inventory);

        // Create RELEASE movement (audit trail)
        StockMovement movement = new StockMovement(
            tenantId,
            productId,
            variantId,
            locationId,
            MovementType.RELEASE,
            quantity, // Positive because it increases quantity_for_sale
            balanceBefore,
            userId,
            "SALES_ORDER",
            orderId,
            reason
        );
        stockMovementRepository.save(movement);
    }

    /**
     * Fulfills reservation by removing from both quantity_available and reserved_quantity
     * Used when invoicing a sales order (stock leaves the warehouse)
     *
     * @param tenantId tenant ID
     * @param productId product ID (null for variants)
     * @param variantId variant ID (null for products)
     * @param locationId stock location ID
     * @param quantity quantity to fulfill
     * @param orderId sales order ID
     * @param orderNumber sales order number
     * @param userId user performing the fulfillment
     */
    @Transactional
    public void fulfill(
            UUID tenantId,
            UUID productId,
            UUID variantId,
            UUID locationId,
            BigDecimal quantity,
            UUID orderId,
            String orderNumber,
            UUID userId) {

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Find inventory record
        Inventory inventory = findInventory(tenantId, productId, variantId, locationId);

        // Fulfill reservation (removes from both available and reserved)
        BigDecimal balanceBefore = inventory.getQuantityAvailable();
        inventory.fulfillReservation(quantity);
        inventoryRepository.save(inventory);

        // Create SALE movement (audit trail)
        StockMovement movement = new StockMovement(
            tenantId,
            productId,
            variantId,
            locationId,
            MovementType.SALE,
            quantity.negate(), // Negative because it's an exit
            balanceBefore,
            userId,
            "SALES_ORDER",
            orderId,
            "Venda OV " + orderNumber
        );
        stockMovementRepository.save(movement);

        // Story 5.4: Enqueue stock sync to marketplaces after fulfillment
        enqueueMarketplaceStockSync(tenantId, productId, variantId);
    }

    /**
     * Helper method to find inventory by product/variant and location
     */
    private Inventory findInventory(UUID tenantId, UUID productId, UUID variantId, UUID locationId) {
        if (productId != null) {
            return inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Inventory not found for product " + productId + " at location " + locationId));
        } else if (variantId != null) {
            return inventoryRepository.findByTenantIdAndVariantIdAndLocationId(tenantId, variantId, locationId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Inventory not found for variant " + variantId + " at location " + locationId));
        } else {
            throw new IllegalArgumentException("Either productId or variantId must be provided");
        }
    }

    /**
     * Enqueue stock sync to marketplaces after sale/fulfillment
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
            // Log error but don't fail fulfillment if sync enqueue fails
            // Sync can be triggered manually later if needed
            System.err.println("Error enqueuing marketplace stock sync: " + e.getMessage());
        }
    }
}
