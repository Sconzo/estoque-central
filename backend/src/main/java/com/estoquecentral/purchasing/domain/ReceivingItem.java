package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ReceivingItem entity - Represents individual items in a receiving transaction
 * Story 3.4: Receiving Processing and Weighted Average Cost Update
 */
@Table("receiving_items")
public class ReceivingItem {

    @Id
    private UUID id;

    @Column("receiving_id")
    private UUID receivingId;

    @Column("purchase_order_item_id")
    private UUID purchaseOrderItemId;

    @Column("product_id")
    private UUID productId;

    @Column("variant_id")
    private UUID variantId;

    @Column("quantity_received")
    private BigDecimal quantityReceived;

    @Column("unit_cost")
    private BigDecimal unitCost;

    @Column("new_weighted_average_cost")
    private BigDecimal newWeightedAverageCost;

    @Column("notes")
    private String notes;

    // Constructors
    public ReceivingItem() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReceivingId() {
        return receivingId;
    }

    public void setReceivingId(UUID receivingId) {
        this.receivingId = receivingId;
    }

    public UUID getPurchaseOrderItemId() {
        return purchaseOrderItemId;
    }

    public void setPurchaseOrderItemId(UUID purchaseOrderItemId) {
        this.purchaseOrderItemId = purchaseOrderItemId;
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

    public BigDecimal getNewWeightedAverageCost() {
        return newWeightedAverageCost;
    }

    public void setNewWeightedAverageCost(BigDecimal newWeightedAverageCost) {
        this.newWeightedAverageCost = newWeightedAverageCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
