package com.estoquecentral.reporting.adapter.in.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Inventory Movement Filter DTO
 * Filter parameters for movement reports
 */
public record InventoryMovementFilterDTO(
        LocalDate startDate,
        LocalDate endDate,
        UUID productId,
        UUID locationId,
        String movementType,
        String movementDirection,
        Integer limit
) {
    public InventoryMovementFilterDTO {
        // Set default limit if not provided
        limit = limit != null ? limit : 1000;

        // Validate limit range
        if (limit < 1 || limit > 10000) {
            throw new IllegalArgumentException("Limit must be between 1 and 10000");
        }

        // Validate movement direction
        if (movementDirection != null &&
                !movementDirection.equals("IN") &&
                !movementDirection.equals("OUT")) {
            throw new IllegalArgumentException("Movement direction must be 'IN' or 'OUT'");
        }

        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    /**
     * Returns true if any filter is applied
     */
    public boolean hasFilters() {
        return startDate != null ||
                endDate != null ||
                productId != null ||
                locationId != null ||
                movementType != null ||
                movementDirection != null;
    }

    /**
     * Returns number of active filters
     */
    public int getActiveFilterCount() {
        int count = 0;
        if (startDate != null) count++;
        if (endDate != null) count++;
        if (productId != null) count++;
        if (locationId != null) count++;
        if (movementType != null) count++;
        if (movementDirection != null) count++;
        return count;
    }

    /**
     * Returns a copy with default date range (last 30 days) if no dates provided
     */
    public InventoryMovementFilterDTO withDefaultDateRange() {
        if (startDate == null && endDate == null) {
            return new InventoryMovementFilterDTO(
                    LocalDate.now().minusDays(30),
                    LocalDate.now(),
                    productId,
                    locationId,
                    movementType,
                    movementDirection,
                    limit
            );
        }
        return this;
    }
}
