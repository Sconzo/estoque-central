package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.MovementType;
import com.estoquecentral.inventory.domain.StockMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockMovementResponse - Response for stock movement queries
 * Story 2.8: Stock Movement History - AC2, AC3
 *
 * Enriched with product/location/user names for display purposes
 */
public class StockMovementResponse {

    private UUID id;
    private UUID tenantId;

    // Product or Variant
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String productSku;

    // Location
    private UUID stockLocationId;
    private String locationName;
    private String locationCode;

    // Movement details
    private MovementType type;
    private String typeDisplayName;
    private String typeIcon;

    private BigDecimal quantity;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    // Audit
    private UUID userId;
    private String userName;

    // Document reference
    private String documentType;
    private UUID documentId;

    private String reason;

    private LocalDateTime createdAt;

    // ============================================================
    // Factory Method
    // ============================================================

    /**
     * Creates a response from a domain entity
     * Note: Names are not included and should be enriched by the service
     */
    public static StockMovementResponse fromEntity(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        response.setTenantId(movement.getTenantId());
        response.setProductId(movement.getProductId());
        response.setVariantId(movement.getVariantId());
        response.setStockLocationId(movement.getStockLocationId());
        response.setType(movement.getType());
        response.setTypeDisplayName(movement.getType().getDisplayName());
        response.setTypeIcon(movement.getType().getIcon());
        response.setQuantity(movement.getQuantity());
        response.setBalanceBefore(movement.getBalanceBefore());
        response.setBalanceAfter(movement.getBalanceAfter());
        response.setUserId(movement.getUserId());
        response.setDocumentType(movement.getDocumentType());
        response.setDocumentId(movement.getDocumentId());
        response.setReason(movement.getReason());
        response.setCreatedAt(movement.getCreatedAt());
        return response;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public UUID getStockLocationId() {
        return stockLocationId;
    }

    public void setStockLocationId(UUID stockLocationId) {
        this.stockLocationId = stockLocationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public String getTypeDisplayName() {
        return typeDisplayName;
    }

    public void setTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
    }

    public String getTypeIcon() {
        return typeIcon;
    }

    public void setTypeIcon(String typeIcon) {
        this.typeIcon = typeIcon;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
}
