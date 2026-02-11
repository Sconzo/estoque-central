package com.estoquecentral.catalog.adapter.in.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ProductCsvRow - Represents a single row from CSV import (SIMPLE products only)
 *
 * <p>Contains:
 * <ul>
 *   <li>Row number for error reporting</li>
 *   <li>CSV fields (name, sku, price, etc.)</li>
 *   <li>Validation errors (if any)</li>
 *   <li>Valid flag indicating if row passed validation</li>
 * </ul>
 */
public class ProductCsvRow {
    private int rowNumber;

    // CSV fields
    private String name;
    private String sku;
    private String barcode;
    private String description;
    private String category;           // Category name from CSV
    private UUID resolvedCategoryId;   // Resolved after lookup
    private BigDecimal price;
    private BigDecimal cost;
    private String unit;
    private Boolean controlsInventory;

    // Validation
    private List<String> errors;
    private boolean valid;

    public ProductCsvRow() {
        this.errors = new ArrayList<>();
        this.valid = true;
    }

    public ProductCsvRow(int rowNumber) {
        this();
        this.rowNumber = rowNumber;
    }

    /**
     * Adds a validation error
     */
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    /**
     * Checks if row has any validation errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    // Getters and Setters

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UUID getResolvedCategoryId() {
        return resolvedCategoryId;
    }

    public void setResolvedCategoryId(UUID resolvedCategoryId) {
        this.resolvedCategoryId = resolvedCategoryId;
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

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
