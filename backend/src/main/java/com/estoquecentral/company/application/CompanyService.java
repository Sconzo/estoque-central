package com.estoquecentral.company.application;

import com.estoquecentral.company.adapter.out.CompanyRepository;
import com.estoquecentral.company.adapter.out.CompanyUserRepository;
import com.estoquecentral.company.domain.Company;
import com.estoquecentral.company.domain.CompanyUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public CompanyService(
        CompanyRepository companyRepository,
        CompanyUserRepository companyUserRepository,
        TenantProvisioner tenantProvisioner
    ) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.tenantProvisioner = tenantProvisioner;
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
     * @param name Company name
     * @param cnpj Company CNPJ
     * @param email Company email
     * @param phone Company phone
     * @param ownerUserId User ID of the company owner
     * @return Created company with tenantId and schemaName populated
     * @throws IllegalArgumentException if CNPJ already exists
     * @throws TenantProvisioner.TenantProvisioningException if tenant provisioning fails
     */
    @Transactional
    public Company createCompany(String name, String cnpj, String email, String phone, Long ownerUserId) {
        logger.info("Creating company: name={}, cnpj={}, email={}, ownerUserId={}",
                name, cnpj, email, ownerUserId);

        // Validate CNPJ uniqueness
        if (companyRepository.existsByCnpj(cnpj)) {
            throw new IllegalArgumentException("Company with CNPJ " + cnpj + " already exists");
        }

        // AC3: Provision tenant schema (create schema + run migrations + seed profiles)
        logger.debug("Provisioning tenant schema...");
        TenantProvisioner.TenantProvisionResult provisionResult = tenantProvisioner.provisionTenant();

        if (!provisionResult.success()) {
            logger.error("Tenant provisioning failed: {}", provisionResult.errorMessage());
            throw new TenantProvisioner.TenantProvisioningException(
                    "Failed to provision tenant: " + provisionResult.errorMessage(), null);
        }

        logger.info("Tenant provisioned: tenantId={}, schemaName={}",
                provisionResult.tenantId(), provisionResult.schemaName());

        // AC2: Create company record with tenantId and schemaName
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
}
