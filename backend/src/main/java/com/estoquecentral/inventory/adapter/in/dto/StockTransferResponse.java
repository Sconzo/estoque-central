package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.StockTransfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockTransferResponse - Response for stock transfer queries
 * Story 2.9: Stock Transfer Between Locations - AC5
 */
public class StockTransferResponse {
    private UUID id;
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String productSku;
    private UUID originLocationId;
    private String originLocationName;
    private String originLocationCode;
    private UUID destinationLocationId;
    private String destinationLocationName;
    private String destinationLocationCode;
    private BigDecimal quantity;
    private String reason;
    private UUID userId;
    private String userName;
    private String status;
    private LocalDateTime createdAt;

    public static StockTransferResponse fromEntity(StockTransfer transfer) {
        StockTransferResponse response = new StockTransferResponse();
        response.setId(transfer.getId());
        response.setProductId(transfer.getProductId());
        response.setVariantId(transfer.getVariantId());
        response.setOriginLocationId(transfer.getOriginLocationId());
        response.setDestinationLocationId(transfer.getDestinationLocationId());
        response.setQuantity(transfer.getQuantity());
        response.setReason(transfer.getReason());
        response.setUserId(transfer.getUserId());
        response.setStatus(transfer.getStatus().name());
        response.setCreatedAt(transfer.getCreatedAt());
        return response;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public UUID getOriginLocationId() { return originLocationId; }
    public void setOriginLocationId(UUID originLocationId) { this.originLocationId = originLocationId; }
    public String getOriginLocationName() { return originLocationName; }
    public void setOriginLocationName(String originLocationName) { this.originLocationName = originLocationName; }
    public String getOriginLocationCode() { return originLocationCode; }
    public void setOriginLocationCode(String originLocationCode) { this.originLocationCode = originLocationCode; }
    public UUID getDestinationLocationId() { return destinationLocationId; }
    public void setDestinationLocationId(UUID destinationLocationId) { this.destinationLocationId = destinationLocationId; }
    public String getDestinationLocationName() { return destinationLocationName; }
    public void setDestinationLocationName(String destinationLocationName) { this.destinationLocationName = destinationLocationName; }
    public String getDestinationLocationCode() { return destinationLocationCode; }
    public void setDestinationLocationCode(String destinationLocationCode) { this.destinationLocationCode = destinationLocationCode; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
