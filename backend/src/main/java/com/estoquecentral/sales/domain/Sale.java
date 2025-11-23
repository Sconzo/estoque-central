package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sale entity - Represents a sale transaction
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Table("sales")
public class Sale {
    @Id
    private UUID id;
    private UUID tenantId;

    // Sale identification
    private String saleNumber;
    private UUID customerId;
    private UUID stockLocationId;

    // Payment
    private PaymentMethod paymentMethod;
    private BigDecimal paymentAmountReceived;
    private BigDecimal changeAmount;

    // Totals
    private BigDecimal totalAmount;
    private BigDecimal discount;

    // NFCe
    private NfceStatus nfceStatus;
    private String nfceKey;
    private String nfceXml;
    private String nfceErrorMessage;

    // Audit
    private UUID createdByUserId;
    private LocalDateTime saleDate;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    // Constructors
    public Sale() {
        this.nfceStatus = NfceStatus.PENDING;
        this.discount = BigDecimal.ZERO;
        this.saleDate = LocalDateTime.now();
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

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getStockLocationId() {
        return stockLocationId;
    }

    public void setStockLocationId(UUID stockLocationId) {
        this.stockLocationId = stockLocationId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getPaymentAmountReceived() {
        return paymentAmountReceived;
    }

    public void setPaymentAmountReceived(BigDecimal paymentAmountReceived) {
        this.paymentAmountReceived = paymentAmountReceived;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public NfceStatus getNfceStatus() {
        return nfceStatus;
    }

    public void setNfceStatus(NfceStatus nfceStatus) {
        this.nfceStatus = nfceStatus;
    }

    public String getNfceKey() {
        return nfceKey;
    }

    public void setNfceKey(String nfceKey) {
        this.nfceKey = nfceKey;
    }

    public String getNfceXml() {
        return nfceXml;
    }

    public void setNfceXml(String nfceXml) {
        this.nfceXml = nfceXml;
    }

    public String getNfceErrorMessage() {
        return nfceErrorMessage;
    }

    public void setNfceErrorMessage(String nfceErrorMessage) {
        this.nfceErrorMessage = nfceErrorMessage;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
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

    // Business methods
    public void calculateChangeAmount() {
        if (this.paymentAmountReceived != null && this.totalAmount != null) {
            this.changeAmount = this.paymentAmountReceived.subtract(this.totalAmount);
        }
    }

    public boolean isNfceEmitted() {
        return this.nfceStatus == NfceStatus.EMITTED;
    }

    public boolean isNfcePending() {
        return this.nfceStatus == NfceStatus.PENDING;
    }

    public void markNfceAsEmitted(String nfceKey, String nfceXml) {
        this.nfceStatus = NfceStatus.EMITTED;
        this.nfceKey = nfceKey;
        this.nfceXml = nfceXml;
        this.nfceErrorMessage = null;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void markNfceAsFailed(String errorMessage) {
        this.nfceStatus = NfceStatus.FAILED;
        this.nfceErrorMessage = errorMessage;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void markNfceAsPending() {
        this.nfceStatus = NfceStatus.PENDING;
        this.dataAtualizacao = LocalDateTime.now();
    }
}
