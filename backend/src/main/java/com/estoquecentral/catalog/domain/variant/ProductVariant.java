package com.estoquecentral.catalog.domain.variant;

import com.estoquecentral.catalog.domain.ProductStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProductVariant - Variant of a parent product
 *
 * <p>Example: "T-shirt Basic - Red - M"
 * <ul>
 *   <li>Parent: "T-shirt Basic" (VARIANT_PARENT)</li>
 *   <li>Variant: "T-shirt Basic - Red - M" (VARIANT)</li>
 *   <li>Attributes: Color=Red, Size=M</li>
 * </ul>
 */
@Table("product_variants")
public class ProductVariant {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID parentProductId;
    private String sku;
    private String barcode;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal cost;
    private String imageUrl;
    private ProductStatus status;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public ProductVariant() {
    }

    public ProductVariant(UUID tenantId, UUID parentProductId, String sku, String name,
                          BigDecimal price, BigDecimal cost) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.parentProductId = parentProductId;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.cost = cost;
        this.status = ProductStatus.ACTIVE;
        this.ativo = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String name, String description, BigDecimal price, BigDecimal cost,
                       String imageUrl, UUID updatedBy) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.cost = cost;
        this.imageUrl = imageUrl;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(ProductStatus status, UUID updatedBy) {
        this.status = status;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.ativo = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.ativo != null && this.ativo;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getParentProductId() { return parentProductId; }
    public void setParentProductId(UUID parentProductId) { this.parentProductId = parentProductId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
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
