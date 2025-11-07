package com.estoquecentral.catalog.adapter.in.dto;

import com.estoquecentral.catalog.domain.Category;

import java.time.Instant;
import java.util.UUID;

/**
 * CategoryDTO - Data Transfer Object for Category
 *
 * <p>Response DTO containing category information for API responses.
 *
 * @see Category
 */
public class CategoryDTO {

    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private Boolean ativo;
    private Instant createdAt;
    private Instant updatedAt;

    public CategoryDTO() {
    }

    public CategoryDTO(UUID id, String name, String description, UUID parentId,
                      Boolean ativo, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Converts Category entity to DTO
     *
     * @param category category entity
     * @return category DTO
     */
    public static CategoryDTO fromEntity(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                category.getAtivo(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
