package com.estoquecentral.marketplace.adapter.out;

import com.estoquecentral.marketplace.domain.MarketplaceSyncQueue;
import com.estoquecentral.marketplace.domain.SyncStatus;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MarketplaceSyncQueue
 * Story 5.4: Stock Synchronization to Mercado Livre - AC1
 */
@Repository
public interface MarketplaceSyncQueueRepository extends CrudRepository<MarketplaceSyncQueue, UUID> {

    /**
     * Find pending items ordered by priority and creation time
     * AC2: Worker de Sincronização
     */
    @Query("SELECT * FROM marketplace_sync_queue " +
           "WHERE status = 'PENDING' " +
           "ORDER BY priority DESC, created_at ASC " +
           "LIMIT :limit")
    List<MarketplaceSyncQueue> findPendingItems(@Param("limit") int limit);

    /**
     * Find pending items for a specific tenant
     */
    @Query("SELECT * FROM marketplace_sync_queue " +
           "WHERE tenant_id = :tenantId " +
           "AND status = 'PENDING' " +
           "ORDER BY priority DESC, created_at ASC " +
           "LIMIT :limit")
    List<MarketplaceSyncQueue> findPendingItemsByTenant(
        @Param("tenantId") UUID tenantId,
        @Param("limit") int limit
    );

    /**
     * Check if item already exists in queue (for deduplication)
     * AC1: Deduplica
     */
    @Query("SELECT * FROM marketplace_sync_queue " +
           "WHERE tenant_id = :tenantId " +
           "AND product_id = :productId " +
           "AND (:variantId IS NULL AND variant_id IS NULL OR variant_id = :variantId) " +
           "AND marketplace = CAST(:marketplace AS VARCHAR) " +
           "AND sync_type = CAST(:syncType AS VARCHAR) " +
           "AND status IN ('PENDING', 'PROCESSING')")
    Optional<MarketplaceSyncQueue> findExistingQueueItem(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId,
        @Param("variantId") UUID variantId,
        @Param("marketplace") String marketplace,
        @Param("syncType") String syncType
    );

    /**
     * Delete completed items older than X days
     */
    @Modifying
    @Query("DELETE FROM marketplace_sync_queue " +
           "WHERE status IN ('SUCCESS', 'FAILED') " +
           "AND processed_at < NOW() - INTERVAL ':days days'")
    void deleteOldCompletedItems(@Param("days") int days);

    /**
     * Count pending items
     */
    @Query("SELECT COUNT(*) FROM marketplace_sync_queue WHERE status = 'PENDING'")
    long countPending();

    /**
     * Count processing items
     */
    @Query("SELECT COUNT(*) FROM marketplace_sync_queue WHERE status = 'PROCESSING'")
    long countProcessing();
}
