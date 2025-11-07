package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory Movement Detail DTO
 * Detailed information about a single inventory movement
 */
public record InventoryMovementDetailDTO(
        UUID movementId,
        LocalDateTime movementDate,
        LocalDate movementDateOnly,
        String movementType,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalValue,
        String referenceType,
        UUID referenceId,
        String notes,

        // Product details
        UUID productId,
        String sku,
        String productName,
        String categoryName,
        String unitOfMeasure,

        // Location details
        UUID locationId,
        String locationCode,
        String locationName,
        String locationType,

        // Inventory state
        BigDecimal currentStock,
        BigDecimal minimumQuantity,
        BigDecimal maximumQuantity,

        // Movement classification
        String movementDirection,

        // Audit
        UUID createdBy,
        LocalDateTime createdAt
) {
    public InventoryMovementDetailDTO {
        quantity = quantity != null ? quantity : BigDecimal.ZERO;
        unitCost = unitCost != null ? unitCost : BigDecimal.ZERO;
        totalValue = totalValue != null ? totalValue : BigDecimal.ZERO;
        currentStock = currentStock != null ? currentStock : BigDecimal.ZERO;
        minimumQuantity = minimumQuantity != null ? minimumQuantity : BigDecimal.ZERO;
        maximumQuantity = maximumQuantity != null ? maximumQuantity : BigDecimal.ZERO;
    }

    /**
     * Returns movement type display name in Portuguese
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
     * Returns movement direction display name
     */
    public String getMovementDirectionDisplay() {
        return switch (movementDirection) {
            case "IN" -> "Entrada";
            case "OUT" -> "Saída";
            default -> "Outro";
        };
    }

    /**
     * Returns true if this is an inbound movement
     */
    public boolean isInboundMovement() {
        return "IN".equals(movementDirection);
    }

    /**
     * Returns true if this is an outbound movement
     */
    public boolean isOutboundMovement() {
        return "OUT".equals(movementDirection);
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
     * Returns formatted quantity with unit of measure
     */
    public String getFormattedQuantity() {
        if (unitOfMeasure != null && !unitOfMeasure.isEmpty()) {
            return quantity + " " + unitOfMeasure;
        }
        return quantity.toString();
    }
}
