package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.Sale;
import com.estoquecentral.sales.domain.NfceStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Sale Repository
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Repository
public interface SaleRepository extends CrudRepository<Sale, UUID> {

    @Query("SELECT * FROM sales WHERE tenant_id = :tenantId AND id = :id")
    Optional<Sale> findByTenantIdAndId(@Param("tenantId") UUID tenantId, @Param("id") UUID id);

    @Query("SELECT * FROM sales WHERE tenant_id = :tenantId AND sale_number = :saleNumber")
    Optional<Sale> findByTenantIdAndSaleNumber(@Param("tenantId") UUID tenantId, @Param("saleNumber") String saleNumber);

    @Query("SELECT * FROM sales WHERE tenant_id = :tenantId AND nfce_status = :status::varchar")
    List<Sale> findByTenantIdAndNfceStatus(@Param("tenantId") UUID tenantId, @Param("status") String status);

    @Query("SELECT * FROM sales WHERE tenant_id = :tenantId ORDER BY sale_date DESC LIMIT :limit OFFSET :offset")
    List<Sale> findByTenantIdPaginated(@Param("tenantId") UUID tenantId, @Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT COUNT(*) FROM sales WHERE tenant_id = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT * FROM sales WHERE tenant_id = :tenantId AND customer_id = :customerId ORDER BY sale_date DESC")
    List<Sale> findByTenantIdAndCustomerId(@Param("tenantId") UUID tenantId, @Param("customerId") UUID customerId);

    @Query("SELECT COUNT(*) FROM sales WHERE tenant_id = :tenantId AND sale_number LIKE :pattern")
    long countBySaleNumberPattern(@Param("tenantId") UUID tenantId, @Param("pattern") String pattern);
}
