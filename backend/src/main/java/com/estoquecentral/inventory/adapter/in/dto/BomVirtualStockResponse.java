package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.catalog.application.composite.CompositeProductService;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * BomVirtualStockResponse - Response for BOM Virtual stock calculation
 * Story 2.7 - AC4: Calculate stock for composite products with virtual BOM
 */
public class BomVirtualStockResponse {
    private UUID productId;
    private String productName;
    private String productSku;
    private BigDecimal quantityForSale;
    private UUID limitingComponentId;
    private String limitingComponentName;
    private String message;

    public static BomVirtualStockResponse fromCompositeResponse(
            CompositeProductService.AvailableStockResponse response,
            String productName,
            String productSku) {
        BomVirtualStockResponse dto = new BomVirtualStockResponse();
        dto.setProductId(response.getProductId());
        dto.setProductName(productName);
        dto.setProductSku(productSku);
        dto.setQuantityForSale(response.getAvailableQuantity());
        dto.setLimitingComponentId(response.getLimitingComponentId());
        dto.setLimitingComponentName(response.getLimitingComponentName());
        dto.setMessage(response.getMessage());
        return dto;
    }

    // Getters and Setters
    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
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

    public BigDecimal getQuantityForSale() {
        return quantityForSale;
    }

    public void setQuantityForSale(BigDecimal quantityForSale) {
        this.quantityForSale = quantityForSale;
    }

    public UUID getLimitingComponentId() {
        return limitingComponentId;
    }

    public void setLimitingComponentId(UUID limitingComponentId) {
        this.limitingComponentId = limitingComponentId;
    }

    public String getLimitingComponentName() {
        return limitingComponentName;
    }

    public void setLimitingComponentName(String limitingComponentName) {
        this.limitingComponentName = limitingComponentName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
