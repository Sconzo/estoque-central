package com.estoquecentral.catalog.adapter.in.dto;

import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductComponent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BomComponentResponse - Response with component details
 */
public class BomComponentResponse {

    private UUID id;
    private UUID componentProductId;
    private String componentSku;
    private String componentName;
    private BigDecimal quantityRequired;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BomComponentResponse() {
    }

    /**
     * Creates response from ProductComponent and Product
     */
    public static BomComponentResponse from(ProductComponent component, Product componentProduct) {
        BomComponentResponse response = new BomComponentResponse();
        response.id = component.getId();
        response.componentProductId = component.getComponentProductId();
        response.componentSku = componentProduct.getSku();
        response.componentName = componentProduct.getName();
        response.quantityRequired = component.getQuantityRequired();
        response.unit = componentProduct.getUnit();
        response.createdAt = component.getCreatedAt();
        response.updatedAt = component.getUpdatedAt();
        return response;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getComponentProductId() {
        return componentProductId;
    }

    public void setComponentProductId(UUID componentProductId) {
        this.componentProductId = componentProductId;
    }

    public String getComponentSku() {
        return componentSku;
    }

    public void setComponentSku(String componentSku) {
        this.componentSku = componentSku;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
}
