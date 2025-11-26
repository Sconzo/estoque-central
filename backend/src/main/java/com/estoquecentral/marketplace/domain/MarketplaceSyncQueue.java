package com.estoquecentral.marketplace.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MarketplaceSyncQueue entity - Queue for marketplace synchronizations
 * Story 5.4: Stock Synchronization to Mercado Livre - AC1
 */
@Table("marketplace_sync_queue")
public class MarketplaceSyncQueue {

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

    @Column("priority")
    private Integer priority; // 0=normal, 1=high (manual)

    @Column("status")
    private SyncStatus status;

    @Column("retry_count")
    private Integer retryCount;

    @Column("max_retries")
    private Integer maxRetries;

    @Column("last_error")
    private String lastError;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("processed_at")
    private LocalDateTime processedAt;

    // Constructors

    public MarketplaceSyncQueue() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.priority = 0;
        this.status = SyncStatus.PENDING;
        this.retryCount = 0;
        this.maxRetries = 3;
        this.syncType = SyncType.STOCK;
    }

    public MarketplaceSyncQueue(UUID tenantId, UUID productId, UUID variantId, Marketplace marketplace) {
        this();
        this.tenantId = tenantId;
        this.productId = productId;
        this.variantId = variantId;
        this.marketplace = marketplace;
    }

    // Helper methods

    public void markAsProcessing() {
        this.status = SyncStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = SyncStatus.SUCCESS;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.retryCount++;
        this.lastError = error;
        this.updatedAt = LocalDateTime.now();

        if (this.retryCount >= this.maxRetries) {
            this.status = SyncStatus.FAILED;
        } else {
            this.status = SyncStatus.PENDING; // Retry
        }
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public void setStatus(SyncStatus status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
