package com.estoquecentral.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Category - Product category with hierarchical tree structure
 *
 * <p>Represents a product category in a hierarchical tree structure using
 * self-referencing parent_id. Supports unlimited depth for organizing products
 * in logical structures (e.g., Electronics > Computers > Notebooks).
 *
 * <p><strong>Hierarchical Structure:</strong>
 * <pre>
 * Root Categories (parent_id = null)
 * ├── Category A (parent_id = root_id)
 * │   ├── Subcategory A.1
 * │   └── Subcategory A.2
 * └── Category B
 *     └── Subcategory B.1
 * </pre>
 *
 * <p><strong>Multi-tenancy:</strong> Categories are tenant-specific and
 * stored in tenant schemas. Each tenant has isolated category tree.
 *
 * <p><strong>Business Rules:</strong>
 * <ul>
 *   <li>Category name must be unique within same parent</li>
 *   <li>Cannot create circular references (category as its own ancestor)</li>
 *   <li>Cannot delete category if it has active products</li>
 *   <li>Soft delete (ativo flag) preserves data for auditing</li>
 * </ul>
 *
 * @see com.estoquecentral.catalog.application.CategoryService
 */
@Table("categories")
public class Category {

    @Id
    private UUID id;

    private String name;
    private String description;
    private UUID parentId;  // Self-reference for hierarchy
    private Boolean ativo;

    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    /**
     * Default constructor (required by Spring Data JDBC)
     */
    public Category() {
        this.ativo = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Constructor for creating new category
     *
     * @param id unique identifier
     * @param name category name (must be unique within parent)
     * @param description optional description
     * @param parentId parent category ID (null for root)
     */
    public Category(UUID id, String name, String description, UUID parentId) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
    }

    /**
     * Full constructor with audit fields
     */
    public Category(UUID id, String name, String description, UUID parentId,
                    Boolean ativo, Instant createdAt, Instant updatedAt,
                    UUID createdBy, UUID updatedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // ==================== Business Methods ====================

    /**
     * Updates category information
     *
     * @param name new name (optional)
     * @param description new description (optional)
     * @param updatedBy user performing the update
     */
    public void update(String name, String description, UUID updatedBy) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    /**
     * Deactivates category (soft delete)
     */
    public void deactivate() {
        this.ativo = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Activates previously deactivated category
     */
    public void activate() {
        this.ativo = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if this is a root category (no parent)
     *
     * @return true if root category, false otherwise
     */
    public boolean isRoot() {
        return this.parentId == null;
    }

    /**
     * Checks if category is active
     *
     * @return true if active, false if deactivated
     */
    public boolean isActive() {
        return this.ativo != null && this.ativo;
    }

    // ==================== Getters and Setters ====================

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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", ativo=" + ativo +
                '}';
    }
}
