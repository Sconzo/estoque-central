package com.estoquecentral.inventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockAdjustment - Manual stock adjustments entity
 * Story 3.5: Stock Adjustment (Ajuste de Estoque)
 */
@Table("stock_adjustments")
public class StockAdjustment {

    @Id
    private UUID id;
    private UUID tenantId;
    private String adjustmentNumber;
    private UUID productId;
    private UUID variantId;
    private UUID stockLocationId;
    private AdjustmentType adjustmentType;
    private BigDecimal quantity;
    private AdjustmentReasonCode reasonCode;
    private String reasonDescription;
    private UUID adjustedByUserId;
    private LocalDate adjustmentDate;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;

    // Constructors
    public StockAdjustment() {
        this.createdAt = LocalDateTime.now();
    }

    public StockAdjustment(UUID tenantId, UUID productId, UUID stockLocationId,
                           AdjustmentType adjustmentType, BigDecimal quantity,
                           AdjustmentReasonCode reasonCode, String reasonDescription) {
        this();
        this.tenantId = tenantId;
        this.productId = productId;
        this.stockLocationId = stockLocationId;
        this.adjustmentType = adjustmentType;
        this.quantity = quantity;
        this.reasonCode = reasonCode;
        this.reasonDescription = reasonDescription;
        this.adjustmentDate = LocalDate.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
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

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public UUID getStockLocationId() {
        return stockLocationId;
    }

    public void setStockLocationId(UUID stockLocationId) {
        this.stockLocationId = stockLocationId;
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
