package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.Customer;
import com.estoquecentral.sales.domain.CustomerType;
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
 * CustomerRepository - Data access for Customer entities.
 *
 * Story 4.1: Customer Management
 *
 * This repository operates on TENANT schemas (not public schema).
 * Each tenant has its own `customers` table with isolated customer data.
 *
 * The correct schema is automatically selected via TenantContext
 * and TenantRoutingDataSource.
 *
 * @see Customer
 * @see com.estoquecentral.shared.tenant.TenantContext
 * @see com.estoquecentral.shared.tenant.TenantRoutingDataSource
 */
@Repository
public interface CustomerRepository extends
        CrudRepository<Customer, UUID>,
        PagingAndSortingRepository<Customer, UUID> {

    /**
     * Finds all customers by tenant ID with active status filter.
     *
     * @param tenantId the tenant ID
     * @param ativo active status filter
     * @param pageable pagination parameters
     * @return Page of customers
     */
    @Query("SELECT * FROM customers WHERE tenant_id = :tenantId AND ativo = :ativo ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<Customer> findByTenantIdAndAtivo(
        @Param("tenantId") UUID tenantId,
        @Param("ativo") Boolean ativo,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId AND ativo = :ativo")
    long countByTenantIdAndAtivo(@Param("tenantId") UUID tenantId, @Param("ativo") Boolean ativo);

    /**
     * Finds customers by tenant ID (all active customers by default).
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return Page of active customers
     */
    @Query("SELECT * FROM customers WHERE tenant_id = :tenantId AND ativo = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<Customer> findByTenantIdPaginated(
        @Param("tenantId") UUID tenantId,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    /**
     * Finds customer by tenant ID and CPF (encrypted field).
     *
     * @param tenantId the tenant ID
     * @param cpf the CPF (will be encrypted before query)
     * @return Optional containing the customer if found
     */
    @Query("SELECT * FROM customers WHERE tenant_id = :tenantId AND cpf = :cpf")
    Optional<Customer> findByTenantIdAndCpf(@Param("tenantId") UUID tenantId, @Param("cpf") String cpf);

    /**
     * Finds customer by tenant ID and CNPJ (encrypted field).
     *
     * @param tenantId the tenant ID
     * @param cnpj the CNPJ (will be encrypted before query)
     * @return Optional containing the customer if found
     */
    @Query("SELECT * FROM customers WHERE tenant_id = :tenantId AND cnpj = :cnpj")
    Optional<Customer> findByTenantIdAndCnpj(@Param("tenantId") UUID tenantId, @Param("cnpj") String cnpj);

    /**
     * Finds customer by tenant ID and email (encrypted field).
     *
     * @param tenantId the tenant ID
     * @param email the email (will be encrypted before query)
     * @return Optional containing the customer if found
     */
    @Query("SELECT * FROM customers WHERE tenant_id = :tenantId AND email = :email")
    Optional<Customer> findByTenantIdAndEmail(@Param("tenantId") UUID tenantId, @Param("email") String email);

    /**
     * Finds customers by customer type.
     *
     * @param tenantId the tenant ID
     * @param customerType the customer type (INDIVIDUAL or BUSINESS)
     * @param pageable pagination parameters
     * @return Page of customers
     */
    @Query("SELECT * FROM customers WHERE tenant_id = :tenantId AND customer_type = :customerType AND ativo = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<Customer> findByTenantIdAndCustomerType(
        @Param("tenantId") UUID tenantId,
        @Param("customerType") String customerType,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId AND customer_type = :customerType AND ativo = true")
    long countByTenantIdAndCustomerType(@Param("tenantId") UUID tenantId, @Param("customerType") String customerType);

    /**
     * Quick search for customers by name (first_name, last_name, company_name, or trade_name).
     * Used for autocomplete in PDV and Order screens.
     *
     * Performance target: < 500ms (NFR3)
     *
     * @param tenantId the tenant ID
     * @param searchTerm the search term (partial match)
     * @param pageable pagination parameters (typically LIMIT 10)
     * @return List of customers matching the search term
     */
    @Query("""
        SELECT * FROM customers
        WHERE tenant_id = :tenantId
          AND ativo = true
          AND (
              LOWER(first_name || ' ' || last_name) LIKE LOWER('%' || :searchTerm || '%')
              OR LOWER(company_name) LIKE LOWER('%' || :searchTerm || '%')
              OR LOWER(trade_name) LIKE LOWER('%' || :searchTerm || '%')
          )
        ORDER BY
            CASE
                WHEN customer_type = 'INDIVIDUAL' THEN first_name
                ELSE company_name
            END
        """)
    List<Customer> quickSearch(@Param("tenantId") UUID tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Gets the default "Consumidor Final" customer for a tenant.
     *
     * @param tenantId the tenant ID
     * @return Optional containing the default consumer customer
     */
    @Query("SELECT * FROM customers WHERE tenant_id = :tenantId AND is_default_consumer = true LIMIT 1")
    Optional<Customer> findDefaultConsumer(@Param("tenantId") UUID tenantId);

    /**
     * Checks if a CPF already exists for the tenant (for uniqueness validation).
     *
     * @param tenantId the tenant ID
     * @param cpf the CPF (encrypted)
     * @return true if CPF exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM customers WHERE tenant_id = :tenantId AND cpf = :cpf")
    boolean existsByTenantIdAndCpf(@Param("tenantId") UUID tenantId, @Param("cpf") String cpf);

    /**
     * Checks if a CNPJ already exists for the tenant (for uniqueness validation).
     *
     * @param tenantId the tenant ID
     * @param cnpj the CNPJ (encrypted)
     * @return true if CNPJ exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM customers WHERE tenant_id = :tenantId AND cnpj = :cnpj")
    boolean existsByTenantIdAndCnpj(@Param("tenantId") UUID tenantId, @Param("cnpj") String cnpj);

    /**
     * Checks if an email already exists for the tenant (for uniqueness validation).
     *
     * @param tenantId the tenant ID
     * @param email the email (encrypted)
     * @return true if email exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM customers WHERE tenant_id = :tenantId AND email = :email")
    boolean existsByTenantIdAndEmail(@Param("tenantId") UUID tenantId, @Param("email") String email);
}
