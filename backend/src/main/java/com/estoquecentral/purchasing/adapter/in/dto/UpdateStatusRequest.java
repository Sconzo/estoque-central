package com.estoquecentral.purchasing.adapter.in.dto;

import com.estoquecentral.purchasing.domain.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating purchase order status
 * Story 3.2: Purchase Order Creation
 */
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private PurchaseOrderStatus status;

    // Getters and Setters
    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }
}
