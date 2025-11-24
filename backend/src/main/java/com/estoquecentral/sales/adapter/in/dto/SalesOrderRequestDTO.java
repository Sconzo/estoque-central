package com.estoquecentral.sales.adapter.in.dto;

import com.estoquecentral.sales.domain.PaymentTerms;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating/updating sales orders
 * Story 4.5: Sales Order B2B Interface
 */
public class SalesOrderRequestDTO {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Stock location ID is required")
    private UUID stockLocationId;

    private LocalDate orderDate;
    private LocalDate deliveryDateExpected;
    private PaymentTerms paymentTerms;
    private String notes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SalesOrderItemRequestDTO> items;

    public SalesOrderRequestDTO() {}

    // Getters and Setters

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SalesOrderItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<SalesOrderItemRequestDTO> items) {
        this.items = items;
    }

    /**
     * Inner DTO for sales order items in request
     */
    public static class SalesOrderItemRequestDTO {

        private UUID productId;
        private UUID variantId;

        @NotNull(message = "Quantity is required")
        private BigDecimal quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;

        public SalesOrderItemRequestDTO() {}

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public UUID getVariantId() {
            return variantId;
        }

        public void setVariantId(UUID variantId) {
            this.variantId = variantId;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
