package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("supplier_products")
public class SupplierProduct {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID supplierId;
    private UUID productId;
    private String supplierSku;
    private String supplierProductName;
    private BigDecimal costPrice;
    private String currency;
    private LocalDateTime lastPriceUpdate;
    private Integer leadTimeDays;
    private BigDecimal minimumOrderQuantity;
    private Boolean isPreferredSupplier;
    private Boolean isAvailable;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean ativo;

    public SupplierProduct() {
        this.currency = "BRL";
        this.isPreferredSupplier = false;
        this.isAvailable = true;
        this.ativo = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCostPrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cost price must be positive");
        }
        this.costPrice = newPrice;
        this.lastPriceUpdate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsPreferred() {
        this.isPreferredSupplier = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void removePreferred() {
        this.isPreferredSupplier = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsAvailable() {
        this.isAvailable = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsUnavailable() {
        this.isAvailable = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canBeOrdered() {
        return this.isAvailable && this.ativo;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getSupplierSku() { return supplierSku; }
    public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }

    public String getSupplierProductName() { return supplierProductName; }
    public void setSupplierProductName(String supplierProductName) { this.supplierProductName = supplierProductName; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public BigDecimal getMinimumOrderQuantity() { return minimumOrderQuantity; }
    public void setMinimumOrderQuantity(BigDecimal minimumOrderQuantity) { this.minimumOrderQuantity = minimumOrderQuantity; }

    public Boolean getIsPreferredSupplier() { return isPreferredSupplier; }
    public void setIsPreferredSupplier(Boolean isPreferredSupplier) { this.isPreferredSupplier = isPreferredSupplier; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
