package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.Inventory;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * StockResponse - DTO for stock query responses
 * Story 2.7 - AC2: Stock query endpoints
 */
public class StockResponse {
    private UUID id;
    private UUID productId;
    private UUID variantId;
    private UUID locationId;
    private String locationName;
    private String locationCode;
    private String productName;
    private String productSku;
    private BigDecimal quantityAvailable;
    private BigDecimal reservedQuantity;
    private BigDecimal quantityForSale;
    private BigDecimal minimumQuantity;
    private BigDecimal maximumQuantity;
    private String stockStatus; // OK, LOW, CRITICAL, NOT_SET
    private Double percentageOfMinimum;

    public static StockResponse fromEntity(Inventory inventory) {
        StockResponse response = new StockResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProductId());
        response.setVariantId(inventory.getVariantId());
        response.setLocationId(inventory.getLocationId());
        response.setQuantityAvailable(inventory.getQuantityAvailable());
        response.setReservedQuantity(inventory.getReservedQuantity());
        response.setQuantityForSale(inventory.getQuantityForSale());
        response.setMinimumQuantity(inventory.getMinimumQuantity());
        response.setMaximumQuantity(inventory.getMaximumQuantity());
        response.setStockStatus(calculateStockStatus(inventory));
        response.setPercentageOfMinimum(calculatePercentageOfMinimum(inventory));
        return response;
    }

    private static String calculateStockStatus(Inventory inventory) {
        if (inventory.getMinimumQuantity() == null || inventory.getMinimumQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return "NOT_SET";
        }

        BigDecimal forSale = inventory.getComputedQuantityForSale();
        BigDecimal minimum = inventory.getMinimumQuantity();

        if (forSale.compareTo(minimum) >= 0) {
            return "OK";
        } else if (forSale.compareTo(minimum.multiply(BigDecimal.valueOf(0.2))) >= 0) {
            return "LOW";
        } else {
            return "CRITICAL";
        }
    }

    private static Double calculatePercentageOfMinimum(Inventory inventory) {
        if (inventory.getMinimumQuantity() == null || inventory.getMinimumQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal forSale = inventory.getComputedQuantityForSale();
        BigDecimal minimum = inventory.getMinimumQuantity();

        return forSale.multiply(BigDecimal.valueOf(100))
                     .divide(minimum, 2, BigDecimal.ROUND_HALF_UP)
                     .doubleValue();
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

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
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

    public BigDecimal getMaximumQuantity() {
        return maximumQuantity;
    }

    public void setMaximumQuantity(BigDecimal maximumQuantity) {
        this.maximumQuantity = maximumQuantity;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public Double getPercentageOfMinimum() {
        return percentageOfMinimum;
    }

    public void setPercentageOfMinimum(Double percentageOfMinimum) {
        this.percentageOfMinimum = percentageOfMinimum;
    }
}
