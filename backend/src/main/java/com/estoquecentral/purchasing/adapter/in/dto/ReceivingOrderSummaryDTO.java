package com.estoquecentral.purchasing.adapter.in.dto;

import com.estoquecentral.purchasing.domain.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for pending purchase orders summary (Story 3.3 AC3)
 * Used in mobile receiving interface to list orders pending receipt
 */
public class ReceivingOrderSummaryDTO {
    private UUID id;
    private String orderNumber;
    private String supplierName;
    private LocalDate orderDate;
    private PurchaseOrderStatus status;
    private ItemsSummary itemsSummary;

    public static class ItemsSummary {
        private int totalItems;
        private int totalReceived;
        private int totalPending;
        private BigDecimal totalAmount;

        public ItemsSummary() {}

        public ItemsSummary(int totalItems, int totalReceived, int totalPending, BigDecimal totalAmount) {
            this.totalItems = totalItems;
            this.totalReceived = totalReceived;
            this.totalPending = totalPending;
            this.totalAmount = totalAmount;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }

        public int getTotalReceived() {
            return totalReceived;
        }

        public void setTotalReceived(int totalReceived) {
            this.totalReceived = totalReceived;
        }

        public int getTotalPending() {
            return totalPending;
        }

        public void setTotalPending(int totalPending) {
            this.totalPending = totalPending;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public ReceivingOrderSummaryDTO() {}

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

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }

    public ItemsSummary getItemsSummary() {
        return itemsSummary;
    }

    public void setItemsSummary(ItemsSummary itemsSummary) {
        this.itemsSummary = itemsSummary;
    }
}
