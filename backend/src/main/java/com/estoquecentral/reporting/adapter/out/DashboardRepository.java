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
