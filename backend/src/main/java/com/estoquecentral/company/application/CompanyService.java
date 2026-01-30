package com.estoquecentral.company.application;

import com.estoquecentral.common.exception.SchemaProvisioningException;
import com.estoquecentral.company.adapter.in.dto.SwitchContextResponse;
import com.estoquecentral.company.adapter.in.dto.UserCompanyResponse;
import com.estoquecentral.company.adapter.out.CompanyRepository;
import com.estoquecentral.company.adapter.out.CompanyUserRepository;
import com.estoquecentral.company.domain.Company;
import com.estoquecentral.company.domain.CompanyUser;
import com.estoquecentral.auth.application.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for company management.
 *
 * <p>Handles company creation, management, and user associations following
 * the use cases defined in Epics 8, 9, and 10.</p>
 *
 * @since 1.0
 */
@Service
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CompanyTenantProvisioner companyTenantProvisioner;
    private final JwtService jwtService;
    private final JdbcTemplate jdbcTemplate;

    public CompanyService(
        CompanyRepository companyRepository,
        CompanyUserRepository companyUserRepository,
        CompanyTenantProvisioner companyTenantProvisioner,
        JwtService jwtService,
        JdbcTemplate jdbcTemplate
    ) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.companyTenantProvisioner = companyTenantProvisioner;
        this.jwtService = jwtService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates a new company with full tenant provisioning (Epic 8 - Self-service company creation).
     *
     * <p><strong>Story 8.1 - Acceptance Criteria:</strong>
     * <ul>
     *   <li>AC2: Creates company record in public.companies</li>
     *   <li>AC3: Provisions tenant schema with all tables and default profiles</li>
     *   <li>AC4: Creates user-company association with ADMIN role</li>
     * </ul>
     *
     * <p><strong>Story 8.5 - Error Handling:</strong>
     * <ul>
     *   <li>AC2: Duplicate company names are allowed (only schema_name unique)</li>
     *   <li>AC3: Database connection failures throw DataAccessException → 503</li>
     *   <li>AC4: Schema creation failure triggers automatic rollback</li>
     *   <li>AC5: Critical errors logged to Application Insights</li>
     * </ul>
     *
     * @param name Company name (duplicates allowed)
     * @param cnpj Company CNPJ (optional)
     * @param email Company email
     * @param phone Company phone
     * @param ownerUserId User ID of the company owner
     * @return Created company with tenantId and schemaName populated
     * @throws SchemaProvisioningException if tenant provisioning fails (with automatic rollback)
     * @throws DataAccessException if database connection fails (Story 8.5 - AC3)
     */
    // NOTE: NO @Transactional here! CompanyTenantProvisioner.provisionTenant() uses REQUIRES_NEW
    // to create its own transaction. Having @Transactional here causes nested transaction conflicts.
    public Company createCompany(String name, String cnpj, String email, String phone, UUID ownerUserId) {
        logger.info("Creating company: name={}, cnpj={}, email={}, ownerUserId={}",
                name, cnpj, email, ownerUserId);

        try {
            // AC3: Provision tenant schema (create schema + run migrations + seed profiles)
            logger.debug("Provisioning tenant schema...");
            CompanyTenantProvisioner.TenantProvisionResult provisionResult = companyTenantProvisioner.provisionTenant();

            if (!provisionResult.success()) {
                // AC5: Critical error logging
                logger.error("CRITICAL: Tenant provisioning failed: {}", provisionResult.errorMessage());
                throw new SchemaProvisioningException(
                        "Failed to provision tenant: " + provisionResult.errorMessage(), null);
            }

            logger.info("Tenant provisioned: tenantId={}, schemaName={}",
                    provisionResult.tenantId(), provisionResult.schemaName());

            // Insert tenant record in public.tenants table
            insertTenantRecord(provisionResult.tenantId(), provisionResult.schemaName(), name, email, cnpj);

            // AC2: Create company record with tenantId and schemaName
            // Note: Duplicate names are allowed - only schema_name is unique
            Company company = Company.create(name, cnpj, email, phone, ownerUserId);
            Company companyWithTenant = company.withTenantSchema(
                    provisionResult.tenantId(),
                    provisionResult.schemaName()
            );

            Company savedCompany = companyRepository.save(companyWithTenant);
            logger.info("Company created: id={}, tenantId={}, schemaName={}",
                    savedCompany.id(), savedCompany.tenantId(), savedCompany.schemaName());

            // AC4: Create user-company association with ADMIN role
            CompanyUser ownerAssociation = CompanyUser.invite(savedCompany.id(), ownerUserId, "ADMIN");
            CompanyUser acceptedAssociation = ownerAssociation.accept();
            companyUserRepository.save(acceptedAssociation);

            logger.info("Owner added as ADMIN: userId={}, companyId={}", ownerUserId, savedCompany.id());

            return savedCompany;

        } catch (DataAccessException e) {
            // AC3 + AC5: Database connection failure - critical logging
            logger.error("CRITICAL: Database connection failed during company creation: name={}, userId={}",
                    name, ownerUserId, e);
            throw e; // Re-throw to be handled by GlobalExceptionHandler → 503
        } catch (SchemaProvisioningException e) {
            // AC4 + AC5: Schema provisioning failure - rollback already performed by TenantProvisioner
            logger.error("CRITICAL: Schema provisioning failed for company: name={}, userId={}",
                    name, ownerUserId, e);
            throw e; // Re-throw to be handled by GlobalExceptionHandler → 500
        }
    }

    /**
     * Finds all companies accessible by a user (Epic 9 - Company context selection).
     *
     * @param userId User ID
     * @return List of companies where user is a collaborator
     */
    public List<Company> findUserCompanies(UUID userId) {
        return companyRepository.findAllByUserId(userId);
    }

    /**
     * Gets all companies linked to a user with their role information (Story 8.4 - AC2).
     *
     * <p><strong>Query Details:</strong>
     * <ul>
     *   <li>Joins public.users + public.company_users + public.companies</li>
     *   <li>Filters by user_id = {userId} AND company_users.active = true</li>
     *   <li>Returns company details including user's role in each company</li>
     * </ul>
     *
     * <p><strong>Performance:</strong> Response time must be < 200ms (AC4, NFR5)
     *
     * @param userId User ID from JWT authentication
     * @return List of UserCompanyResponse (empty list if user has no companies)
     */
    public List<UserCompanyResponse> getUserCompanies(UUID userId) {
        logger.debug("Fetching companies for user: {}", userId);

        // Direct JDBC query to avoid Spring Data JDBC mapping issues with UUID
        String sql = """
            SELECT
                c.id,
                c.tenant_id,
                c.name,
                c.cnpj,
                cu.role
            FROM public.companies c
            INNER JOIN public.company_users cu ON c.id = cu.company_id
            WHERE cu.user_id = ?
              AND cu.active = true
              AND c.active = true
            ORDER BY c.name
            """;

        List<UserCompanyResponse> companies = jdbcTemplate.query(sql,
            (rs, rowNum) -> new UserCompanyResponse(
                UUID.fromString(rs.getString("id")),
                rs.getObject("tenant_id") != null ? rs.getObject("tenant_id").toString() : null,
                rs.getString("name"),
                rs.getString("cnpj"),
                null,
                rs.getString("role")
            ),
            userId
        );

        logger.debug("Found {} companies for user {} with tenantIds: {}",
            companies.size(), userId,
            companies.stream().map(UserCompanyResponse::tenantId).toList());
        return companies;
    }

    /**
     * Updates company information (Epic 10 - Company management).
     *
     * @param companyId Company ID
     * @param name New company name
     * @param email New company email
     * @param phone New company phone
     * @return Updated company
     */
    public Company getCompanyById(UUID companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
    }

    /**
     * Gets a company by tenant ID (Story 10.5, 10.6).
     *
     * @param tenantId Tenant UUID
     * @return Company entity
     * @throws IllegalArgumentException if company not found
     */
    public Company getCompanyByTenantId(UUID tenantId) {
        return companyRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found for tenantId: " + tenantId));
    }

    /**
     * Updates a company's information (Epic 10 - Company management).
     *
     * @param companyId Company ID
     * @param name New company name
     * @param email New company email
     * @param phone New company phone
     * @return Updated company
     */
    @Transactional
    public Company updateCompany(UUID companyId, String name, String email, String phone) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        Company updatedCompany = company.update(name, email, phone);
        return companyRepository.save(updatedCompany);
    }

    /**
     * Deactivates a company (Epic 10 - Company management).
     *
     * @param companyId Company ID
     * @deprecated Use {@link #deleteCompanyWithValidation(Long)} for Story 10.6
     */
    @Deprecated
    @Transactional
    public void deleteCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        Company deactivatedCompany = company.deactivate();
        companyRepository.save(deactivatedCompany);
    }

    /**
     * Deletes a company with orphan protection (Story 10.6 - AC2, AC3).
     *
     * <p><strong>Orphan Protection (AC2):</strong>
     * <ul>
     *   <li>Checks if any users are ONLY linked to this company</li>
     *   <li>Blocks deletion if orphan users exist</li>
     * </ul>
     *
     * <p><strong>Soft Delete (AC3):</strong>
     * <ul>
     *   <li>Sets company.active = false</li>
     *   <li>Sets all company_users.active = false for this company</li>
     *   <li>Retains tenant schema for recovery</li>
     * </ul>
     *
     * @param companyId Company ID
     * @throws IllegalArgumentException if company not found
     * @throws IllegalStateException if orphan users exist
     */
    @Transactional
    public void deleteCompanyWithValidation(UUID companyId) {
        logger.info("Deleting company with validation: companyId={}", companyId);

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        // AC2: Orphan protection - check if any users are ONLY linked to this company
        List<CompanyUser> companyUsers = companyUserRepository.findAllActiveByCompanyId(companyId);

        for (CompanyUser companyUser : companyUsers) {
            // Check if this user has other active company associations
            List<CompanyUser> userCompanies = companyUserRepository.findAllActiveByUserId(companyUser.userId());

            if (userCompanies.size() == 1 && userCompanies.get(0).companyId().equals(companyId)) {
                // User is ONLY linked to this company - orphan detected
                logger.warn("Orphan user detected: userId={}, companyId={}", companyUser.userId(), companyId);
                throw new IllegalStateException(
                        "Cannot delete company: User " + companyUser.userId() +
                        " would become orphaned. Please ensure all users have access to other companies first.");
            }
        }

        // AC3: Soft delete company
        Company deactivatedCompany = company.deactivate();
        companyRepository.save(deactivatedCompany);
        logger.info("Company deactivated: id={}", companyId);

        // AC3: Deactivate all company_user associations
        for (CompanyUser companyUser : companyUsers) {
            CompanyUser deactivated = companyUser.deactivate();
            companyUserRepository.save(deactivated);
        }
        logger.info("Deactivated {} company_user associations for company {}", companyUsers.size(), companyId);

        // AC3: Tenant schema is retained (not dropped) for recovery
        logger.info("Company deleted successfully: id={}, schema retained for recovery", companyId);
    }

    /**
     * Switches user's company context (Story 9.1).
     *
     * <p><strong>Story 9.1 - Acceptance Criteria:</strong>
     * <ul>
     *   <li>AC1: Authenticated endpoint with JWT</li>
     *   <li>AC2: Validates tenantId exists and user has active access</li>
     *   <li>AC3: Generates new JWT with updated tenant and roles</li>
     *   <li>AC4: Returns 403 if unauthorized</li>
     *   <li>AC5: Completes in < 500ms</li>
     * </ul>
     *
     * @param userId User ID from JWT authentication
     * @param email User email from JWT
     * @param tenantId Target company's tenant ID (UUID string)
     * @return SwitchContextResponse with new JWT and company info
     * @throws IllegalArgumentException if tenantId is invalid UUID
     * @throws AccessDeniedException if user doesn't have access to tenant (Story 9.1 - AC4)
     */
    public SwitchContextResponse switchContext(UUID userId, String email, String tenantId) {
        logger.info("Switching context for user {} to tenant {}", userId, tenantId);

        long startTime = System.currentTimeMillis();

        // AC2: Parse and validate tenant ID
        UUID targetTenantId;
        try {
            targetTenantId = UUID.fromString(tenantId);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid tenant ID format: {}", tenantId);
            throw new IllegalArgumentException("Tenant ID inválido: " + tenantId);
        }

        // AC2: Validate user has access to this tenant
        boolean hasAccess = companyRepository.hasUserAccessToTenant(userId, targetTenantId);
        if (!hasAccess) {
            logger.warn("User {} does not have access to tenant {}", userId, targetTenantId);
            // AC4: Return 403 Forbidden
            throw new AccessDeniedException("Você não tem acesso a esta empresa");
        }

        // AC2: Get company information
        Company company = companyRepository.findByTenantId(targetTenantId)
            .orElseThrow(() -> {
                logger.error("Tenant {} not found but user has access - data inconsistency", targetTenantId);
                return new IllegalArgumentException("Empresa não encontrada");
            });

        // AC3: Get user's role in this company
        String role = companyRepository.findUserRoleInTenant(userId, targetTenantId)
            .orElseThrow(() -> {
                logger.error("User {} has access to tenant {} but role not found - data inconsistency",
                        userId, targetTenantId);
                return new IllegalArgumentException("Perfil de usuário não encontrado");
            });

        // AC3: Generate new JWT with updated context
        String newToken = jwtService.generateContextSwitchToken(userId, targetTenantId, email, role);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Context switch completed in {}ms for user {} to tenant {}", duration, userId, targetTenantId);

        // AC5: Performance check - should complete in < 500ms
        if (duration > 500) {
            logger.warn("Context switch took longer than 500ms: {}ms (NFR3 violation)", duration);
        }

        // AC3: Return response with new token and company info
        return new SwitchContextResponse(
            newToken,
            targetTenantId.toString(),
            company.name(),
            List.of(role)
        );
    }

    /**
     * Inserts a tenant record into public.tenants table.
     * This is required for tenant context management and multi-tenant schema isolation.
     *
     * <p>Note: CNPJ is stored only in public.companies table, not in tenants.
     * The tenants table is for schema management, while companies holds business data.
     *
     * @param tenantId Tenant UUID
     * @param schemaName Schema name (e.g., tenant_xxxxx)
     * @param name Company/tenant name
     * @param email Company email
     * @param cnpj Company CNPJ (ignored - stored only in companies table)
     */
    private void insertTenantRecord(UUID tenantId, String schemaName, String name, String email, String cnpj) {
        logger.debug("Inserting tenant record: tenantId={}, schemaName={}", tenantId, schemaName);

        // Note: CNPJ is not inserted here - it's stored in public.companies table only
        String sql = "INSERT INTO tenants (id, nome, schema_name, email, ativo, data_criacao, data_atualizacao) " +
                     "VALUES (?, ?, ?, ?, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        jdbcTemplate.update(sql, tenantId, name, schemaName, email);

        logger.info("Tenant record inserted: tenantId={}, schemaName={}", tenantId, schemaName);
    }
}
