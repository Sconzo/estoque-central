package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("payment_webhooks")
public class PaymentWebhook {

    @Id
    private UUID id;
    private UUID tenantId;
    private String provider;
    private String eventType;
    private String eventId;
    private UUID paymentId;
    private UUID orderId;
    private String payload;
    private String headers;
    private WebhookStatus status;
    private LocalDateTime processedAt;
    private Integer processingAttempts;
    private String lastProcessingError;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;

    public PaymentWebhook() {
        this.status = WebhookStatus.PENDING;
        this.processingAttempts = 0;
        this.receivedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == WebhookStatus.PENDING;
    }

    public boolean isProcessing() {
        return this.status == WebhookStatus.PROCESSING;
    }

    public boolean isProcessed() {
        return this.status == WebhookStatus.PROCESSED;
    }

    public boolean isFailed() {
        return this.status == WebhookStatus.FAILED;
    }

    public boolean isIgnored() {
        return this.status == WebhookStatus.IGNORED;
    }

    public void startProcessing() {
        this.status = WebhookStatus.PROCESSING;
        this.processingAttempts++;
    }

    public void markAsProcessed() {
        this.status = WebhookStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = WebhookStatus.FAILED;
        this.lastProcessingError = errorMessage;
    }

    public void ignore() {
        this.status = WebhookStatus.IGNORED;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }

    public WebhookStatus getStatus() { return status; }
    public void setStatus(WebhookStatus status) { this.status = status; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public Integer getProcessingAttempts() { return processingAttempts; }
    public void setProcessingAttempts(Integer processingAttempts) { this.processingAttempts = processingAttempts; }

    public String getLastProcessingError() { return lastProcessingError; }
    public void setLastProcessingError(String lastProcessingError) { this.lastProcessingError = lastProcessingError; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
