package com.estoquecentral.auth.adapter.in;

import com.estoquecentral.auth.adapter.in.dto.GoogleCallbackRequest;
import com.estoquecentral.auth.adapter.in.dto.LoginResponse;
import com.estoquecentral.auth.application.GoogleAuthService;
import com.estoquecentral.shared.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API for authentication
 *
 * <p>This controller handles Google OAuth 2.0 authentication flow.
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>POST /api/auth/google/callback - Authenticate with Google ID token</li>
 * </ul>
 *
 * <p><strong>Authentication Flow:</strong>
 * <ol>
 *   <li>Frontend obtains Google ID token (via Google Sign-In library)</li>
 *   <li>Frontend calls POST /api/auth/google/callback with idToken + tenantId</li>
 *   <li>Backend validates token with Google</li>
 *   <li>Backend creates/updates user in tenant database</li>
 *   <li>Backend generates JWT token</li>
 *   <li>Frontend stores JWT token and includes it in all future requests (Authorization: Bearer {token})</li>
 * </ol>
 *
 * @see GoogleAuthService
 * @see com.estoquecentral.auth.adapter.in.security.JwtAuthenticationFilter
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API (Google OAuth 2.0)")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final GoogleAuthService googleAuthService;

    @Autowired
    public AuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    /**
     * Authenticates a user with Google OAuth 2.0.
     *
     * <p>This endpoint:
     * <ol>
     *   <li>Receives Google ID token from frontend</li>
     *   <li>Sets TenantContext based on request.tenantId</li>
     *   <li>Validates token with Google's servers</li>
     *   <li>Creates/updates user in tenant database</li>
     *   <li>Generates JWT token for our application</li>
     *   <li>Returns JWT token to frontend</li>
     * </ol>
     *
     * <p><strong>Request Body:</strong>
     * <pre>{@code
     * {
     *   "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE...",
     *   "tenantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     * <pre>{@code
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * }</pre>
     *
     * @param request the Google callback request (idToken + tenantId)
     * @return LoginResponse containing JWT token
     */
    @PostMapping("/google/callback")
    @Operation(
            summary = "Authenticate with Google OAuth 2.0",
            description = "Validates Google ID token, creates/updates user, and returns JWT token for our application",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful - JWT token returned",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid Google ID token or tenant ID"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Google token validation failed or user inactive"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error - Failed to communicate with Google API"
                    )
            }
    )
    public ResponseEntity<LoginResponse> googleCallback(@Valid @RequestBody GoogleCallbackRequest request) {
        logger.info("Received Google OAuth callback for tenantId: {}", request.getTenantId());

        try {
            // Set TenantContext for multi-tenancy
            TenantContext.setTenantId(request.getTenantId().toString());
            logger.debug("TenantContext set to: {}", request.getTenantId());

            // Authenticate user with Google and generate JWT
            String jwtToken = googleAuthService.authenticateWithGoogle(request.getIdToken());

            logger.info("User authenticated successfully - JWT token generated");

            // Return JWT token to frontend
            return ResponseEntity.ok(new LoginResponse(jwtToken));

        } catch (IllegalArgumentException e) {
            // Invalid token, tenant ID, or user inactive
            logger.warn("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (org.springframework.web.client.RestClientException e) {
            // Failed to communicate with Google API
            logger.error("Failed to communicate with Google API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            // Unexpected error
            logger.error("Unexpected error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            // Always clear TenantContext after request
            TenantContext.clear();
        }
    }

    /**
     * Health check endpoint (public - no authentication required).
     *
     * @return OK status
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Public endpoint to verify auth service is running",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Auth service is healthy")
            }
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
