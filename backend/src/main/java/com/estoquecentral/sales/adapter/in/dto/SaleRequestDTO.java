package com.estoquecentral.sales.adapter.in.dto;

import com.estoquecentral.sales.domain.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * SaleRequestDTO - Request DTO for creating sales
 * Story 4.3: NFCe Emission and Stock Decrease
 */
public class SaleRequestDTO {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Stock location ID is required")
    private UUID stockLocationId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment amount received is required")
    @Positive(message = "Payment amount received must be positive")
    private BigDecimal paymentAmountReceived;

    @NotEmpty(message = "Items list cannot be empty")
    private List<ItemRequestDTO> items;

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

    public List<ItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemRequestDTO> items) {
        this.items = items;
    }

    /**
     * Inner class for sale item requests
     */
    public static class ItemRequestDTO {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        private UUID variantId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private BigDecimal quantity;

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;

        // Getters and Setters
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
