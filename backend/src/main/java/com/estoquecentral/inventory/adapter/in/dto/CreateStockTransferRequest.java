package com.estoquecentral.inventory.adapter.in.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CreateStockTransferRequest - Request to create a stock transfer
 * Story 2.9: Stock Transfer Between Locations - AC2
 */
public class CreateStockTransferRequest {

    private UUID productId;
    private UUID variantId;

    @NotNull(message = "Origin location is required")
    private UUID originLocationId;

    @NotNull(message = "Destination location is required")
    private UUID destinationLocationId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    private String reason;

    public void validate() {
        if (productId == null && variantId == null) {
            throw new IllegalArgumentException("Either productId or variantId must be provided");
        }
        if (productId != null && variantId != null) {
            throw new IllegalArgumentException("Cannot provide both productId and variantId");
        }
        if (originLocationId != null && originLocationId.equals(destinationLocationId)) {
            throw new IllegalArgumentException("Origin and destination locations must be different");
        }
    }

    // Getters and Setters
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }
    public UUID getOriginLocationId() { return originLocationId; }
    public void setOriginLocationId(UUID originLocationId) { this.originLocationId = originLocationId; }
    public UUID getDestinationLocationId() { return destinationLocationId; }
    public void setDestinationLocationId(UUID destinationLocationId) { this.destinationLocationId = destinationLocationId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
