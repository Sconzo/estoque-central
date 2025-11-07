package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inventory Value Summary DTO
 * Total inventory value by location using weighted average cost
 */
public record InventoryValueSummaryDTO(
        UUID locationId,
        String locationCode,
        String locationName,
        String locationType,
        Integer uniqueProducts,
        BigDecimal totalQuantity,
        BigDecimal totalValueAtCost,
        BigDecimal averageProductCost
) {
    public InventoryValueSummaryDTO {
        uniqueProducts = uniqueProducts != null ? uniqueProducts : 0;
        totalQuantity = totalQuantity != null ? totalQuantity : BigDecimal.ZERO;
        totalValueAtCost = totalValueAtCost != null ? totalValueAtCost : BigDecimal.ZERO;
        averageProductCost = averageProductCost != null ? averageProductCost : BigDecimal.ZERO;
    }

    /**
     * Returns location type display name
     */
    public String getLocationTypeDisplay() {
        return switch (locationType) {
            case "WAREHOUSE" -> "Armazém";
            case "STORE" -> "Loja";
            case "DISTRIBUTION_CENTER" -> "Centro de Distribuição";
            case "SUPPLIER" -> "Fornecedor";
            case "CUSTOMER" -> "Cliente";
            case "VIRTUAL" -> "Virtual";
            default -> locationType;
        };
    }

    /**
     * Returns average value per product
     */
    public BigDecimal getAverageValuePerProduct() {
        if (uniqueProducts == null || uniqueProducts == 0) {
            return BigDecimal.ZERO;
        }
        return totalValueAtCost.divide(
                BigDecimal.valueOf(uniqueProducts),
                2,
                BigDecimal.ROUND_HALF_UP
        );
    }

    /**
     * Returns average quantity per product
     */
    public BigDecimal getAverageQuantityPerProduct() {
        if (uniqueProducts == null || uniqueProducts == 0) {
            return BigDecimal.ZERO;
        }
        return totalQuantity.divide(
                BigDecimal.valueOf(uniqueProducts),
                2,
                BigDecimal.ROUND_HALF_UP
        );
    }
}
