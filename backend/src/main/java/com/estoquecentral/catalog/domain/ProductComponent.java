package com.estoquecentral.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProductComponent - Represents a component in a Bill of Materials (BOM)
 *
 * <p>Defines which products compose a composite product/kit and how many units are needed.
 * Example: "Kit Churrasco" needs 1 unit of "Carvão", 1 unit of "Acendedor", 6 units of "Espetos"
 *
 * <p>Validations:
 * <ul>
 *   <li>Component cannot be a COMPOSITE product (prevents recursion)</li>
 *   <li>Quantity required must be > 0</li>
 *   <li>Product cannot be its own component</li>
 * </ul>
 */
@Table("product_components")
public class ProductComponent implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID productId;          // The composite product (the kit)
    private UUID componentProductId; // The component (part of the kit)
    private BigDecimal quantityRequired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    @Transient
    private boolean isNew = false;

    /**
     * Constructor for creating new product component
     */
    public ProductComponent(UUID productId, UUID componentProductId, BigDecimal quantityRequired) {
        if (productId == null || componentProductId == null) {
            throw new IllegalArgumentException("Product and component IDs cannot be null");
        }
        if (productId.equals(componentProductId)) {
            throw new IllegalArgumentException("Product cannot be its own component");
        }
        if (quantityRequired == null || quantityRequired.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity required must be greater than zero");
        }

        this.id = UUID.randomUUID();
        this.productId = productId;
        this.componentProductId = componentProductId;
        this.quantityRequired = quantityRequired;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isNew = true;
    }

    /**
     * Default constructor for Spring Data JDBC
     */
    public ProductComponent() {
    }

    // Persistable implementation

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

    /**
     * Updates quantity required
     */
    public void updateQuantity(BigDecimal newQuantity) {
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantityRequired = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Getters and Setters ====================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getComponentProductId() {
        return componentProductId;
    }

    public void setComponentProductId(UUID componentProductId) {
        this.componentProductId = componentProductId;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
