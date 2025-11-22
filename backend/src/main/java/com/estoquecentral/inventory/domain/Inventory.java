package com.estoquecentral.inventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory - Domain entity for inventory tracking
 *
 * <p>Represents current inventory levels for a product/variant at a specific location.
 * Tracks quantity, reserved quantity, and min/max levels for alerts.
 *
 * <p><strong>Key Concepts:</strong>
 * <ul>
 *   <li>quantityAvailable - Total physical quantity in stock</li>
 *   <li>reservedQuantity - Quantity reserved for pending orders</li>
 *   <li>quantityForSale - Computed: quantityAvailable - reservedQuantity (GENERATED column, never negative)</li>
 *   <li>locationId - FK to locations table (warehouse, store, etc.)</li>
 *   <li>productId - For simple/composite products (NULL for variants)</li>
 *   <li>variantId - For product variants (NULL for simple/composite)</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong>
 * <ul>
 *   <li>quantityAvailable >= 0</li>
 *   <li>reservedQuantity >= 0</li>
 *   <li>reservedQuantity <= quantityAvailable</li>
 *   <li>quantityForSale = MAX(0, quantityAvailable - reservedQuantity)</li>
 *   <li>Either productId OR variantId must be NOT NULL (XOR constraint)</li>
 *   <li>One inventory record per (product/variant, location) combination</li>
 * </ul>
 *
 * <p><strong>Story 2.7:</strong> Multi-Warehouse Stock Control
 *
 * @see InventoryMovement
 */
@Table("inventory")
public class Inventory {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID productId;             // NULL for variants
    private UUID variantId;             // NULL for simple/composite products
    private UUID locationId;            // FK to locations table
    private BigDecimal quantityAvailable;
    private BigDecimal reservedQuantity;
    private BigDecimal quantityForSale; // Computed by database (GENERATED column)
    private BigDecimal minimumQuantity;
    private BigDecimal maximumQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating new inventory for simple/composite product
     */
    public Inventory(UUID tenantId, UUID productId, UUID locationId, BigDecimal quantityAvailable) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = productId;
        this.variantId = null;
        this.locationId = locationId;
        this.quantityAvailable = quantityAvailable != null ? quantityAvailable : BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for creating new inventory for product variant
     */
    public Inventory(UUID tenantId, UUID variantId, UUID locationId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = null;
        this.variantId = variantId;
        this.locationId = locationId;
        this.quantityAvailable = BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Default constructor for Spring Data JDBC
     */
    public Inventory() {
    }

    /**
     * Adds quantity to inventory
     *
     * @param amount amount to add
     * @throws IllegalArgumentException if amount is negative
     */
    public void addQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantityAvailable = this.quantityAvailable.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes quantity from inventory
     *
     * @param amount amount to remove
     * @throws IllegalArgumentException if amount is negative or exceeds available quantity for sale
     */
    public void removeQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal forSale = this.getComputedQuantityForSale();
        if (amount.compareTo(forSale) > 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient quantity for sale. Available: %s, Requested: %s",
                            forSale, amount));
        }

        this.quantityAvailable = this.quantityAvailable.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reserves quantity for pending order
     *
     * @param amount amount to reserve
     * @throws IllegalArgumentException if amount is negative or exceeds quantity for sale
     */
    public void reserve(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal forSale = this.getComputedQuantityForSale();
        if (amount.compareTo(forSale) > 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient quantity for sale to reserve. Available: %s, Requested: %s",
                            forSale, amount));
        }

        this.reservedQuantity = this.reservedQuantity.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Unreserves quantity (cancels reservation)
     *
     * @param amount amount to unreserve
     * @throws IllegalArgumentException if amount is negative or exceeds reserved quantity
     */
    public void unreserve(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (amount.compareTo(this.reservedQuantity) > 0) {
            throw new IllegalArgumentException(
                    String.format("Cannot unreserve more than reserved. Reserved: %s, Requested: %s",
                            this.reservedQuantity, amount));
        }

        this.reservedQuantity = this.reservedQuantity.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Fulfills reservation by removing from both quantityAvailable and reserved
     *
     * @param amount amount to fulfill
     * @throws IllegalArgumentException if amount is negative or exceeds reserved quantity
     */
    public void fulfillReservation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (amount.compareTo(this.reservedQuantity) > 0) {
            throw new IllegalArgumentException(
                    String.format("Cannot fulfill more than reserved. Reserved: %s, Requested: %s",
                            this.reservedQuantity, amount));
        }

        this.quantityAvailable = this.quantityAvailable.subtract(amount);
        this.reservedQuantity = this.reservedQuantity.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adjusts quantity to specific value (for corrections)
     *
     * @param newQuantity new quantity value
     * @throws IllegalArgumentException if new quantity is negative or less than reserved
     */
    public void adjustTo(BigDecimal newQuantity) {
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (newQuantity.compareTo(this.reservedQuantity) < 0) {
            throw new IllegalArgumentException(
                    String.format("Quantity cannot be less than reserved quantity. Reserved: %s, New quantity: %s",
                            this.reservedQuantity, newQuantity));
        }

        this.quantityAvailable = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Sets min/max quantity levels for alerts
     *
     * @param minimumQuantity minimum quantity threshold
     * @param maximumQuantity maximum quantity threshold
     */
    public void setLevels(BigDecimal minimumQuantity, BigDecimal maximumQuantity) {
        if (minimumQuantity != null && maximumQuantity != null &&
                minimumQuantity.compareTo(maximumQuantity) > 0) {
            throw new IllegalArgumentException("Minimum quantity cannot be greater than maximum quantity");
        }

        this.minimumQuantity = minimumQuantity;
        this.maximumQuantity = maximumQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if inventory is below minimum level (based on quantityForSale)
     *
     * @return true if below minimum
     */
    public boolean isBelowMinimum() {
        if (this.minimumQuantity == null) {
            return false;
        }
        BigDecimal forSale = getComputedQuantityForSale();
        return forSale.compareTo(this.minimumQuantity) < 0;
    }

    /**
     * Checks if inventory is above maximum level
     *
     * @return true if above maximum
     */
    public boolean isAboveMaximum() {
        return this.maximumQuantity != null && this.quantityAvailable.compareTo(this.maximumQuantity) > 0;
    }

    /**
     * Gets quantity for sale (quantityAvailable - reserved)
     * Note: In DB this is a GENERATED column, but we compute it here for domain logic
     * Never returns negative (returns 0 if calculation would be negative)
     *
     * @return quantity for sale (never negative)
     */
    public BigDecimal getComputedQuantityForSale() {
        BigDecimal forSale = this.quantityAvailable.subtract(this.reservedQuantity);
        return forSale.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : forSale;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(BigDecimal quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public BigDecimal getQuantityForSale() {
        return quantityForSale;
    }

    public void setQuantityForSale(BigDecimal quantityForSale) {
        this.quantityForSale = quantityForSale;
    }

    public BigDecimal getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(BigDecimal minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public BigDecimal getMaximumQuantity() {
        return maximumQuantity;
    }

    public void setMaximumQuantity(BigDecimal maximumQuantity) {
        this.maximumQuantity = maximumQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
