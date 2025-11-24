package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.MovementReason;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public class StockMovementRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Location ID is required")
    private UUID locationId;

    @NotNull(message = "Reason is required")
    private MovementReason reason;

    private String notes;
    private String referenceType;
    private UUID referenceId;

    // Getters and Setters
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }
    public MovementReason getReason() { return reason; }
    public void setReason(MovementReason reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }
}
