package com.estoquecentral.inventory.adapter.out;

import com.estoquecentral.inventory.domain.StockTransfer;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * StockTransferRepository - Data access for stock transfers
 * Story 2.9: Stock Transfer Between Locations
 *
 * AC5: Query de histórico com filtros
 */
@Repository
public interface StockTransferRepository extends CrudRepository<StockTransfer, UUID> {

    // ============================================================
    // Basic Queries
    // ============================================================

    /**
     * Find all transfers for a tenant
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find all transfers for a product
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND product_id = :productId
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndProductId(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId
    );

    /**
     * Find all transfers for a variant
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND variant_id = :variantId
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndVariantId(
            @Param("tenantId") UUID tenantId,
            @Param("variantId") UUID variantId
    );

    // ============================================================
    // Location-based Queries
    // ============================================================

    /**
     * Find all transfers FROM a specific location
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND origin_location_id = :locationId
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndOriginLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("locationId") UUID locationId
    );

    /**
     * Find all transfers TO a specific location
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND destination_location_id = :locationId
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndDestinationLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("locationId") UUID locationId
    );

    /**
     * Find transfers between two specific locations
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND origin_location_id = :originId
          AND destination_location_id = :destinationId
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndLocations(
            @Param("tenantId") UUID tenantId,
            @Param("originId") UUID originId,
            @Param("destinationId") UUID destinationId
    );

    // ============================================================
    // Date Range Queries
    // ============================================================

    /**
     * Find transfers within a date range
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND created_at >= :startDate
          AND created_at <= :endDate
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find transfers for a product within a date range
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND product_id = :productId
          AND created_at >= :startDate
          AND created_at <= :endDate
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndProductIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ============================================================
    // User Queries
    // ============================================================

    /**
     * Find all transfers performed by a specific user
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND user_id = :userId
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndUserId(
            @Param("tenantId") UUID tenantId,
            @Param("userId") UUID userId
    );

    // ============================================================
    // Status Queries
    // ============================================================

    /**
     * Find transfers by status
     */
    @Query("""
        SELECT * FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND status = :status
        ORDER BY created_at DESC
        """)
    List<StockTransfer> findByTenantIdAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("status") String status
    );

    // ============================================================
    // Statistics Queries
    // ============================================================

    /**
     * Count transfers for a product
     */
    @Query("""
        SELECT COUNT(*) FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND product_id = :productId
        """)
    Long countByTenantIdAndProductId(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId
    );

    /**
     * Count transfers from a location
     */
    @Query("""
        SELECT COUNT(*) FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND origin_location_id = :locationId
        """)
    Long countByTenantIdAndOriginLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("locationId") UUID locationId
    );

    /**
     * Count transfers to a location
     */
    @Query("""
        SELECT COUNT(*) FROM stock_transfers
        WHERE tenant_id = :tenantId
          AND destination_location_id = :locationId
        """)
    Long countByTenantIdAndDestinationLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("locationId") UUID locationId
    );
}
