package com.estoquecentral.marketplace.adapter.out;

import com.estoquecentral.marketplace.domain.MarketplaceOrder;
import com.estoquecentral.marketplace.domain.OrderStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MarketplaceOrder
 * Story 5.5: Import and Process Orders from Mercado Livre
 */
@Repository
public interface MarketplaceOrderRepository extends CrudRepository<MarketplaceOrder, UUID> {

    /**
     * Find order by tenant, marketplace and external order ID
     */
    @Query("SELECT * FROM marketplace_orders WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND order_id_marketplace = :orderIdMarketplace")
    Optional<MarketplaceOrder> findByTenantIdAndMarketplaceAndOrderId(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("orderIdMarketplace") String orderIdMarketplace
    );

    /**
     * Check if order already exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM marketplace_orders WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND order_id_marketplace = :orderIdMarketplace)")
    boolean existsByTenantIdAndMarketplaceAndOrderId(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("orderIdMarketplace") String orderIdMarketplace
    );

    /**
     * Find all orders for a tenant and marketplace
     */
    @Query("SELECT * FROM marketplace_orders WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace ORDER BY imported_at DESC")
    List<MarketplaceOrder> findByTenantIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Find orders by status
     */
    @Query("SELECT * FROM marketplace_orders WHERE tenant_id = :tenantId " +
           "AND status = :status ORDER BY imported_at DESC")
    List<MarketplaceOrder> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Find orders imported after a certain date (for polling)
     */
    @Query("SELECT * FROM marketplace_orders WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND imported_at > :afterDate " +
           "ORDER BY imported_at DESC")
    List<MarketplaceOrder> findRecentOrders(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("afterDate") LocalDateTime afterDate
    );

    /**
     * Find orders within date range
     */
    @Query("SELECT * FROM marketplace_orders WHERE tenant_id = :tenantId " +
           "AND imported_at BETWEEN :startDate AND :endDate " +
           "ORDER BY imported_at DESC")
    List<MarketplaceOrder> findOrdersByDateRange(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
