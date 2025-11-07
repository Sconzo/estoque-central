package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("carts")
public class Cart {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID customerId;
    private String sessionId;
    private UUID userId;
    private CartStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal total;
    private String couponCode;
    private BigDecimal discountPercentage;
    private UUID locationId;
    private String notes;
    private UUID convertedToOrderId;
    private LocalDateTime convertedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cart() {
        this.status = CartStatus.ACTIVE;
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.shippingAmount = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    public void calculateTotals() {
        this.total = this.subtotal
                .subtract(this.discountAmount)
                .add(this.taxAmount)
                .add(this.shippingAmount);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == CartStatus.ACTIVE;
    }

    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void expire() {
        this.status = CartStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public void convert(UUID orderId) {
        this.status = CartStatus.CONVERTED;
        this.convertedToOrderId = orderId;
        this.convertedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public CartStatus getStatus() { return status; }
    public void setStatus(CartStatus status) { this.status = status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    // ... outros getters/setters
}
