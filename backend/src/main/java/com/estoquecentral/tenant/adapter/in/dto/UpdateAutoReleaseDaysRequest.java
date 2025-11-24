package com.estoquecentral.tenant.adapter.in.dto;

/**
 * UpdateAutoReleaseDaysRequest - Request to update auto-release days setting
 * Story 4.6: Stock Reservation and Automatic Release - AC5
 */
public record UpdateAutoReleaseDaysRequest(
    int days
) {
    public UpdateAutoReleaseDaysRequest {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
    }
}
