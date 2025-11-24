package com.estoquecentral.tenant.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantSetting - Configurable settings per tenant
 * Story 4.6: Stock Reservation and Automatic Release
 *
 * <p>Stores configuration settings that can be customized per tenant.
 * Each setting is identified by a key-value pair within a tenant context.
 *
 * <p><strong>Key Settings:</strong>
 * <ul>
 *   <li>sales_order_auto_release_days - Days before auto-releasing sales order reservations (default: 7)</li>
 * </ul>
 */
@Table("tenant_settings")
public class TenantSetting {

    @Id
    private UUID id;
    private UUID tenantId;
    private String settingKey;
    private String settingValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for Spring Data JDBC
     */
    public TenantSetting() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with required fields
     */
    public TenantSetting(UUID tenantId, String settingKey, String settingValue) {
        this.tenantId = tenantId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods

    /**
     * Updates the setting value
     */
    public void updateValue(String newValue) {
        this.settingValue = newValue;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets setting value as integer
     */
    public Integer getIntValue() {
        try {
            return Integer.parseInt(this.settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets setting value as boolean
     */
    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(this.settingValue);
    }

    /**
     * Gets setting value as long
     */
    public Long getLongValue() {
        try {
            return Long.parseLong(this.settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
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

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
        this.updatedAt = LocalDateTime.now();
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
}
