package com.estoquecentral.sales.adapter.in.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for sales order items
 * Story 4.5: Sales Order B2B Interface
 */
public class SalesOrderItemResponseDTO {

    private UUID id;
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String productSku;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReserved;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Stock availability info
    private StockInfo stockInfo;

    public SalesOrderItemResponseDTO() {}

    public static class StockInfo {
        private BigDecimal available;
        private BigDecimal reserved;
        private BigDecimal forSale;

        public StockInfo() {}

        public StockInfo(BigDecimal available, BigDecimal reserved, BigDecimal forSale) {
            this.available = available;
            this.reserved = reserved;
            this.forSale = forSale;
        }

        public BigDecimal getAvailable() {
            return available;
        }

        public void setAvailable(BigDecimal available) {
            this.available = available;
        }

        public BigDecimal getReserved() {
            return reserved;
        }

        public void setReserved(BigDecimal reserved) {
            this.reserved = reserved;
        }

        public BigDecimal getForSale() {
            return forSale;
        }

        public void setForSale(BigDecimal forSale) {
            this.forSale = forSale;
        }
    }

    // Getters and Setters

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

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
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

    public StockInfo getStockInfo() {
        return stockInfo;
    }

    public void setStockInfo(StockInfo stockInfo) {
        this.stockInfo = stockInfo;
    }
}
