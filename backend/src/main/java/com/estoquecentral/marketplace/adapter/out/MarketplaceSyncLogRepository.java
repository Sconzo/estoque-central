package com.estoquecentral.marketplace.adapter.out;

import com.estoquecentral.marketplace.domain.MarketplaceSyncLog;
import com.estoquecentral.marketplace.domain.SyncStatus;
import com.estoquecentral.marketplace.domain.SyncType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for MarketplaceSyncLog
 * Story 5.4: Stock Synchronization to Mercado Livre - AC4
 */
@Repository
public interface MarketplaceSyncLogRepository extends
        CrudRepository<MarketplaceSyncLog, UUID>,
        PagingAndSortingRepository<MarketplaceSyncLog, UUID> {

    /**
     * Find logs by tenant with pagination
     * Note: Returns List instead of Page due to Spring Data JDBC limitations with @Query
     */
    @Query("SELECT * FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId " +
           "ORDER BY created_at DESC " +
           "LIMIT :limit OFFSET :offset")
    List<MarketplaceSyncLog> findByTenantId(
        @Param("tenantId") UUID tenantId,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find logs by product
     */
    @Query("SELECT * FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId " +
           "AND product_id = :productId " +
           "ORDER BY created_at DESC")
    List<MarketplaceSyncLog> findByProductId(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId
    );

    /**
     * Find logs by status
     * Note: Returns List instead of Page due to Spring Data JDBC limitations with @Query
     */
    @Query("SELECT * FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId " +
           "AND status = CAST(:status AS VARCHAR) " +
           "ORDER BY created_at DESC " +
           "LIMIT :limit OFFSET :offset")
    List<MarketplaceSyncLog> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId " +
           "AND status = CAST(:status AS VARCHAR)")
    long countByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Find logs by date range
     * Note: Returns List instead of Page due to Spring Data JDBC limitations with @Query
     */
    @Query("SELECT * FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId " +
           "AND created_at BETWEEN :startDate AND :endDate " +
           "ORDER BY created_at DESC " +
           "LIMIT :limit OFFSET :offset")
    List<MarketplaceSyncLog> findByTenantIdAndDateRange(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId " +
           "AND created_at BETWEEN :startDate AND :endDate")
    long countByTenantIdAndDateRange(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get latest log for a product/marketplace combination
     */
    @Query("SELECT * FROM marketplace_sync_logs " +
           "WHERE tenant_id = :tenantId " +
           "AND product_id = :productId " +
           "AND marketplace = CAST(:marketplace AS VARCHAR) " +
           "AND sync_type = CAST(:syncType AS VARCHAR) " +
           "ORDER BY created_at DESC " +
           "LIMIT 1")
    MarketplaceSyncLog findLatestByProductAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId,
        @Param("marketplace") String marketplace,
        @Param("syncType") String syncType
    );
}
