package com.estoquecentral.inventory.adapter.in.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * FrequentAdjustmentDTO - DTO for products with frequent adjustments
 * Story 3.5: Stock Adjustment (AC11)
 */
public class FrequentAdjustmentDTO {

    private UUID productId;
    private String productName;
    private String productSku;
    private UUID stockLocationId;
    private String stockLocationName;
    private long totalAdjustments;
    private BigDecimal totalIncrease;
    private BigDecimal totalDecrease;

    // Constructors
    public FrequentAdjustmentDTO() {}

    public FrequentAdjustmentDTO(
            UUID productId,
            String productName,
            String productSku,
            UUID stockLocationId,
            String stockLocationName,
            long totalAdjustments,
            BigDecimal totalIncrease,
            BigDecimal totalDecrease) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.stockLocationId = stockLocationId;
        this.stockLocationName = stockLocationName;
        this.totalAdjustments = totalAdjustments;
        this.totalIncrease = totalIncrease;
        this.totalDecrease = totalDecrease;
    }

    // Getters and Setters
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

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public UUID getStockLocationId() {
        return stockLocationId;
    }

    public void setStockLocationId(UUID stockLocationId) {
        this.stockLocationId = stockLocationId;
    }

    public String getStockLocationName() {
        return stockLocationName;
    }

    public void setStockLocationName(String stockLocationName) {
        this.stockLocationName = stockLocationName;
    }

    public long getTotalAdjustments() {
        return totalAdjustments;
    }

    public void setTotalAdjustments(long totalAdjustments) {
        this.totalAdjustments = totalAdjustments;
    }

    public BigDecimal getTotalIncrease() {
        return totalIncrease;
    }

    public void setTotalIncrease(BigDecimal totalIncrease) {
        this.totalIncrease = totalIncrease;
    }

    public BigDecimal getTotalDecrease() {
        return totalDecrease;
    }

    public void setTotalDecrease(BigDecimal totalDecrease) {
        this.totalDecrease = totalDecrease;
    }
}
