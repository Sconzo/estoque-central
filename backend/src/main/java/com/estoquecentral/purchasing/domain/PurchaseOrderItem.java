package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("purchase_order_items")
public class PurchaseOrderItem {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID purchaseOrderId;
    private UUID productId;
    private UUID productVariantId;
    private String productSku;
    private String productName;
    private String supplierSku;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReceived;
    private BigDecimal quantityCancelled;
    private String unitOfMeasure;
    private BigDecimal unitCost;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PurchaseOrderItem() {
        this.quantityReceived = BigDecimal.ZERO;
        this.quantityCancelled = BigDecimal.ZERO;
        this.unitOfMeasure = "UN";
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateTotals() {
        this.subtotal = this.unitCost.multiply(this.quantityOrdered);
        this.total = this.subtotal
                .subtract(this.discountAmount)
                .add(this.taxAmount);
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getQuantityPending() {
        return this.quantityOrdered
                .subtract(this.quantityReceived)
                .subtract(this.quantityCancelled);
    }

    public boolean isFullyReceived() {
        return this.quantityReceived.compareTo(this.quantityOrdered) == 0;
    }

    public boolean isPartiallyReceived() {
        return this.quantityReceived.compareTo(BigDecimal.ZERO) > 0 &&
               this.quantityReceived.compareTo(this.quantityOrdered) < 0;
    }

    public boolean isPendingReceive() {
        return this.quantityReceived.compareTo(BigDecimal.ZERO) == 0 &&
               this.quantityCancelled.compareTo(this.quantityOrdered) < 0;
    }

    public boolean isCancelled() {
        return this.quantityCancelled.compareTo(this.quantityOrdered) == 0;
    }

    public void receiveQuantity(BigDecimal receivedQty) {
        BigDecimal pending = getQuantityPending();
        if (receivedQty.compareTo(pending) > 0) {
            throw new IllegalArgumentException("Cannot receive more than pending quantity");
        }
        this.quantityReceived = this.quantityReceived.add(receivedQty);
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelQuantity(BigDecimal cancelledQty) {
        BigDecimal pending = getQuantityPending();
        if (cancelledQty.compareTo(pending) > 0) {
            throw new IllegalArgumentException("Cannot cancel more than pending quantity");
        }
        this.quantityCancelled = this.quantityCancelled.add(cancelledQty);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasVariant() {
        return this.productVariantId != null;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getProductVariantId() { return productVariantId; }
    public void setProductVariantId(UUID productVariantId) { this.productVariantId = productVariantId; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSupplierSku() { return supplierSku; }
    public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }

    public BigDecimal getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(BigDecimal quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getQuantityCancelled() { return quantityCancelled; }
    public void setQuantityCancelled(BigDecimal quantityCancelled) { this.quantityCancelled = quantityCancelled; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
