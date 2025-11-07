package com.estoquecentral.reporting.adapter.out;

import com.estoquecentral.reporting.adapter.in.dto.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Inventory Movement Report Repository
 * Accesses movement report views and functions
 */
@Repository
public class InventoryMovementReportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InventoryMovementReportRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get detailed inventory movements with filters
     */
    public List<InventoryMovementDetailDTO> getMovements(InventoryMovementFilterDTO filter) {
        String sql = """
                SELECT * FROM get_inventory_movement_report(
                    :startDate,
                    :endDate,
                    :productId,
                    :locationId,
                    :movementType,
                    :movementDirection,
                    :limit
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", filter.startDate())
                .addValue("endDate", filter.endDate())
                .addValue("productId", filter.productId())
                .addValue("locationId", filter.locationId())
                .addValue("movementType", filter.movementType())
                .addValue("movementDirection", filter.movementDirection())
                .addValue("limit", filter.limit());

        return jdbcTemplate.query(sql, params, this::mapInventoryMovementDetail);
    }

    /**
     * Get movement summary by type
     */
    public List<InventoryMovementSummaryByTypeDTO> getSummaryByType() {
        String sql = "SELECT * FROM v_inventory_movements_summary_by_type";

        return jdbcTemplate.query(sql, new HashMap<>(), (rs, rowNum) ->
                new InventoryMovementSummaryByTypeDTO(
                        rs.getString("movement_type"),
                        rs.getString("movement_direction"),
                        rs.getLong("movement_count"),
                        rs.getBigDecimal("total_quantity"),
                        rs.getBigDecimal("total_value"),
                        rs.getBigDecimal("average_unit_cost"),
                        getLocalDateTime(rs, "first_movement_date"),
                        getLocalDateTime(rs, "last_movement_date")
                )
        );
    }

    /**
     * Get movement summary by product
     */
    public List<InventoryMovementSummaryByProductDTO> getSummaryByProduct() {
        String sql = "SELECT * FROM v_inventory_movements_summary_by_product LIMIT 100";

        return jdbcTemplate.query(sql, new HashMap<>(), (rs, rowNum) ->
                new InventoryMovementSummaryByProductDTO(
                        getUUID(rs, "product_id"),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getLong("total_movements"),
                        rs.getLong("in_movements_count"),
                        rs.getBigDecimal("total_quantity_in"),
                        rs.getLong("out_movements_count"),
                        rs.getBigDecimal("total_quantity_out"),
                        rs.getBigDecimal("net_quantity_change"),
                        rs.getBigDecimal("total_value_moved"),
                        getLocalDateTime(rs, "first_movement_date"),
                        getLocalDateTime(rs, "last_movement_date"),
                        rs.getBigDecimal("current_stock")
                )
        );
    }

    /**
     * Get movement summary by product with filters
     */
    public List<InventoryMovementSummaryByProductDTO> getSummaryByProduct(
            UUID productId,
            String categoryName,
            Integer limit
    ) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM v_inventory_movements_summary_by_product WHERE 1=1 "
        );

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (productId != null) {
            sql.append("AND product_id = :productId ");
            params.addValue("productId", productId);
        }

        if (categoryName != null && !categoryName.isEmpty()) {
            sql.append("AND LOWER(category_name) LIKE LOWER(:categoryName) ");
            params.addValue("categoryName", "%" + categoryName + "%");
        }

        sql.append("ORDER BY total_value_moved DESC LIMIT :limit");
        params.addValue("limit", limit != null ? limit : 100);

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) ->
                new InventoryMovementSummaryByProductDTO(
                        getUUID(rs, "product_id"),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getLong("total_movements"),
                        rs.getLong("in_movements_count"),
                        rs.getBigDecimal("total_quantity_in"),
                        rs.getLong("out_movements_count"),
                        rs.getBigDecimal("total_quantity_out"),
                        rs.getBigDecimal("net_quantity_change"),
                        rs.getBigDecimal("total_value_moved"),
                        getLocalDateTime(rs, "first_movement_date"),
                        getLocalDateTime(rs, "last_movement_date"),
                        rs.getBigDecimal("current_stock")
                )
        );
    }

    /**
     * Get recent movements (last 30 days)
     */
    public List<InventoryMovementDetailDTO> getRecentMovements() {
        String sql = """
                SELECT
                    NULL::UUID AS movement_id,
                    movement_date,
                    DATE(movement_date) AS movement_date_only,
                    movement_type,
                    quantity,
                    unit_cost,
                    total_value,
                    reference_type,
                    NULL::UUID AS reference_id,
                    notes,
                    NULL::UUID AS product_id,
                    sku,
                    product_name,
                    NULL AS category_name,
                    NULL AS unit_of_measure,
                    NULL::UUID AS location_id,
                    NULL AS location_code,
                    location_name,
                    NULL AS location_type,
                    NULL::NUMERIC AS current_stock,
                    NULL::NUMERIC AS minimum_quantity,
                    NULL::NUMERIC AS maximum_quantity,
                    movement_direction,
                    NULL::UUID AS created_by,
                    NULL::TIMESTAMP AS created_at
                FROM v_inventory_movements_recent
                """;

        return jdbcTemplate.query(sql, new HashMap<>(), this::mapInventoryMovementDetail);
    }

    /**
     * Count movements with filters
     */
    public long countMovements(InventoryMovementFilterDTO filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM v_inventory_movements_detailed WHERE 1=1 "
        );

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filter.startDate() != null) {
            sql.append("AND movement_date_only >= :startDate ");
            params.addValue("startDate", filter.startDate());
        }

        if (filter.endDate() != null) {
            sql.append("AND movement_date_only <= :endDate ");
            params.addValue("endDate", filter.endDate());
        }

        if (filter.productId() != null) {
            sql.append("AND product_id = :productId ");
            params.addValue("productId", filter.productId());
        }

        if (filter.locationId() != null) {
            sql.append("AND location_id = :locationId ");
            params.addValue("locationId", filter.locationId());
        }

        if (filter.movementType() != null) {
            sql.append("AND movement_type = :movementType ");
            params.addValue("movementType", filter.movementType());
        }

        if (filter.movementDirection() != null) {
            sql.append("AND movement_direction = :movementDirection ");
            params.addValue("movementDirection", filter.movementDirection());
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Get movement totals with filters
     */
    public MovementTotalsDTO getMovementTotals(InventoryMovementFilterDTO filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    COUNT(*) AS total_movements,
                    SUM(CASE WHEN movement_direction = 'IN' THEN 1 ELSE 0 END) AS in_count,
                    SUM(CASE WHEN movement_direction = 'OUT' THEN 1 ELSE 0 END) AS out_count,
                    SUM(CASE WHEN movement_direction = 'IN' THEN quantity ELSE 0 END) AS total_quantity_in,
                    SUM(CASE WHEN movement_direction = 'OUT' THEN ABS(quantity) ELSE 0 END) AS total_quantity_out,
                    SUM(CASE WHEN movement_direction = 'IN' THEN total_value ELSE 0 END) AS total_value_in,
                    SUM(CASE WHEN movement_direction = 'OUT' THEN total_value ELSE 0 END) AS total_value_out
                FROM v_inventory_movements_detailed
                WHERE 1=1
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filter.startDate() != null) {
            sql.append("AND movement_date_only >= :startDate ");
            params.addValue("startDate", filter.startDate());
        }

        if (filter.endDate() != null) {
            sql.append("AND movement_date_only <= :endDate ");
            params.addValue("endDate", filter.endDate());
        }

        if (filter.productId() != null) {
            sql.append("AND product_id = :productId ");
            params.addValue("productId", filter.productId());
        }

        if (filter.locationId() != null) {
            sql.append("AND location_id = :locationId ");
            params.addValue("locationId", filter.locationId());
        }

        if (filter.movementType() != null) {
            sql.append("AND movement_type = :movementType ");
            params.addValue("movementType", filter.movementType());
        }

        if (filter.movementDirection() != null) {
            sql.append("AND movement_direction = :movementDirection ");
            params.addValue("movementDirection", filter.movementDirection());
        }

        return jdbcTemplate.queryForObject(sql.toString(), params, (rs, rowNum) ->
                new MovementTotalsDTO(
                        rs.getLong("total_movements"),
                        rs.getLong("in_count"),
                        rs.getLong("out_count"),
                        rs.getBigDecimal("total_quantity_in"),
                        rs.getBigDecimal("total_quantity_out"),
                        rs.getBigDecimal("total_value_in"),
                        rs.getBigDecimal("total_value_out")
                )
        );
    }

    // Helper methods

    private InventoryMovementDetailDTO mapInventoryMovementDetail(ResultSet rs, int rowNum) throws SQLException {
        return new InventoryMovementDetailDTO(
                getUUID(rs, "movement_id"),
                getLocalDateTime(rs, "movement_date"),
                rs.getDate("movement_date_only") != null ? rs.getDate("movement_date_only").toLocalDate() : null,
                rs.getString("movement_type"),
                rs.getBigDecimal("quantity"),
                rs.getBigDecimal("unit_cost"),
                rs.getBigDecimal("total_value"),
                rs.getString("reference_type"),
                getUUID(rs, "reference_id"),
                rs.getString("notes"),
                getUUID(rs, "product_id"),
                rs.getString("sku"),
                rs.getString("product_name"),
                rs.getString("category_name"),
                rs.getString("unit_of_measure"),
                getUUID(rs, "location_id"),
                rs.getString("location_code"),
                rs.getString("location_name"),
                rs.getString("location_type"),
                rs.getBigDecimal("current_stock"),
                rs.getBigDecimal("minimum_quantity"),
                rs.getBigDecimal("maximum_quantity"),
                rs.getString("movement_direction"),
                getUUID(rs, "created_by"),
                getLocalDateTime(rs, "created_at")
        );
    }

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

    /**
     * Movement Totals DTO
     */
    public record MovementTotalsDTO(
            Long totalMovements,
            Long inCount,
            Long outCount,
            BigDecimal totalQuantityIn,
            BigDecimal totalQuantityOut,
            BigDecimal totalValueIn,
            BigDecimal totalValueOut
    ) {
    }
}
