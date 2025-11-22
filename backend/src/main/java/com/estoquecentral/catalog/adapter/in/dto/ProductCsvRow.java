package com.estoquecentral.catalog.adapter.in.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductCsvRow - Represents a single row from CSV import
 *
 * <p>Contains:
 * <ul>
 *   <li>Row number for error reporting</li>
 *   <li>All CSV fields (type, name, sku, price, etc.)</li>
 *   <li>Validation errors (if any)</li>
 *   <li>Valid flag indicating if row passed validation</li>
 * </ul>
 */
public class ProductCsvRow {
    private int rowNumber;

    // CSV fields
    private String type;           // SIMPLE, VARIANT_PARENT, VARIANT, COMPOSITE
    private String name;
    private String sku;
    private String barcode;
    private String description;
    private String categoryId;
    private BigDecimal price;
    private BigDecimal cost;
    private String unit;
    private Boolean controlsInventory;
    private String status;         // ACTIVE, INACTIVE, DISCONTINUED
    private String bomType;        // VIRTUAL, PHYSICAL (for COMPOSITE products)

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBomType() {
        return bomType;
    }

    public void setBomType(String bomType) {
        this.bomType = bomType;
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
