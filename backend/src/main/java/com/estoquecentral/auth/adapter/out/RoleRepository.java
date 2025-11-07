package com.estoquecentral.auth.adapter.out;

import com.estoquecentral.auth.domain.Role;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RoleRepository - Data access for Role entities
 *
 * <p>This repository operates on the PUBLIC schema.
 * Roles are global (shared across all tenants).
 *
 * @see Role
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, UUID> {

    /**
     * Finds all active roles.
     *
     * @return list of active roles
     */
    @Query("SELECT * FROM public.roles WHERE ativo = true ORDER BY nome")
    List<Role> findByAtivoTrue();

    /**
     * Finds a role by name.
     *
     * @param nome the role name (e.g., "ADMIN")
     * @return Optional containing the role if found
     */
    @Query("SELECT * FROM public.roles WHERE nome = :nome")
    Optional<Role> findByNome(@Param("nome") String nome);

    /**
     * Finds roles by category.
     *
     * @param categoria the category (GESTAO, OPERACIONAL, SISTEMA)
     * @return list of roles in that category
     */
    @Query("SELECT * FROM public.roles WHERE categoria = :categoria AND ativo = true ORDER BY nome")
    List<Role> findByCategoria(@Param("categoria") String categoria);

    /**
     * Finds all roles (active and inactive).
     *
     * @return list of all roles
     */
    @Query("SELECT * FROM public.roles ORDER BY nome")
    List<Role> findAllRoles();
}
