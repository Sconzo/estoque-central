package com.estoquecentral.purchasing.adapter.in.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ProcessReceivingRequest {
    @NotNull
    private UUID purchaseOrderId;

    @NotNull
    private LocalDate receivingDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<ReceivingItemRequest> items;

    public static class ReceivingItemRequest {
        @NotNull
        private UUID purchaseOrderItemId;

        @NotNull
        private BigDecimal quantityReceived;

        private String notes;

        public UUID getPurchaseOrderItemId() { return purchaseOrderItemId; }
        public void setPurchaseOrderItemId(UUID purchaseOrderItemId) { this.purchaseOrderItemId = purchaseOrderItemId; }
        public BigDecimal getQuantityReceived() { return quantityReceived; }
        public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public LocalDate getReceivingDate() { return receivingDate; }
    public void setReceivingDate(LocalDate receivingDate) { this.receivingDate = receivingDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<ReceivingItemRequest> getItems() { return items; }
    public void setItems(List<ReceivingItemRequest> items) { this.items = items; }
}
