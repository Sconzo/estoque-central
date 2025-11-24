package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.NfceStatus;
import com.estoquecentral.sales.domain.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * SaleRepository - Repository for Sale entity
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Repository
public interface SaleRepository extends CrudRepository<Sale, UUID> {

    Optional<Sale> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Sale> findByTenantIdAndSaleNumber(UUID tenantId, String saleNumber);

    @Query("""
        SELECT sale_number
        FROM sales
        WHERE tenant_id = :tenantId
          AND sale_number LIKE :prefix || '%'
        ORDER BY sale_number DESC
        LIMIT 1
        """)
    Optional<String> findMaxSaleNumberByTenantAndYearMonth(
            @Param("tenantId") UUID tenantId,
            @Param("prefix") String prefix
    );

    // Story 4.4: NFCe Retry and Cancellation
    Page<Sale> findByTenantIdAndNfceStatus(UUID tenantId, NfceStatus status, Pageable pageable);

    @Query("""
        SELECT *
        FROM sales
        WHERE tenant_id = :tenantId
          AND (nfce_status = 'PENDING' OR nfce_status = 'FAILED')
        ORDER BY sale_date DESC
        """)
    Page<Sale> findByTenantIdAndNfceStatusPendingOrFailed(
            @Param("tenantId") UUID tenantId,
            Pageable pageable
    );
}
