package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("purchase_orders")
public class PurchaseOrder {

    @Id
    private UUID id;
    private UUID tenantId;
    private String poNumber;
    private UUID supplierId;
    private UUID locationId;
    private PurchaseOrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal otherCosts;
    private BigDecimal total;
    private String paymentMethod;
    private String paymentTerms;
    private POPaymentStatus paymentStatus;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDateTime approvedAt;
    private LocalDateTime sentToSupplierAt;
    private LocalDateTime cancelledAt;
    private UUID approvedBy;
    private String approvalNotes;
    private String notes;
    private String internalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public PurchaseOrder() {
        this.status = PurchaseOrderStatus.DRAFT;
        this.paymentStatus = POPaymentStatus.PENDING;
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.shippingAmount = BigDecimal.ZERO;
        this.otherCosts = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.orderDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateTotals() {
        this.total = this.subtotal
                .subtract(this.discountAmount)
                .add(this.taxAmount)
                .add(this.shippingAmount)
                .add(this.otherCosts);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDraft() {
        return this.status == PurchaseOrderStatus.DRAFT;
    }

    public boolean isPendingApproval() {
        return this.status == PurchaseOrderStatus.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return this.status == PurchaseOrderStatus.APPROVED;
    }

    public boolean isSentToSupplier() {
        return this.status == PurchaseOrderStatus.SENT_TO_SUPPLIER;
    }

    public boolean isPartiallyReceived() {
        return this.status == PurchaseOrderStatus.PARTIALLY_RECEIVED;
    }

    public boolean isReceived() {
        return this.status == PurchaseOrderStatus.RECEIVED;
    }

    public boolean isCancelled() {
        return this.status == PurchaseOrderStatus.CANCELLED;
    }

    public boolean isClosed() {
        return this.status == PurchaseOrderStatus.CLOSED;
    }

    public boolean canBeApproved() {
        return this.status == PurchaseOrderStatus.DRAFT ||
               this.status == PurchaseOrderStatus.PENDING_APPROVAL;
    }

    public boolean canBeSentToSupplier() {
        return this.status == PurchaseOrderStatus.APPROVED;
    }

    public boolean canBeCancelled() {
        return this.status == PurchaseOrderStatus.DRAFT ||
               this.status == PurchaseOrderStatus.PENDING_APPROVAL ||
               this.status == PurchaseOrderStatus.APPROVED ||
               this.status == PurchaseOrderStatus.SENT_TO_SUPPLIER;
    }

    public boolean canReceiveItems() {
        return this.status == PurchaseOrderStatus.SENT_TO_SUPPLIER ||
               this.status == PurchaseOrderStatus.PARTIALLY_RECEIVED;
    }

    public void submitForApproval() {
        this.status = PurchaseOrderStatus.PENDING_APPROVAL;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(UUID approverId, String notes) {
        if (!canBeApproved()) {
            throw new IllegalStateException("PO cannot be approved in current status: " + this.status);
        }
        this.status = PurchaseOrderStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
        this.approvalNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void sendToSupplier() {
        if (!canBeSentToSupplier()) {
            throw new IllegalStateException("PO cannot be sent in current status: " + this.status);
        }
        this.status = PurchaseOrderStatus.SENT_TO_SUPPLIER;
        this.sentToSupplierAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsPartiallyReceived() {
        this.status = PurchaseOrderStatus.PARTIALLY_RECEIVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsReceived() {
        this.status = PurchaseOrderStatus.RECEIVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("PO cannot be cancelled in current status: " + this.status);
        }
        this.status = PurchaseOrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = PurchaseOrderStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        return this.expectedDeliveryDate != null &&
               LocalDate.now().isAfter(this.expectedDeliveryDate) &&
               !isReceived() &&
               !isCancelled() &&
               !isClosed();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public PurchaseOrderStatus getStatus() { return status; }
    public void setStatus(PurchaseOrderStatus status) { this.status = status; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getShippingAmount() { return shippingAmount; }
    public void setShippingAmount(BigDecimal shippingAmount) { this.shippingAmount = shippingAmount; }

    public BigDecimal getOtherCosts() { return otherCosts; }
    public void setOtherCosts(BigDecimal otherCosts) { this.otherCosts = otherCosts; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public POPaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(POPaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getSentToSupplierAt() { return sentToSupplierAt; }
    public void setSentToSupplierAt(LocalDateTime sentToSupplierAt) { this.sentToSupplierAt = sentToSupplierAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovalNotes() { return approvalNotes; }
    public void setApprovalNotes(String approvalNotes) { this.approvalNotes = approvalNotes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}
