package com.estoquecentral.auth.adapter.in;

import com.estoquecentral.auth.adapter.in.dto.RoleCreateRequest;
import com.estoquecentral.auth.adapter.in.dto.RoleDTO;
import com.estoquecentral.auth.application.RoleService;
import com.estoquecentral.auth.domain.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RoleController - REST API for role management
 *
 * <p><strong>Security:</strong> All endpoints require ADMIN role
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>GET /api/roles - List all roles</li>
 *   <li>GET /api/roles/{id} - Get role by ID</li>
 *   <li>POST /api/roles - Create new role</li>
 *   <li>PUT /api/roles/{id} - Update role</li>
 *   <li>DELETE /api/roles/{id} - Deactivate role</li>
 * </ul>
 *
 * @see RoleService
 */
@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Role management API (RBAC)")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Lists all active roles.
     *
     * @param categoria optional filter by category (GESTAO, OPERACIONAL, SISTEMA)
     * @return list of roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all roles", description = "Lists all active roles, optionally filtered by category")
    public ResponseEntity<List<RoleDTO>> listar(@RequestParam(required = false) String categoria) {
        List<Role> roles;

        if (categoria != null && !categoria.isBlank()) {
            roles = roleService.listByCategoria(categoria);
        } else {
            roles = roleService.listAll();
        }

        List<RoleDTO> roleDTOs = roles.stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roleDTOs);
    }

    /**
     * Gets a role by ID.
     *
     * @param id the role ID
     * @return the role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get role by ID", description = "Retrieves a specific role by its ID")
    public ResponseEntity<RoleDTO> getById(@PathVariable UUID id) {
        Role role = roleService.getById(id);
        return ResponseEntity.ok(RoleDTO.fromEntity(role));
    }

    /**
     * Creates a new role.
     *
     * @param request the role creation request
     * @return the created role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new role", description = "Creates a new custom role (admin only)")
    public ResponseEntity<RoleDTO> criar(@Valid @RequestBody RoleCreateRequest request) {
        Role role = roleService.create(
                request.getNome(),
                request.getDescricao(),
                request.getCategoria()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(RoleDTO.fromEntity(role));
    }

    /**
     * Updates a role.
     *
     * @param id      the role ID
     * @param request the update request
     * @return the updated role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update role", description = "Updates role description and/or category")
    public ResponseEntity<RoleDTO> atualizar(
            @PathVariable UUID id,
            @RequestBody RoleCreateRequest request) {
        Role role = roleService.update(id, request.getDescricao(), request.getCategoria());
        return ResponseEntity.ok(RoleDTO.fromEntity(role));
    }

    /**
     * Deactivates a role (soft delete).
     *
     * @param id the role ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate role", description = "Soft deletes a role (sets ativo=false)")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        roleService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
