package com.estoquecentral.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProductAttribute - Descriptive key/value attribute for a product
 *
 * <p>Examples: "Memória RAM: 8GB", "Processador: i7", "Material: 100% algodão"
 *
 * <p>Applicable to SIMPLE and VARIANT_PARENT products.
 */
@Table("product_attributes")
public class ProductAttribute implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private String attributeKey;
    private String attributeValue;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew = false;

    public ProductAttribute() {
    }

    public ProductAttribute(UUID tenantId, UUID productId, String attributeKey, String attributeValue, Integer sortOrder) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.productId = productId;
        this.attributeKey = attributeKey;
        this.attributeValue = attributeValue;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.createdAt = LocalDateTime.now();
        this.isNew = true;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
