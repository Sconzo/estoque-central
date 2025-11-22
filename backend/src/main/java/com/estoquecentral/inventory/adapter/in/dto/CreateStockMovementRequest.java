package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.MovementType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CreateStockMovementRequest - Request to create a stock movement
 * Story 2.8: Stock Movement History - AC1
 *
 * This is used for manual movements (ENTRY, EXIT, ADJUSTMENT).
 * Automated movements (SALE, PURCHASE, etc.) are created internally by the system.
 */
public class CreateStockMovementRequest {

    @NotNull(message = "Product ID or Variant ID is required")
    private UUID productId;

    private UUID variantId;

    @NotNull(message = "Stock location is required")
    private UUID stockLocationId;

    @NotNull(message = "Movement type is required")
    private MovementType type;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    private String reason;

    // Optional document reference (for linking movements to source documents)
    private String documentType;
    private UUID documentId;

    // ============================================================
    // Constructors
    // ============================================================

    public CreateStockMovementRequest() {
    }

    public CreateStockMovementRequest(UUID productId, UUID variantId, UUID stockLocationId,
                                     MovementType type, BigDecimal quantity, String reason) {
        this.productId = productId;
        this.variantId = variantId;
        this.stockLocationId = stockLocationId;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
    }

    // ============================================================
    // Validation
    // ============================================================

    /**
     * Validates that either productId or variantId is set (XOR)
     */
    public void validate() {
        if (productId == null && variantId == null) {
            throw new IllegalArgumentException("Either productId or variantId must be provided");
        }
        if (productId != null && variantId != null) {
            throw new IllegalArgumentException("Cannot provide both productId and variantId");
        }
    }

    // ============================================================
    // Getters and Setters
    // ============================================================

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public UUID getStockLocationId() {
        return stockLocationId;
    }

    public void setStockLocationId(UUID stockLocationId) {
        this.stockLocationId = stockLocationId;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }
}
