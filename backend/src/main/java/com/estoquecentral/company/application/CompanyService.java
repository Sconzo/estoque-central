package com.estoquecentral.company.application;

import com.estoquecentral.company.adapter.out.CompanyRepository;
import com.estoquecentral.company.adapter.out.CompanyUserRepository;
import com.estoquecentral.company.domain.Company;
import com.estoquecentral.company.domain.CompanyUser;
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

    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;

    public CompanyService(
        CompanyRepository companyRepository,
        CompanyUserRepository companyUserRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
    }

    /**
     * Creates a new company (Epic 8 - Self-service company creation).
     *
     * @param name Company name
     * @param cnpj Company CNPJ
     * @param email Company email
     * @param phone Company phone
     * @param ownerUserId User ID of the company owner
     * @return Created company
     * @throws IllegalArgumentException if CNPJ already exists
     */
    @Transactional
    public Company createCompany(String name, String cnpj, String email, String phone, Long ownerUserId) {
        if (companyRepository.existsByCnpj(cnpj)) {
            throw new IllegalArgumentException("Company with CNPJ " + cnpj + " already exists");
        }

        Company company = Company.create(name, cnpj, email, phone, ownerUserId);
        Company savedCompany = companyRepository.save(company);

        // Automatically add owner as ADMIN
        CompanyUser ownerAssociation = CompanyUser.invite(savedCompany.id(), ownerUserId, "ADMIN");
        CompanyUser acceptedAssociation = ownerAssociation.accept();
        companyUserRepository.save(acceptedAssociation);

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
