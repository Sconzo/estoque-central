package com.estoquecentral.auth.adapter.in.security;

import com.estoquecentral.auth.application.JwtService;
import com.estoquecentral.auth.application.PublicUserService;
import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Usuario;
import com.estoquecentral.shared.tenant.TenantContext;
import io.jsonwebtoken.Claims;
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
import java.util.Collections;
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
    private final PublicUserService publicUserService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserService userService, PublicUserService publicUserService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.publicUserService = publicUserService;
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
            Claims claims = jwtService.validateToken(jwt);
            String userIdStr = claims.getSubject();
            String tenantIdStr = claims.get("tenantId", String.class);
            List<String> roles = jwtService.getRolesFromToken(jwt);

            logg.debug("JWT validated for user: {} (tenant: {})", userIdStr, tenantIdStr);

            // Step 4: Parse user ID (now always UUID)
            UUID userId = UUID.fromString(userIdStr);

            // Check if this is a public user (no tenant) or tenant user
            boolean hasValidTenant = (tenantIdStr != null && !tenantIdStr.equals("null"));

            if (hasValidTenant) {
                // User has a company/tenant
                UUID tenantId = UUID.fromString(tenantIdStr);

                // Set TenantContext (CRITICAL for multi-tenancy)
                TenantContext.setTenantId(tenantId.toString());
                logg.trace("TenantContext set to: {}", tenantId);

                // Load user from public.users and validate
                com.estoquecentral.auth.domain.User publicUser = publicUserService.getUserById(userId);

                if (!publicUser.getAtivo()) {
                    logg.warn("Inactive user attempted to access tenant: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Create Spring Security Authentication with roles from JWT
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userIdStr,         // Principal (user ID as string)
                                null,              // Credentials (not needed for JWT)
                                authorities        // Authorities (roles)
                        );

                authentication.setDetails(claims);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logg.debug("SecurityContext populated for user: {} in tenant: {} (roles: {})", userIdStr, tenantId, roles);

            } else {
                // Public user without company/tenant (new user flow)
                logg.debug("Public user (no tenant): userId={}", userId);

                // Load user from public.users
                com.estoquecentral.auth.domain.User publicUser = publicUserService.getUserById(userId);

                // Check if user is active
                if (!publicUser.getAtivo()) {
                    logg.warn("Inactive public user attempted to login: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Create Spring Security Authentication with empty roles
                List<SimpleGrantedAuthority> authorities = Collections.emptyList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId.toString(), // Principal (user ID)
                                null,              // Credentials (not needed for JWT)
                                authorities        // Authorities (empty - no roles without company)
                        );

                authentication.setDetails(claims);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logg.debug("SecurityContext populated for public user: {} (no roles, no tenant)", userId);
            }

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
