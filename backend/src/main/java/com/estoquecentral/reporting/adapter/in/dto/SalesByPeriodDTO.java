package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sales by Period DTO
 * Sales aggregated by different time periods
 */
public record SalesByPeriodDTO(
        LocalDate saleDate,
        Integer saleYear,
        Integer saleMonth,
        Integer saleWeek,
        String yearMonth,
        String yearWeek,
        Long orderCount,
        Long uniqueCustomers,
        Long totalItems,
        BigDecimal totalSales,
        BigDecimal averageTicket,
        Long paidOrders,
        Long pendingPaymentOrders,
        BigDecimal paidAmount
) {
    public SalesByPeriodDTO {
        orderCount = orderCount != null ? orderCount : 0L;
        uniqueCustomers = uniqueCustomers != null ? uniqueCustomers : 0L;
        totalItems = totalItems != null ? totalItems : 0L;
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        averageTicket = averageTicket != null ? averageTicket : BigDecimal.ZERO;
        paidOrders = paidOrders != null ? paidOrders : 0L;
        pendingPaymentOrders = pendingPaymentOrders != null ? pendingPaymentOrders : 0L;
        paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO;
    }

    /**
     * Returns payment rate
     */
    public double getPaymentRate() {
        if (orderCount == null || orderCount == 0) {
            return 0.0;
        }
        return ((double) paidOrders / orderCount) * 100;
    }

    /**
     * Returns pending amount
     */
    public BigDecimal getPendingAmount() {
        return totalSales.subtract(paidAmount);
    }

    /**
     * Returns month name in Portuguese
     */
    public String getMonthName() {
        if (saleMonth == null) return "";
        return switch (saleMonth) {
            case 1 -> "Janeiro";
            case 2 -> "Fevereiro";
            case 3 -> "Março";
            case 4 -> "Abril";
            case 5 -> "Maio";
            case 6 -> "Junho";
            case 7 -> "Julho";
            case 8 -> "Agosto";
            case 9 -> "Setembro";
            case 10 -> "Outubro";
            case 11 -> "Novembro";
            case 12 -> "Dezembro";
            default -> "";
        };
    }
}
