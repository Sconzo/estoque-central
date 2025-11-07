package com.estoquecentral.auth.application;

import com.estoquecentral.auth.domain.Usuario;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * GoogleAuthService - Service for Google OAuth 2.0 authentication
 *
 * <p>This service handles the OAuth 2.0 flow with Google:
 * <ol>
 *   <li>Receive Google ID token from frontend</li>
 *   <li>Validate token with Google's tokeninfo endpoint</li>
 *   <li>Extract user information (sub, email, name, picture)</li>
 *   <li>Create or update user in database</li>
 *   <li>Generate JWT token for our application</li>
 * </ol>
 *
 * <p><strong>Security:</strong>
 * <ul>
 *   <li>ALWAYS validates Google ID token with Google's servers</li>
 *   <li>Verifies token audience matches our client ID</li>
 *   <li>Checks token expiration</li>
 * </ul>
 *
 * <p><strong>Multi-tenancy:</strong>
 * TenantContext must be set BEFORE calling authenticateWithGoogle(),
 * as the user will be created/updated in the current tenant schema.
 *
 * @see Usuario
 * @see UserService
 * @see JwtService
 */
@Service
public class GoogleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

    private static final String GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final String googleClientId;
    private final UserService userService;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    @Autowired
    public GoogleAuthService(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId,
            UserService userService,
            JwtService jwtService) {
        this.googleClientId = googleClientId;
        this.userService = userService;
        this.jwtService = jwtService;
        this.restTemplate = new RestTemplate();
        logger.info("GoogleAuthService initialized with clientId: {}", googleClientId);
    }

    /**
     * Authenticates a user with Google OAuth 2.0.
     *
     * <p>Flow:
     * <ol>
     *   <li>Validate Google ID token with Google's servers</li>
     *   <li>Extract user information (sub, email, name, picture)</li>
     *   <li>Find or create user in tenant database</li>
     *   <li>Generate JWT token for our application</li>
     * </ol>
     *
     * <p><strong>IMPORTANT:</strong> TenantContext must be set BEFORE calling this method.
     * The frontend must determine the tenant (e.g., from subdomain or URL path)
     * and set it via X-Tenant-ID header.
     *
     * @param googleIdToken the Google ID token received from frontend
     * @return JWT token for our application
     * @throws IllegalArgumentException if token is invalid or tenant context not set
     * @throws RestClientException if Google API is unreachable
     */
    @Transactional
    public String authenticateWithGoogle(String googleIdToken) {
        logger.info("Authenticating user with Google OAuth 2.0");

        // Step 1: Validate tenantId from TenantContext
        String tenantIdStr = TenantContext.getTenantId();
        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            logger.error("TenantContext not set - cannot authenticate user");
            throw new IllegalArgumentException(
                    "Tenant context not set. Frontend must send X-Tenant-ID header."
            );
        }

        UUID tenantId;
        try {
            tenantId = UUID.fromString(tenantIdStr);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid tenant ID: {}", tenantIdStr);
            throw new IllegalArgumentException("Invalid tenant ID: " + tenantIdStr, e);
        }

        logger.debug("TenantContext set to: {}", tenantId);

        // Step 2: Validate Google ID token with Google's servers
        Map<String, Object> googleUserInfo = validateGoogleToken(googleIdToken);

        // Step 3: Extract user information
        String googleId = (String) googleUserInfo.get("sub");
        String email = (String) googleUserInfo.get("email");
        String nome = (String) googleUserInfo.get("name");
        String pictureUrl = (String) googleUserInfo.get("picture");

        logger.info("Google token validated successfully for email: {}", email);

        // Step 4: Find or create user in tenant database
        Usuario usuario = userService.findOrCreateUser(googleId, email, nome, pictureUrl, tenantId);

        // Check if user is active
        if (!usuario.getAtivo()) {
            logger.warn("Inactive user attempted to login: {} ({})", email, usuario.getId());
            throw new IllegalArgumentException("User account is inactive. Please contact support.");
        }

        // Step 5: Generate JWT token for our application
        String jwtToken = jwtService.generateToken(usuario);

        logger.info("User authenticated successfully: {} ({})", email, usuario.getId());

        return jwtToken;
    }

    /**
     * Validates a Google ID token by calling Google's tokeninfo endpoint.
     *
     * <p>This method:
     * <ul>
     *   <li>Sends the token to Google's tokeninfo API</li>
     *   <li>Verifies the token is valid and not expired</li>
     *   <li>Verifies the audience (aud) matches our client ID</li>
     *   <li>Returns user information from the token</li>
     * </ul>
     *
     * @param googleIdToken the Google ID token
     * @return Map containing user info (sub, email, name, picture, aud, exp, iat)
     * @throws IllegalArgumentException if token is invalid or audience mismatch
     * @throws RestClientException if Google API is unreachable
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> validateGoogleToken(String googleIdToken) {
        logger.debug("Validating Google ID token with Google's tokeninfo endpoint");

        try {
            // Call Google's tokeninfo endpoint
            String url = GOOGLE_TOKENINFO_URL + googleIdToken;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                logger.error("Google tokeninfo API returned null response");
                throw new IllegalArgumentException("Failed to validate Google ID token");
            }

            // Check for error response
            if (response.containsKey("error") || response.containsKey("error_description")) {
                String error = (String) response.get("error_description");
                logger.warn("Google token validation failed: {}", error);
                throw new IllegalArgumentException("Invalid Google ID token: " + error);
            }

            // Verify audience (aud) matches our client ID
            String audience = (String) response.get("aud");
            if (!googleClientId.equals(audience)) {
                logger.warn("Token audience mismatch. Expected: {}, Got: {}", googleClientId, audience);
                throw new IllegalArgumentException(
                        "Invalid token audience. Token not intended for this application."
                );
            }

            // Verify required fields are present
            if (!response.containsKey("sub") || !response.containsKey("email")) {
                logger.error("Google token missing required fields (sub, email)");
                throw new IllegalArgumentException("Google token missing required user information");
            }

            logger.debug("Google ID token validated successfully");
            return response;

        } catch (RestClientException e) {
            logger.error("Failed to call Google tokeninfo API", e);
            throw new RestClientException("Failed to validate Google token. Please try again.", e);
        }
    }
}
