package com.estoquecentral.sales.adapter.in.dto;

import com.estoquecentral.sales.domain.NfceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * SaleResponseDTO - Response DTO for sales
 * Story 4.3: NFCe Emission and Stock Decrease
 */
public class SaleResponseDTO {

    private UUID id;
    private String saleNumber;
    private UUID customerId;
    private BigDecimal totalAmount;
    private BigDecimal changeAmount;
    private NfceStatus nfceStatus;
    private String nfceKey;
    private LocalDateTime saleDate;
    private List<ItemResponseDTO> items;

    // Constructors
    public SaleResponseDTO() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
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

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public List<ItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemResponseDTO> items) {
        this.items = items;
    }

    /**
     * Inner class for sale item responses
     */
    public static class ItemResponseDTO {

        private UUID productId;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        // Constructors
        public ItemResponseDTO() {}

        // Getters and Setters
        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
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

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
