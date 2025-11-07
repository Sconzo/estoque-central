package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dashboard Summary DTO
 * Contains all key metrics for management dashboard
 */
public record DashboardSummaryDTO(
        // Today's sales metrics
        BigDecimal dailyTotalSales,
        Integer dailyOrderCount,
        Integer dailyItemCount,

        // Inventory metrics
        BigDecimal totalInventoryValue,
        BigDecimal totalInventoryQuantity,
        Integer totalUniqueProducts,

        // Critical stock alerts
        Integer outOfStockCount,
        Integer criticalStockCount,
        Integer lowStockCount,
        BigDecimal totalReplenishmentCost,

        // Pending orders
        Integer pendingOrdersCount,
        BigDecimal pendingOrdersValue,
        Integer overdueOrdersCount,

        // Timestamp
        LocalDateTime snapshotTime
) {
    public DashboardSummaryDTO {
        dailyTotalSales = dailyTotalSales != null ? dailyTotalSales : BigDecimal.ZERO;
        dailyOrderCount = dailyOrderCount != null ? dailyOrderCount : 0;
        dailyItemCount = dailyItemCount != null ? dailyItemCount : 0;

        totalInventoryValue = totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO;
        totalInventoryQuantity = totalInventoryQuantity != null ? totalInventoryQuantity : BigDecimal.ZERO;
        totalUniqueProducts = totalUniqueProducts != null ? totalUniqueProducts : 0;

        outOfStockCount = outOfStockCount != null ? outOfStockCount : 0;
        criticalStockCount = criticalStockCount != null ? criticalStockCount : 0;
        lowStockCount = lowStockCount != null ? lowStockCount : 0;
        totalReplenishmentCost = totalReplenishmentCost != null ? totalReplenishmentCost : BigDecimal.ZERO;

        pendingOrdersCount = pendingOrdersCount != null ? pendingOrdersCount : 0;
        pendingOrdersValue = pendingOrdersValue != null ? pendingOrdersValue : BigDecimal.ZERO;
        overdueOrdersCount = overdueOrdersCount != null ? overdueOrdersCount : 0;

        snapshotTime = snapshotTime != null ? snapshotTime : LocalDateTime.now();
    }

    /**
     * Returns total number of stock alerts
     */
    public int getTotalStockAlerts() {
        return outOfStockCount + criticalStockCount + lowStockCount;
    }

    /**
     * Returns average ticket value for today
     */
    public BigDecimal getAverageTicket() {
        if (dailyOrderCount == null || dailyOrderCount == 0) {
            return BigDecimal.ZERO;
        }
        return dailyTotalSales.divide(
                BigDecimal.valueOf(dailyOrderCount),
                2,
                BigDecimal.ROUND_HALF_UP
        );
    }
}
