package com.estoquecentral.company.adapter.in;

import com.estoquecentral.auth.domain.User;
import com.estoquecentral.company.adapter.in.dto.CollaboratorDTO;
import com.estoquecentral.company.adapter.in.dto.CollaboratorDetailDTO;
import com.estoquecentral.company.adapter.in.dto.InviteCollaboratorRequest;
import com.estoquecentral.company.adapter.in.dto.InviteCollaboratorResponse;
import com.estoquecentral.company.application.CollaboratorService;
import com.estoquecentral.company.application.CompanyService;
import com.estoquecentral.company.domain.Company;
import com.estoquecentral.company.domain.CompanyUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CollaboratorController - REST API for collaborator management.
 *
 * <p>Story 10.1 - Backend endpoint for inviting collaborators
 * <p>Story 10.2 - Backend endpoint for listing collaborators
 * <p>Story 10.3 - Backend endpoint for removing collaborators
 * <p>Story 10.4 - Backend endpoint for promoting collaborators
 *
 * <p><strong>Authenticated Endpoints (ADMIN role required):</strong>
 * <ul>
 *   <li>POST /api/companies/{companyId}/collaborators - Invite collaborator (Story 10.1)</li>
 *   <li>GET /api/companies/{companyId}/collaborators - List collaborators (Story 10.2)</li>
 *   <li>DELETE /api/companies/{companyId}/collaborators/{userId} - Remove collaborator (Story 10.3)</li>
 *   <li>PUT /api/companies/{companyId}/collaborators/{userId}/promote - Promote to admin (Story 10.4)</li>
 * </ul>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/companies/{companyId}/collaborators")
public class CollaboratorController {

    private static final Logger logger = LoggerFactory.getLogger(CollaboratorController.class);

    private final CollaboratorService collaboratorService;
    private final CompanyService companyService;

    public CollaboratorController(
            CollaboratorService collaboratorService,
            CompanyService companyService) {
        this.collaboratorService = collaboratorService;
        this.companyService = companyService;
    }

