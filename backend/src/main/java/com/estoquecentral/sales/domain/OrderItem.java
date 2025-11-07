package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("order_items")
public class OrderItem {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID orderId;
    private UUID productId;
    private UUID productVariantId;
    private String productName;
    private String productSku;
    private String variantName;
    private String variantSku;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private BigDecimal quantityFulfilled;
    private BigDecimal quantityCancelled;
    private BigDecimal quantityRefunded;
    private Boolean inventoryReserved;
    private Boolean inventoryFulfilled;
    private String customOptions;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderItem() {
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.quantityFulfilled = BigDecimal.ZERO;
        this.quantityCancelled = BigDecimal.ZERO;
        this.quantityRefunded = BigDecimal.ZERO;
        this.inventoryReserved = false;
        this.inventoryFulfilled = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateTotals() {
        this.subtotal = this.unitPrice.multiply(this.quantity);
        this.total = this.subtotal
                .subtract(this.discountAmount)
                .add(this.taxAmount);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasVariant() {
        return this.productVariantId != null;
    }

    public boolean isFullyFulfilled() {
        return this.quantityFulfilled.compareTo(this.quantity) == 0;
    }

    public boolean isPartiallyFulfilled() {
        return this.quantityFulfilled.compareTo(BigDecimal.ZERO) > 0 &&
               this.quantityFulfilled.compareTo(this.quantity) < 0;
    }

    public boolean isPendingFulfillment() {
        return this.quantityFulfilled.compareTo(BigDecimal.ZERO) == 0 &&
               this.quantityCancelled.compareTo(this.quantity) < 0;
    }

    public boolean isCancelled() {
        return this.quantityCancelled.compareTo(this.quantity) == 0;
    }

    public BigDecimal getRemainingQuantity() {
        return this.quantity
                .subtract(this.quantityFulfilled)
                .subtract(this.quantityCancelled);
    }

    public void fulfill(BigDecimal fulfilledQty) {
        BigDecimal remaining = getRemainingQuantity();
        if (fulfilledQty.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Cannot fulfill more than remaining quantity");
        }
        this.quantityFulfilled = this.quantityFulfilled.add(fulfilledQty);
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelQuantity(BigDecimal cancelledQty) {
        BigDecimal remaining = getRemainingQuantity();
        if (cancelledQty.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Cannot cancel more than remaining quantity");
        }
        this.quantityCancelled = this.quantityCancelled.add(cancelledQty);
        this.updatedAt = LocalDateTime.now();
    }

    public void refund(BigDecimal refundedQty) {
        if (refundedQty.compareTo(this.quantityFulfilled) > 0) {
            throw new IllegalArgumentException("Cannot refund more than fulfilled quantity");
        }
        this.quantityRefunded = this.quantityRefunded.add(refundedQty);
        this.updatedAt = LocalDateTime.now();
    }

    public void markInventoryReserved() {
        this.inventoryReserved = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void markInventoryFulfilled() {
        this.inventoryFulfilled = true;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getProductVariantId() { return productVariantId; }
    public void setProductVariantId(UUID productVariantId) { this.productVariantId = productVariantId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public String getVariantSku() { return variantSku; }
    public void setVariantSku(String variantSku) { this.variantSku = variantSku; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getQuantityFulfilled() { return quantityFulfilled; }
    public void setQuantityFulfilled(BigDecimal quantityFulfilled) { this.quantityFulfilled = quantityFulfilled; }

    public BigDecimal getQuantityCancelled() { return quantityCancelled; }
    public void setQuantityCancelled(BigDecimal quantityCancelled) { this.quantityCancelled = quantityCancelled; }

    public BigDecimal getQuantityRefunded() { return quantityRefunded; }
    public void setQuantityRefunded(BigDecimal quantityRefunded) { this.quantityRefunded = quantityRefunded; }

    public Boolean getInventoryReserved() { return inventoryReserved; }
    public void setInventoryReserved(Boolean inventoryReserved) { this.inventoryReserved = inventoryReserved; }

    public Boolean getInventoryFulfilled() { return inventoryFulfilled; }
    public void setInventoryFulfilled(Boolean inventoryFulfilled) { this.inventoryFulfilled = inventoryFulfilled; }

    public String getCustomOptions() { return customOptions; }
    public void setCustomOptions(String customOptions) { this.customOptions = customOptions; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
