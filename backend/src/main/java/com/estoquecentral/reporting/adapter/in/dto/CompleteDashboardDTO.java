package com.estoquecentral.reporting.adapter.in.dto;

import java.util.List;

/**
 * Complete Dashboard DTO
 * Contains all dashboard data in a single response
 */
public record CompleteDashboardDTO(
        DashboardSummaryDTO summary,
        List<DailySalesByChannelDTO> salesByChannel,
        List<CriticalStockProductDTO> criticalStock,
        List<PendingOrdersSummaryDTO> pendingOrders,
        List<InventoryValueSummaryDTO> inventoryByLocation
) {
    public CompleteDashboardDTO {
        salesByChannel = salesByChannel != null ? salesByChannel : List.of();
        criticalStock = criticalStock != null ? criticalStock : List.of();
        pendingOrders = pendingOrders != null ? pendingOrders : List.of();
        inventoryByLocation = inventoryByLocation != null ? inventoryByLocation : List.of();
    }

    /**
     * Returns total number of channels with sales today
     */
    public int getActiveChannelsCount() {
        return salesByChannel.size();
    }

    /**
     * Returns total critical stock items
     */
    public int getCriticalStockItemsCount() {
        return criticalStock.size();
    }

    /**
     * Returns total locations with inventory
     */
    public int getActiveLocationsCount() {
        return inventoryByLocation.size();
    }

    /**
     * Returns true if there are critical alerts
     */
    public boolean hasCriticalAlerts() {
        return summary != null && summary.getTotalStockAlerts() > 0;
    }

    /**
     * Returns true if there are overdue orders
     */
    public boolean hasOverdueOrders() {
        return summary != null && summary.overdueOrdersCount() > 0;
    }
}
