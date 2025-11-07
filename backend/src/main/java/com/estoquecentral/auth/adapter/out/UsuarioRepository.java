package com.estoquecentral.auth.adapter.out;

import com.estoquecentral.auth.domain.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * UsuarioRepository - Data access for Usuario entities
 *
 * <p>This repository operates on TENANT schemas (not public schema).
 * Each tenant has its own `usuarios` table with isolated user data.
 *
 * <p>The correct schema is automatically selected via TenantContext
 * and TenantRoutingDataSource (integrated with JwtAuthenticationFilter).
 *
 * @see Usuario
 * @see com.estoquecentral.shared.tenant.TenantContext
 * @see com.estoquecentral.shared.tenant.TenantRoutingDataSource
 */
@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, UUID> {

    /**
     * Finds a user by Google ID (sub claim from Google OAuth).
     *
     * <p>Google ID is unique per tenant.
     *
     * @param googleId the Google user ID
     * @return Optional containing the user if found in current tenant's schema
     */
    Optional<Usuario> findByGoogleId(String googleId);

    /**
     * Finds a user by email.
     *
     * <p>Email is unique per tenant.
     *
     * @param email the user email
     * @return Optional containing the user if found in current tenant's schema
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Checks if a user exists by Google ID.
     *
     * @param googleId the Google user ID
     * @return true if user exists in current tenant's schema, false otherwise
     */
    boolean existsByGoogleId(String googleId);

    /**
     * Checks if a user exists by email.
     *
     * @param email the user email
     * @return true if user exists in current tenant's schema, false otherwise
     */
    boolean existsByEmail(String email);
}
