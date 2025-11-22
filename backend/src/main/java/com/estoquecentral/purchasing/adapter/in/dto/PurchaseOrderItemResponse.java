package com.estoquecentral.purchasing.adapter.in.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for purchase order item
 * Story 3.2: Purchase Order Creation
 */
public class PurchaseOrderItemResponse {

    private UUID id;
    private ProductSummary product;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReceived;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String notes;

    public static class ProductSummary {
        private UUID id;
        private String sku;
        private String name;

        public ProductSummary(UUID id, String sku, String name) {
            this.id = id;
            this.sku = sku;
            this.name = name;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProductSummary getProduct() {
        return product;
    }

    public void setProduct(ProductSummary product) {
        this.product = product;
    }

    public BigDecimal getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(BigDecimal quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public BigDecimal getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(BigDecimal quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
