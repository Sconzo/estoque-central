package com.estoquecentral.auth.application;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import com.estoquecentral.auth.adapter.out.UsuarioRepository;
import com.estoquecentral.auth.domain.Role;
import com.estoquecentral.auth.domain.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserService - Application service for user management
 *
 * <p>Handles user lifecycle operations:
 * <ul>
 *   <li>Find or create user from Google OAuth data</li>
 *   <li>Update existing user information</li>
 *   <li>Retrieve user by ID</li>
 *   <li>Assign profile to user (RBAC)</li>
 *   <li>Get user roles (via profile)</li>
 * </ul>
 *
 * <p><strong>Multi-tenancy:</strong> This service operates on tenant-specific
 * schemas. The TenantContext must be set before calling these methods
 * (automatically done by JwtAuthenticationFilter).
 *
 * @see Usuario
 * @see UsuarioRepository
 * @see GoogleAuthService
 * @see ProfileService
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final ProfileService profileService;

    @Autowired
    public UserService(
            UsuarioRepository usuarioRepository,
            TenantRepository tenantRepository,
            ProfileService profileService) {
        this.usuarioRepository = usuarioRepository;
        this.tenantRepository = tenantRepository;
        this.profileService = profileService;
    }

    /**
     * Finds or creates a user from Google OAuth data.
     *
     * <p>If user with googleId exists:
     * <ul>
     *   <li>Update nome and pictureUrl (may have changed in Google)</li>
     *   <li>Return existing user</li>
     * </ul>
     *
     * <p>If user doesn't exist:
     * <ul>
     *   <li>Create new usuario WITHOUT profile (admin must assign later)</li>
     *   <li>Return new user</li>
     * </ul>
     *
     * <p><strong>Important:</strong> TenantContext must be set before calling,
     * as this determines which schema the user is created/updated in.
     *
     * @param googleId   Google user ID (sub claim)
     * @param email      user email
     * @param nome       user name
     * @param pictureUrl Google profile picture URL
     * @param tenantId   tenant ID this user belongs to
     * @return the created or updated Usuario
     * @throws IllegalArgumentException if tenantId is invalid
     */
    @Transactional
    public Usuario findOrCreateUser(String googleId, String email, String nome,
                                    String pictureUrl, UUID tenantId) {
        logger.debug("Finding or creating user: googleId={}, email={}, tenantId={}",
                googleId, email, tenantId);

        // Validate tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }

        // Check if user already exists by googleId
        Optional<Usuario> existingUser = usuarioRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            // User exists - update information
            Usuario usuario = existingUser.get();
            logger.info("User found by googleId: {} - updating information", googleId);

            usuario.updateFromGoogle(nome, pictureUrl);
            usuario = usuarioRepository.save(usuario);

            logger.info("User updated successfully: {}", usuario);
            return usuario;
        }

        // User doesn't exist - create new
        logger.info("User not found by googleId: {} - creating new user", googleId);

        Usuario newUsuario = new Usuario(
                UUID.randomUUID(),
                googleId,
                email,
                nome,
                pictureUrl,
                tenantId
        );

        newUsuario = usuarioRepository.save(newUsuario);
        logger.info("New user created successfully (no profile assigned): {}", newUsuario);

        return newUsuario;
    }

    /**
     * Lists all users in the current tenant.
     *
     * @return list of all users ordered by name
     */
    public List<Usuario> listAll() {
        logger.debug("Listing all users");
        return usuarioRepository.findAllOrderByNome();
    }

    /**
     * Lists all active users in the current tenant.
     *
     * @return list of active users ordered by name
     */
    public List<Usuario> listActive() {
        logger.debug("Listing active users");
        return usuarioRepository.findByAtivoTrue();
    }

    /**
     * Retrieves a user by ID.
     *
     * <p><strong>Important:</strong> TenantContext must be set before calling,
     * as this determines which schema to query.
     *
     * @param userId the user ID
     * @return the Usuario
     * @throws IllegalArgumentException if user not found
     */
    public Usuario getUserById(UUID userId) {
        logger.debug("Retrieving user by ID: {}", userId);

        return usuarioRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", userId);
                    return new IllegalArgumentException("User not found: " + userId);
                });
    }

    /**
     * Retrieves a user by Google ID.
     *
     * @param googleId the Google user ID
     * @return Optional containing the user if found
     */
    public Optional<Usuario> getUserByGoogleId(String googleId) {
        logger.debug("Retrieving user by googleId: {}", googleId);
        return usuarioRepository.findByGoogleId(googleId);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the user email
     * @return Optional containing the user if found
     */
    public Optional<Usuario> getUserByEmail(String email) {
        logger.debug("Retrieving user by email: {}", email);
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Checks if a user is active.
     *
     * @param userId the user ID
     * @return true if user exists and is active, false otherwise
     */
    public boolean isUserActive(UUID userId) {
        return usuarioRepository.findById(userId)
                .map(Usuario::getAtivo)
                .orElse(false);
    }

    /**
     * Deactivates a user (soft delete).
     *
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public void deactivateUser(UUID userId) {
        logger.info("Deactivating user: {}", userId);

        Usuario usuario = getUserById(userId);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        logger.info("User deactivated successfully: {}", userId);
    }

    /**
     * Activates a user.
     *
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public void activateUser(UUID userId) {
        logger.info("Activating user: {}", userId);

        Usuario usuario = getUserById(userId);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        logger.info("User activated successfully: {}", userId);
    }

    /**
     * Assigns a profile to a user.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param userId    the user ID
     * @param profileId the profile ID to assign
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public void assignProfile(UUID userId, UUID profileId) {
        logger.info("Assigning profile {} to user {}", profileId, userId);

        Usuario usuario = getUserById(userId);
        usuario.assignProfile(profileId);
        usuarioRepository.save(usuario);

        logger.info("Profile assigned successfully");
    }

    /**
     * Gets all roles for a user (via their profile).
     *
     * @param userId the user ID
     * @return list of roles (empty if user has no profile)
     */
    public List<Role> getUserRoles(UUID userId) {
        logger.debug("Getting roles for user: {}", userId);

        Usuario usuario = getUserById(userId);

        if (!usuario.hasProfile()) {
            logger.debug("User has no profile - returning empty roles list");
            return Collections.emptyList();
        }

        // Get roles from profile via ProfileService
        return profileService.getRolesByProfile(usuario.getProfileId());
    }
}
