package com.estoquecentral.reporting.adapter.in.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Top Products Filter DTO
 */
public record TopProductsFilterDTO(
        LocalDate startDate,
        LocalDate endDate,
        UUID categoryId,
        String salesChannel,
        String orderBy, // 'revenue' or 'quantity'
        Integer limit
) {
    public TopProductsFilterDTO {
        orderBy = (orderBy == null || orderBy.isEmpty()) ? "revenue" : orderBy;
        limit = limit != null ? limit : 50;

        if (!orderBy.equals("revenue") && !orderBy.equals("quantity")) {
            throw new IllegalArgumentException("orderBy must be 'revenue' or 'quantity'");
        }

        if (limit < 1 || limit > 500) {
            throw new IllegalArgumentException("limit must be between 1 and 500");
        }
    }
}
