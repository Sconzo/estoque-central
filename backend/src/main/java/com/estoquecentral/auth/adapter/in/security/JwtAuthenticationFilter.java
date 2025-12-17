package com.estoquecentral.auth.adapter.in.security;

import com.estoquecentral.auth.application.JwtService;
import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Usuario;
import com.estoquecentral.shared.tenant.TenantContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JwtAuthenticationFilter - Validates JWT tokens and populates SecurityContext
 *
 * <p>This filter runs for EVERY HTTP request and:
 * <ol>
 *   <li>Extracts JWT token from Authorization header</li>
 *   <li>Validates token (signature, expiration)</li>
 *   <li>Sets TenantContext with tenant ID from token (integrates with Story 1.3)</li>
 *   <li>Loads user from database</li>
 *   <li>Populates Spring SecurityContext with authenticated user</li>
 * </ol>
 *
 * <p><strong>Integration with Multi-Tenancy:</strong>
 * This filter extracts tenantId from JWT and sets it in TenantContext,
 * allowing TenantRoutingDataSource to route database queries to the
 * correct tenant schema.
 *
 * <p><strong>Filter Order:</strong>
 * This filter runs BEFORE UsernamePasswordAuthenticationFilter in the chain.
 *
 * @see JwtService
 * @see UserService
 * @see TenantContext
 * @see com.estoquecentral.shared.tenant.TenantRoutingDataSource
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logg = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Filters incoming HTTP requests to validate JWT tokens.
     *
     * <p>Flow:
     * <ol>
     *   <li>Check if Authorization header is present</li>
     *   <li>Extract JWT token (remove "Bearer " prefix)</li>
     *   <li>Validate token</li>
     *   <li>Extract userId and tenantId from token</li>
     *   <li>Set TenantContext (for multi-tenancy)</li>
     *   <li>Load user from database</li>
     *   <li>Create Authentication object</li>
     *   <li>Set SecurityContext</li>
     * </ol>
     *
     * <p>If any step fails, the request continues WITHOUT authentication
     * (SecurityContext remains empty). Protected endpoints will return 401.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract Authorization header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // No JWT token - continue without authentication
            logg.trace("No Authorization header found for URI: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Step 2: Extract JWT token
            String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();

            // Step 3: Validate token and extract claims
            UUID userId = jwtService.getUserIdFromToken(jwt);
            UUID tenantId = jwtService.getTenantIdFromToken(jwt);
            List<String> roles = jwtService.getRolesFromToken(jwt);

            logg.debug("JWT validated for user: {} (tenant: {})", userId, tenantId);

            // Step 4: Set TenantContext (CRITICAL for multi-tenancy)
            TenantContext.setTenantId(tenantId.toString());
            logg.trace("TenantContext set to: {}", tenantId);

            // Step 5: Load user from database (in tenant schema)
            Usuario usuario = userService.getUserById(userId);

            // Check if user is active
            if (!usuario.getAtivo()) {
                logg.warn("Inactive user attempted to login: {}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // Step 6: Create Spring Security Authentication
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(), // Principal (user ID)
                            null,              // Credentials (not needed for JWT)
                            authorities        // Authorities (roles)
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Step 7: Set SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logg.debug("SecurityContext populated for user: {} (roles: {})", userId, roles);

        } catch (JwtException e) {
            logg.warn("JWT validation failed: {}", e.getMessage());
            // Continue without authentication - protected endpoints will return 401

        } catch (IllegalArgumentException e) {
            logg.warn("User not found or invalid: {}", e.getMessage());
            // Continue without authentication

        } catch (Exception e) {
            logg.error("Unexpected error in JWT filter", e);
            // Continue without authentication
        }

        // Continue filter chain
        filterChain.doFilter(request, response);

        // NOTE: TenantContext.clear() is called by TenantInterceptor.afterCompletion()
        // We don't clear it here to allow access during request processing
    }
}
