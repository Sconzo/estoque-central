package com.estoquecentral.reporting.adapter.in.dto;

import java.time.LocalDate;

/**
 * Sales Filter DTO
 * Filter parameters for sales reports
 */
public record SalesFilterDTO(
        LocalDate startDate,
        LocalDate endDate,
        String salesChannel,
        String groupBy // 'day', 'week', 'month'
) {
    public SalesFilterDTO {
        // Set default group by if not provided
        if (groupBy == null || groupBy.isEmpty()) {
            groupBy = "day";
        }

        // Validate group by
        if (!groupBy.equals("day") && !groupBy.equals("week") && !groupBy.equals("month")) {
            throw new IllegalArgumentException("groupBy must be 'day', 'week', or 'month'");
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
        return startDate != null || endDate != null || salesChannel != null;
    }

    /**
     * Returns a copy with default date range (last 30 days) if no dates provided
     */
    public SalesFilterDTO withDefaultDateRange() {
        if (startDate == null && endDate == null) {
            return new SalesFilterDTO(
                    LocalDate.now().minusDays(30),
                    LocalDate.now(),
                    salesChannel,
                    groupBy
            );
        }
        return this;
    }

    /**
     * Returns period label
     */
    public String getPeriodLabel() {
        return switch (groupBy) {
            case "day" -> "Diário";
            case "week" -> "Semanal";
            case "month" -> "Mensal";
            default -> "Diário";
        };
    }
}
