package com.estoquecentral.inventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("stock_alerts")
public class StockAlert {

    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private AlertType alertType;
    private UUID productId;
    private UUID locationId;
    private UUID categoryId;
    private BigDecimal thresholdQuantity;
    private BigDecimal thresholdPercentage;
    private Boolean notifyEmail;
    private Boolean notifyWebhook;
    private Boolean notifyInternal;
    private String emailRecipients;
    private String webhookUrl;
    private Integer frequencyHours;
    private LocalDateTime lastTriggeredAt;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public StockAlert() {
    }

    public boolean canTrigger() {
        if (!ativo) return false;
        if (lastTriggeredAt == null) return true;
        LocalDateTime nextTrigger = lastTriggeredAt.plusHours(frequencyHours);
        return LocalDateTime.now().isAfter(nextTrigger);
    }

    public void updateLastTriggered() {
        this.lastTriggeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public BigDecimal getThresholdQuantity() { return thresholdQuantity; }
    public void setThresholdQuantity(BigDecimal thresholdQuantity) { this.thresholdQuantity = thresholdQuantity; }
    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean notifyEmail) { this.notifyEmail = notifyEmail; }
    public String getEmailRecipients() { return emailRecipients; }
    public void setEmailRecipients(String emailRecipients) { this.emailRecipients = emailRecipients; }
    public LocalDateTime getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    // ... outros getters/setters omitidos para brevidade
}
