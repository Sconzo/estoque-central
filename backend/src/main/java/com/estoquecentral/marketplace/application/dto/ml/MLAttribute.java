package com.estoquecentral.marketplace.application.dto.ml;

/**
 * DTO for Mercado Livre attribute
 * Story 5.3: Publish Products to Mercado Livre - AC3
 */
public class MLAttribute {

    private String id;
    private String name;
    private String valueId;
    private String valueName;

    public MLAttribute() {
    }

    public MLAttribute(String id, String valueName) {
        this.id = id;
        this.valueName = valueName;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
