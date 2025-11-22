package com.estoquecentral.inventory.adapter.in.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * SetMinimumQuantityRequest - Request to set minimum stock quantity
 * Story 2.7 - AC5: Define minimum stock threshold
 */
public class SetMinimumQuantityRequest {

    @NotNull(message = "Stock location ID is required")
    private UUID stockLocationId;

    @NotNull(message = "Minimum quantity is required")
    @PositiveOrZero(message = "Minimum quantity must be zero or positive")
    private BigDecimal minimumQuantity;

    // Getters and Setters
    public UUID getStockLocationId() {
        return stockLocationId;
    }

    public void setStockLocationId(UUID stockLocationId) {
        this.stockLocationId = stockLocationId;
    }

    public BigDecimal getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(BigDecimal minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }
}
