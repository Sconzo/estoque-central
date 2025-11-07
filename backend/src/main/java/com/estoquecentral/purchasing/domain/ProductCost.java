package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("product_costs")
public class ProductCost {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private UUID productVariantId;
    private UUID locationId;
    private BigDecimal averageCost;
    private BigDecimal lastCost;
    private BigDecimal currentQuantity;
    private BigDecimal totalValue;
    private BigDecimal totalPurchases;
    private BigDecimal totalPurchaseValue;
    private LocalDateTime lastMovementDate;
    private String lastMovementType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductCost() {
        this.averageCost = BigDecimal.ZERO;
        this.currentQuantity = BigDecimal.ZERO;
        this.totalValue = BigDecimal.ZERO;
        this.totalPurchases = BigDecimal.ZERO;
        this.totalPurchaseValue = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculateAveragePurchaseCost() {
        if (this.totalPurchases.compareTo(BigDecimal.ZERO) > 0) {
            return this.totalPurchaseValue.divide(this.totalPurchases, 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateMarginPercentage(BigDecimal sellingPrice) {
        if (sellingPrice.compareTo(BigDecimal.ZERO) > 0 &&
            this.averageCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margin = sellingPrice.subtract(this.averageCost);
            return margin.divide(sellingPrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateMarginAmount(BigDecimal sellingPrice) {
        return sellingPrice.subtract(this.averageCost);
    }

    public boolean hasLowMargin(BigDecimal sellingPrice, BigDecimal threshold) {
        BigDecimal marginPct = calculateMarginPercentage(sellingPrice);
        return marginPct.compareTo(threshold) < 0;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getProductVariantId() { return productVariantId; }
    public void setProductVariantId(UUID productVariantId) { this.productVariantId = productVariantId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public BigDecimal getAverageCost() { return averageCost; }
    public void setAverageCost(BigDecimal averageCost) { this.averageCost = averageCost; }

    public BigDecimal getLastCost() { return lastCost; }
    public void setLastCost(BigDecimal lastCost) { this.lastCost = lastCost; }

    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(BigDecimal totalPurchases) { this.totalPurchases = totalPurchases; }

    public BigDecimal getTotalPurchaseValue() { return totalPurchaseValue; }
    public void setTotalPurchaseValue(BigDecimal totalPurchaseValue) { this.totalPurchaseValue = totalPurchaseValue; }

    public LocalDateTime getLastMovementDate() { return lastMovementDate; }
    public void setLastMovementDate(LocalDateTime lastMovementDate) { this.lastMovementDate = lastMovementDate; }

    public String getLastMovementType() { return lastMovementType; }
    public void setLastMovementType(String lastMovementType) { this.lastMovementType = lastMovementType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
