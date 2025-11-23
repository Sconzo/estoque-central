package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.AdjustmentReasonCode;
import com.estoquecentral.inventory.domain.AdjustmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockAdjustmentResponseDTO - Response DTO for stock adjustments
 * Story 3.5: Stock Adjustment
 */
public class StockAdjustmentResponseDTO {

    private UUID id;
    private String adjustmentNumber;
    private UUID productId;
    private String productName;
    private String productSku;
    private UUID variantId;
    private String variantName;
    private UUID stockLocationId;
    private String stockLocationName;
    private AdjustmentType adjustmentType;
    private BigDecimal quantity;
    private AdjustmentReasonCode reasonCode;
    private String reasonDescription;
    private UUID adjustedByUserId;
    private String adjustedByUserName;
    private LocalDate adjustmentDate;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;

    // Constructors
    public StockAdjustmentResponseDTO() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAdjustmentNumber() {
        return adjustmentNumber;
    }

    public void setAdjustmentNumber(String adjustmentNumber) {
        this.adjustmentNumber = adjustmentNumber;
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

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
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

    public AdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(AdjustmentType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public AdjustmentReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(AdjustmentReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public UUID getAdjustedByUserId() {
        return adjustedByUserId;
    }

    public void setAdjustedByUserId(UUID adjustedByUserId) {
        this.adjustedByUserId = adjustedByUserId;
    }

    public String getAdjustedByUserName() {
        return adjustedByUserName;
    }

    public void setAdjustedByUserName(String adjustedByUserName) {
        this.adjustedByUserName = adjustedByUserName;
    }

    public LocalDate getAdjustmentDate() {
        return adjustmentDate;
    }

    public void setAdjustmentDate(LocalDate adjustmentDate) {
        this.adjustmentDate = adjustmentDate;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
