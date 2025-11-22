package com.estoquecentral.catalog.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * CreateVariantProductRequest - Request to create product with variants
 *
 * Creates parent product and defines attributes (without generating variants yet)
 */
public class CreateVariantProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Base SKU is required")
    private String baseSku;

    private String description;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private BigDecimal cost;

    private String unit = "UN";

    @NotNull(message = "Attributes are required")
    @Size(min = 1, max = 3, message = "Must have between 1 and 3 attributes")
    private List<VariantAttributeDTO> attributes;

    public CreateVariantProductRequest() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseSku() {
        return baseSku;
    }

    public void setBaseSku(String baseSku) {
        this.baseSku = baseSku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<VariantAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<VariantAttributeDTO> attributes) {
        this.attributes = attributes;
    }
}
