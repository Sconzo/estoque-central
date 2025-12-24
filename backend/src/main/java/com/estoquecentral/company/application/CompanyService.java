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
    private final TenantProvisioner tenantProvisioner;
    private final JwtService jwtService;

    public CompanyService(
        CompanyRepository companyRepository,
        CompanyUserRepository companyUserRepository,
        TenantProvisioner tenantProvisioner,
        JwtService jwtService
    ) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.tenantProvisioner = tenantProvisioner;
        this.jwtService = jwtService;
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
    @Transactional
    public Company createCompany(String name, String cnpj, String email, String phone, Long ownerUserId) {
        logger.info("Creating company: name={}, cnpj={}, email={}, ownerUserId={}",
                name, cnpj, email, ownerUserId);

        try {
            // AC3: Provision tenant schema (create schema + run migrations + seed profiles)
            logger.debug("Provisioning tenant schema...");
            TenantProvisioner.TenantProvisionResult provisionResult = tenantProvisioner.provisionTenant();

            if (!provisionResult.success()) {
                // AC5: Critical error logging
                logger.error("CRITICAL: Tenant provisioning failed: {}", provisionResult.errorMessage());
                throw new SchemaProvisioningException(
                        "Failed to provision tenant: " + provisionResult.errorMessage(), null);
            }

            logger.info("Tenant provisioned: tenantId={}, schemaName={}",
                    provisionResult.tenantId(), provisionResult.schemaName());

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
    public List<Company> findUserCompanies(Long userId) {
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
    public List<UserCompanyResponse> getUserCompanies(Long userId) {
        logger.debug("Fetching companies for user: {}", userId);
        List<UserCompanyResponse> companies = companyRepository.findUserCompaniesWithRoles(userId);
        logger.debug("Found {} companies for user {}", companies.size(), userId);
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
    @Transactional
    public Company updateCompany(Long companyId, String name, String email, String phone) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        Company updatedCompany = company.update(name, email, phone);
        return companyRepository.save(updatedCompany);
    }

    /**
     * Deactivates a company (Epic 10 - Company management).
     *
     * @param companyId Company ID
     */
    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        Company deactivatedCompany = company.deactivate();
        companyRepository.save(deactivatedCompany);
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
    public SwitchContextResponse switchContext(Long userId, String email, String tenantId) {
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
}
