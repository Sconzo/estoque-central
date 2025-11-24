package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SalesOrder - B2B Sales Order entity
 * Story 4.5: Sales Order B2B Interface
 *
 * Represents wholesale/B2B orders with customer, location, and delivery tracking.
 * Different from PDV sales (sales table) which are point-of-sale transactions.
 */
@Table("sales_orders")
public class SalesOrder {

    @Id
    private UUID id;
    private UUID tenantId;
    private String orderNumber;
    private UUID customerId;
    private UUID stockLocationId;
    private SalesOrderStatus status;
    private LocalDate orderDate;
    private LocalDate deliveryDateExpected;
    private PaymentTerms paymentTerms;
    private BigDecimal totalAmount;
    private String notes;
    private UUID createdByUserId;
    private LocalDateTime dataCriacao;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    public SalesOrder() {
        this.status = SalesOrderStatus.DRAFT;
        this.totalAmount = BigDecimal.ZERO;
        this.orderDate = LocalDate.now();
        this.dataCriacao = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods

    public boolean isDraft() {
        return this.status == SalesOrderStatus.DRAFT;
    }

    public boolean isConfirmed() {
        return this.status == SalesOrderStatus.CONFIRMED;
    }

    public boolean isInvoiced() {
        return this.status == SalesOrderStatus.INVOICED;
    }

    public boolean isCancelled() {
        return this.status == SalesOrderStatus.CANCELLED;
    }

    public boolean canBeUpdated() {
        return this.status == SalesOrderStatus.DRAFT;
    }

    public boolean canBeConfirmed() {
        return this.status == SalesOrderStatus.DRAFT;
    }

    public boolean canBeCancelled() {
        return this.status == SalesOrderStatus.DRAFT ||
               this.status == SalesOrderStatus.CONFIRMED;
    }

    public void confirm() {
        if (!canBeConfirmed()) {
            throw new IllegalStateException("Cannot confirm order in status: " + this.status);
        }
        this.status = SalesOrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel order in status: " + this.status);
        }
        this.status = SalesOrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInvoiced() {
        if (!isConfirmed()) {
            throw new IllegalStateException("Can only invoice confirmed orders");
        }
        this.status = SalesOrderStatus.INVOICED;
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateTotal(BigDecimal total) {
        this.totalAmount = total;
        this.updatedAt = LocalDateTime.now();
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public SalesOrderStatus getStatus() {
        return status;
    }

    public void setStatus(SalesOrderStatus status) {
        this.status = status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getDeliveryDateExpected() {
        return deliveryDateExpected;
    }

    public void setDeliveryDateExpected(LocalDate deliveryDateExpected) {
        this.deliveryDateExpected = deliveryDateExpected;
    }

    public PaymentTerms getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(PaymentTerms paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