    /**
     * Invites a collaborator to join a company (Story 10.1 - AC1, AC2, AC3, AC4, AC5).
     *
     * <p><strong>Authentication Required:</strong> JWT with ADMIN role (AC1, ARCH21)
     *
     * <p><strong>Request:</strong> POST /api/companies/{companyId}/collaborators
     * <pre>{@code
     * {
     *   "email": "colaborador@example.com",
     *   "role": "USER"
     * }
     * }</pre>
     *
     * <p><strong>Process:</strong>
     * <ol>
     *   <li>Verify current user has ADMIN role in the company (AC1)</li>
     *   <li>Check if user with email exists in public.users (AC2, AC3)</li>
     *   <li>If not exists: create placeholder user with google_id=NULL (AC3)</li>
     *   <li>Create company_users association (AC2, AC4)</li>
     *   <li>Prevent duplicate invitations (AC5)</li>
     * </ol>
     *
     * <p><strong>Response:</strong> 201 Created with collaborator details
     *
     * <p><strong>Errors:</strong>
     * <ul>
     *   <li>400 Bad Request: Invalid email format</li>
     *   <li>403 Forbidden: User is not ADMIN</li>
     *   <li>409 Conflict: User already associated with company (AC5)</li>
     * </ul>
     *
     * @param companyId Company ID from path
     * @param request Invitation request with email and role
     * @param authentication Spring Security authentication (contains userId from JWT)
     * @return 201 Created with collaborator details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InviteCollaboratorResponse> inviteCollaborator(
            @PathVariable UUID companyId,
            @Valid @RequestBody InviteCollaboratorRequest request,
            Authentication authentication) {

        // Get current user ID from JWT (now always UUID)
        UUID currentUserId = UUID.fromString(authentication.getName());

        logger.info("POST /api/companies/{}/collaborators - Admin user {} inviting: email={}, role={}",
                companyId, currentUserId, request.email(), request.role());

        try {
            // Verify company exists and user has access
            Company company = companyService.getCompanyById(companyId);
            logger.debug("Company found: id={}, name={}", company.id(), company.name());

            // Invite collaborator (Story 10.1 - AC2, AC3, AC4)
            CompanyUser invitation = collaboratorService.inviteCollaboratorByEmail(
                    companyId,
                    request.email(),
                    request.role()
            );

            // Fetch user details for response
            User user = collaboratorService.findUserById(invitation.userId());

            InviteCollaboratorResponse response = InviteCollaboratorResponse.from(invitation, user);

            logger.info("Collaborator invited successfully: collaboratorId={}, userId={}, email={}, role={}",
                    invitation.id(), invitation.userId(), user.getEmail(), invitation.role());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // AC5: Duplicate invitation (409 Conflict)
            if (e.getMessage().contains("already associated")) {
                logger.warn("Duplicate invitation attempt: {}", e.getMessage());
                throw new IllegalStateException(e.getMessage()); // Will be mapped to 409 by exception handler
            }
            logger.error("Invitation failed - validation error: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("Invitation failed - unexpected error", e);
            throw new RuntimeException("Failed to invite collaborator: " + e.getMessage(), e);
        }
    }

    /**
     * Lists all active collaborators for a company (Story 10.2 - AC1, AC2).
     *
     * <p><strong>Authentication Required:</strong> JWT authentication (AC1)
     *
     * <p><strong>Query:</strong> Joins company_users + users tables (AC2)
     * and filters by companyId AND active = true
     *
     * <p><strong>Performance:</strong> Response time must be < 200ms (AC3, NFR5)
     *
     * @param companyId Company ID from path
     * @param authentication Spring Security authentication
     * @return 200 OK with list of collaborators including user details
     */
    @GetMapping
    public ResponseEntity<List<CollaboratorDetailDTO>> listCollaborators(
            @PathVariable UUID companyId,
            Authentication authentication) {

        UUID currentUserId = UUID.fromString(authentication.getName());
        logger.info("GET /api/companies/{}/collaborators - User {} listing collaborators", companyId, currentUserId);

        // AC2: Fetch collaborators with user details (joins company_users + users)
        java.util.Map<CompanyUser, User> collaboratorsMap = collaboratorService.listCollaboratorsWithDetails(companyId);

        List<CollaboratorDetailDTO> response = collaboratorsMap.entrySet().stream()
                .map(entry -> CollaboratorDetailDTO.from(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        logger.info("Found {} collaborators for company {}", response.size(), companyId);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a collaborator from a company (Story 10.3 - AC1, AC2, AC3, AC4).
     *
     * <p><strong>Authentication Required:</strong> JWT with ADMIN role (AC1)
     *
     * <p><strong>Soft Delete:</strong> Sets company_user.active = false (AC2)
     *
     * <p><strong>Validations:</strong>
     * <ul>
     *   <li>AC3: Self-removal protection (returns 400 Bad Request)</li>
     *   <li>AC4: Last admin protection (returns 400 Bad Request)</li>
     * </ul>
     *
     * @param companyId Company ID from path
     * @param userId User ID to remove
     * @param authentication Spring Security authentication
     * @return 204 No Content on success, 400 Bad Request if validation fails
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeCollaborator(
            @PathVariable UUID companyId,
            @PathVariable UUID userId,
            Authentication authentication) {

        UUID currentUserId = UUID.fromString(authentication.getName());
        logger.info("DELETE /api/companies/{}/collaborators/{} - Admin {} removing collaborator",
                companyId, userId, currentUserId);

        try {
            // Pass currentUserId for self-removal validation (AC3)
            collaboratorService.removeCollaborator(companyId, userId, currentUserId);

            logger.info("Collaborator removed successfully: userId={}, companyId={}", userId, companyId);
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            // AC3, AC4: Validation failures (self-removal or last admin)
            logger.warn("Collaborator removal validation failed: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage()); // Will be mapped to 400 by exception handler
        }
    }

    /**
     * Promotes a collaborator to ADMIN role (Story 10.4 - AC1, AC2, AC3).
     *
     * <p><strong>Authentication Required:</strong> JWT with ADMIN role (AC1)
     *
     * <p><strong>Validations:</strong>
     * <ul>
     *   <li>AC2: Already admin check (returns 400 Bad Request)</li>
     * </ul>
     *
     * <p><strong>Multi-admin support:</strong>
     * <ul>
     *   <li>AC3: Supports multiple admins (no limit on number of admins)</li>
     * </ul>
     *
     * @param companyId Company ID from path
     * @param userId User ID to promote
     * @param authentication Spring Security authentication
     * @return 200 OK on success, 400 Bad Request if user is already admin
     */
    @PutMapping("/{userId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> promoteToAdmin(
            @PathVariable UUID companyId,
            @PathVariable UUID userId,
            Authentication authentication) {

        UUID currentUserId = UUID.fromString(authentication.getName());
        logger.info("PUT /api/companies/{}/collaborators/{}/promote - Admin {} promoting collaborator to ADMIN",
                companyId, userId, currentUserId);

        try {
            // AC1, AC3: Promote to ADMIN (supports multiple admins)
            collaboratorService.promoteToAdmin(companyId, userId);

            logger.info("Collaborator promoted to ADMIN: userId={}, companyId={}", userId, companyId);
            return ResponseEntity.ok().build();

        } catch (IllegalStateException e) {
            // AC2: Already admin validation failure
            logger.warn("Promotion validation failed: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage()); // Will be mapped to 400 by exception handler
        }
    }
}
