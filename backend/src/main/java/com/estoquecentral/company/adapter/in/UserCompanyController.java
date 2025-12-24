package com.estoquecentral.company.adapter.in;

import com.estoquecentral.company.adapter.in.dto.SwitchContextRequest;
import com.estoquecentral.company.adapter.in.dto.SwitchContextResponse;
import com.estoquecentral.company.adapter.in.dto.UserCompanyResponse;
import com.estoquecentral.company.application.CompanyService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserCompanyController - REST API for user-company associations.
 *
 * <p>Story 8.4 - List user's companies for multi-company navigation
 * <p>Story 9.1 - Switch company context
 *
 * <p><strong>Authenticated Endpoints:</strong>
 * <ul>
 *   <li>GET /api/users/me/companies - Get list of companies linked to current user</li>
 *   <li>PUT /api/users/me/context - Switch company context (Story 9.1)</li>
 * </ul>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/users/me")
public class UserCompanyController {

    private static final Logger logger = LoggerFactory.getLogger(UserCompanyController.class);

    private final CompanyService companyService;

    public UserCompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Gets all companies linked to the current authenticated user (Story 8.4 - AC1, AC2).
     *
     * <p><strong>Authentication Required:</strong> Extracts userId from JWT token (AC1)
     *
     * <p><strong>Query:</strong> Joins public.users + public.company_users + public.companies
     * and filters by user_id AND company_users.active = true (AC2)
     *
     * <p><strong>Response:</strong> Returns JSON array with company details including
     * user's profile/role in each company (AC2)
     *
     * <p><strong>Performance:</strong> Response time must be < 200ms (AC4, NFR5)
     *
     * @param authentication Spring Security authentication (contains userId from JWT)
     * @return 200 OK with list of companies (empty array if no companies found - AC3)
     */
    @GetMapping("/companies")
    public ResponseEntity<List<UserCompanyResponse>> getMyCompanies(Authentication authentication) {
        // AC1: Extract userId from JWT payload
        Long userId = Long.parseLong(authentication.getName());

        logger.info("GET /api/users/me/companies - Fetching companies for user: {}", userId);

        // AC2: Query user companies with joins
        List<UserCompanyResponse> companies = companyService.getUserCompanies(userId);

        logger.info("Found {} companies for user {}", companies.size(), userId);

        // AC3: Return 200 OK even if list is empty
        return ResponseEntity.ok(companies);
    }

    /**
     * Switches user's company context (Story 9.1 - AC1).
     *
     * <p><strong>Authentication Required:</strong> JWT authentication (ARCH24)
     *
     * <p><strong>Request:</strong> PUT /api/users/me/context
     * <pre>{@code
     * {
     *   "tenantId": "uuid-string"
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong> 200 OK with new JWT and company info
     * <pre>{@code
     * {
     *   "token": "new-jwt-token",
     *   "tenantId": "uuid",
     *   "companyName": "Company Name",
     *   "roles": ["ADMIN"]
     * }
     * }</pre>
     *
     * <p><strong>Errors:</strong>
     * <ul>
     *   <li>400 Bad Request: Invalid tenant ID format</li>
     *   <li>403 Forbidden: User doesn't have access to this company (AC4)</li>
     *   <li>404 Not Found: Company not found</li>
     * </ul>
     *
     * <p><strong>Performance:</strong> Response time must be < 500ms (AC5, NFR3, FR10)
     *
     * @param request Contains target tenant ID
     * @param authentication Spring Security authentication (contains userId and email from JWT)
     * @return 200 OK with new JWT and company info
     */
    @PutMapping("/context")
    public ResponseEntity<SwitchContextResponse> switchContext(
            @Valid @RequestBody SwitchContextRequest request,
            Authentication authentication) {

        // AC1: Extract user info from JWT
        Long userId = Long.parseLong(authentication.getName());

        // Get email from authentication details
        String email = (String) ((Claims) authentication.getDetails()).get("email");

        logger.info("PUT /api/users/me/context - User {} switching to tenant {}", userId, request.tenantId());

        // AC2, AC3, AC4, AC5: Delegate to service layer
        SwitchContextResponse response = companyService.switchContext(userId, email, request.tenantId());

        logger.info("Context switch successful for user {} to company {}", userId, response.companyName());

        return ResponseEntity.ok(response);
    }
}
