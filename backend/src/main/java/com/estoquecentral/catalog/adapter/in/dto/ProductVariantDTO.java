package com.estoquecentral.catalog.adapter.in.dto;

import com.estoquecentral.catalog.domain.variant.ProductVariant;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * ProductVariantDTO - Product variant details
 */
public class ProductVariantDTO {
    private UUID id;
    private UUID parentProductId;
    private String sku;
    private String barcode;
    private String name;
    private BigDecimal price;
    private BigDecimal cost;
    private String status;
    private Boolean ativo;
    private Map<String, String> attributeCombination;

    public static ProductVariantDTO fromEntity(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getId());
        dto.setParentProductId(variant.getParentProductId());
        dto.setSku(variant.getSku());
        dto.setBarcode(variant.getBarcode());
        dto.setName(variant.getName());
        dto.setPrice(variant.getPrice());
        dto.setCost(variant.getCost());
        dto.setStatus(variant.getStatus() != null ? variant.getStatus().name() : null);
        dto.setAtivo(variant.getAtivo());
        // attributeCombination will be populated by service
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(UUID parentProductId) {
        this.parentProductId = parentProductId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Map<String, String> getAttributeCombination() {
        return attributeCombination;
    }

    public void setAttributeCombination(Map<String, String> attributeCombination) {
        this.attributeCombination = attributeCombination;
    }
}
