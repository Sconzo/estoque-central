package com.estoquecentral.reporting.adapter.out;

import com.estoquecentral.reporting.adapter.in.dto.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Sales Report Repository
 * Accesses sales report views and functions
 */
@Repository
public class SalesReportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SalesReportRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get sales by date and channel with filters
     */
    public List<SalesByDateChannelDTO> getSalesByDateAndChannel(SalesFilterDTO filter) {
        String sql = """
                SELECT * FROM v_sales_by_date_and_channel
                WHERE 1=1
                """ +
                (filter.startDate() != null ? " AND sale_date >= :startDate " : "") +
                (filter.endDate() != null ? " AND sale_date <= :endDate " : "") +
                (filter.salesChannel() != null ? " AND sales_channel = :salesChannel " : "") +
                " ORDER BY sale_date DESC, total_sales DESC LIMIT 1000";

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filter.startDate() != null) params.addValue("startDate", filter.startDate());
        if (filter.endDate() != null) params.addValue("endDate", filter.endDate());
        if (filter.salesChannel() != null) params.addValue("salesChannel", filter.salesChannel());

        return jdbcTemplate.query(sql, params, this::mapSalesByDateChannel);
    }

    /**
     * Get sales by channel summary
     */
    public List<SalesByChannelSummaryDTO> getSalesByChannelSummary() {
        String sql = "SELECT * FROM v_sales_by_channel_summary";

        return jdbcTemplate.query(sql, new HashMap<>(), (rs, rowNum) ->
                new SalesByChannelSummaryDTO(
                        rs.getString("sales_channel"),
                        rs.getLong("total_orders"),
                        rs.getLong("unique_customers"),
                        rs.getBigDecimal("orders_per_customer"),
                        rs.getLong("total_items"),
                        rs.getBigDecimal("average_items_per_order"),
                        rs.getBigDecimal("total_subtotal"),
                        rs.getBigDecimal("total_discount"),
                        rs.getBigDecimal("total_shipping"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("average_ticket"),
                        rs.getBigDecimal("min_ticket"),
                        rs.getBigDecimal("max_ticket"),
                        rs.getBigDecimal("discount_percentage"),
                        getLocalDateTime(rs, "first_sale"),
                        getLocalDateTime(rs, "last_sale"),
                        rs.getLong("pending_orders"),
                        rs.getLong("confirmed_orders"),
                        rs.getLong("delivered_orders"),
                        rs.getLong("paid_orders")
                )
        );
    }

    /**
     * Get sales by period (day/week/month)
     */
    public List<SalesByPeriodDTO> getSalesByPeriod(SalesFilterDTO filter) {
        String sql = """
                SELECT * FROM v_sales_by_period
                WHERE 1=1
                """ +
                (filter.startDate() != null ? " AND sale_date >= :startDate " : "") +
                (filter.endDate() != null ? " AND sale_date <= :endDate " : "") +
                " ORDER BY sale_date DESC LIMIT 1000";

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filter.startDate() != null) params.addValue("startDate", filter.startDate());
        if (filter.endDate() != null) params.addValue("endDate", filter.endDate());

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new SalesByPeriodDTO(
                        rs.getDate("sale_date") != null ? rs.getDate("sale_date").toLocalDate() : null,
                        (Integer) rs.getObject("sale_year"),
                        (Integer) rs.getObject("sale_month"),
                        (Integer) rs.getObject("sale_week"),
                        rs.getString("year_month"),
                        rs.getString("year_week"),
                        rs.getLong("order_count"),
                        rs.getLong("unique_customers"),
                        rs.getLong("total_items"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("average_ticket"),
                        rs.getLong("paid_orders"),
                        rs.getLong("pending_payment_orders"),
                        rs.getBigDecimal("paid_amount")
                )
        );
    }

    /**
     * Get sales trend (last 30 days)
     */
    public List<SalesTrendDTO> getSalesTrend30Days() {
        String sql = "SELECT * FROM v_sales_trend_30days";

        return jdbcTemplate.query(sql, new HashMap<>(), (rs, rowNum) ->
                new SalesTrendDTO(
                        rs.getDate("sale_date").toLocalDate(),
                        rs.getLong("order_count"),
                        rs.getLong("unique_customers"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("average_ticket"),
                        rs.getBigDecimal("moving_avg_7days")
                )
        );
    }

    /**
     * Get sales using function (grouped by period)
     */
    public List<SalesReportPeriodDTO> getSalesReportByPeriod(SalesFilterDTO filter) {
        String sql = """
                SELECT * FROM get_sales_report_by_period(
                    :startDate,
                    :endDate,
                    :salesChannel,
                    :groupBy
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", filter.startDate())
                .addValue("endDate", filter.endDate())
                .addValue("salesChannel", filter.salesChannel())
                .addValue("groupBy", filter.groupBy());

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new SalesReportPeriodDTO(
                        rs.getString("period_key"),
                        rs.getString("sales_channel"),
                        rs.getLong("order_count"),
                        rs.getLong("unique_customers"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("average_ticket"),
                        rs.getLong("total_items"),
                        rs.getBigDecimal("total_quantity")
                )
        );
    }

    /**
     * Get sales totals with filters
     */
    public SalesTotalsDTO getSalesTotals(SalesFilterDTO filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    COUNT(DISTINCT o.id) AS total_orders,
                    COUNT(DISTINCT o.customer_id) AS unique_customers,
                    SUM(o.total_items) AS total_items,
                    SUM(o.subtotal) AS total_subtotal,
                    SUM(o.discount_amount) AS total_discount,
                    SUM(o.shipping_cost) AS total_shipping,
                    SUM(o.total_amount) AS total_sales,
                    ROUND(AVG(o.total_amount), 2) AS average_ticket,
                    MIN(o.total_amount) AS min_ticket,
                    MAX(o.total_amount) AS max_ticket
                FROM orders o
                WHERE o.ativo = true
                  AND o.status NOT IN ('CANCELLED', 'REJECTED')
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filter.startDate() != null) {
            sql.append(" AND DATE(o.order_date) >= :startDate ");
            params.addValue("startDate", filter.startDate());
        }

        if (filter.endDate() != null) {
            sql.append(" AND DATE(o.order_date) <= :endDate ");
            params.addValue("endDate", filter.endDate());
        }

        if (filter.salesChannel() != null) {
            sql.append(" AND o.sales_channel = :salesChannel ");
            params.addValue("salesChannel", filter.salesChannel());
        }

        return jdbcTemplate.queryForObject(sql.toString(), params, (rs, rowNum) ->
                new SalesTotalsDTO(
                        rs.getLong("total_orders"),
                        rs.getLong("unique_customers"),
                        rs.getLong("total_items"),
                        rs.getBigDecimal("total_subtotal"),
                        rs.getBigDecimal("total_discount"),
                        rs.getBigDecimal("total_shipping"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("average_ticket"),
                        rs.getBigDecimal("min_ticket"),
                        rs.getBigDecimal("max_ticket")
                )
        );
    }

    // Helper methods

    private SalesByDateChannelDTO mapSalesByDateChannel(ResultSet rs, int rowNum) throws SQLException {
        return new SalesByDateChannelDTO(
                rs.getDate("sale_date").toLocalDate(),
                rs.getString("sales_channel"),
                rs.getLong("order_count"),
                rs.getLong("unique_customers"),
                rs.getLong("total_items"),
                rs.getBigDecimal("total_quantity"),
                rs.getBigDecimal("total_subtotal"),
                rs.getBigDecimal("total_discount"),
                rs.getBigDecimal("total_shipping"),
                rs.getBigDecimal("total_sales"),
                rs.getBigDecimal("average_ticket"),
                rs.getBigDecimal("min_ticket"),
                rs.getBigDecimal("max_ticket"),
                getLocalDateTime(rs, "first_sale_time"),
                getLocalDateTime(rs, "last_sale_time")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        var timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    /**
     * Sales Report Period DTO (from function)
     */
    public record SalesReportPeriodDTO(
            String periodKey,
            String salesChannel,
            Long orderCount,
            Long uniqueCustomers,
            BigDecimal totalSales,
            BigDecimal averageTicket,
            Long totalItems,
            BigDecimal totalQuantity
    ) {
    }

    /**
     * Sales Totals DTO
     */
    public record SalesTotalsDTO(
            Long totalOrders,
            Long uniqueCustomers,
            Long totalItems,
            BigDecimal totalSubtotal,
            BigDecimal totalDiscount,
            BigDecimal totalShipping,
            BigDecimal totalSales,
            BigDecimal averageTicket,
            BigDecimal minTicket,
            BigDecimal maxTicket
    ) {
    }
}
