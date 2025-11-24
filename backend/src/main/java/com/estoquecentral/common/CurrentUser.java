package com.estoquecentral.common;

import com.estoquecentral.shared.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

/**
 * CurrentUser - Represents the currently authenticated user
 * 
 * This class wraps Spring Security Authentication and provides convenient
 * access to user ID, tenant ID, and roles for use in controllers.
 */
public class CurrentUser {
    
    private final String principal;
    private final Collection<? extends GrantedAuthority> authorities;
    
    public CurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication cannot be null");
        }
        this.principal = authentication.getPrincipal().toString();
        this.authorities = authentication.getAuthorities();
    }
    
    /**
     * Gets the user ID
     * @return user ID as UUID
     */
    public UUID getUserId() {
        return UUID.fromString(principal);
    }
    
    /**
     * Gets the tenant ID from TenantContext
     * @return tenant ID as UUID
     */
    public UUID getTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("TenantContext not set");
        }
        return UUID.fromString(tenantId);
    }
    
    /**
     * Gets the user's authorities/roles
     * @return collection of granted authorities
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    /**
     * Checks if user has a specific role
     * @param role role name (without ROLE_ prefix)
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        String roleWithPrefix = "ROLE_" + role;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }
}
