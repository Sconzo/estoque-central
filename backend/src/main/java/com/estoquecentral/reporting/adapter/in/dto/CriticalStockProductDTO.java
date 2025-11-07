package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Critical Stock Product DTO
 * Products below minimum stock level requiring replenishment
 */
public record CriticalStockProductDTO(
        UUID productId,
        String sku,
        String productName,
        String categoryName,
        UUID locationId,
        String locationCode,
        String locationName,
        BigDecimal currentQuantity,
        BigDecimal minimumQuantity,
        BigDecimal maximumQuantity,
        BigDecimal reorderPoint,
        BigDecimal quantityNeeded,
        String alertLevel,
        BigDecimal unitCost,
        BigDecimal replenishmentCost,
        LocalDateTime lastUpdated
) {
    public CriticalStockProductDTO {
        currentQuantity = currentQuantity != null ? currentQuantity : BigDecimal.ZERO;
        minimumQuantity = minimumQuantity != null ? minimumQuantity : BigDecimal.ZERO;
        maximumQuantity = maximumQuantity != null ? maximumQuantity : BigDecimal.ZERO;
        reorderPoint = reorderPoint != null ? reorderPoint : BigDecimal.ZERO;
        quantityNeeded = quantityNeeded != null ? quantityNeeded : BigDecimal.ZERO;
        unitCost = unitCost != null ? unitCost : BigDecimal.ZERO;
        replenishmentCost = replenishmentCost != null ? replenishmentCost : BigDecimal.ZERO;
    }

    /**
     * Returns alert severity level
     * 1 = Critical (OUT_OF_STOCK)
     * 2 = High (CRITICAL)
     * 3 = Medium (LOW)
     */
    public int getAlertSeverity() {
        return switch (alertLevel) {
            case "OUT_OF_STOCK" -> 1;
            case "CRITICAL" -> 2;
            case "LOW" -> 3;
            default -> 4;
        };
    }

    /**
     * Returns stock percentage relative to minimum
     */
    public double getStockPercentage() {
        if (minimumQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return currentQuantity
                .divide(minimumQuantity, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Returns true if stock is completely out
     */
    public boolean isOutOfStock() {
        return "OUT_OF_STOCK".equals(alertLevel);
    }

    /**
     * Returns display label for alert level
     */
    public String getAlertLevelDisplay() {
        return switch (alertLevel) {
            case "OUT_OF_STOCK" -> "Esgotado";
            case "CRITICAL" -> "Crítico";
            case "LOW" -> "Baixo";
            default -> "OK";
        };
    }
}
