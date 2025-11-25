package com.estoquecentral.shared.tenant.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantSetting - Tenant-specific configuration settings
 * Story 4.6: Stock Reservation and Automatic Release - AC5
 *
 * <p>Stores configuration key-value pairs per tenant:
 * <ul>
 *   <li>sales_order_auto_release_days: Number of days before auto-releasing reserved stock (default: 7)</li>
 *   <li>Other tenant-specific settings can be added in the future</li>
 * </ul>
 */
@Table("tenant_settings")
public class TenantSetting {

    @Id
    private UUID id;
    private UUID tenantId;
    private String settingKey;
    private String settingValue;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public TenantSetting() {
    }

    public TenantSetting(UUID tenantId, String settingKey, String settingValue) {
        this.tenantId = tenantId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
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
        this.dataAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
