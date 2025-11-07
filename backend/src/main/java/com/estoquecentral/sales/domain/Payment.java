package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("payments")
public class Payment {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID orderId;
    private String paymentNumber;
    private String externalPaymentId;
    private PaymentMethod paymentMethod;
    private String paymentProvider;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    // Card details
    private String cardBrand;
    private String cardLastDigits;
    private String cardHolderName;

    // PIX details
    private String pixKey;
    private String pixQrCode;
    private String pixQrCodeImageUrl;

    // Boleto details
    private String boletoBarcode;
    private String boletoUrl;
    private LocalDate boletoDueDate;

    // Metadata
    private String gatewayResponse;
    private String paymentMetadata;

    // Timestamps
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime failedAt;
    private LocalDateTime expiresAt;

    // Failure details
    private String failureCode;
    private String failureMessage;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public Payment() {
        this.status = PaymentStatus.PENDING;
        this.currency = "BRL";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isAuthorized() {
        return this.status == PaymentStatus.AUTHORIZED;
    }

    public boolean isCaptured() {
        return this.status == PaymentStatus.CAPTURED;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    public boolean isCancelled() {
        return this.status == PaymentStatus.CANCELLED;
    }

    public boolean isRefunded() {
        return this.status == PaymentStatus.REFUNDED ||
               this.status == PaymentStatus.PARTIALLY_REFUNDED;
    }

    public boolean isExpired() {
        return this.expiresAt != null &&
               LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean canBeAuthorized() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean canBeCaptured() {
        return this.status == PaymentStatus.PENDING ||
               this.status == PaymentStatus.AUTHORIZED;
    }

    public boolean canBeCancelled() {
        return this.status == PaymentStatus.PENDING ||
               this.status == PaymentStatus.AUTHORIZED;
    }

    public boolean canBeRefunded() {
        return this.status == PaymentStatus.CAPTURED;
    }

    public void authorize() {
        if (!canBeAuthorized()) {
            throw new IllegalStateException("Payment cannot be authorized in current status: " + this.status);
        }
        this.status = PaymentStatus.AUTHORIZED;
        this.authorizedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void capture() {
        if (!canBeCaptured()) {
            throw new IllegalStateException("Payment cannot be captured in current status: " + this.status);
        }
        this.status = PaymentStatus.CAPTURED;
        this.capturedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Payment cannot be cancelled in current status: " + this.status);
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String errorCode, String errorMessage) {
        this.status = PaymentStatus.FAILED;
        this.failureCode = errorCode;
        this.failureMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsRefunded(boolean isPartial) {
        if (!canBeRefunded()) {
            throw new IllegalStateException("Payment cannot be refunded in current status: " + this.status);
        }
        this.status = isPartial ? PaymentStatus.PARTIALLY_REFUNDED : PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = PaymentStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCreditCard() {
        return this.paymentMethod == PaymentMethod.CREDIT_CARD;
    }

    public boolean isDebitCard() {
        return this.paymentMethod == PaymentMethod.DEBIT_CARD;
    }

    public boolean isPix() {
        return this.paymentMethod == PaymentMethod.PIX;
    }

    public boolean isBoleto() {
        return this.paymentMethod == PaymentMethod.BOLETO;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(String paymentNumber) { this.paymentNumber = paymentNumber; }

    public String getExternalPaymentId() { return externalPaymentId; }
    public void setExternalPaymentId(String externalPaymentId) { this.externalPaymentId = externalPaymentId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

    public String getCardLastDigits() { return cardLastDigits; }
    public void setCardLastDigits(String cardLastDigits) { this.cardLastDigits = cardLastDigits; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }

    public String getPixQrCode() { return pixQrCode; }
    public void setPixQrCode(String pixQrCode) { this.pixQrCode = pixQrCode; }

    public String getPixQrCodeImageUrl() { return pixQrCodeImageUrl; }
    public void setPixQrCodeImageUrl(String pixQrCodeImageUrl) { this.pixQrCodeImageUrl = pixQrCodeImageUrl; }

    public String getBoletoBarcode() { return boletoBarcode; }
    public void setBoletoBarcode(String boletoBarcode) { this.boletoBarcode = boletoBarcode; }

    public String getBoletoUrl() { return boletoUrl; }
    public void setBoletoUrl(String boletoUrl) { this.boletoUrl = boletoUrl; }

    public LocalDate getBoletoDueDate() { return boletoDueDate; }
    public void setBoletoDueDate(LocalDate boletoDueDate) { this.boletoDueDate = boletoDueDate; }

    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    public String getPaymentMetadata() { return paymentMetadata; }
    public void setPaymentMetadata(String paymentMetadata) { this.paymentMetadata = paymentMetadata; }

    public LocalDateTime getAuthorizedAt() { return authorizedAt; }
    public void setAuthorizedAt(LocalDateTime authorizedAt) { this.authorizedAt = authorizedAt; }

    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

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
