package com.estoquecentral.catalog.domain.variant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * VariantAttribute - Attribute definition for variants
 *
 * <p>Examples: Color, Size, Material, Style
 */
@Table("variant_attributes")
public class VariantAttribute implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private String displayName;
    private AttributeType type;
    private Integer sortOrder;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    @Transient
    private boolean isNew = false;

    public VariantAttribute() {
    }

    public VariantAttribute(UUID tenantId, String name, String displayName, AttributeType type, Integer sortOrder) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.name = name;
        this.displayName = displayName;
        this.type = type != null ? type : AttributeType.TEXT;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.ativo = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isNew = true;
    }

    // Persistable implementation

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

    public void update(String displayName, AttributeType type, Integer sortOrder, UUID updatedBy) {
        this.displayName = displayName;
        this.type = type;
        this.sortOrder = sortOrder;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public AttributeType getType() { return type; }
    public void setType(AttributeType type) { this.type = type; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}
