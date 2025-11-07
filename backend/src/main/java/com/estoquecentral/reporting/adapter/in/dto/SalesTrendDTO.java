package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sales Trend DTO
 * Daily sales trend with moving average
 */
public record SalesTrendDTO(
        LocalDate saleDate,
        Long orderCount,
        Long uniqueCustomers,
        BigDecimal totalSales,
        BigDecimal averageTicket,
        BigDecimal movingAvg7Days
) {
    public SalesTrendDTO {
        orderCount = orderCount != null ? orderCount : 0L;
        uniqueCustomers = uniqueCustomers != null ? uniqueCustomers : 0L;
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        averageTicket = averageTicket != null ? averageTicket : BigDecimal.ZERO;
        movingAvg7Days = movingAvg7Days != null ? movingAvg7Days : BigDecimal.ZERO;
    }

    /**
     * Returns true if sales are above moving average
     */
    public boolean isAboveAverage() {
        return totalSales.compareTo(movingAvg7Days) > 0;
    }

    /**
     * Returns percentage difference from moving average
     */
    public double getPercentageDifferenceFromAverage() {
        if (movingAvg7Days.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return totalSales.subtract(movingAvg7Days)
                .divide(movingAvg7Days, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Returns trend indicator
     */
    public String getTrendIndicator() {
        double diff = getPercentageDifferenceFromAverage();
        if (diff > 10) return "UP";
        if (diff < -10) return "DOWN";
        return "STABLE";
    }
}
