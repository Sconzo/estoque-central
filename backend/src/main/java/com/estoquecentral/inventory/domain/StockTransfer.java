package com.estoquecentral.inventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockTransfer - Entity for stock transfers between locations
 * Story 2.9: Stock Transfer Between Locations
 *
 * Represents a transfer of stock from one location to another.
 * Each transfer creates two stock movements (EXIT from origin, ENTRY to destination).
 *
 * AC1: Tabela stock_transfers
 * AC2: Validação de transferências
 * AC3: Transação atômica
 */
@Table("stock_transfers")
public class StockTransfer {

    @Id
    private UUID id;

    private UUID tenantId;

    // Product or Variant (XOR - only one should be set)
    private UUID productId;
    private UUID variantId;

    // Origin and destination locations
    private UUID originLocationId;
    private UUID destinationLocationId;

    // Transfer details
    private BigDecimal quantity;
    private String reason;

    // Audit
    private UUID userId;

    // Status
    private TransferStatus status;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ============================================================
    // Constructors
    // ============================================================

    /**
     * Default constructor for Spring Data JDBC
     */
    public StockTransfer() {
        this.status = TransferStatus.COMPLETED;
    }

    /**
     * Constructor for product transfer
     */
    public StockTransfer(UUID tenantId, UUID productId, UUID originLocationId,
                        UUID destinationLocationId, BigDecimal quantity,
                        String reason, UUID userId) {
        this.tenantId = tenantId;
        this.productId = productId;
        this.variantId = null;
        this.originLocationId = originLocationId;
        this.destinationLocationId = destinationLocationId;
        this.quantity = quantity;
        this.reason = reason;
        this.userId = userId;
        this.status = TransferStatus.COMPLETED;
    }

    /**
     * Constructor for variant transfer
     */
    public StockTransfer(UUID tenantId, UUID variantId, UUID originLocationId,
                        UUID destinationLocationId, BigDecimal quantity,
                        String reason, UUID userId, boolean isVariant) {
        this.tenantId = tenantId;
        this.productId = null;
        this.variantId = variantId;
        this.originLocationId = originLocationId;
        this.destinationLocationId = destinationLocationId;
        this.quantity = quantity;
        this.reason = reason;
        this.userId = userId;
        this.status = TransferStatus.COMPLETED;
    }

    // ============================================================
    // Business Methods
    // ============================================================

    /**
     * Validates the transfer
     */
    public void validate() {
        if (tenantId == null) {
            throw new IllegalStateException("TenantId is required");
        }
        if (productId == null && variantId == null) {
            throw new IllegalStateException("Either productId or variantId must be set");
        }
        if (productId != null && variantId != null) {
            throw new IllegalStateException("Cannot set both productId and variantId");
        }
        if (originLocationId == null) {
            throw new IllegalStateException("Origin location is required");
        }
        if (destinationLocationId == null) {
            throw new IllegalStateException("Destination location is required");
        }
        if (originLocationId.equals(destinationLocationId)) {
            throw new IllegalStateException("Origin and destination locations must be different");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Quantity must be positive");
        }
        if (userId == null) {
            throw new IllegalStateException("UserId is required");
        }
    }

    /**
     * Checks if transfer is for a product (not variant)
     */
    public boolean isProductTransfer() {
        return productId != null;
    }

    /**
     * Checks if transfer is for a variant
     */
    public boolean isVariantTransfer() {
        return variantId != null;
    }

    /**
     * Gets the effective item ID (product or variant)
     */
    public UUID getEffectiveItemId() {
        return productId != null ? productId : variantId;
    }

    /**
     * Marks transfer as completed
     */
    public void complete() {
        this.status = TransferStatus.COMPLETED;
    }

    /**
     * Marks transfer as cancelled
     */
    public void cancel() {
        this.status = TransferStatus.CANCELLED;
    }

    /**
     * Checks if transfer is completed
     */
    public boolean isCompleted() {
        return this.status == TransferStatus.COMPLETED;
    }

    /**
     * Checks if transfer is pending
     */
    public boolean isPending() {
        return this.status == TransferStatus.PENDING;
    }

    // ============================================================
    // Getters and Setters
    // ============================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

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

    public UUID getOriginLocationId() {
        return originLocationId;
    }

    public void setOriginLocationId(UUID originLocationId) {
        this.originLocationId = originLocationId;
    }

    public UUID getDestinationLocationId() {
        return destinationLocationId;
    }

    public void setDestinationLocationId(UUID destinationLocationId) {
        this.destinationLocationId = destinationLocationId;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // Transfer Status Enum
    // ============================================================

    public enum TransferStatus {
        PENDING("Pendente"),
        COMPLETED("Concluído"),
        CANCELLED("Cancelado");

        private final String displayName;

        TransferStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ============================================================
    // Object Methods
    // ============================================================

    @Override
    public String toString() {
        return "StockTransfer{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", productId=" + productId +
                ", variantId=" + variantId +
                ", originLocationId=" + originLocationId +
                ", destinationLocationId=" + destinationLocationId +
                ", quantity=" + quantity +
                ", reason='" + reason + '\'' +
                ", userId=" + userId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
