package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Movement Summary by Type DTO
 * Statistics grouped by movement type
 */
public record InventoryMovementSummaryByTypeDTO(
        String movementType,
        String movementDirection,
        Long movementCount,
        BigDecimal totalQuantity,
        BigDecimal totalValue,
        BigDecimal averageUnitCost,
        LocalDateTime firstMovementDate,
        LocalDateTime lastMovementDate
) {
    public InventoryMovementSummaryByTypeDTO {
        movementCount = movementCount != null ? movementCount : 0L;
        totalQuantity = totalQuantity != null ? totalQuantity : BigDecimal.ZERO;
        totalValue = totalValue != null ? totalValue : BigDecimal.ZERO;
        averageUnitCost = averageUnitCost != null ? averageUnitCost : BigDecimal.ZERO;
    }

    /**
     * Returns movement type display name
     */
    public String getMovementTypeDisplay() {
        return switch (movementType) {
            case "PURCHASE" -> "Compra";
            case "SALE" -> "Venda";
            case "ADJUSTMENT_IN" -> "Ajuste Entrada";
            case "ADJUSTMENT_OUT" -> "Ajuste Saída";
            case "TRANSFER_IN" -> "Transferência Entrada";
            case "TRANSFER_OUT" -> "Transferência Saída";
            case "RETURN_FROM_CUSTOMER" -> "Devolução Cliente";
            case "RETURN_TO_SUPPLIER" -> "Devolução Fornecedor";
            default -> movementType;
        };
    }

    /**
     * Returns average value per movement
     */
    public BigDecimal getAverageValuePerMovement() {
        if (movementCount == null || movementCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalValue.divide(
                BigDecimal.valueOf(movementCount),
                2,
                BigDecimal.ROUND_HALF_UP
        );
    }

    /**
     * Returns average quantity per movement
     */
    public BigDecimal getAverageQuantityPerMovement() {
        if (movementCount == null || movementCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalQuantity.divide(
                BigDecimal.valueOf(movementCount),
                2,
                BigDecimal.ROUND_HALF_UP
        );
    }
}
