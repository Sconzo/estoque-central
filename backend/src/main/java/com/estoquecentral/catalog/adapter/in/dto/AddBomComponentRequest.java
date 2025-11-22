package com.estoquecentral.catalog.adapter.in.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * AddBomComponentRequest - Request to add component to BOM
 */
public class AddBomComponentRequest {

    @NotNull(message = "Component product ID is required")
    private UUID componentProductId;

    @NotNull(message = "Quantity required is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
    private BigDecimal quantityRequired;

    public AddBomComponentRequest() {
    }

    public AddBomComponentRequest(UUID componentProductId, BigDecimal quantityRequired) {
        this.componentProductId = componentProductId;
        this.quantityRequired = quantityRequired;
    }

    public UUID getComponentProductId() {
        return componentProductId;
    }

    public void setComponentProductId(UUID componentProductId) {
        this.componentProductId = componentProductId;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }
}
