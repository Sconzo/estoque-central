package com.estoquecentral.inventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory - Domain entity for inventory tracking
 *
 * <p>Represents current inventory levels for a product at a specific location.
 * Tracks quantity, reserved quantity, and min/max levels for alerts.
 *
 * <p><strong>Key Concepts:</strong>
 * <ul>
 *   <li>quantity - Total physical quantity in stock</li>
 *   <li>reservedQuantity - Quantity reserved for pending orders</li>
 *   <li>availableQuantity - Computed: quantity - reservedQuantity (GENERATED column)</li>
 *   <li>location - Storage location (DEFAULT for single location, future multi-location support)</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong>
 * <ul>
 *   <li>quantity >= 0</li>
 *   <li>reservedQuantity >= 0</li>
 *   <li>reservedQuantity <= quantity</li>
 *   <li>availableQuantity = quantity - reservedQuantity</li>
 *   <li>One inventory record per product-location combination</li>
 * </ul>
 *
 * @see InventoryMovement
 */
@Table("inventory")
public class Inventory {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private BigDecimal quantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity; // Computed by database
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating new inventory
     */
    public Inventory(UUID tenantId, UUID productId, BigDecimal quantity, String location) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = productId;
        this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
        this.location = location != null ? location : "DEFAULT";
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
        this.quantity = this.quantity.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes quantity from inventory
     *
     * @param amount amount to remove
     * @throws IllegalArgumentException if amount is negative or exceeds available quantity
     */
    public void removeQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal available = this.quantity.subtract(this.reservedQuantity);
        if (amount.compareTo(available) > 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient available quantity. Available: %s, Requested: %s",
                            available, amount));
        }

        this.quantity = this.quantity.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reserves quantity for pending order
     *
     * @param amount amount to reserve
     * @throws IllegalArgumentException if amount is negative or exceeds available quantity
     */
    public void reserve(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal available = this.quantity.subtract(this.reservedQuantity);
        if (amount.compareTo(available) > 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient available quantity to reserve. Available: %s, Requested: %s",
                            available, amount));
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
     * Fulfills reservation by removing from both quantity and reserved
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

        this.quantity = this.quantity.subtract(amount);
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

        this.quantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Sets min/max quantity levels for alerts
     *
     * @param minQuantity minimum quantity threshold
     * @param maxQuantity maximum quantity threshold
     */
    public void setLevels(BigDecimal minQuantity, BigDecimal maxQuantity) {
        if (minQuantity != null && maxQuantity != null &&
                minQuantity.compareTo(maxQuantity) > 0) {
            throw new IllegalArgumentException("Min quantity cannot be greater than max quantity");
        }

        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if inventory is below minimum level
     *
     * @return true if below minimum
     */
    public boolean isBelowMinimum() {
        return this.minQuantity != null && this.quantity.compareTo(this.minQuantity) < 0;
    }

    /**
     * Checks if inventory is above maximum level
     *
     * @return true if above maximum
     */
    public boolean isAboveMaximum() {
        return this.maxQuantity != null && this.quantity.compareTo(this.maxQuantity) > 0;
    }

    /**
     * Gets available quantity (quantity - reserved)
     * Note: In DB this is a GENERATED column, but we compute it here for domain logic
     *
     * @return available quantity
     */
    public BigDecimal getComputedAvailableQuantity() {
        return this.quantity.subtract(this.reservedQuantity);
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(BigDecimal minQuantity) {
        this.minQuantity = minQuantity;
    }

    public BigDecimal getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(BigDecimal maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
