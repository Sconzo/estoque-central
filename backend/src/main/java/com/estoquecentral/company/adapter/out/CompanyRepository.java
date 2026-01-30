package com.estoquecentral.company.adapter.out;

import com.estoquecentral.company.adapter.in.dto.UserCompanyResponse;
import com.estoquecentral.company.domain.Company;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Company aggregate.
 *
 * @since 1.0
 */
@Repository
public interface CompanyRepository extends CrudRepository<Company, UUID> {

    /**
     * Finds all active companies for a specific user.
     */
    @Query("""
        SELECT c.* FROM companies c
        INNER JOIN company_users cu ON c.id = cu.company_id
        WHERE cu.user_id = :userId AND cu.active = true AND c.active = true
        ORDER BY c.name
        """)
    List<Company> findAllByUserId(@Param("userId") UUID userId);

    /**
     * Finds a company by CNPJ.
     */
    Optional<Company> findByCnpj(String cnpj);

    /**
     * Checks if a company with the given CNPJ already exists.
     */
    @Query("SELECT COUNT(*) > 0 FROM companies WHERE cnpj = :cnpj")
    boolean existsByCnpj(@Param("cnpj") String cnpj);

    /**
     * Finds all active companies.
     */
    @Query("SELECT * FROM companies WHERE active = true ORDER BY name")
    List<Company> findAllActive();

    /**
     * Finds all companies for a user with their role information (Story 8.4 - AC2).
     *
     * <p>This query joins public.users + public.company_users + public.companies
     * and returns the data needed for the user company list response.
     *
     * @param userId User ID from JWT authentication
     * @return List of UserCompanyResponse with company details and user's role
     */
    @Query("""
        SELECT
            c.id,
            c.tenant_id::text AS "tenantId",
            c.name AS "nome",
            c.cnpj,
            NULL AS "profileId",
            cu.role AS "profileName"
        FROM public.companies c
        INNER JOIN public.company_users cu ON c.id = cu.company_id
        WHERE cu.user_id = :userId
          AND cu.active = true
          AND c.active = true
        ORDER BY c.name
        """)
    List<UserCompanyResponse> findUserCompaniesWithRoles(@Param("userId") UUID userId);

    /**
     * Finds a company by tenant ID (Story 9.1 - AC2).
     *
     * @param tenantId Tenant ID (UUID)
     * @return Optional Company
     */
    @Query("SELECT * FROM public.companies WHERE tenant_id = :tenantId AND active = true")
    Optional<Company> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Validates if a user has access to a specific tenant (Story 9.1 - AC2).
     *
     * <p>Validates that:
     * <ul>
     *   <li>Tenant exists in public.companies</li>
     *   <li>User has access (exists in public.company_users)</li>
     *   <li>User's access is active (company_users.active = true)</li>
     *   <li>Company is active (companies.active = true)</li>
     * </ul>
     *
     * @param userId User ID
     * @param tenantId Tenant ID (UUID)
     * @return true if user has active access to tenant, false otherwise
     */
    @Query("""
        SELECT COUNT(*) > 0
        FROM public.companies c
        INNER JOIN public.company_users cu ON c.id = cu.company_id
        WHERE c.tenant_id = :tenantId
          AND cu.user_id = :userId
          AND cu.active = true
          AND c.active = true
        """)
    boolean hasUserAccessToTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    /**
     * Finds user's role in a specific company (Story 9.1 - AC3).
     *
     * @param userId User ID
     * @param tenantId Tenant ID (UUID)
     * @return User's role string (e.g., "ADMIN", "GERENTE", "VENDEDOR")
     */
    @Query("""
        SELECT cu.role
        FROM public.companies c
        INNER JOIN public.company_users cu ON c.id = cu.company_id
        WHERE c.tenant_id = :tenantId
          AND cu.user_id = :userId
          AND cu.active = true
          AND c.active = true
        """)
    Optional<String> findUserRoleInTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);
}
