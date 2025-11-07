package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory Movement Summary by Product DTO
 * Statistics grouped by product
 */
public record InventoryMovementSummaryByProductDTO(
        UUID productId,
        String sku,
        String productName,
        String categoryName,
        Long totalMovements,
        Long inMovementsCount,
        BigDecimal totalQuantityIn,
        Long outMovementsCount,
        BigDecimal totalQuantityOut,
        BigDecimal netQuantityChange,
        BigDecimal totalValueMoved,
        LocalDateTime firstMovementDate,
        LocalDateTime lastMovementDate,
        BigDecimal currentStock
) {
    public InventoryMovementSummaryByProductDTO {
        totalMovements = totalMovements != null ? totalMovements : 0L;
        inMovementsCount = inMovementsCount != null ? inMovementsCount : 0L;
        totalQuantityIn = totalQuantityIn != null ? totalQuantityIn : BigDecimal.ZERO;
        outMovementsCount = outMovementsCount != null ? outMovementsCount : 0L;
        totalQuantityOut = totalQuantityOut != null ? totalQuantityOut : BigDecimal.ZERO;
        netQuantityChange = netQuantityChange != null ? netQuantityChange : BigDecimal.ZERO;
        totalValueMoved = totalValueMoved != null ? totalValueMoved : BigDecimal.ZERO;
        currentStock = currentStock != null ? currentStock : BigDecimal.ZERO;
    }

    /**
     * Returns turnover ratio (out / in)
     */
    public double getTurnoverRatio() {
        if (totalQuantityIn.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return totalQuantityOut
                .divide(totalQuantityIn, 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    /**
     * Returns percentage of inbound movements
     */
    public double getInboundPercentage() {
        if (totalMovements == null || totalMovements == 0) {
            return 0.0;
        }
        return ((double) inMovementsCount / totalMovements) * 100;
    }

    /**
     * Returns percentage of outbound movements
     */
    public double getOutboundPercentage() {
        if (totalMovements == null || totalMovements == 0) {
            return 0.0;
        }
        return ((double) outMovementsCount / totalMovements) * 100;
    }

    /**
     * Returns average value per movement
     */
    public BigDecimal getAverageValuePerMovement() {
        if (totalMovements == null || totalMovements == 0) {
            return BigDecimal.ZERO;
        }
        return totalValueMoved.divide(
                BigDecimal.valueOf(totalMovements),
                2,
                BigDecimal.ROUND_HALF_UP
        );
    }

    /**
     * Returns true if product has more outbound than inbound
     */
    public boolean isHighTurnover() {
        return totalQuantityOut.compareTo(totalQuantityIn) > 0;
    }
}
