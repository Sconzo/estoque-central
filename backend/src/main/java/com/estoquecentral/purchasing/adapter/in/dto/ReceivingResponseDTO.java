package com.estoquecentral.purchasing.adapter.in.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReceivingResponseDTO {
    private UUID id;
    private String receivingNumber;
    private UUID purchaseOrderId;
    private String poNumber;
    private UUID stockLocationId;
    private String locationName;
    private LocalDate receivingDate;
    private UUID receivedByUserId;
    private String receivedByUserName;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
    private List<ReceivingItemResponseDTO> items;

    public ReceivingResponseDTO() {}

    public ReceivingResponseDTO(
            UUID id,
            String receivingNumber,
            UUID purchaseOrderId,
            String poNumber,
            UUID stockLocationId,
            String locationName,
            LocalDate receivingDate,
            UUID receivedByUserId,
            String receivedByUserName,
            String notes,
            String status,
            LocalDateTime createdAt,
            List<ReceivingItemResponseDTO> items) {
        this.id = id;
        this.receivingNumber = receivingNumber;
        this.purchaseOrderId = purchaseOrderId;
        this.poNumber = poNumber;
        this.stockLocationId = stockLocationId;
        this.locationName = locationName;
        this.receivingDate = receivingDate;
        this.receivedByUserId = receivedByUserId;
        this.receivedByUserName = receivedByUserName;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getReceivingNumber() { return receivingNumber; }
    public void setReceivingNumber(String receivingNumber) { this.receivingNumber = receivingNumber; }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public UUID getStockLocationId() { return stockLocationId; }
    public void setStockLocationId(UUID stockLocationId) { this.stockLocationId = stockLocationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public LocalDate getReceivingDate() { return receivingDate; }
    public void setReceivingDate(LocalDate receivingDate) { this.receivingDate = receivingDate; }

    public UUID getReceivedByUserId() { return receivedByUserId; }
    public void setReceivedByUserId(UUID receivedByUserId) { this.receivedByUserId = receivedByUserId; }

    public String getReceivedByUserName() { return receivedByUserName; }
    public void setReceivedByUserName(String receivedByUserName) { this.receivedByUserName = receivedByUserName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ReceivingItemResponseDTO> getItems() { return items; }
    public void setItems(List<ReceivingItemResponseDTO> items) { this.items = items; }
}
