package com.estoquecentral.purchasing.adapter.in.dto;

import com.estoquecentral.purchasing.domain.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for purchase order
 * Story 3.2: Purchase Order Creation
 */
public class PurchaseOrderResponse {

    private UUID id;
    private String poNumber;
    private SupplierSummary supplier;
    private LocationSummary stockLocation;
    private PurchaseOrderStatus status;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private BigDecimal totalAmount;
    private String notes;
    private List<PurchaseOrderItemResponse> items;
    private UserSummary createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested DTOs
    public static class SupplierSummary {
        private UUID id;
        private String companyName;

        public SupplierSummary(UUID id, String companyName) {
            this.id = id;
            this.companyName = companyName;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
    }

    public static class LocationSummary {
        private UUID id;
        private String name;

        public LocationSummary(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class UserSummary {
        private UUID id;
        private String name;

        public UserSummary(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public SupplierSummary getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierSummary supplier) {
        this.supplier = supplier;
    }

    public LocationSummary getStockLocation() {
        return stockLocation;
    }

    public void setStockLocation(LocationSummary stockLocation) {
        this.stockLocation = stockLocation;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
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

    public List<PurchaseOrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItemResponse> items) {
        this.items = items;
    }

    public UserSummary getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserSummary createdBy) {
        this.createdBy = createdBy;
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
}
