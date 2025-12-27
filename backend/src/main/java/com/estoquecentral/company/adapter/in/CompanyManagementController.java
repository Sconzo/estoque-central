package com.estoquecentral.company.adapter.in;

import com.estoquecentral.auth.application.JwtService;
import com.estoquecentral.company.adapter.in.dto.CompanyDTO;
import com.estoquecentral.company.adapter.in.dto.UpdateCompanyRequest;
import com.estoquecentral.company.application.CompanyService;
import com.estoquecentral.company.domain.Company;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * CompanyManagementController - REST API for authenticated company management.
 *
 * <p>Story 10.5 - Update company data
 * <p>Story 10.6 - Delete company
 *
 * <p><strong>Authenticated Endpoints (ADMIN role required):</strong>
 * <ul>
 *   <li>PUT /api/companies/current - Update current company (Story 10.5)</li>
 *   <li>DELETE /api/companies/current - Delete current company (Story 10.6)</li>
 * </ul>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/companies/current")
public class CompanyManagementController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyManagementController.class);

    private final CompanyService companyService;
    private final JwtService jwtService;

    public CompanyManagementController(CompanyService companyService, JwtService jwtService) {
        this.companyService = companyService;
        this.jwtService = jwtService;
    }

    /**
     * Updates current company information (Story 10.5 - AC1, AC2, AC3).
     *
     * <p><strong>Authentication Required:</strong> JWT with ADMIN role (AC1)
     *
     * <p><strong>Process:</strong>
     * <ol>
     *   <li>Extract tenantId from JWT</li>
     *   <li>Lookup company by tenantId</li>
     *   <li>Update company data (AC2)</li>
     *   <li>Update data_atualizacao timestamp (AC2)</li>
     *   <li>Return updated company data (AC3)</li>
     * </ol>
     *
     * @param request Company update request
     * @param authentication Spring Security authentication (contains JWT)
     * @return 200 OK with updated company data
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTO> updateCompany(
            @Valid @RequestBody UpdateCompanyRequest request,
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {

        Long currentUserId = Long.parseLong(authentication.getName());
        logger.info("PUT /api/companies/current - Admin {} updating company", currentUserId);

        try {
            // Extract tenantId from JWT
            String token = authHeader.replace("Bearer ", "");
            Claims claims = jwtService.validateToken(token);
            String tenantIdStr = claims.get("tenantId", String.class);
            UUID tenantId = UUID.fromString(tenantIdStr);

            logger.debug("Extracted tenantId from JWT: {}", tenantId);

            // Lookup company by tenantId
            Company company = companyService.getCompanyByTenantId(tenantId);

            // AC2: Update company data and timestamp
            Company updatedCompany = companyService.updateCompany(
                    company.id(),
                    request.name(),
                    request.email(),
                    request.phone()
            );

            logger.info("Company updated successfully: id={}, name={}", updatedCompany.id(), updatedCompany.name());

            // AC3: Return updated company data
            CompanyDTO response = CompanyDTO.from(updatedCompany);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Company update failed", e);
            throw new RuntimeException("Failed to update company: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes current company (Story 10.6 - AC1, AC2, AC3).
     *
     * <p><strong>Authentication Required:</strong> JWT with ADMIN role (AC1)
     *
     * <p><strong>Orphan Protection:</strong>
     * <ul>
     *   <li>AC2: Checks if users are ONLY linked to this company</li>
     *   <li>AC2: Blocks deletion if orphan users exist</li>
     * </ul>
     *
     * <p><strong>Soft Delete:</strong>
     * <ul>
     *   <li>AC3: Sets company.active = false</li>
     *   <li>AC3: Sets all company_users.active = false</li>
     *   <li>AC3: Retains tenant schema for recovery</li>
     * </ul>
     *
     * @param authentication Spring Security authentication (contains JWT)
     * @return 204 No Content on success, 400 Bad Request if orphan users exist
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompany(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {

        Long currentUserId = Long.parseLong(authentication.getName());
        logger.info("DELETE /api/companies/current - Admin {} deleting company", currentUserId);

        try {
            // Extract tenantId from JWT
            String token = authHeader.replace("Bearer ", "");
            Claims claims = jwtService.validateToken(token);
            String tenantIdStr = claims.get("tenantId", String.class);
            UUID tenantId = UUID.fromString(tenantIdStr);

            logger.debug("Extracted tenantId from JWT: {}", tenantId);

            // Lookup company by tenantId
            Company company = companyService.getCompanyByTenantId(tenantId);

            // AC2, AC3: Delete company (with orphan protection and soft delete)
            companyService.deleteCompanyWithValidation(company.id());

            logger.info("Company deleted successfully: id={}", company.id());
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            // AC2: Orphan users exist - block deletion
            logger.warn("Company deletion blocked: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage()); // Will be mapped to 400 by exception handler

        } catch (Exception e) {
            logger.error("Company deletion failed", e);
            throw new RuntimeException("Failed to delete company: " + e.getMessage(), e);
        }
    }
}
