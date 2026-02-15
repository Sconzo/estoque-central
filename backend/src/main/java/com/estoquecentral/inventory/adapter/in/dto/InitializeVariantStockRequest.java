package com.estoquecentral.inventory.adapter.in.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request to initialize stock for a product variant at a location.
 */
public class InitializeVariantStockRequest {

    @NotNull(message = "Location ID is required")
    private UUID locationId;

    @PositiveOrZero(message = "Initial quantity must be zero or positive")
    private BigDecimal initialQuantity;

    @PositiveOrZero(message = "Minimum quantity must be zero or positive")
    private BigDecimal minimumQuantity;

    @PositiveOrZero(message = "Maximum quantity must be zero or positive")
    private BigDecimal maximumQuantity;

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(BigDecimal initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public BigDecimal getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(BigDecimal minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public BigDecimal getMaximumQuantity() {
        return maximumQuantity;
    }

    public void setMaximumQuantity(BigDecimal maximumQuantity) {
        this.maximumQuantity = maximumQuantity;
    }
}
