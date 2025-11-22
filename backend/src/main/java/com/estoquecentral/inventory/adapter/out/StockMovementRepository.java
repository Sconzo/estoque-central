package com.estoquecentral.inventory.adapter.out;

import com.estoquecentral.inventory.domain.MovementType;
import com.estoquecentral.inventory.domain.StockMovement;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * StockMovementRepository - Data access for stock movement history
 * Story 2.8: Stock Movement History
 *
 * NOTE: This repository only supports INSERT and SELECT operations.
 * UPDATE and DELETE are blocked by database triggers to maintain audit trail integrity.
 *
 * AC2: Query movements by product, variant, location, type, date range
 * AC3: Retrieve movement timeline for traceability
 */
@Repository
public interface StockMovementRepository extends CrudRepository<StockMovement, UUID> {

    // ============================================================
    // AC2: Query by Product
    // ============================================================

    /**
     * Find all movements for a product (across all locations)
     * Ordered by most recent first
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND product_id = :productId
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndProductId(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId
    );

    /**
     * Find movements for a product at a specific location
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND product_id = :productId
          AND stock_location_id = :locationId
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndProductIdAndLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("locationId") UUID locationId
    );

    // ============================================================
    // AC2: Query by Variant
    // ============================================================

    /**
     * Find all movements for a variant (across all locations)
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND variant_id = :variantId
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndVariantId(
            @Param("tenantId") UUID tenantId,
            @Param("variantId") UUID variantId
    );

    /**
     * Find movements for a variant at a specific location
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND variant_id = :variantId
          AND stock_location_id = :locationId
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndVariantIdAndLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("variantId") UUID variantId,
            @Param("locationId") UUID locationId
    );

    // ============================================================
    // AC2: Query by Location
    // ============================================================

    /**
     * Find all movements at a specific location
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND stock_location_id = :locationId
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("locationId") UUID locationId
    );

    // ============================================================
    // AC2: Query by Movement Type
    // ============================================================

    /**
     * Find movements by type (e.g., all SALES, all PURCHASES)
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND type = :type
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndType(
            @Param("tenantId") UUID tenantId,
            @Param("type") String type
    );

    // ============================================================
    // AC2: Query by Date Range
    // ============================================================

    /**
     * Find movements within a date range
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND created_at >= :startDate
          AND created_at <= :endDate
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find movements for a product within a date range
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND product_id = :productId
          AND created_at >= :startDate
          AND created_at <= :endDate
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndProductIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ============================================================
    // AC3: Query by Document Reference
    // ============================================================

    /**
     * Find all movements linked to a specific document
     * (e.g., all movements from a sale or purchase)
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND document_type = :documentType
          AND document_id = :documentId
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndDocument(
            @Param("tenantId") UUID tenantId,
            @Param("documentType") String documentType,
            @Param("documentId") UUID documentId
    );

    // ============================================================
    // AC3: Query by User
    // ============================================================

    /**
     * Find all movements performed by a specific user
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND user_id = :userId
        ORDER BY created_at DESC
        """)
    List<StockMovement> findByTenantIdAndUserId(
            @Param("tenantId") UUID tenantId,
            @Param("userId") UUID userId
    );

    // ============================================================
    // AC4: Latest Balance Query
    // ============================================================

    /**
     * Get the latest movement for a product at a location
     * (to retrieve the current balance)
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND product_id = :productId
          AND stock_location_id = :locationId
        ORDER BY created_at DESC
        LIMIT 1
        """)
    StockMovement findLatestByTenantIdAndProductIdAndLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("locationId") UUID locationId
    );

    /**
     * Get the latest movement for a variant at a location
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
          AND variant_id = :variantId
          AND stock_location_id = :locationId
        ORDER BY created_at DESC
        LIMIT 1
        """)
    StockMovement findLatestByTenantIdAndVariantIdAndLocationId(
            @Param("tenantId") UUID tenantId,
            @Param("variantId") UUID variantId,
            @Param("locationId") UUID locationId
    );

    // ============================================================
    // Complex Queries (Multiple Filters)
    // ============================================================

    /**
     * Find movements with multiple optional filters
     * Note: This would ideally be implemented with a criteria builder or QueryDSL
     * For now, the service layer will handle dynamic filtering
     */
    @Query("""
        SELECT * FROM stock_movements
        WHERE tenant_id = :tenantId
        ORDER BY created_at DESC
        LIMIT 1000
        """)
    List<StockMovement> findAllByTenantId(@Param("tenantId") UUID tenantId);

    // ============================================================
    // Statistics Queries
    // ============================================================

    /**
     * Count total movements for a product
     */
    @Query("""
        SELECT COUNT(*) FROM stock_movements
        WHERE tenant_id = :tenantId
          AND product_id = :productId
        """)
    Long countByTenantIdAndProductId(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId
    );

    /**
     * Count movements by type for a product
     */
    @Query("""
        SELECT COUNT(*) FROM stock_movements
        WHERE tenant_id = :tenantId
          AND product_id = :productId
          AND type = :type
        """)
    Long countByTenantIdAndProductIdAndType(
            @Param("tenantId") UUID tenantId,
            @Param("productId") UUID productId,
            @Param("type") String type
    );
}
