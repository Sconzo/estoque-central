package com.estoquecentral.marketplace.adapter.out;

import com.estoquecentral.marketplace.domain.ConnectionStatus;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceConnection;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MarketplaceConnection entity
 * Story 5.1: Mercado Livre OAuth2 Authentication
 */
@Repository
public interface MarketplaceConnectionRepository extends CrudRepository<MarketplaceConnection, UUID> {

    /**
     * Find connection by tenant and marketplace (should be unique)
     */
    @Query("SELECT * FROM marketplace_connections WHERE tenant_id = :tenantId AND marketplace = CAST(:marketplace AS VARCHAR)")
    Optional<MarketplaceConnection> findByTenantIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Find all connections for a tenant
     */
    @Query("SELECT * FROM marketplace_connections WHERE tenant_id = :tenantId")
    List<MarketplaceConnection> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find connections with tokens expiring soon for a specific tenant (for refresh job)
     * AC4: Scheduled job queries connections with token_expires_at < NOW() + threshold
     */
    @Query("SELECT * FROM marketplace_connections " +
           "WHERE tenant_id = :tenantId " +
           "AND status = CAST(:status AS VARCHAR) " +
           "AND token_expires_at < :expirationThreshold")
    List<MarketplaceConnection> findExpiringConnectionsByTenant(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status,
        @Param("expirationThreshold") LocalDateTime expirationThreshold
    );

    /**
     * Find connections with tokens expiring soon (for refresh job)
     * AC4: Scheduled job queries connections with token_expires_at < NOW() + threshold
     * @deprecated Use findExpiringConnectionsByTenant() instead to ensure proper tenant context
     */
    @Deprecated
    @Query("SELECT * FROM marketplace_connections " +
           "WHERE status = CAST(:status AS VARCHAR) " +
           "AND token_expires_at < :expirationThreshold")
    List<MarketplaceConnection> findExpiringConnections(
        @Param("status") String status,
        @Param("expirationThreshold") LocalDateTime expirationThreshold
    );

    /**
     * Find all active (CONNECTED) connections for a specific tenant
     */
    @Query("SELECT * FROM marketplace_connections WHERE tenant_id = :tenantId AND status = 'CONNECTED'")
    List<MarketplaceConnection> findAllConnectedByTenant(@Param("tenantId") UUID tenantId);

    /**
     * Find all active (CONNECTED) connections
     * @deprecated Use findAllConnectedByTenant() instead to ensure proper tenant context
     */
    @Deprecated
    @Query("SELECT * FROM marketplace_connections WHERE status = 'CONNECTED'")
    List<MarketplaceConnection> findAllConnected();

    /**
     * Find connections by tenant, marketplace and status
     * Story 5.5: Order polling job
     */
    @Query("SELECT * FROM marketplace_connections WHERE tenant_id = :tenantId AND marketplace = CAST(:marketplace AS VARCHAR) AND status = CAST(:status AS VARCHAR)")
    List<MarketplaceConnection> findByTenantAndMarketplaceAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("status") String status
    );

    /**
     * Find connections by marketplace and status
     * Story 5.5: Order polling job
     * @deprecated Use findByTenantAndMarketplaceAndStatus() instead to ensure proper tenant context
     */
    @Deprecated
    @Query("SELECT * FROM marketplace_connections WHERE marketplace = CAST(:marketplace AS VARCHAR) AND status = CAST(:status AS VARCHAR)")
    List<MarketplaceConnection> findByMarketplaceAndStatus(
        @Param("marketplace") String marketplace,
        @Param("status") String status
    );

    /**
     * Check if connection exists for tenant and marketplace
     */
    @Query("SELECT COUNT(*) > 0 FROM marketplace_connections WHERE tenant_id = :tenantId AND marketplace = CAST(:marketplace AS VARCHAR)")
    boolean existsByTenantIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Delete connection by tenant and marketplace
     */
    @Query("DELETE FROM marketplace_connections WHERE tenant_id = :tenantId AND marketplace = CAST(:marketplace AS VARCHAR)")
    void deleteByTenantIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );
}
