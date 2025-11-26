package com.estoquecentral.marketplace.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MarketplaceSyncLog entity - Audit log for marketplace synchronizations
 * Story 5.4: Stock Synchronization to Mercado Livre - AC4
 */
@Table("marketplace_sync_logs")
public class MarketplaceSyncLog {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("product_id")
    private UUID productId;

    @Column("variant_id")
    private UUID variantId;

    @Column("marketplace")
    private Marketplace marketplace;

    @Column("sync_type")
    private SyncType syncType;

    @Column("old_value")
    private BigDecimal oldValue;

    @Column("new_value")
    private BigDecimal newValue;

    @Column("status")
    private SyncStatus status;

    @Column("error_message")
    private String errorMessage;

    @Column("retry_count")
    private Integer retryCount;

    @Column("synced_at")
    private LocalDateTime syncedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    // Constructors

    public MarketplaceSyncLog() {
        this.createdAt = LocalDateTime.now();
        this.syncedAt = LocalDateTime.now();
        this.retryCount = 0;
    }

    public MarketplaceSyncLog(UUID tenantId, UUID productId, UUID variantId, Marketplace marketplace,
                              SyncType syncType, BigDecimal oldValue, BigDecimal newValue, SyncStatus status) {
        this();
        this.tenantId = tenantId;
        this.productId = productId;
        this.variantId = variantId;
        this.marketplace = marketplace;
        this.syncType = syncType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.status = status;
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

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public void setSyncType(SyncType syncType) {
        this.syncType = syncType;
    }

    public BigDecimal getOldValue() {
        return oldValue;
    }

    public void setOldValue(BigDecimal oldValue) {
        this.oldValue = oldValue;
    }

    public BigDecimal getNewValue() {
        return newValue;
    }

    public void setNewValue(BigDecimal newValue) {
        this.newValue = newValue;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public void setStatus(SyncStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
