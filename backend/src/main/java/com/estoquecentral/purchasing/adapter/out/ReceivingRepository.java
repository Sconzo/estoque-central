package com.estoquecentral.purchasing.adapter.out;

import com.estoquecentral.purchasing.domain.Receiving;
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
 * ReceivingRepository - Data access for receivings
 * Story 3.4: Receiving Processing and Weighted Average Cost Update
 */
@Repository
public interface ReceivingRepository extends
        CrudRepository<Receiving, UUID>,
        PagingAndSortingRepository<Receiving, UUID> {

    /**
     * Find all receivings for a tenant
     */
    @Query("""
        SELECT * FROM receivings
        WHERE tenant_id = :tenantId
        ORDER BY created_at DESC
        """)
    List<Receiving> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find receiving by receiving number
     */
    @Query("""
        SELECT * FROM receivings
        WHERE tenant_id = :tenantId
          AND receiving_number = :receivingNumber
        LIMIT 1
        """)
    Optional<Receiving> findByTenantIdAndReceivingNumber(
        @Param("tenantId") UUID tenantId,
        @Param("receivingNumber") String receivingNumber
    );

    /**
     * Find receivings by purchase order
     */
    @Query("""
        SELECT * FROM receivings
        WHERE tenant_id = :tenantId
          AND purchase_order_id = :purchaseOrderId
        ORDER BY receiving_date DESC
        """)
    List<Receiving> findByTenantIdAndPurchaseOrderId(
        @Param("tenantId") UUID tenantId,
        @Param("purchaseOrderId") UUID purchaseOrderId
    );

    /**
     * Search receivings with filters
     */
    @Query("""
        SELECT * FROM receivings
        WHERE tenant_id = :tenantId
          AND (CAST(:purchaseOrderId AS uuid) IS NULL OR purchase_order_id = CAST(:purchaseOrderId AS uuid))
          AND (CAST(:stockLocationId AS uuid) IS NULL OR stock_location_id = CAST(:stockLocationId AS uuid))
          AND (CAST(:receivingDateFrom AS date) IS NULL OR receiving_date >= CAST(:receivingDateFrom AS date))
          AND (CAST(:receivingDateTo AS date) IS NULL OR receiving_date <= CAST(:receivingDateTo AS date))
          AND (CAST(:status AS text) IS NULL OR status = CAST(:status AS text))
        ORDER BY receiving_date DESC, created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    java.util.List<Receiving> search(
        @Param("tenantId") UUID tenantId,
        @Param("purchaseOrderId") UUID purchaseOrderId,
        @Param("stockLocationId") UUID stockLocationId,
        @Param("receivingDateFrom") LocalDate receivingDateFrom,
        @Param("receivingDateTo") LocalDate receivingDateTo,
        @Param("status") String status,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("""
        SELECT COUNT(*) FROM receivings
        WHERE tenant_id = :tenantId
          AND (CAST(:purchaseOrderId AS uuid) IS NULL OR purchase_order_id = CAST(:purchaseOrderId AS uuid))
          AND (CAST(:stockLocationId AS uuid) IS NULL OR stock_location_id = CAST(:stockLocationId AS uuid))
          AND (CAST(:receivingDateFrom AS date) IS NULL OR receiving_date >= CAST(:receivingDateFrom AS date))
          AND (CAST(:receivingDateTo AS date) IS NULL OR receiving_date <= CAST(:receivingDateTo AS date))
          AND (CAST(:status AS text) IS NULL OR status = CAST(:status AS text))
        """)
    long countSearch(
        @Param("tenantId") UUID tenantId,
        @Param("purchaseOrderId") UUID purchaseOrderId,
        @Param("stockLocationId") UUID stockLocationId,
        @Param("receivingDateFrom") LocalDate receivingDateFrom,
        @Param("receivingDateTo") LocalDate receivingDateTo,
        @Param("status") String status
    );

    /**
     * Find maximum receiving number for a tenant and year-month
     */
    @Query("""
        SELECT MAX(receiving_number) FROM receivings
        WHERE tenant_id = :tenantId
          AND receiving_number LIKE CONCAT('RCV-', :yearMonth, '-%')
        """)
    Optional<String> findMaxReceivingNumberByTenantAndYearMonth(
        @Param("tenantId") UUID tenantId,
        @Param("yearMonth") String yearMonth
    );
}
