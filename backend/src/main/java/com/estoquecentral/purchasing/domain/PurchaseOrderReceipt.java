package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("purchase_order_receipts")
public class PurchaseOrderReceipt {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID purchaseOrderId;
    private String receiptNumber;
    private UUID locationId;
    private LocalDateTime receiptDate;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private BigDecimal invoiceValue;
    private QualityCheckStatus qualityCheckStatus;
    private String qualityCheckNotes;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID receivedBy;

    public PurchaseOrderReceipt() {
        this.receiptDate = LocalDateTime.now();
        this.qualityCheckStatus = QualityCheckStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isQualityCheckPending() {
        return this.qualityCheckStatus == QualityCheckStatus.PENDING;
    }

    public boolean isQualityCheckApproved() {
        return this.qualityCheckStatus == QualityCheckStatus.APPROVED;
    }

    public boolean isQualityCheckRejected() {
        return this.qualityCheckStatus == QualityCheckStatus.REJECTED;
    }

    public boolean isQualityCheckPartial() {
        return this.qualityCheckStatus == QualityCheckStatus.PARTIAL;
    }

    public void approveQualityCheck(String notes) {
        this.qualityCheckStatus = QualityCheckStatus.APPROVED;
        this.qualityCheckNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void rejectQualityCheck(String notes) {
        this.qualityCheckStatus = QualityCheckStatus.REJECTED;
        this.qualityCheckNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsPartialQualityCheck(String notes) {
        this.qualityCheckStatus = QualityCheckStatus.PARTIAL;
        this.qualityCheckNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public LocalDateTime getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDateTime receiptDate) { this.receiptDate = receiptDate; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public BigDecimal getInvoiceValue() { return invoiceValue; }
    public void setInvoiceValue(BigDecimal invoiceValue) { this.invoiceValue = invoiceValue; }

    public QualityCheckStatus getQualityCheckStatus() { return qualityCheckStatus; }
    public void setQualityCheckStatus(QualityCheckStatus qualityCheckStatus) { this.qualityCheckStatus = qualityCheckStatus; }

    public String getQualityCheckNotes() { return qualityCheckNotes; }
    public void setQualityCheckNotes(String qualityCheckNotes) { this.qualityCheckNotes = qualityCheckNotes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getReceivedBy() { return receivedBy; }
    public void setReceivedBy(UUID receivedBy) { this.receivedBy = receivedBy; }
}
