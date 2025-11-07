package com.estoquecentral.auth.application;

import com.estoquecentral.auth.adapter.out.RoleRepository;
import com.estoquecentral.auth.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * RoleService - Application service for role management
 *
 * <p>Handles role lifecycle operations:
 * <ul>
 *   <li>List all roles (optionally filtered by category)</li>
 *   <li>Create new roles (admin only)</li>
 *   <li>Update role metadata</li>
 *   <li>Deactivate roles (soft delete)</li>
 * </ul>
 *
 * <p><strong>Security:</strong> All role management operations require ADMIN role.
 *
 * @see Role
 * @see RoleRepository
 */
@Service
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Lists all active roles.
     *
     * @return list of active roles
     */
    public List<Role> listAll() {
        logger.debug("Listing all active roles");
        return roleRepository.findByAtivoTrue();
    }

    /**
     * Lists roles by category.
     *
     * @param categoria the category (GESTAO, OPERACIONAL, SISTEMA)
     * @return list of roles in that category
     */
    public List<Role> listByCategoria(String categoria) {
        logger.debug("Listing roles by categoria: {}", categoria);
        return roleRepository.findByCategoria(categoria);
    }

    /**
     * Finds a role by ID.
     *
     * @param id the role ID
     * @return the Role
     * @throws IllegalArgumentException if role not found
     */
    public Role getById(UUID id) {
        logger.debug("Finding role by ID: {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));
    }

    /**
     * Finds a role by name.
     *
     * @param nome the role name (e.g., "ADMIN")
     * @return the Role
     * @throws IllegalArgumentException if role not found
     */
    public Role getByNome(String nome) {
        logger.debug("Finding role by nome: {}", nome);
        return roleRepository.findByNome(nome)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + nome));
    }

    /**
     * Creates a new role.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param nome       role name (e.g., "CUSTOM_ROLE")
     * @param descricao  human-readable description
     * @param categoria  role category (GESTAO, OPERACIONAL, SISTEMA)
     * @return the created Role
     * @throws IllegalArgumentException if role name already exists
     */
    @Transactional
    public Role create(String nome, String descricao, String categoria) {
        logger.info("Creating new role: {}", nome);

        // Check if role already exists
        if (roleRepository.findByNome(nome).isPresent()) {
            throw new IllegalArgumentException("Role already exists: " + nome);
        }

        // Validate categoria
        if (!List.of("GESTAO", "OPERACIONAL", "SISTEMA").contains(categoria)) {
            throw new IllegalArgumentException("Invalid categoria: " + categoria);
        }

        Role role = new Role(UUID.randomUUID(), nome, descricao, categoria);
        role = roleRepository.save(role);

        logger.info("Role created successfully: {}", role);
        return role;
    }

    /**
     * Updates a role's metadata.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param id         role ID
     * @param descricao  new description (optional)
     * @param categoria  new category (optional)
     * @return the updated Role
     * @throws IllegalArgumentException if role not found
     */
    @Transactional
    public Role update(UUID id, String descricao, String categoria) {
        logger.info("Updating role: {}", id);

        Role role = getById(id);
        role.update(descricao, categoria);
        role = roleRepository.save(role);

        logger.info("Role updated successfully: {}", role);
        return role;
    }

    /**
     * Deactivates a role (soft delete).
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param id the role ID
     * @throws IllegalArgumentException if role not found
     */
    @Transactional
    public void deactivate(UUID id) {
        logger.info("Deactivating role: {}", id);

        Role role = getById(id);
        role.deactivate();
        roleRepository.save(role);

        logger.info("Role deactivated successfully: {}", id);
    }

    /**
     * Activates a role.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param id the role ID
     * @throws IllegalArgumentException if role not found
     */
    @Transactional
    public void activate(UUID id) {
        logger.info("Activating role: {}", id);

        Role role = getById(id);
        role.activate();
        roleRepository.save(role);

        logger.info("Role activated successfully: {}", id);
    }
}
