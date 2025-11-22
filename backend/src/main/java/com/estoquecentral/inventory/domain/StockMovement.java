package com.estoquecentral.inventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockMovement - Immutable audit trail of stock movements
 * Story 2.8: Stock Movement History
 *
 * This entity is append-only (immutable). The database has triggers that
 * prevent UPDATE and DELETE operations to maintain audit trail integrity.
 *
 * AC1: Records all stock movements with complete traceability
 * AC2: Tracks balance before/after each movement
 * AC3: Links to source documents (sales, purchases, transfers, etc.)
 */
@Table("stock_movements")
public class StockMovement {

    @Id
    private UUID id;

    private UUID tenantId;

    // Product or Variant (XOR - only one should be set)
    private UUID productId;
    private UUID variantId;

    private UUID stockLocationId;

    private MovementType type;

    // Quantity moved (positive = entry, negative = exit)
    private BigDecimal quantity;

    // Balance tracking
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    // Audit: who performed the movement
    private UUID userId;

    // Document reference (generic FK)
    private String documentType;  // SALE, PURCHASE, TRANSFER, etc.
    private UUID documentId;

    // Reason/observation
    private String reason;

    // Immutable timestamp (set by database)
    private LocalDateTime createdAt;

    // ============================================================
    // Constructors
    // ============================================================

    /**
     * Default constructor for Spring Data JDBC
     */
    public StockMovement() {
    }

    /**
     * Constructor for product movement
     */
    public StockMovement(UUID tenantId, UUID productId, UUID stockLocationId, MovementType type,
                        BigDecimal quantity, BigDecimal balanceBefore, UUID userId, String reason) {
        this.tenantId = tenantId;
        this.productId = productId;
        this.variantId = null;
        this.stockLocationId = stockLocationId;
        this.type = type;
        this.quantity = quantity;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceBefore.add(quantity);
        this.userId = userId;
        this.reason = reason;
    }

    /**
     * Constructor for variant movement
     */
    public StockMovement(UUID tenantId, UUID variantId, UUID stockLocationId, MovementType type,
                        BigDecimal quantity, BigDecimal balanceBefore, UUID userId, String reason,
                        boolean isVariant) {
        this.tenantId = tenantId;
        this.productId = null;
        this.variantId = variantId;
        this.stockLocationId = stockLocationId;
        this.type = type;
        this.quantity = quantity;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceBefore.add(quantity);
        this.userId = userId;
        this.reason = reason;
    }

    /**
     * Full constructor with document reference
     */
    public StockMovement(UUID tenantId, UUID productId, UUID variantId, UUID stockLocationId,
                        MovementType type, BigDecimal quantity, BigDecimal balanceBefore,
                        UUID userId, String documentType, UUID documentId, String reason) {
        this.tenantId = tenantId;
        this.productId = productId;
        this.variantId = variantId;
        this.stockLocationId = stockLocationId;
        this.type = type;
        this.quantity = quantity;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceBefore.add(quantity);
        this.userId = userId;
        this.documentType = documentType;
        this.documentId = documentId;
        this.reason = reason;
    }

    // ============================================================
    // Business Methods
    // ============================================================

    /**
     * Validates that the movement is consistent
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
        if (stockLocationId == null) {
            throw new IllegalStateException("StockLocationId is required");
        }
        if (type == null) {
            throw new IllegalStateException("Movement type is required");
        }
        if (quantity == null) {
            throw new IllegalStateException("Quantity is required");
        }
        if (balanceBefore == null) {
            throw new IllegalStateException("BalanceBefore is required");
        }
        if (userId == null) {
            throw new IllegalStateException("UserId is required");
        }

        // Validate balance calculation
        BigDecimal expectedBalanceAfter = balanceBefore.add(quantity);
        if (balanceAfter != null && balanceAfter.compareTo(expectedBalanceAfter) != 0) {
            throw new IllegalStateException(
                "Balance calculation mismatch: balanceAfter should be " + expectedBalanceAfter +
                " but is " + balanceAfter
            );
        }
    }

    /**
     * Checks if this is an entry movement (positive quantity)
     */
    public boolean isEntry() {
        return quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this is an exit movement (negative quantity)
     */
    public boolean isExit() {
        return quantity.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Gets the absolute quantity value
     */
    public BigDecimal getAbsoluteQuantity() {
        return quantity.abs();
    }

    /**
     * Checks if movement is for a product (not variant)
     */
    public boolean isProductMovement() {
        return productId != null;
    }

    /**
     * Checks if movement is for a variant
     */
    public boolean isVariantMovement() {
        return variantId != null;
    }

    /**
     * Gets the effective item ID (product or variant)
     */
    public UUID getEffectiveItemId() {
        return productId != null ? productId : variantId;
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

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ============================================================
    // Object Methods
    // ============================================================

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", productId=" + productId +
                ", variantId=" + variantId +
                ", stockLocationId=" + stockLocationId +
                ", type=" + type +
                ", quantity=" + quantity +
                ", balanceBefore=" + balanceBefore +
                ", balanceAfter=" + balanceAfter +
                ", userId=" + userId +
                ", documentType='" + documentType + '\'' +
                ", documentId=" + documentId +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
