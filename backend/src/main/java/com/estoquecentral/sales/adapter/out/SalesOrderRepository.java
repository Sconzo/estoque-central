package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SalesOrderRepository - Data access for B2B sales orders
 * Story 4.5: Sales Order B2B Interface
 */
@Repository
public interface SalesOrderRepository extends
        CrudRepository<SalesOrder, UUID>,
        PagingAndSortingRepository<SalesOrder, UUID> {

    /**
     * Find all sales orders for a tenant
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
        ORDER BY data_criacao DESC
        """)
    List<SalesOrder> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find sales order by order number
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
          AND order_number = :orderNumber
        LIMIT 1
        """)
    Optional<SalesOrder> findByTenantIdAndOrderNumber(
        @Param("tenantId") UUID tenantId,
        @Param("orderNumber") String orderNumber
    );

    /**
     * Find sales orders by customer
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
          AND customer_id = :customerId
        ORDER BY data_criacao DESC
        """)
    List<SalesOrder> findByTenantIdAndCustomerId(
        @Param("tenantId") UUID tenantId,
        @Param("customerId") UUID customerId
    );

    /**
     * Find sales orders by status
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
          AND status = :status
        ORDER BY data_criacao DESC
        """)
    List<SalesOrder> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Search sales orders with filters and pagination
     * Note: Returns List instead of Page due to Spring Data JDBC limitations with @Query
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
          AND (CAST(:customerId AS uuid) IS NULL OR customer_id = CAST(:customerId AS uuid))
          AND (CAST(:status AS text) IS NULL OR status = CAST(:status AS text))
          AND (CAST(:orderDateFrom AS date) IS NULL OR order_date >= CAST(:orderDateFrom AS date))
          AND (CAST(:orderDateTo AS date) IS NULL OR order_date <= CAST(:orderDateTo AS date))
          AND (CAST(:orderNumber AS text) IS NULL OR order_number ILIKE CONCAT('%', CAST(:orderNumber AS text), '%'))
        ORDER BY data_criacao DESC
        LIMIT :limit OFFSET :offset
        """)
    List<SalesOrder> search(
        @Param("tenantId") UUID tenantId,
        @Param("customerId") UUID customerId,
        @Param("status") String status,
        @Param("orderDateFrom") LocalDate orderDateFrom,
        @Param("orderDateTo") LocalDate orderDateTo,
        @Param("orderNumber") String orderNumber,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    /**
     * Count sales orders matching search filters
     */
    @Query("""
        SELECT COUNT(*) FROM sales_orders
        WHERE tenant_id = :tenantId
          AND (CAST(:customerId AS uuid) IS NULL OR customer_id = CAST(:customerId AS uuid))
          AND (CAST(:status AS text) IS NULL OR status = CAST(:status AS text))
          AND (CAST(:orderDateFrom AS date) IS NULL OR order_date >= CAST(:orderDateFrom AS date))
          AND (CAST(:orderDateTo AS date) IS NULL OR order_date <= CAST(:orderDateTo AS date))
          AND (CAST(:orderNumber AS text) IS NULL OR order_number ILIKE CONCAT('%', CAST(:orderNumber AS text), '%'))
        """)
    long countSearch(
        @Param("tenantId") UUID tenantId,
        @Param("customerId") UUID customerId,
        @Param("status") String status,
        @Param("orderDateFrom") LocalDate orderDateFrom,
        @Param("orderDateTo") LocalDate orderDateTo,
        @Param("orderNumber") String orderNumber
    );

    /**
     * Find maximum order number for a tenant and year-month
     * Used for generating sequential SO numbers
     */
    @Query("""
        SELECT MAX(order_number) FROM sales_orders
        WHERE tenant_id = :tenantId
          AND order_number LIKE CONCAT('SO-', :yearMonth, '-%')
        """)
    Optional<String> findMaxOrderNumberByTenantAndYearMonth(
        @Param("tenantId") UUID tenantId,
        @Param("yearMonth") String yearMonth
    );

    /**
     * Count sales orders by tenant
     */
    @Query("""
        SELECT COUNT(*) FROM sales_orders
        WHERE tenant_id = :tenantId
        """)
    long countByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Count sales orders by status
     */
    @Query("""
        SELECT COUNT(*) FROM sales_orders
        WHERE tenant_id = :tenantId
          AND status = :status
        """)
    long countByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Find overdue sales orders (past expected delivery date, not invoiced/cancelled)
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
          AND delivery_date_expected < CURRENT_DATE
          AND status IN ('DRAFT', 'CONFIRMED')
        ORDER BY delivery_date_expected
        """)
    List<SalesOrder> findOverdueSalesOrders(@Param("tenantId") UUID tenantId);

    /**
     * Find confirmed sales orders older than specified date (for auto-release)
     * Story 4.6: Stock Reservation and Automatic Release
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
          AND status = 'CONFIRMED'
          AND DATE(data_criacao) < :expirationDate
        ORDER BY data_criacao
        """)
    List<SalesOrder> findExpiredConfirmedOrders(
        @Param("tenantId") UUID tenantId,
        @Param("expirationDate") LocalDate expirationDate
    );

    /**
     * Find confirmed sales orders expiring soon (within N days)
     * Story 4.6: Stock Reservation and Automatic Release - AC9
     */
    @Query("""
        SELECT * FROM sales_orders
        WHERE tenant_id = :tenantId
          AND status = 'CONFIRMED'
          AND DATE(data_criacao) < :expirationDate
        ORDER BY data_criacao
        """)
    List<SalesOrder> findExpiringSoon(
        @Param("tenantId") UUID tenantId,
        @Param("expirationDate") LocalDate expirationDate
    );
}
