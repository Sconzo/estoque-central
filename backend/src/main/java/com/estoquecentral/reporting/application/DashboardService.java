package com.estoquecentral.reporting.application;

import com.estoquecentral.reporting.adapter.in.dto.*;
import com.estoquecentral.reporting.adapter.out.DashboardRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard Service
 * Business logic for dashboard operations
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    /**
     * Get complete dashboard data
     * Cached for 60 seconds to avoid excessive queries
     */
    @Cacheable(value = "dashboard", key = "'complete'")
    public CompleteDashboardDTO getCompleteDashboard() {
        return dashboardRepository.getCompleteDashboard();
    }

    /**
     * Get dashboard summary only
     */
    @Cacheable(value = "dashboard", key = "'summary'")
    public DashboardSummaryDTO getDashboardSummary() {
        return dashboardRepository.getDashboardSummary();
    }

    /**
     * Get daily sales by channel
     */
    @Cacheable(value = "dashboard", key = "'sales-by-channel'")
    public List<DailySalesByChannelDTO> getDailySalesByChannel() {
        return dashboardRepository.getDailySalesByChannel();
    }

    /**
     * Get sales by channel with totals
     */
    public Map<String, Object> getDailySalesWithTotals() {
        List<DailySalesByChannelDTO> sales = getDailySalesByChannel();

        BigDecimal totalSales = sales.stream()
                .map(DailySalesByChannelDTO::totalSales)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalOrders = sales.stream()
                .map(DailySalesByChannelDTO::orderCount)
                .reduce(0, Integer::sum);

        return Map.of(
                "channels", sales,
                "totalSales", totalSales,
                "totalOrders", totalOrders,
                "channelCount", sales.size()
        );
    }

    /**
     * Get critical stock products
     */
    @Cacheable(value = "dashboard", key = "'critical-stock-' + #limit")
    public List<CriticalStockProductDTO> getCriticalStockProducts(Integer limit) {
        return dashboardRepository.getCriticalStockProducts(limit);
    }

    /**
     * Get critical stock grouped by alert level
     */
    public Map<String, List<CriticalStockProductDTO>> getCriticalStockByLevel() {
        List<CriticalStockProductDTO> allCritical = dashboardRepository.getCriticalStockProducts(100);

        return allCritical.stream()
                .collect(Collectors.groupingBy(CriticalStockProductDTO::alertLevel));
    }

    /**
     * Get out of stock products only
     */
    public List<CriticalStockProductDTO> getOutOfStockProducts(Integer limit) {
        return dashboardRepository.getCriticalStockByLevel("OUT_OF_STOCK", limit);
    }

    /**
     * Get pending orders summary
     */
    @Cacheable(value = "dashboard", key = "'pending-orders'")
    public List<PendingOrdersSummaryDTO> getPendingOrdersSummary() {
        return dashboardRepository.getPendingOrdersSummary();
    }

    /**
     * Get pending orders by status
     */
    public List<PendingOrdersSummaryDTO> getPendingOrdersByStatus(String status) {
        return dashboardRepository.getPendingOrdersByStatus(status);
    }

    /**
     * Get pending orders grouped by channel
     */
    public Map<String, List<PendingOrdersSummaryDTO>> getPendingOrdersByChannel() {
        List<PendingOrdersSummaryDTO> allPending = getPendingOrdersSummary();

        return allPending.stream()
                .collect(Collectors.groupingBy(PendingOrdersSummaryDTO::salesChannel));
    }

    /**
     * Get inventory value summary
     */
    @Cacheable(value = "dashboard", key = "'inventory-value'")
    public List<InventoryValueSummaryDTO> getInventoryValueSummary() {
        return dashboardRepository.getInventoryValueSummary();
    }

    /**
     * Get inventory statistics
     */
    public Map<String, Object> getInventoryStatistics() {
        List<InventoryValueSummaryDTO> inventory = getInventoryValueSummary();

        BigDecimal totalValue = inventory.stream()
                .map(InventoryValueSummaryDTO::totalValueAtCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalQuantity = inventory.stream()
                .map(InventoryValueSummaryDTO::totalQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalProducts = inventory.stream()
                .map(InventoryValueSummaryDTO::uniqueProducts)
                .reduce(0, Integer::sum);

        return Map.of(
                "locations", inventory,
                "totalValue", totalValue,
                "totalQuantity", totalQuantity,
                "totalProducts", totalProducts,
                "locationCount", inventory.size()
        );
    }

    /**
     * Get alerts summary
     */
    public Map<String, Object> getAlertsSummary() {
        DashboardSummaryDTO summary = getDashboardSummary();

        return Map.of(
                "outOfStock", summary.outOfStockCount(),
                "critical", summary.criticalStockCount(),
                "low", summary.lowStockCount(),
                "total", summary.getTotalStockAlerts(),
                "replenishmentCost", summary.totalReplenishmentCost(),
                "overdueOrders", summary.overdueOrdersCount(),
                "hasCriticalAlerts", summary.getTotalStockAlerts() > 0
        );
    }

    /**
     * Get performance indicators (KPIs)
     */
    public Map<String, Object> getPerformanceIndicators() {
        DashboardSummaryDTO summary = getDashboardSummary();

        return Map.of(
                "dailySales", Map.of(
                        "total", summary.dailyTotalSales(),
                        "orderCount", summary.dailyOrderCount(),
                        "itemCount", summary.dailyItemCount(),
                        "averageTicket", summary.getAverageTicket()
                ),
                "inventory", Map.of(
                        "value", summary.totalInventoryValue(),
                        "quantity", summary.totalInventoryQuantity(),
                        "uniqueProducts", summary.totalUniqueProducts()
                ),
                "alerts", Map.of(
                        "stockAlerts", summary.getTotalStockAlerts(),
                        "outOfStock", summary.outOfStockCount(),
                        "critical", summary.criticalStockCount()
                ),
                "orders", Map.of(
                        "pending", summary.pendingOrdersCount(),
                        "pendingValue", summary.pendingOrdersValue(),
                        "overdue", summary.overdueOrdersCount()
                )
        );
    }
}
