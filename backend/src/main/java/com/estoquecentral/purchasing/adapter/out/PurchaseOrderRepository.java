package com.estoquecentral.purchasing.adapter.out;

import com.estoquecentral.purchasing.domain.PurchaseOrder;
import com.estoquecentral.purchasing.domain.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * PurchaseOrderRepository - Data access for purchase orders
 * Story 3.2: Purchase Order Creation
 */
@Repository
public interface PurchaseOrderRepository extends
        CrudRepository<PurchaseOrder, UUID>,
        PagingAndSortingRepository<PurchaseOrder, UUID> {

    /**
     * Find all purchase orders for a tenant
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
        ORDER BY created_at DESC
        """)
    List<PurchaseOrder> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find purchase orders by tenant with pagination
     * Note: Returns List instead of Page due to Spring Data JDBC limitations
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    List<PurchaseOrder> findByTenantIdPaginated(
        @Param("tenantId") UUID tenantId,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    /**
     * Find purchase order by PO number
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND po_number = :poNumber
        LIMIT 1
        """)
    Optional<PurchaseOrder> findByTenantIdAndPoNumber(
        @Param("tenantId") UUID tenantId,
        @Param("poNumber") String poNumber
    );

    /**
     * Find purchase orders by supplier
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND supplier_id = :supplierId
        ORDER BY created_at DESC
        """)
    List<PurchaseOrder> findByTenantIdAndSupplierId(
        @Param("tenantId") UUID tenantId,
        @Param("supplierId") UUID supplierId
    );

    /**
     * Find purchase orders by status
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND status = :status
        ORDER BY created_at DESC
        """)
    List<PurchaseOrder> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Find purchase orders by supplier and status
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND supplier_id = :supplierId
          AND status = :status
        ORDER BY created_at DESC
        """)
    List<PurchaseOrder> findByTenantIdAndSupplierIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("supplierId") UUID supplierId,
        @Param("status") String status
    );

    /**
     * Search purchase orders with filters
     * Note: Returns List instead of Page due to Spring Data JDBC limitations
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND (:supplierId IS NULL OR supplier_id = :supplierId)
          AND (:status IS NULL OR status = :status)
          AND (:orderDateFrom IS NULL OR order_date >= :orderDateFrom)
          AND (:orderDateTo IS NULL OR order_date <= :orderDateTo)
          AND (:poNumber IS NULL OR po_number = :poNumber)
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    List<PurchaseOrder> search(
        @Param("tenantId") UUID tenantId,
        @Param("supplierId") UUID supplierId,
        @Param("status") String status,
        @Param("orderDateFrom") LocalDate orderDateFrom,
        @Param("orderDateTo") LocalDate orderDateTo,
        @Param("poNumber") String poNumber,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    /**
     * Count purchase orders matching search filters
     */
    @Query("""
        SELECT COUNT(*) FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND (:supplierId IS NULL OR supplier_id = :supplierId)
          AND (:status IS NULL OR status = :status)
          AND (:orderDateFrom IS NULL OR order_date >= :orderDateFrom)
          AND (:orderDateTo IS NULL OR order_date <= :orderDateTo)
          AND (:poNumber IS NULL OR po_number = :poNumber)
        """)
    long countSearch(
        @Param("tenantId") UUID tenantId,
        @Param("supplierId") UUID supplierId,
        @Param("status") String status,
        @Param("orderDateFrom") LocalDate orderDateFrom,
        @Param("orderDateTo") LocalDate orderDateTo,
        @Param("poNumber") String poNumber
    );

    /**
     * Find maximum PO number for a tenant and year-month
     * Used for generating sequential PO numbers
     */
    @Query("""
        SELECT MAX(po_number) FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND po_number LIKE CONCAT('PO-', :yearMonth, '-%')
        """)
    Optional<String> findMaxPoNumberByTenantAndYearMonth(
        @Param("tenantId") UUID tenantId,
        @Param("yearMonth") String yearMonth
    );

    /**
     * Count purchase orders by tenant
     */
    @Query("""
        SELECT COUNT(*) FROM purchase_orders
        WHERE tenant_id = :tenantId
        """)
    long countByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Count purchase orders by status
     */
    @Query("""
        SELECT COUNT(*) FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND status = :status
        """)
    long countByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Find overdue purchase orders
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND expected_delivery_date < CURRENT_DATE
          AND status IN ('SENT_TO_SUPPLIER', 'PARTIALLY_RECEIVED')
        ORDER BY expected_delivery_date
        """)
    List<PurchaseOrder> findOverduePurchaseOrders(@Param("tenantId") UUID tenantId);

    /**
     * Find purchase orders by tenant and status IN list (Story 3.3)
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND status IN (:statuses)
        """)
    List<PurchaseOrder> findByTenantIdAndStatusIn(
        @Param("tenantId") UUID tenantId,
        @Param("statuses") List<String> statuses,
        Sort sort
    );

    /**
     * Find purchase orders by tenant, supplier, and status IN list (Story 3.3)
     */
    @Query("""
        SELECT * FROM purchase_orders
        WHERE tenant_id = :tenantId
          AND supplier_id = :supplierId
          AND status IN (:statuses)
        """)
    List<PurchaseOrder> findByTenantIdAndSupplierIdAndStatusIn(
        @Param("tenantId") UUID tenantId,
        @Param("supplierId") UUID supplierId,
        @Param("statuses") List<String> statuses,
        Sort sort
    );
}
