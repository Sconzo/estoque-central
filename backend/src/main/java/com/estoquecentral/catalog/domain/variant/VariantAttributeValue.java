package com.estoquecentral.catalog.domain.variant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("variant_attribute_values")
public class VariantAttributeValue {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID attributeId;
    private String value;
    private String displayValue;
    private String colorHex;
    private Integer sortOrder;
    private Boolean ativo;
    private LocalDateTime createdAt;

    public VariantAttributeValue() {
    }

    public VariantAttributeValue(UUID tenantId, UUID attributeId, String value, String displayValue, String colorHex, Integer sortOrder) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.attributeId = attributeId;
        this.value = value;
        this.displayValue = displayValue;
        this.colorHex = colorHex;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.ativo = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getAttributeId() { return attributeId; }
    public void setAttributeId(UUID attributeId) { this.attributeId = attributeId; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDisplayValue() { return displayValue; }
    public void setDisplayValue(String displayValue) { this.displayValue = displayValue; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
