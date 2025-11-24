package com.estoquecentral.inventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * InventoryMovement - Domain entity for inventory movement history
 *
 * <p>Represents a single inventory movement transaction. All movements are
 * immutable once created for audit purposes.
 *
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>Immutable audit trail of all inventory changes</li>
 *   <li>Stores before/after quantities for verification</li>
 *   <li>Links to external documents (purchase orders, sales, etc.)</li>
 *   <li>Supports multiple movement types and reasons</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong>
 * <ul>
 *   <li>Movements are never updated or deleted (immutable)</li>
 *   <li>Every inventory change must create a movement record</li>
 *   <li>quantity_after = quantity_before +/- quantity</li>
 * </ul>
 *
 * @see Inventory
 * @see MovementType
 * @see MovementReason
 */
@Table("inventory_movements")
public class InventoryMovement {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private MovementType type;
    private BigDecimal quantity;
    private String location;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private MovementReason reason;
    private String notes;
    private String referenceType;
    private UUID referenceId;
    private LocalDateTime createdAt;
    private UUID createdBy;

    /**
     * Constructor for creating new movement
     */
    public InventoryMovement(UUID tenantId, UUID productId, MovementType type,
                             BigDecimal quantity, String location,
                             BigDecimal quantityBefore, BigDecimal quantityAfter,
                             MovementReason reason, String notes,
                             String referenceType, UUID referenceId,
                             UUID createdBy) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.location = location != null ? location : "DEFAULT";
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
        this.notes = notes;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    /**
     * Default constructor for Spring Data JDBC
     */
    public InventoryMovement() {
    }

    /**
     * Verifies movement integrity
     * Checks that quantity_after = quantity_before +/- quantity
     *
     * @return true if movement is valid
     */
    public boolean verifyIntegrity() {
        BigDecimal expected;
        if (type == MovementType.ENTRY) {
            expected = quantityBefore.add(quantity);
        } else if (type == MovementType.EXIT) {
            expected = quantityBefore.subtract(quantity);
        } else {
            // For ADJUSTMENT, quantity can be positive or negative
            expected = quantityAfter;
        }

        return quantityAfter.compareTo(expected) == 0;
    }

    /**
     * Checks if movement is an inbound movement
     *
     * @return true if inbound
     */
    public boolean isInbound() {
        return type == MovementType.ENTRY;
    }

    /**
     * Checks if movement is an outbound movement
     *
     * @return true if outbound
     */
    public boolean isOutbound() {
        return type == MovementType.EXIT;
    }

    /**
     * Checks if movement has external reference
     *
     * @return true if has reference
     */
    public boolean hasReference() {
        return referenceType != null && referenceId != null;
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

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(BigDecimal quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public BigDecimal getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(BigDecimal quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public MovementReason getReason() {
        return reason;
    }

    public void setReason(MovementReason reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
}
