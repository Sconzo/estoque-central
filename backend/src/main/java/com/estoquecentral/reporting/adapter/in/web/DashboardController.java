package com.estoquecentral.reporting.adapter.in.web;

import com.estoquecentral.reporting.adapter.in.dto.*;
import com.estoquecentral.reporting.application.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Dashboard REST Controller
 * Endpoints for management dashboard
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET /api/dashboard
     * Get complete dashboard with all data
     *
     * Response:
     * {
     *   "summary": { ... },
     *   "salesByChannel": [ ... ],
     *   "criticalStock": [ ... ],
     *   "pendingOrders": [ ... ],
     *   "inventoryByLocation": [ ... ]
     * }
     */
    @GetMapping
    public ResponseEntity<CompleteDashboardDTO> getCompleteDashboard() {
        CompleteDashboardDTO dashboard = dashboardService.getCompleteDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET /api/dashboard/summary
     * Get dashboard summary with key metrics only
     *
     * Response:
     * {
     *   "dailyTotalSales": 12500.00,
     *   "dailyOrderCount": 45,
     *   "totalInventoryValue": 250000.00,
     *   "outOfStockCount": 3,
     *   "criticalStockCount": 8,
     *   "pendingOrdersCount": 12,
     *   ...
     * }
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/dashboard/sales/by-channel
     * Get today's sales breakdown by channel
     *
     * Response:
     * [
     *   {
     *     "salesChannel": "STORE",
     *     "orderCount": 25,
     *     "totalSales": 8500.00,
     *     "averageTicket": 340.00
     *   },
     *   ...
     * ]
     */
    @GetMapping("/sales/by-channel")
    public ResponseEntity<List<DailySalesByChannelDTO>> getDailySalesByChannel() {
        List<DailySalesByChannelDTO> sales = dashboardService.getDailySalesByChannel();
        return ResponseEntity.ok(sales);
    }

    /**
     * GET /api/dashboard/sales/totals
     * Get sales with calculated totals
     *
     * Response:
     * {
     *   "channels": [ ... ],
     *   "totalSales": 12500.00,
     *   "totalOrders": 45,
     *   "channelCount": 3
     * }
     */
    @GetMapping("/sales/totals")
    public ResponseEntity<Map<String, Object>> getDailySalesWithTotals() {
        Map<String, Object> salesData = dashboardService.getDailySalesWithTotals();
        return ResponseEntity.ok(salesData);
    }

    /**
     * GET /api/dashboard/products/count
     * Get total count of active products
     */
    @GetMapping("/products/count")
    public ResponseEntity<Map<String, Integer>> getTotalActiveProducts() {
        Integer count = dashboardService.getTotalActiveProducts();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * GET /api/dashboard/products/top
     * Get top selling products for today
     *
     * Query params:
     * - limit: Max number of products (default: 10)
     */
    @GetMapping("/products/top")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<TopProductDTO> topProducts = dashboardService.getTopProducts(limit);
        return ResponseEntity.ok(topProducts);
    }

    /**
     * GET /api/dashboard/sales/monthly
     * Get aggregated sales for current month
     */
    @GetMapping("/sales/monthly")
    public ResponseEntity<MonthlySalesDTO> getMonthlySales() {
        MonthlySalesDTO monthlySales = dashboardService.getMonthlySales();
        return ResponseEntity.ok(monthlySales);
    }

    /**
     * GET /api/dashboard/customers/active-count
     * Get count of active customers
     */
    @GetMapping("/customers/active-count")
    public ResponseEntity<Map<String, Integer>> getActiveCustomersCount() {
        Integer count = dashboardService.getActiveCustomersCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * GET /api/dashboard/activities/recent
     * Get recent activities (inventory movements + order status changes)
     *
     * Query params:
     * - limit: Max number of activities (default: 10)
     */
    @GetMapping("/activities/recent")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivities(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<RecentActivityDTO> activities = dashboardService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    /**
     * GET /api/dashboard/stock/critical
     * Get products below minimum stock level
     *
     * Query params:
     * - limit: Max number of products (default: 20)
     *
     * Response:
     * [
     *   {
     *     "sku": "NOTE-001",
     *     "productName": "Notebook Dell",
     *     "alertLevel": "CRITICAL",
     *     "currentQuantity": 2,
     *     "minimumQuantity": 10,
     *     "quantityNeeded": 8
     *   },
     *   ...
     * ]
     */
    @GetMapping("/stock/critical")
    public ResponseEntity<List<CriticalStockProductDTO>> getCriticalStock(
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        List<CriticalStockProductDTO> critical = dashboardService.getCriticalStockProducts(limit);
        return ResponseEntity.ok(critical);
    }

    /**
     * GET /api/dashboard/stock/critical/by-level
     * Get critical stock grouped by alert level
     *
     * Response:
     * {
     *   "OUT_OF_STOCK": [ ... ],
     *   "CRITICAL": [ ... ],
     *   "LOW": [ ... ]
     * }
     */
    @GetMapping("/stock/critical/by-level")
    public ResponseEntity<Map<String, List<CriticalStockProductDTO>>> getCriticalStockByLevel() {
        Map<String, List<CriticalStockProductDTO>> grouped = dashboardService.getCriticalStockByLevel();
        return ResponseEntity.ok(grouped);
    }

    /**
     * GET /api/dashboard/stock/out-of-stock
     * Get only out of stock products
     *
     * Query params:
     * - limit: Max number of products (default: 20)
     */
    @GetMapping("/stock/out-of-stock")
    public ResponseEntity<List<CriticalStockProductDTO>> getOutOfStock(
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        List<CriticalStockProductDTO> outOfStock = dashboardService.getOutOfStockProducts(limit);
        return ResponseEntity.ok(outOfStock);
    }

    /**
     * GET /api/dashboard/orders/pending
     * Get pending orders summary by channel and status
     *
     * Response:
     * [
     *   {
     *     "salesChannel": "MARKETPLACE",
     *     "status": "PENDING",
     *     "orderCount": 8,
     *     "totalValue": 3200.00,
     *     "overdueCount": 2
     *   },
     *   ...
     * ]
     */
    @GetMapping("/orders/pending")
    public ResponseEntity<List<PendingOrdersSummaryDTO>> getPendingOrders() {
        List<PendingOrdersSummaryDTO> pending = dashboardService.getPendingOrdersSummary();
        return ResponseEntity.ok(pending);
    }

    /**
     * GET /api/dashboard/orders/pending/by-status
     * Get pending orders by specific status
     *
     * Path params:
     * - status: PENDING, PROCESSING, CONFIRMED, READY_TO_SHIP
     */
    @GetMapping("/orders/pending/by-status/{status}")
    public ResponseEntity<List<PendingOrdersSummaryDTO>> getPendingOrdersByStatus(
            @PathVariable String status
    ) {
        List<PendingOrdersSummaryDTO> orders = dashboardService.getPendingOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/dashboard/orders/pending/by-channel
     * Get pending orders grouped by channel
     *
     * Response:
     * {
     *   "STORE": [ ... ],
     *   "ONLINE": [ ... ],
     *   "MARKETPLACE": [ ... ]
     * }
     */
    @GetMapping("/orders/pending/by-channel")
    public ResponseEntity<Map<String, List<PendingOrdersSummaryDTO>>> getPendingOrdersByChannel() {
        Map<String, List<PendingOrdersSummaryDTO>> grouped = dashboardService.getPendingOrdersByChannel();
        return ResponseEntity.ok(grouped);
    }

    /**
     * GET /api/dashboard/inventory/value
     * Get inventory value summary by location
     *
     * Response:
     * [
     *   {
     *     "locationName": "Armazém Principal",
     *     "totalValueAtCost": 150000.00,
     *     "totalQuantity": 5000,
     *     "uniqueProducts": 150
     *   },
     *   ...
     * ]
     */
    @GetMapping("/inventory/value")
    public ResponseEntity<List<InventoryValueSummaryDTO>> getInventoryValue() {
        List<InventoryValueSummaryDTO> inventory = dashboardService.getInventoryValueSummary();
        return ResponseEntity.ok(inventory);
    }

    /**
     * GET /api/dashboard/inventory/statistics
     * Get inventory statistics with totals
     *
     * Response:
     * {
     *   "locations": [ ... ],
     *   "totalValue": 250000.00,
     *   "totalQuantity": 8000,
     *   "totalProducts": 200,
     *   "locationCount": 3
     * }
     */
    @GetMapping("/inventory/statistics")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        Map<String, Object> stats = dashboardService.getInventoryStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/dashboard/alerts
     * Get alerts summary (stock alerts and overdue orders)
     *
     * Response:
     * {
     *   "outOfStock": 3,
     *   "critical": 8,
     *   "low": 15,
     *   "total": 26,
     *   "replenishmentCost": 45000.00,
     *   "overdueOrders": 5,
     *   "hasCriticalAlerts": true
     * }
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts() {
        Map<String, Object> alerts = dashboardService.getAlertsSummary();
        return ResponseEntity.ok(alerts);
    }

    /**
     * GET /api/dashboard/kpis
     * Get key performance indicators
     *
     * Response:
     * {
     *   "dailySales": { "total": 12500.00, "orderCount": 45, ... },
     *   "inventory": { "value": 250000.00, ... },
     *   "alerts": { "stockAlerts": 26, ... },
     *   "orders": { "pending": 12, "overdue": 5, ... }
     * }
     */
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKPIs() {
        Map<String, Object> kpis = dashboardService.getPerformanceIndicators();
        return ResponseEntity.ok(kpis);
    }
}
