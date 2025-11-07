package com.estoquecentral.catalog.adapter.in.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ProductUpdateRequest - Request DTO for updating products
 *
 * <p>Contains validation rules for product updates.
 * Note: SKU and barcode cannot be updated (would require separate endpoint).
 */
public class ProductUpdateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 200, message = "Product name must be between 1 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be greater than or equal to 0")
    private BigDecimal cost;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    private Boolean controlsInventory;

    public ProductUpdateRequest() {
    }

    public ProductUpdateRequest(String name, String description, UUID categoryId,
                                 BigDecimal price, BigDecimal cost, String unit,
                                 Boolean controlsInventory) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.price = price;
        this.cost = cost;
        this.unit = unit;
        this.controlsInventory = controlsInventory;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getControlsInventory() {
        return controlsInventory;
    }

    public void setControlsInventory(Boolean controlsInventory) {
        this.controlsInventory = controlsInventory;
    }
}
