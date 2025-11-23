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
     */
    @Query("""
        SELECT *
        FROM stock_adjustments
        WHERE tenant_id = :tenantId
          AND (:productId IS NULL OR product_id = :productId)
          AND (:stockLocationId IS NULL OR stock_location_id = :stockLocationId)
          AND (:adjustmentType IS NULL OR adjustment_type = :adjustmentType)
          AND (:reasonCode IS NULL OR reason_code = :reasonCode)
          AND (:adjustmentDateFrom IS NULL OR adjustment_date >= :adjustmentDateFrom)
          AND (:adjustmentDateTo IS NULL OR adjustment_date <= :adjustmentDateTo)
          AND (:userId IS NULL OR adjusted_by_user_id = :userId)
        ORDER BY adjustment_date DESC, created_at DESC
        """)
    Page<StockAdjustment> search(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("stockLocationId") UUID stockLocationId,
            @Param("adjustmentType") String adjustmentType,
            @Param("reasonCode") String reasonCode,
            @Param("adjustmentDateFrom") LocalDate adjustmentDateFrom,
            @Param("adjustmentDateTo") LocalDate adjustmentDateTo,
            @Param("userId") UUID userId,
            Pageable pageable
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
