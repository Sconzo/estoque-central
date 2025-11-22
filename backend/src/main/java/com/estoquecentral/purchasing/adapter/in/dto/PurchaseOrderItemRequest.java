package com.estoquecentral.purchasing.adapter.in.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for purchase order item
 * Story 3.2: Purchase Order Creation
 */
public class PurchaseOrderItemRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    private UUID variantId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantityOrdered;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.0", message = "Unit cost must be greater than or equal to 0")
    private BigDecimal unitCost;

    private String notes;

    // Getters and Setters
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

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
