package com.estoquecentral.inventory.application;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.inventory.adapter.out.InventoryMovementRepository;
import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.adapter.out.LocationRepository;
import com.estoquecentral.inventory.domain.*;
import com.estoquecentral.shared.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * InventoryService - Business logic for inventory management
 *
 * <p>Handles inventory operations with full audit trail:
 * <ul>
 *   <li>Add/remove stock with movement history</li>
 *   <li>Reserve/unreserve inventory</li>
 *   <li>Manual adjustments with reasons</li>
 *   <li>Low stock alerts</li>
 *   <li>Movement history tracking</li>
 * </ul>
 *
 * <p><strong>Story 3.1 scope:</strong> Basic inventory control for simple products.
 *
 * @see Inventory
 * @see InventoryMovement
 */
@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final StockMovementService stockMovementService;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryMovementRepository movementRepository,
                            ProductRepository productRepository,
                            LocationRepository locationRepository,
                            StockMovementService stockMovementService) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
        this.stockMovementService = stockMovementService;
    }

    /**
     * Gets inventory for a product at a specific location
     *
     * @param productId product ID
     * @param locationId location ID
     * @return optional inventory
     */
    @Transactional(readOnly = true)
    public Optional<Inventory> getInventory(UUID productId, UUID locationId) {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId);
    }

    /**
     * Gets inventory by ID
     *
     * @param id inventory ID
     * @return optional inventory
     */
    @Transactional(readOnly = true)
    public Optional<Inventory> getInventoryById(UUID id) {
        return inventoryRepository.findById(id);
    }

    /**
     * Gets all inventory records for a product (all locations)
     *
     * @param productId product ID
     * @return list of inventory records
     */
    @Transactional(readOnly = true)
    public List<Inventory> getAllInventoryForProduct(UUID productId) {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.findAllByTenantIdAndProductId(tenantId, productId);
    }

    /**
     * Gets inventory records by location
     *
     * @param locationId location ID
     * @return list of inventory records
     */
    @Transactional(readOnly = true)
    public List<Inventory> getInventoryByLocation(UUID locationId) {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.findByTenantIdAndLocationId(tenantId, locationId);
    }

    /**
     * Creates inventory record for a product
     *
     * @param productId product ID
     * @param initialQuantity initial quantity
     * @param locationId location ID
     * @param minQuantity minimum quantity threshold
     * @param maxQuantity maximum quantity threshold
     * @param userId user creating the inventory
     * @return created inventory
     */
    public Inventory createInventory(UUID productId, BigDecimal initialQuantity,
                                      UUID locationId, BigDecimal minQuantity, BigDecimal maxQuantity,
                                      UUID userId) {
        UUID tenantId = getTenantIdAsUUID();

        // Validate product exists and controls inventory
        Product product = productRepository.findByIdAndActive(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (!product.shouldControlInventory()) {
            throw new IllegalArgumentException("Product does not control inventory: " + productId);
        }

        // Check if inventory already exists
        if (inventoryRepository.existsByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)) {
            throw new IllegalArgumentException(
                    "Inventory already exists for product " + productId + " at location " + locationId);
        }

        // Create inventory
        Inventory inventory = new Inventory(tenantId, productId, locationId, initialQuantity);
        inventory.setLevels(minQuantity, maxQuantity);
        inventory = inventoryRepository.save(inventory);

        // Create initial movement if quantity > 0
        if (initialQuantity.compareTo(BigDecimal.ZERO) > 0) {
            createMovement(
                    tenantId, productId, MovementType.ENTRY, initialQuantity, locationId,
                    BigDecimal.ZERO, initialQuantity,
                    MovementReason.INITIAL, "Initial inventory setup",
                    null, null, userId
            );
        }

        return inventory;
    }

    /**
     * Adds quantity to inventory (IN movement)
     *
     * @param productId product ID
     * @param quantity quantity to add
     * @param locationId location ID
     * @param reason reason for addition
     * @param notes additional notes
     * @param referenceType reference type (e.g., "PURCHASE_ORDER")
     * @param referenceId reference ID
     * @param userId user performing the operation
     * @return updated inventory
     */
    public Inventory addStock(UUID productId, BigDecimal quantity, UUID locationId,
                               MovementReason reason, String notes,
                               String referenceType, UUID referenceId, UUID userId) {
        validateQuantity(quantity);

        Inventory inventory = getOrCreateInventory(productId, locationId);

        BigDecimal before = inventory.getQuantityAvailable();
        inventory.addQuantity(quantity);
        BigDecimal after = inventory.getQuantityAvailable();

        inventory = inventoryRepository.save(inventory);

        createMovement(
                inventory.getTenantId(), productId, MovementType.ENTRY, quantity, locationId,
                before, after, reason, notes, referenceType, referenceId, userId
        );

        return inventory;
    }

    /**
     * Removes quantity from inventory (OUT movement)
     *
     * @param productId product ID
     * @param quantity quantity to remove
     * @param locationId location ID
     * @param reason reason for removal
     * @param notes additional notes
     * @param referenceType reference type (e.g., "SALE")
     * @param referenceId reference ID
     * @param userId user performing the operation
     * @return updated inventory
     */
    public Inventory removeStock(UUID productId, BigDecimal quantity, UUID locationId,
                                  MovementReason reason, String notes,
                                  String referenceType, UUID referenceId, UUID userId) {
        validateQuantity(quantity);

        UUID tenantId = getTenantIdAsUUID();
        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

        BigDecimal before = inventory.getQuantityAvailable();
        inventory.removeQuantity(quantity);
        BigDecimal after = inventory.getQuantityAvailable();

        inventory = inventoryRepository.save(inventory);

        createMovement(
                inventory.getTenantId(), productId, MovementType.EXIT, quantity, locationId,
                before, after, reason, notes, referenceType, referenceId, userId
        );

        return inventory;
    }

    /**
     * Adjusts inventory to specific quantity (ADJUSTMENT movement)
     *
     * @param productId product ID
     * @param newQuantity new quantity
     * @param locationId location ID
     * @param reason reason for adjustment
     * @param notes additional notes
     * @param userId user performing the operation
     * @return updated inventory
     */
    public Inventory adjustStock(UUID productId, BigDecimal newQuantity, UUID locationId,
                                  MovementReason reason, String notes, UUID userId) {
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New quantity cannot be negative");
        }

        UUID tenantId = getTenantIdAsUUID();
        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

        BigDecimal before = inventory.getQuantityAvailable();
        inventory.adjustTo(newQuantity);
        BigDecimal after = inventory.getQuantityAvailable();

        // Calculate adjustment quantity (can be positive or negative)
        BigDecimal adjustmentQty = after.subtract(before);

        inventory = inventoryRepository.save(inventory);

        createMovement(
                inventory.getTenantId(), productId, MovementType.ADJUSTMENT, adjustmentQty.abs(), locationId,
                before, after, reason, notes, null, null, userId
        );

        return inventory;
    }

    /**
     * Reserves quantity for order
     *
     * @param productId product ID
     * @param quantity quantity to reserve
     * @param locationId location ID
     * @param referenceType reference type (e.g., "ORDER")
     * @param referenceId reference ID
     * @param userId user performing the operation
     * @return updated inventory
     */
    public Inventory reserveStock(UUID productId, BigDecimal quantity, UUID locationId,
                                   String referenceType, UUID referenceId, UUID userId) {
        validateQuantity(quantity);

        UUID tenantId = getTenantIdAsUUID();
        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

        BigDecimal reservedBefore = inventory.getReservedQuantity();
        inventory.reserve(quantity);
        BigDecimal reservedAfter = inventory.getReservedQuantity();

        inventory = inventoryRepository.save(inventory);

        createMovement(
                inventory.getTenantId(), productId, MovementType.RESERVE, quantity, locationId,
                reservedBefore, reservedAfter,
                MovementReason.RESERVATION, "Stock reserved",
                referenceType, referenceId, userId
        );

        return inventory;
    }

    /**
     * Unreserves quantity (cancels reservation)
     *
     * @param productId product ID
     * @param quantity quantity to unreserve
     * @param locationId location ID
     * @param referenceType reference type (e.g., "ORDER")
     * @param referenceId reference ID
     * @param userId user performing the operation
     * @return updated inventory
     */
    public Inventory unreserveStock(UUID productId, BigDecimal quantity, UUID locationId,
                                     String referenceType, UUID referenceId, UUID userId) {
        validateQuantity(quantity);

        UUID tenantId = getTenantIdAsUUID();
        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

        BigDecimal reservedBefore = inventory.getReservedQuantity();
        inventory.unreserve(quantity);
        BigDecimal reservedAfter = inventory.getReservedQuantity();

        inventory = inventoryRepository.save(inventory);

        createMovement(
                inventory.getTenantId(), productId, MovementType.RELEASE, quantity, locationId,
                reservedBefore, reservedAfter,
                MovementReason.UNRESERVATION, "Stock unreserved",
                referenceType, referenceId, userId
        );

        return inventory;
    }

    /**
     * Fulfills reservation by removing from both quantity and reserved
     *
     * @param productId product ID
     * @param quantity quantity to fulfill
     * @param locationId location ID
     * @param referenceType reference type (e.g., "SALE")
     * @param referenceId reference ID
     * @param userId user performing the operation
     * @return updated inventory
     */
    public Inventory fulfillReservation(UUID productId, BigDecimal quantity, UUID locationId,
                                         String referenceType, UUID referenceId, UUID userId) {
        validateQuantity(quantity);

        UUID tenantId = getTenantIdAsUUID();
        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

        BigDecimal before = inventory.getQuantityAvailable();
        inventory.fulfillReservation(quantity);
        BigDecimal after = inventory.getQuantityAvailable();

        inventory = inventoryRepository.save(inventory);

        createMovement(
                inventory.getTenantId(), productId, MovementType.SALE, quantity, locationId,
                before, after, MovementReason.SALE, "Fulfilled reservation",
                referenceType, referenceId, userId
        );

        return inventory;
    }

    /**
     * Sets min/max quantity levels
     *
     * @param productId product ID
     * @param locationId location ID
     * @param minQuantity minimum quantity
     * @param maxQuantity maximum quantity
     * @return updated inventory
     */
    public Inventory setStockLevels(UUID productId, UUID locationId,
                                     BigDecimal minQuantity, BigDecimal maxQuantity) {
        UUID tenantId = getTenantIdAsUUID();
        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

        inventory.setLevels(minQuantity, maxQuantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Gets products with low stock
     *
     * @return list of inventory records below minimum
     */
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockProducts() {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.findBelowMinimum(tenantId);
    }

    /**
     * Gets products with low stock at a specific location
     *
     * @param locationId location ID
     * @return list of inventory records below minimum at location
     */
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockProductsByLocation(UUID locationId) {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.findBelowMinimumByLocation(tenantId, locationId);
    }

    /**
     * Gets products out of stock
     *
     * @return list of inventory records with zero available quantity
     */
    @Transactional(readOnly = true)
    public List<Inventory> getOutOfStockProducts() {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.findOutOfStock(tenantId);
    }

    /**
     * Gets products with excess stock
     *
     * @return list of inventory records above maximum
     */
    @Transactional(readOnly = true)
    public List<Inventory> getExcessStockProducts() {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.findAboveMaximum(tenantId);
    }

    /**
     * Gets movement history for a product
     *
     * @param productId product ID
     * @param pageable pagination parameters
     * @return page of movements
     */
    @Transactional(readOnly = true)
    public Page<InventoryMovement> getMovementHistory(UUID productId, Pageable pageable) {
        List<InventoryMovement> content = movementRepository.findByProductIdOrderByCreatedAtDesc(
            productId,
            pageable.getPageSize(),
            pageable.getOffset()
        );
        long total = movementRepository.countByProductId(productId);
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Gets recent movements
     *
     * @param pageable pagination parameters
     * @return page of recent movements
     */
    @Transactional(readOnly = true)
    public Page<InventoryMovement> getRecentMovements(Pageable pageable) {
        List<InventoryMovement> content = movementRepository.findAllByOrderByCreatedAtDesc(
            pageable.getPageSize(),
            pageable.getOffset()
        );
        long total = movementRepository.countAll();
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Gets total inventory value
     *
     * @return total inventory value
     */
    @Transactional(readOnly = true)
    public Double getTotalInventoryValue() {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.getTotalInventoryValue(tenantId);
    }

    /**
     * Gets total inventory value by location
     *
     * @param locationId location ID
     * @return total inventory value for location
     */
    @Transactional(readOnly = true)
    public Double getTotalInventoryValueByLocation(UUID locationId) {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.getTotalInventoryValueByLocation(tenantId, locationId);
    }

    /**
     * Counts low stock products
     *
     * @return count of products below minimum
     */
    @Transactional(readOnly = true)
    public long countLowStockProducts() {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.countBelowMinimum(tenantId);
    }

    /**
     * Counts out of stock products
     *
     * @return count of products with zero available quantity
     */
    @Transactional(readOnly = true)
    public long countOutOfStockProducts() {
        UUID tenantId = getTenantIdAsUUID();
        return inventoryRepository.countOutOfStock(tenantId);
    }

    // ==================== Private Helper Methods ====================

    private Inventory getOrCreateInventory(UUID productId, UUID locationId) {
        UUID tenantId = getTenantIdAsUUID();
        Optional<Inventory> existing = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                tenantId, productId, locationId);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Auto-create inventory if not exists
        Product product = productRepository.findByIdAndActive(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (!product.shouldControlInventory()) {
            throw new IllegalArgumentException("Product does not control inventory: " + productId);
        }

        Inventory inventory = new Inventory(tenantId, productId, locationId, BigDecimal.ZERO);
        return inventoryRepository.save(inventory);
    }

    private void createMovement(UUID tenantId, UUID productId, MovementType type,
                                BigDecimal quantity, UUID locationId,
                                BigDecimal quantityBefore, BigDecimal quantityAfter,
                                MovementReason reason, String notes,
                                String referenceType, UUID referenceId, UUID userId) {
        // Save to old InventoryMovement table (legacy)
        InventoryMovement movement = new InventoryMovement(
                tenantId, productId, type, quantity, locationId.toString(),
                quantityBefore, quantityAfter,
                reason, notes, referenceType, referenceId, userId
        );
        movementRepository.save(movement);

        // TODO: Integrate with new StockMovementService
        // For now, keeping both systems in parallel
        // Future: Remove InventoryMovement table and use only StockMovement
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    /**
     * Gets tenant ID from TenantContext and converts to UUID
     *
     * @return tenant ID as UUID
     * @throws IllegalStateException if tenant ID is not set
     */
    private UUID getTenantIdAsUUID() {
        String tenantIdStr = TenantContext.getTenantId();
        if (tenantIdStr == null) {
            throw new IllegalStateException("Tenant ID not set in context");
        }
        return UUID.fromString(tenantIdStr);
    }
}
