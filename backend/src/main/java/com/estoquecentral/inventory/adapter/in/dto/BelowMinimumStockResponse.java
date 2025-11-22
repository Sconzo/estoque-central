package com.estoquecentral.inventory.adapter.in.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * BelowMinimumStockResponse - Response for products below minimum stock
 * Story 2.7 - AC6: Products in stock rupture
 */
public class BelowMinimumStockResponse {
    private List<ProductBelowMinimum> products;
    private Integer totalCount;

    public static class ProductBelowMinimum {
        private UUID productId;
        private UUID variantId;
        private String productName;
        private String sku;
        private UUID stockLocationId;
        private String locationName;
        private BigDecimal quantityForSale;
        private BigDecimal minimumQuantity;
        private Double percentageOfMinimum;
        private String severity; // CRITICAL (<50%), LOW (50-80%)

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

        public UUID getStockLocationId() {
            return stockLocationId;
        }

        public void setStockLocationId(UUID stockLocationId) {
            this.stockLocationId = stockLocationId;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        public BigDecimal getQuantityForSale() {
            return quantityForSale;
        }

        public void setQuantityForSale(BigDecimal quantityForSale) {
            this.quantityForSale = quantityForSale;
        }

        public BigDecimal getMinimumQuantity() {
            return minimumQuantity;
        }

        public void setMinimumQuantity(BigDecimal minimumQuantity) {
            this.minimumQuantity = minimumQuantity;
        }

        public Double getPercentageOfMinimum() {
            return percentageOfMinimum;
        }

        public void setPercentageOfMinimum(Double percentageOfMinimum) {
            this.percentageOfMinimum = percentageOfMinimum;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }
    }

    // Getters and Setters
    public List<ProductBelowMinimum> getProducts() {
        return products;
    }

    public void setProducts(List<ProductBelowMinimum> products) {
        this.products = products;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
