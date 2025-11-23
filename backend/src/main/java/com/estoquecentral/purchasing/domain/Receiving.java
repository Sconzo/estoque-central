package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Receiving entity - Represents a purchase order receiving transaction
 * Story 3.4: Receiving Processing and Weighted Average Cost Update
 */
@Table("receivings")
public class Receiving {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("receiving_number")
    private String receivingNumber;

    @Column("purchase_order_id")
    private UUID purchaseOrderId;

    @Column("stock_location_id")
    private UUID stockLocationId;

    @Column("receiving_date")
    private LocalDate receivingDate;

    @Column("received_by_user_id")
    private UUID receivedByUserId;

    @Column("notes")
    private String notes;

    @Column("status")
    private ReceivingStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    // Constructors
    public Receiving() {
        this.status = ReceivingStatus.COMPLETED;
        this.createdAt = LocalDateTime.now();
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

    public String getReceivingNumber() {
        return receivingNumber;
    }

    public void setReceivingNumber(String receivingNumber) {
        this.receivingNumber = receivingNumber;
    }

    public UUID getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(UUID purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public UUID getStockLocationId() {
        return stockLocationId;
    }

    public void setStockLocationId(UUID stockLocationId) {
        this.stockLocationId = stockLocationId;
    }

    public LocalDate getReceivingDate() {
        return receivingDate;
    }

    public void setReceivingDate(LocalDate receivingDate) {
        this.receivingDate = receivingDate;
    }

    public UUID getReceivedByUserId() {
        return receivedByUserId;
    }

    public void setReceivedByUserId(UUID receivedByUserId) {
        this.receivedByUserId = receivedByUserId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ReceivingStatus getStatus() {
        return status;
    }

    public void setStatus(ReceivingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
