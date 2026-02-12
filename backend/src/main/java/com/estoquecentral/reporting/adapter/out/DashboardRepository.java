package com.estoquecentral.reporting.adapter.out;

import com.estoquecentral.reporting.adapter.in.dto.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dashboard Repository
 * Accesses dashboard views and functions
 */
@Repository
public class DashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DashboardRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get dashboard summary with all key metrics
     */
    public DashboardSummaryDTO getDashboardSummary() {
        String sql = "SELECT * FROM v_dashboard_summary";

        return jdbcTemplate.queryForObject(sql, new HashMap<>(), (rs, rowNum) ->
                new DashboardSummaryDTO(
                        rs.getBigDecimal("daily_total_sales"),
                        rs.getInt("daily_order_count"),
                        rs.getInt("daily_item_count"),
                        rs.getBigDecimal("total_inventory_value"),
                        rs.getBigDecimal("total_inventory_quantity"),
                        rs.getInt("total_unique_products"),
                        rs.getInt("out_of_stock_count"),
                        rs.getInt("critical_stock_count"),
                        rs.getInt("low_stock_count"),
                        rs.getBigDecimal("total_replenishment_cost"),
                        rs.getInt("pending_orders_count"),
                        rs.getBigDecimal("pending_orders_value"),
                        rs.getInt("overdue_orders_count"),
                        rs.getTimestamp("snapshot_time").toLocalDateTime()
                )
        );
    }

    /**
     * Get daily sales breakdown by channel
     */
    public List<DailySalesByChannelDTO> getDailySalesByChannel() {
        String sql = "SELECT * FROM v_daily_sales_by_channel";

        return jdbcTemplate.query(sql, new HashMap<>(), (rs, rowNum) ->
                new DailySalesByChannelDTO(
                        rs.getString("sales_channel"),
                        rs.getInt("order_count"),
                        rs.getInt("item_count"),
                        rs.getBigDecimal("total_quantity"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("average_ticket"),
                        getLocalDateTime(rs, "first_order_time"),
                        getLocalDateTime(rs, "last_order_time")
                )
        );
    }

    /**
     * Get critical stock products (below minimum)
     */
    public List<CriticalStockProductDTO> getCriticalStockProducts(Integer limit) {
        String sql = """
                SELECT * FROM v_critical_stock_products
                ORDER BY
                    CASE alert_level
                        WHEN 'OUT_OF_STOCK' THEN 1
                        WHEN 'CRITICAL' THEN 2
                        WHEN 'LOW' THEN 3
                        ELSE 4
                    END,
                    current_quantity ASC
                LIMIT :limit
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit != null ? limit : 20);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new CriticalStockProductDTO(
                        getUUID(rs, "product_id"),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        getUUID(rs, "location_id"),
                        rs.getString("location_code"),
                        rs.getString("location_name"),
                        rs.getBigDecimal("current_quantity"),
                        rs.getBigDecimal("minimum_quantity"),
                        rs.getBigDecimal("maximum_quantity"),
                        rs.getBigDecimal("reorder_point"),
                        rs.getBigDecimal("quantity_needed"),
                        rs.getString("alert_level"),
                        rs.getBigDecimal("unit_cost"),
                        rs.getBigDecimal("replenishment_cost"),
                        getLocalDateTime(rs, "last_updated")
                )
        );
    }

    /**
     * Get pending orders summary
     */
    public List<PendingOrdersSummaryDTO> getPendingOrdersSummary() {
        String sql = "SELECT * FROM v_pending_orders_summary";

        return jdbcTemplate.query(sql, new HashMap<>(), (rs, rowNum) ->
                new PendingOrdersSummaryDTO(
                        rs.getString("sales_channel"),
                        rs.getString("status"),
                        rs.getInt("order_count"),
                        rs.getInt("total_items"),
                        rs.getBigDecimal("total_value"),
                        rs.getBigDecimal("average_order_value"),
                        getLocalDateTime(rs, "oldest_order_date"),
                        getLocalDateTime(rs, "newest_order_date"),
                        rs.getInt("overdue_count")
                )
        );
    }

    /**
     * Get inventory value summary by location
     */
    public List<InventoryValueSummaryDTO> getInventoryValueSummary() {
        String sql = "SELECT * FROM v_inventory_value_summary";

        return jdbcTemplate.query(sql, new HashMap<>(), (rs, rowNum) ->
                new InventoryValueSummaryDTO(
                        getUUID(rs, "location_id"),
                        rs.getString("location_code"),
                        rs.getString("location_name"),
                        rs.getString("location_type"),
                        rs.getInt("unique_products"),
                        rs.getBigDecimal("total_quantity"),
                        rs.getBigDecimal("total_value_at_cost"),
                        rs.getBigDecimal("average_product_cost")
                )
        );
    }

    /**
     * Get complete dashboard data
     */
    public CompleteDashboardDTO getCompleteDashboard() {
        DashboardSummaryDTO summary = getDashboardSummary();
        List<DailySalesByChannelDTO> salesByChannel = getDailySalesByChannel();
        List<CriticalStockProductDTO> criticalStock = getCriticalStockProducts(20);
        List<PendingOrdersSummaryDTO> pendingOrders = getPendingOrdersSummary();
        List<InventoryValueSummaryDTO> inventoryByLocation = getInventoryValueSummary();

        return new CompleteDashboardDTO(
                summary,
                salesByChannel,
                criticalStock,
                pendingOrders,
                inventoryByLocation
        );
    }

    /**
     * Get critical stock products by alert level
     */
    public List<CriticalStockProductDTO> getCriticalStockByLevel(String alertLevel, Integer limit) {
        String sql = """
                SELECT * FROM v_critical_stock_products
                WHERE alert_level = :alertLevel
                ORDER BY current_quantity ASC
                LIMIT :limit
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("alertLevel", alertLevel);
        params.put("limit", limit != null ? limit : 20);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new CriticalStockProductDTO(
                        getUUID(rs, "product_id"),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        getUUID(rs, "location_id"),
                        rs.getString("location_code"),
                        rs.getString("location_name"),
                        rs.getBigDecimal("current_quantity"),
                        rs.getBigDecimal("minimum_quantity"),
                        rs.getBigDecimal("maximum_quantity"),
                        rs.getBigDecimal("reorder_point"),
                        rs.getBigDecimal("quantity_needed"),
                        rs.getString("alert_level"),
                        rs.getBigDecimal("unit_cost"),
                        rs.getBigDecimal("replenishment_cost"),
                        getLocalDateTime(rs, "last_updated")
                )
        );
    }

    /**
     * Get pending orders by status
     */
    public List<PendingOrdersSummaryDTO> getPendingOrdersByStatus(String status) {
        String sql = """
                SELECT * FROM v_pending_orders_summary
                WHERE status = :status
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("status", status);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new PendingOrdersSummaryDTO(
                        rs.getString("sales_channel"),
                        rs.getString("status"),
                        rs.getInt("order_count"),
                        rs.getInt("total_items"),
                        rs.getBigDecimal("total_value"),
                        rs.getBigDecimal("average_order_value"),
                        getLocalDateTime(rs, "oldest_order_date"),
                        getLocalDateTime(rs, "newest_order_date"),
                        rs.getInt("overdue_count")
                )
        );
    }

    /**
     * Get top selling products for today from v_top_products_today view
     */
    public List<TopProductDTO> getTopProducts(Integer limit) {
        String sql = """
                SELECT * FROM v_top_products_today
                LIMIT :limit
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit != null ? limit : 10);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new TopProductDTO(
                        getUUID(rs, "product_id"),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getLong("order_count"),
                        null, // uniqueCustomers - not in view
                        rs.getBigDecimal("total_quantity_sold"),
                        rs.getBigDecimal("total_revenue"),
                        rs.getBigDecimal("average_price"),
                        null, // minPrice
                        null, // maxPrice
                        null, // revenuePerUnit
                        null, // avgQuantityPerOrder
                        rs.getBigDecimal("current_stock"),
                        null, // firstSaleDate
                        null, // lastSaleDate
                        (long) rowNum + 1 // rankPosition
                )
        );
    }

    /**
     * Get monthly sales aggregation for current month
     */
    public MonthlySalesDTO getMonthlySales() {
        String sql = """
                SELECT
                    COUNT(*) AS order_count,
                    COALESCE(SUM(total), 0) AS total_sales,
                    COALESCE(AVG(total), 0) AS average_ticket
                FROM orders
                WHERE created_at >= date_trunc('month', CURRENT_DATE)
                  AND status NOT IN ('CANCELLED', 'REJECTED')
                """;

        return jdbcTemplate.queryForObject(sql, new HashMap<>(), (rs, rowNum) ->
                new MonthlySalesDTO(
                        rs.getInt("order_count"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("average_ticket")
                )
        );
    }

    /**
     * Get count of active customers (customers who have placed at least one order)
     */
    public Integer getActiveCustomersCount() {
        String sql = """
                SELECT COUNT(DISTINCT customer_id)
                FROM orders
                WHERE status NOT IN ('CANCELLED', 'FAILED')
                """;
        return jdbcTemplate.queryForObject(sql, new HashMap<>(), Integer.class);
    }

    /**
     * Get total count of active products
     */
    public Integer getTotalActiveProducts() {
        String sql = "SELECT COUNT(*) FROM products WHERE ativo = true";
        return jdbcTemplate.queryForObject(sql, new HashMap<>(), Integer.class);
    }

    /**
     * Get recent activities combining inventory movements and order status changes
     */
    public List<RecentActivityDTO> getRecentActivities(Integer limit) {
        String sql = """
                (
                    SELECT
                        'estoque' AS tipo,
                        im.type || ' - ' || p.name AS descricao,
                        im.created_at AS timestamp
                    FROM inventory_movements im
                    INNER JOIN products p ON p.id = im.product_id
                    ORDER BY im.created_at DESC
                    LIMIT :limit
                )
                UNION ALL
                (
                    SELECT
                        'venda' AS tipo,
                        'Pedido #' || o.order_number || ' ' || COALESCE(osh.from_status, '?') || ' -> ' || osh.to_status AS descricao,
                        osh.changed_at AS timestamp
                    FROM order_status_history osh
                    INNER JOIN orders o ON o.id = osh.order_id
                    ORDER BY osh.changed_at DESC
                    LIMIT :limit
                )
                ORDER BY timestamp DESC
                LIMIT :limit
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit != null ? limit : 10);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new RecentActivityDTO(
                        rs.getString("tipo"),
                        rs.getString("descricao"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                )
        );
    }

    // Helper methods

    private UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) {
            return null;
        }
        if (value instanceof UUID) {
            return (UUID) value;
        }
        return UUID.fromString(value.toString());
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        var timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
