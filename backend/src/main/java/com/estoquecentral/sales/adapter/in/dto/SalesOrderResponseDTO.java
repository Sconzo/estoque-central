package com.estoquecentral.sales.adapter.in.dto;

import com.estoquecentral.sales.domain.PaymentTerms;
import com.estoquecentral.sales.domain.SalesOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for sales orders
 * Story 4.5: Sales Order B2B Interface
 */
public class SalesOrderResponseDTO {

    private UUID id;
    private String orderNumber;
    private SalesOrderStatus status;
    private LocalDate orderDate;
    private LocalDate deliveryDateExpected;
    private PaymentTerms paymentTerms;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Summary fields
    private CustomerSummary customer;
    private LocationSummary location;
    private List<SalesOrderItemResponseDTO> items;

    public SalesOrderResponseDTO() {}

    // Inner classes for summaries

    public static class CustomerSummary {
        private UUID id;
        private String name;
        private String documentNumber; // CPF or CNPJ

        public CustomerSummary() {}

        public CustomerSummary(UUID id, String name, String documentNumber) {
            this.id = id;
            this.name = name;
            this.documentNumber = documentNumber;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDocumentNumber() {
            return documentNumber;
        }

        public void setDocumentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
        }
    }

    public static class LocationSummary {
        private UUID id;
        private String name;

        public LocationSummary() {}

        public LocationSummary(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public CustomerSummary getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerSummary customer) {
        this.customer = customer;
    }

    public LocationSummary getLocation() {
        return location;
    }

    public void setLocation(LocationSummary location) {
        this.location = location;
    }

    public List<SalesOrderItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<SalesOrderItemResponseDTO> items) {
        this.items = items;
    }
}
