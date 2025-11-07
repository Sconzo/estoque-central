package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("payment_refunds")
public class PaymentRefund {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID paymentId;
    private UUID orderId;
    private String refundNumber;
    private String externalRefundId;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private String notes;
    private RefundStatus status;
    private String gatewayResponse;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime failedAt;
    private String failureCode;
    private String failureMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public PaymentRefund() {
        this.status = RefundStatus.PENDING;
        this.currency = "BRL";
        this.requestedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == RefundStatus.PENDING;
    }

    public boolean isProcessing() {
        return this.status == RefundStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return this.status == RefundStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == RefundStatus.FAILED;
    }

    public boolean isCancelled() {
        return this.status == RefundStatus.CANCELLED;
    }

    public void startProcessing() {
        this.status = RefundStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = RefundStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String errorCode, String errorMessage) {
        this.status = RefundStatus.FAILED;
        this.failureCode = errorCode;
        this.failureMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = RefundStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getRefundNumber() { return refundNumber; }
    public void setRefundNumber(String refundNumber) { this.refundNumber = refundNumber; }

    public String getExternalRefundId() { return externalRefundId; }
    public void setExternalRefundId(String externalRefundId) { this.externalRefundId = externalRefundId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }

    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}
