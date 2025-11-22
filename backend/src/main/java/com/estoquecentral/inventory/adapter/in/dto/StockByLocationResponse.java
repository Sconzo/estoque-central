package com.estoquecentral.inventory.adapter.in.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * StockByLocationResponse - Aggregated stock by location
 * Story 2.7 - AC2: Stock by location drill-down
 */
public class StockByLocationResponse {
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String productSku;
    private Integer totalLocations;
    private BigDecimal totalQuantityAvailable;
    private BigDecimal totalReservedQuantity;
    private BigDecimal totalQuantityForSale;
    private List<LocationStock> byLocation;

    public static class LocationStock {
        private UUID stockLocationId;
        private String locationName;
        private String locationCode;
        private BigDecimal quantityAvailable;
        private BigDecimal reservedQuantity;
        private BigDecimal quantityForSale;
        private BigDecimal minimumQuantity;
        private String status; // OK, LOW, CRITICAL

        // Getters and Setters
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

        public String getLocationCode() {
            return locationCode;
        }

        public void setLocationCode(String locationCode) {
            this.locationCode = locationCode;
        }

        public BigDecimal getQuantityAvailable() {
            return quantityAvailable;
        }

        public void setQuantityAvailable(BigDecimal quantityAvailable) {
            this.quantityAvailable = quantityAvailable;
        }

        public BigDecimal getReservedQuantity() {
            return reservedQuantity;
        }

        public void setReservedQuantity(BigDecimal reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

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

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(Integer totalLocations) {
        this.totalLocations = totalLocations;
    }

    public BigDecimal getTotalQuantityAvailable() {
        return totalQuantityAvailable;
    }

    public void setTotalQuantityAvailable(BigDecimal totalQuantityAvailable) {
        this.totalQuantityAvailable = totalQuantityAvailable;
    }

    public BigDecimal getTotalReservedQuantity() {
        return totalReservedQuantity;
    }

    public void setTotalReservedQuantity(BigDecimal totalReservedQuantity) {
        this.totalReservedQuantity = totalReservedQuantity;
    }

    public BigDecimal getTotalQuantityForSale() {
        return totalQuantityForSale;
    }

    public void setTotalQuantityForSale(BigDecimal totalQuantityForSale) {
        this.totalQuantityForSale = totalQuantityForSale;
    }

    public List<LocationStock> getByLocation() {
        return byLocation;
    }

    public void setByLocation(List<LocationStock> byLocation) {
        this.byLocation = byLocation;
    }
}
