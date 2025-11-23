package com.estoquecentral.purchasing.adapter.in.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for receiving item details (Story 3.3 AC4)
 * Represents individual item within a purchase order for receiving
 */
public class ReceivingItemDTO {
    private UUID id;
    private UUID productId;
    private String productName;
    private String sku;
    private String barcode;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReceived;
    private BigDecimal quantityPending;
    private BigDecimal unitCost;

    public ReceivingItemDTO() {}

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public BigDecimal getQuantityPending() {
        return quantityPending;
    }

    public void setQuantityPending(BigDecimal quantityPending) {
        this.quantityPending = quantityPending;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
