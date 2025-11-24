package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SalesOrderItem - Line item for B2B Sales Orders
 * Story 4.5: Sales Order B2B Interface
 *
 * Represents individual products/variants in a sales order with quantity and pricing.
 * Tracks quantity reserved for future stock reservation (Story 4.6).
 */
@Table("sales_order_items")
public class SalesOrderItem {

    @Id
    private UUID id;
    private UUID salesOrderId;
    private UUID productId;
    private UUID variantId;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReserved;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SalesOrderItem() {
        this.quantityOrdered = BigDecimal.ZERO;
        this.quantityReserved = BigDecimal.ZERO;
        this.unitPrice = BigDecimal.ZERO;
        this.totalPrice = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods

    /**
     * Calculate total price based on quantity and unit price
     */
    public void calculateTotal() {
        this.totalPrice = this.quantityOrdered.multiply(this.unitPrice);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if item has a product (not variant)
     */
    public boolean hasProduct() {
        return this.productId != null;
    }

    /**
     * Check if item has a variant
     */
    public boolean hasVariant() {
        return this.variantId != null;
    }

    /**
     * Get the effective item ID (product or variant)
     */
    public UUID getEffectiveItemId() {
        return hasVariant() ? variantId : productId;
    }

    /**
     * Check if item is fully reserved
     */
    public boolean isFullyReserved() {
        return this.quantityReserved.compareTo(this.quantityOrdered) >= 0;
    }

    /**
     * Get remaining quantity to reserve
     */
    public BigDecimal getRemainingToReserve() {
        BigDecimal remaining = this.quantityOrdered.subtract(this.quantityReserved);
        return remaining.max(BigDecimal.ZERO);
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(UUID salesOrderId) {
        this.salesOrderId = salesOrderId;
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

    public BigDecimal getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(BigDecimal quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public BigDecimal getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(BigDecimal quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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
