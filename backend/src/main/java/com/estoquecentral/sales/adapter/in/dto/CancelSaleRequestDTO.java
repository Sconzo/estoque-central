package com.estoquecentral.sales.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CancelSaleRequestDTO - Request DTO for cancelling a sale with refund
 * Story 4.4: NFCe Retry and Cancellation
 */
public class CancelSaleRequestDTO {

    @NotBlank(message = "Justification is required")
    @Size(min = 10, message = "Justification must be at least 10 characters")
    private String justification;

    // Constructors
    public CancelSaleRequestDTO() {}

    public CancelSaleRequestDTO(String justification) {
        this.justification = justification;
    }

    // Getters and Setters
    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
