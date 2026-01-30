package com.estoquecentral.company.adapter.in;

import com.estoquecentral.company.adapter.in.dto.CreateCompanyRequest;
import com.estoquecentral.company.adapter.in.dto.CreateCompanyResponse;
import com.estoquecentral.company.application.CompanyService;
import com.estoquecentral.company.domain.Company;
import com.estoquecentral.auth.application.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * CompanyController - REST API for company management.
 *
 * <p>Story 8.1 - Self-service company creation
 *
 * <p><strong>Public Endpoints:</strong>
 * <ul>
 *   <li>POST /api/public/companies - Create new company (no authentication required)</li>
 * </ul>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/public/companies")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;
    private final JwtService jwtService;

    public CompanyController(CompanyService companyService, JwtService jwtService) {
        this.companyService = companyService;
        this.jwtService = jwtService;
    }

    /**
     * Creates a new company with full tenant provisioning (Story 8.1 - AC1, AC2, AC3, AC4, AC5).
     *
     * <p><strong>Public Endpoint:</strong> No authentication required (AC1)
     *
     * <p><strong>Process:</strong>
     * <ol>
     *   <li>Validate request (cnpj, email format)</li>
     *   <li>Provision tenant schema (create schema + run migrations + seed profiles) - AC3</li>
     *   <li>Create company record in public.companies - AC2</li>
     *   <li>Create user-company association with ADMIN role - AC4</li>
     *   <li>Generate JWT token with tenantId and roles: [ADMIN] - AC5</li>
     * </ol>
     *
     * @param request company creation request
     * @return 201 Created with company details and JWT token
     */
    @PostMapping
    public ResponseEntity<CreateCompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        logger.info("POST /api/public/companies - Creating company: nome={}, cnpj={}, userId={}",
                request.nome(), request.cnpj(), request.userId());

        try {
            // Parse userId as UUID
            UUID userId = UUID.fromString(request.userId());

            // Create company with tenant provisioning (AC2, AC3, AC4)
            Company company = companyService.createCompany(
                    request.nome(),
                    request.cnpj(),
                    request.email(),
                    request.telefone(),
                    userId
            );

            logger.info("Company created successfully: id={}, tenantId={}, schemaName={}",
                    company.id(), company.tenantId(), company.schemaName());

            // AC5: Generate JWT token with tenantId and roles: [ADMIN]
            String token = jwtService.generateCompanyToken(
                    userId,
                    company.tenantId(),
                    request.email()
            );

            logger.info("JWT token generated for user {} in company {}", request.userId(), company.id());

            // AC5: Return 201 Created with tenantId, nome, schemaName, and JWT token
            CreateCompanyResponse response = new CreateCompanyResponse(
                    company.tenantId(),
                    company.name(),
                    company.schemaName(),
                    token
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // AC6: Error handling for validation errors (e.g., duplicate CNPJ, invalid UUID)
            logger.warn("Company creation failed - validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {
            // AC6: Error handling for provisioning failures
            logger.error("Company creation failed - internal error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
