package com.estoquecentral.purchasing.adapter.in.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ReceivingItemResponseDTO {
    private UUID id;
    private UUID purchaseOrderItemId;
    private UUID productId;
    private String productName;
    private String sku;
    private UUID variantId;
    private String variantName;
    private BigDecimal quantityReceived;
    private BigDecimal unitCost;
    private BigDecimal newWeightedAverageCost;
    private String notes;

    public ReceivingItemResponseDTO() {}

    public ReceivingItemResponseDTO(
            UUID id,
            UUID purchaseOrderItemId,
            UUID productId,
            String productName,
            String sku,
            UUID variantId,
            String variantName,
            BigDecimal quantityReceived,
            BigDecimal unitCost,
            BigDecimal newWeightedAverageCost,
            String notes) {
        this.id = id;
        this.purchaseOrderItemId = purchaseOrderItemId;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.variantId = variantId;
        this.variantName = variantName;
        this.quantityReceived = quantityReceived;
        this.unitCost = unitCost;
        this.newWeightedAverageCost = newWeightedAverageCost;
        this.notes = notes;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPurchaseOrderItemId() { return purchaseOrderItemId; }
    public void setPurchaseOrderItemId(UUID purchaseOrderItemId) { this.purchaseOrderItemId = purchaseOrderItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getNewWeightedAverageCost() { return newWeightedAverageCost; }
    public void setNewWeightedAverageCost(BigDecimal newWeightedAverageCost) { this.newWeightedAverageCost = newWeightedAverageCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
