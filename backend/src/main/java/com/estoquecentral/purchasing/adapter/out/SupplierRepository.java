package com.estoquecentral.purchasing.adapter.out;

import com.estoquecentral.purchasing.domain.Supplier;
import com.estoquecentral.purchasing.domain.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SupplierRepository - Data access for suppliers
 * Story 3.1: Supplier Management
 */
@Repository
public interface SupplierRepository extends
        CrudRepository<Supplier, UUID>,
        PagingAndSortingRepository<Supplier, UUID> {

    /**
     * Find all suppliers for a tenant (active only by default)
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND ativo = true
        ORDER BY company_name
        """)
    List<Supplier> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find suppliers by tenant and active status
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND ativo = :ativo
        ORDER BY company_name
        """)
    List<Supplier> findByTenantIdAndAtivo(
        @Param("tenantId") UUID tenantId,
        @Param("ativo") Boolean ativo
    );

    /**
     * Find suppliers by tenant with pagination
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND ativo = true
        ORDER BY company_name
        """)
    Page<Supplier> findByTenantId(
        @Param("tenantId") UUID tenantId,
        Pageable pageable
    );

    /**
     * Find supplier by CNPJ (unique per tenant)
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND cnpj = :cnpj
        LIMIT 1
        """)
    Optional<Supplier> findByTenantIdAndCnpj(
        @Param("tenantId") UUID tenantId,
        @Param("cnpj") String cnpj
    );

    /**
     * Find supplier by CPF (unique per tenant)
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND cpf = :cpf
        LIMIT 1
        """)
    Optional<Supplier> findByTenantIdAndCpf(
        @Param("tenantId") UUID tenantId,
        @Param("cpf") String cpf
    );

    /**
     * Find supplier by supplier code
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND supplier_code = :code
        LIMIT 1
        """)
    Optional<Supplier> findByTenantIdAndSupplierCode(
        @Param("tenantId") UUID tenantId,
        @Param("code") String code
    );

    /**
     * Search suppliers by name (company name or trade name)
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND (
            LOWER(company_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(trade_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
          )
          AND ativo = true
        ORDER BY company_name
        """)
    List<Supplier> searchByName(
        @Param("tenantId") UUID tenantId,
        @Param("searchTerm") String searchTerm
    );

    /**
     * Search suppliers with pagination and filters
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND (:searchTerm IS NULL OR
               LOWER(company_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
               LOWER(trade_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
               cnpj LIKE CONCAT('%', :searchTerm, '%'))
          AND (:status IS NULL OR status = :status)
          AND (:ativo IS NULL OR ativo = :ativo)
        ORDER BY company_name
        """)
    Page<Supplier> search(
        @Param("tenantId") UUID tenantId,
        @Param("searchTerm") String searchTerm,
        @Param("status") String status,
        @Param("ativo") Boolean ativo,
        Pageable pageable
    );

    /**
     * Find active suppliers by status
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND status = :status
          AND ativo = true
        ORDER BY company_name
        """)
    List<Supplier> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Find preferred suppliers
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND is_preferred = true
          AND status = 'ACTIVE'
          AND ativo = true
        ORDER BY company_name
        """)
    List<Supplier> findPreferredSuppliers(@Param("tenantId") UUID tenantId);

    /**
     * Find suppliers by category
     */
    @Query("""
        SELECT * FROM suppliers
        WHERE tenant_id = :tenantId
          AND supplier_category = :category
          AND ativo = true
        ORDER BY company_name
        """)
    List<Supplier> findByTenantIdAndCategory(
        @Param("tenantId") UUID tenantId,
        @Param("category") String category
    );

    /**
     * Count suppliers by tenant
     */
    @Query("""
        SELECT COUNT(*) FROM suppliers
        WHERE tenant_id = :tenantId
          AND ativo = true
        """)
    long countByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Count suppliers by status
     */
    @Query("""
        SELECT COUNT(*) FROM suppliers
        WHERE tenant_id = :tenantId
          AND status = :status
          AND ativo = true
        """)
    long countByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status
    );

    /**
     * Check if CNPJ exists for another supplier
     */
    @Query("""
        SELECT EXISTS (
            SELECT 1 FROM suppliers
            WHERE tenant_id = :tenantId
              AND cnpj = :cnpj
              AND id != :excludeId
        )
        """)
    boolean existsByTenantIdAndCnpjAndIdNot(
        @Param("tenantId") UUID tenantId,
        @Param("cnpj") String cnpj,
        @Param("excludeId") UUID excludeId
    );
}
