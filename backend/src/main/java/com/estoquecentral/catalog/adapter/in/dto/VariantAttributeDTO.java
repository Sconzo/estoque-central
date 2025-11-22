package com.estoquecentral.catalog.adapter.in.dto;

import java.util.List;
import java.util.UUID;

/**
 * VariantAttributeDTO - Attribute with its values
 *
 * Used for creating/displaying variant attributes (e.g., "Color" with values ["Red", "Blue"])
 */
public class VariantAttributeDTO {
    private String name;
    private List<String> values;

    public VariantAttributeDTO() {
    }

    public VariantAttributeDTO(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
