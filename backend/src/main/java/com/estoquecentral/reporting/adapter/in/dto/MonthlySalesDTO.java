package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;

/**
 * Monthly Sales DTO
 * Aggregated sales metrics for the current month
 */
public record MonthlySalesDTO(
        Integer orderCount,
        BigDecimal totalSales,
        BigDecimal averageTicket
) {
    public MonthlySalesDTO {
        orderCount = orderCount != null ? orderCount : 0;
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        averageTicket = averageTicket != null ? averageTicket : BigDecimal.ZERO;
    }
}
