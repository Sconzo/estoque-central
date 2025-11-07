package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("cart_items")
public class CartItem {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID cartId;
    private UUID productId;
    private UUID productVariantId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private BigDecimal availableQuantity;
    private Boolean isAvailable;
    private String customOptions;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartItem() {
        this.discountAmount = BigDecimal.ZERO;
        this.isAvailable = true;
    }

    public void calculateTotals() {
        this.subtotal = this.unitPrice.multiply(this.quantity);
        this.total = this.subtotal.subtract(this.discountAmount);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateQuantity(BigDecimal newQuantity) {
        this.quantity = newQuantity;
        calculateTotals();
    }

    public boolean hasVariant() {
        return this.productVariantId != null;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCartId() { return cartId; }
    public void setCartId(UUID cartId) { this.cartId = cartId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getProductVariantId() { return productVariantId; }
    public void setProductVariantId(UUID productVariantId) { this.productVariantId = productVariantId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    // ... outros getters/setters
}
