package com.estoquecentral.marketplace.application.dto;

import com.estoquecentral.marketplace.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for order preview/listing
 * Story 5.5: Import and Process Orders from Mercado Livre - AC6
 */
public class OrderPreviewResponse {
    private UUID id;
    private String orderIdMarketplace;
    private String marketplace;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentStatus;
    private String shippingStatus;
    private LocalDateTime importedAt;
    private UUID saleId;
    private UUID salesOrderId;
    private boolean hasInternalRecord;  // true if linked to sale or sales_order

    public OrderPreviewResponse() {
    }

    public OrderPreviewResponse(UUID id, String orderIdMarketplace, String marketplace,
                                String customerName, String customerEmail, BigDecimal totalAmount,
                                OrderStatus status, String paymentStatus, String shippingStatus,
                                LocalDateTime importedAt, UUID saleId, UUID salesOrderId) {
        this.id = id;
        this.orderIdMarketplace = orderIdMarketplace;
        this.marketplace = marketplace;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.shippingStatus = shippingStatus;
        this.importedAt = importedAt;
        this.saleId = saleId;
        this.salesOrderId = salesOrderId;
        this.hasInternalRecord = (saleId != null || salesOrderId != null);
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderIdMarketplace() {
        return orderIdMarketplace;
    }

    public void setOrderIdMarketplace(String orderIdMarketplace) {
        this.orderIdMarketplace = orderIdMarketplace;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = marketplace;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public UUID getSaleId() {
        return saleId;
    }

    public void setSaleId(UUID saleId) {
        this.saleId = saleId;
        this.hasInternalRecord = (saleId != null || salesOrderId != null);
    }

    public UUID getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(UUID salesOrderId) {
        this.salesOrderId = salesOrderId;
        this.hasInternalRecord = (saleId != null || salesOrderId != null);
    }

    public boolean isHasInternalRecord() {
        return hasInternalRecord;
    }

    public void setHasInternalRecord(boolean hasInternalRecord) {
        this.hasInternalRecord = hasInternalRecord;
    }
}
