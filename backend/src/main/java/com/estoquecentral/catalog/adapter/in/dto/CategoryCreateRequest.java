package com.estoquecentral.catalog.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * CategoryCreateRequest - Request DTO for creating/updating categories
 *
 * <p>Contains validation rules for category creation.
 */
public class CategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private UUID parentId;

    public CategoryCreateRequest() {
    }

    public CategoryCreateRequest(String name, String description, UUID parentId) {
        this.name = name;
        this.description = description;
        this.parentId = parentId;
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

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }
}
