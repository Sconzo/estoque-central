package com.estoquecentral.auth.application;

import com.estoquecentral.auth.domain.Role;
import com.estoquecentral.auth.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JwtService - Service for generating and validating JWT tokens
 *
 * <p>This service uses JJWT library to create and validate JWT tokens.
 * Tokens are signed with HS256 algorithm using a secret key from configuration.
 *
 * <p><strong>JWT Structure:</strong>
 * <pre>{@code
 * {
 *   "sub": "user-uuid",
 *   "tenantId": "tenant-uuid",
 *   "email": "user@example.com",
 *   "profileId": "profile-uuid",
 *   "roles": ["ADMIN", "GERENTE"],
 *   "iat": 1699900000,
 *   "exp": 1699986400
 * }
 * }</pre>
 *
 * @see Usuario
 * @see com.estoquecentral.auth.adapter.in.security.JwtAuthenticationFilter
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private static final long JWT_EXPIRATION_MS = 86400000; // 24 hours

    private final SecretKey signingKey;
    private final UserService userService;

    /**
     * Constructor that initializes signing key from configuration.
     *
     * @param jwtSecret secret key from application.properties (min 256 bits / 32 chars)
     * @param userService service to fetch user roles from profile
     */
    @Autowired
    public JwtService(@Value("${app.jwt.secret}") String jwtSecret, UserService userService) {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 256 bits (32 characters). " +
                    "Please configure app.jwt.secret in application.properties"
            );
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.userService = userService;
        logger.info("JwtService initialized with HS256 signing key (secret length: {} chars, key hash: {})",
                jwtSecret.length(), Integer.toHexString(this.signingKey.hashCode()));
    }

    /**
     * Generates a JWT token for a given user.
     *
     * <p>Token contains:
     * <ul>
     *   <li>sub: User ID (UUID)</li>
     *   <li>tenantId: Tenant ID (UUID)</li>
     *   <li>email: User email</li>
     *   <li>profileId: Profile ID (UUID, nullable)</li>
     *   <li>roles: User roles from profile (array of role names)</li>
     *   <li>iat: Issued at timestamp</li>
     *   <li>exp: Expiration timestamp (iat + 24h)</li>
     * </ul>
     *
     * @param usuario the user to generate token for
     * @return JWT token string
     */
    public String generateToken(Usuario usuario) {
        logger.debug("Generating JWT token for user: {}", usuario.getEmail());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + JWT_EXPIRATION_MS);

        // Get roles from user's profile
        List<Role> roles = userService.getUserRoles(usuario.getId());
        List<String> roleNames = roles.stream()
                .map(Role::getNome)
                .collect(Collectors.toList());

        // If user has no profile, roles will be empty list
        if (roleNames.isEmpty()) {
            logger.debug("User has no profile - generating token with empty roles array");
        }

        String token = Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("tenantId", usuario.getTenantId().toString())
                .claim("email", usuario.getEmail())
                .claim("profileId", usuario.getProfileId() != null ? usuario.getProfileId().toString() : null)
                .claim("roles", roleNames)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        logger.debug("JWT token generated successfully for user: {} with roles: {} (expires at: {})",
                usuario.getEmail(), roleNames, expiration);
        logger.debug("Generated token (first 50 chars): {}", token.substring(0, Math.min(50, token.length())));
        logger.debug("Generated token (last 20 chars): {}", token.substring(Math.max(0, token.length() - 20)));

        return token;
    }

    /**
     * Validates a JWT token and extracts claims.
     *
     * <p>Validation includes:
     * <ul>
     *   <li>Signature verification</li>
     *   <li>Expiration check</li>
     *   <li>Claims structure</li>
     * </ul>
     *
     * @param token the JWT token to validate
     * @return Claims object containing token payload
     * @throws io.jsonwebtoken.JwtException if token is invalid, expired, or malformed
     */
    public Claims validateToken(String token) {
        logger.debug("Validating token (first 50 chars): {}", token.substring(0, Math.min(50, token.length())));
        logger.debug("Validating token (last 20 chars): {}", token.substring(Math.max(0, token.length() - 20)));
        logger.debug("Token length: {}, Key hash: {}", token.length(), Integer.toHexString(this.signingKey.hashCode()));

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            logger.trace("JWT token validated successfully for user: {}", claims.getSubject());
            return claims;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (io.jsonwebtoken.JwtException e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts user ID from a JWT token.
     *
     * @param token the JWT token
     * @return User ID (UUID)
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extracts tenant ID from a JWT token.
     *
     * @param token the JWT token
     * @return Tenant ID (UUID)
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public UUID getTenantIdFromToken(String token) {
        Claims claims = validateToken(token);
        String tenantId = claims.get("tenantId", String.class);
        return UUID.fromString(tenantId);
    }

    /**
     * Extracts email from a JWT token.
     *
     * @param token the JWT token
     * @return User email
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Extracts profile ID from a JWT token.
     *
     * @param token the JWT token
     * @return Profile ID (UUID), or null if user has no profile
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public UUID getProfileIdFromToken(String token) {
        Claims claims = validateToken(token);
        String profileId = claims.get("profileId", String.class);
        return profileId != null ? UUID.fromString(profileId) : null;
    }

    /**
     * Extracts roles from a JWT token.
     *
     * @param token the JWT token
     * @return List of role strings (empty list if user has no profile)
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = validateToken(token);
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    /**
     * Checks if a token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Generates a JWT token for a user in a specific company context (Story 8.1 - AC5).
     *
     * <p>This method is used during self-service company creation to generate a token
     * for the company owner WITHOUT requiring a tenant-specific Usuario object.
     *
     * <p>Token contains:
     * <ul>
     *   <li>sub: User ID (from public.users)</li>
     *   <li>tenantId: Company's tenant ID (UUID)</li>
     *   <li>email: User email</li>
     *   <li>roles: [ADMIN] (company owner always gets ADMIN role)</li>
     *   <li>iat: Issued at timestamp</li>
     *   <li>exp: Expiration timestamp (iat + 24h)</li>
     * </ul>
     *
     * @param userId User ID from public.users table
     * @param tenantId Company's tenant ID
     * @param email User email
     * @return JWT token string
     */
    public String generateCompanyToken(Long userId, UUID tenantId, String email) {
        logger.debug("Generating company JWT token for userId: {} in tenantId: {}", userId, tenantId);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + JWT_EXPIRATION_MS);

        // Company owner always gets ADMIN role (Story 8.1 - AC4, AC5)
        List<String> roles = List.of("ADMIN");

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tenantId", tenantId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        logger.debug("Company JWT token generated successfully for user: {} with roles: {} (expires at: {})",
                email, roles, expiration);

        return token;
    }
}
