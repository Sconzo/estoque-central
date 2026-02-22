package com.estoquecentral.inventory.adapter.out;

import com.estoquecentral.inventory.domain.StockAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

/**
 * StockAdjustmentRepository - Repository for stock adjustments
 * Story 3.5: Stock Adjustment
 */
@Repository
public interface StockAdjustmentRepository extends
        CrudRepository<StockAdjustment, UUID>,
        PagingAndSortingRepository<StockAdjustment, UUID> {

    /**
     * Find max adjustment number for tenant and year-month (for number generation)
     */
    @Query("""
        SELECT adjustment_number
        FROM stock_adjustments
        WHERE tenant_id = :tenantId
          AND adjustment_number LIKE CONCAT('ADJ-', :yearMonth, '-%')
        ORDER BY adjustment_number DESC
        LIMIT 1
        """)
    Optional<String> findMaxAdjustmentNumberByTenantAndYearMonth(
            @Param("tenantId") UUID tenantId,
            @Param("yearMonth") String yearMonth
    );

    /**
     * Search adjustments with filters
     * Note: Returns List instead of Page due to Spring Data JDBC limitations with @Query
     */
    @Query("""
        SELECT *
        FROM stock_adjustments
        WHERE tenant_id = :tenantId
          AND (CAST(:productId AS uuid) IS NULL OR product_id = CAST(:productId AS uuid))
          AND (CAST(:stockLocationId AS uuid) IS NULL OR stock_location_id = CAST(:stockLocationId AS uuid))
          AND (CAST(:adjustmentType AS text) IS NULL OR adjustment_type = CAST(:adjustmentType AS text))
          AND (CAST(:reasonCode AS text) IS NULL OR reason_code = CAST(:reasonCode AS text))
          AND (CAST(:adjustmentDateFrom AS date) IS NULL OR adjustment_date >= CAST(:adjustmentDateFrom AS date))
          AND (CAST(:adjustmentDateTo AS date) IS NULL OR adjustment_date <= CAST(:adjustmentDateTo AS date))
          AND (CAST(:userId AS uuid) IS NULL OR adjusted_by_user_id = CAST(:userId AS uuid))
        ORDER BY adjustment_date DESC, created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    List<StockAdjustment> search(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("stockLocationId") UUID stockLocationId,
            @Param("adjustmentType") String adjustmentType,
            @Param("reasonCode") String reasonCode,
            @Param("adjustmentDateFrom") LocalDate adjustmentDateFrom,
            @Param("adjustmentDateTo") LocalDate adjustmentDateTo,
            @Param("userId") UUID userId,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    /**
     * Count total adjustments matching filters (for pagination)
     */
    @Query("""
        SELECT COUNT(*)
        FROM stock_adjustments
        WHERE tenant_id = :tenantId
          AND (CAST(:productId AS uuid) IS NULL OR product_id = CAST(:productId AS uuid))
          AND (CAST(:stockLocationId AS uuid) IS NULL OR stock_location_id = CAST(:stockLocationId AS uuid))
          AND (CAST(:adjustmentType AS text) IS NULL OR adjustment_type = CAST(:adjustmentType AS text))
          AND (CAST(:reasonCode AS text) IS NULL OR reason_code = CAST(:reasonCode AS text))
          AND (CAST(:adjustmentDateFrom AS date) IS NULL OR adjustment_date >= CAST(:adjustmentDateFrom AS date))
          AND (CAST(:adjustmentDateTo AS date) IS NULL OR adjustment_date <= CAST(:adjustmentDateTo AS date))
          AND (CAST(:userId AS uuid) IS NULL OR adjusted_by_user_id = CAST(:userId AS uuid))
        """)
    long countSearch(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("stockLocationId") UUID stockLocationId,
            @Param("adjustmentType") String adjustmentType,
            @Param("reasonCode") String reasonCode,
            @Param("adjustmentDateFrom") LocalDate adjustmentDateFrom,
            @Param("adjustmentDateTo") LocalDate adjustmentDateTo,
            @Param("userId") UUID userId
    );

    /**
     * Find products with frequent adjustments (3+ in last 30 days)
     * Returns raw data that needs to be processed into DTOs
     */
    @Query("""
        SELECT
            sa.product_id,
            sa.stock_location_id,
            COUNT(*) as total_adjustments,
            SUM(CASE WHEN sa.adjustment_type = 'INCREASE' THEN sa.quantity ELSE 0 END) as total_increase,
            SUM(CASE WHEN sa.adjustment_type = 'DECREASE' THEN sa.quantity ELSE 0 END) as total_decrease
        FROM stock_adjustments sa
        WHERE sa.tenant_id = :tenantId
          AND sa.adjustment_date >= :dateFrom
        GROUP BY sa.product_id, sa.stock_location_id
        HAVING COUNT(*) >= 3
        ORDER BY COUNT(*) DESC
        LIMIT 20
        """)
    List<Map<String, Object>> findFrequentAdjustmentsRaw(
            @Param("tenantId") UUID tenantId,
            @Param("dateFrom") LocalDate dateFrom
    );
}
