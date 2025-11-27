package com.estoquecentral.marketplace.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MarketplaceOrder Entity - Represents orders imported from marketplaces
 * Story 5.5: Import and Process Orders from Mercado Livre - AC1
 */
@Table("marketplace_orders")
public class MarketplaceOrder {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("marketplace")
    private Marketplace marketplace;

    @Column("order_id_marketplace")
    private String orderIdMarketplace;

    @Column("sale_id")
    private UUID saleId;  // FK to sales table

    @Column("sales_order_id")
    private UUID salesOrderId;  // FK to sales_orders table

    @Column("customer_name")
    private String customerName;

    @Column("customer_email")
    private String customerEmail;

    @Column("customer_phone")
    private String customerPhone;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("status")
    private OrderStatus status;

    @Column("payment_status")
    private String paymentStatus;  // ML payment status (approved, pending, etc)

    @Column("shipping_status")
    private String shippingStatus;  // ML shipping status (ready_to_ship, shipped, delivered)

    @Column("ml_raw_data")
    private String mlRawData;  // JSONB stored as String

    @Column("imported_at")
    private LocalDateTime importedAt;

    @Column("last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column("data_criacao")
    private LocalDateTime dataCriacao;

    @Column("data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Constructors

    public MarketplaceOrder() {
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

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public String getOrderIdMarketplace() {
        return orderIdMarketplace;
    }

    public void setOrderIdMarketplace(String orderIdMarketplace) {
        this.orderIdMarketplace = orderIdMarketplace;
    }

    public UUID getSaleId() {
        return saleId;
    }

    public void setSaleId(UUID saleId) {
        this.saleId = saleId;
    }

    public UUID getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(UUID salesOrderId) {
        this.salesOrderId = salesOrderId;
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

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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

    public String getMlRawData() {
        return mlRawData;
    }

    public void setMlRawData(String mlRawData) {
        this.mlRawData = mlRawData;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
