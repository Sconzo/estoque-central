package com.estoquecentral.auth.adapter.in;

import com.estoquecentral.auth.adapter.in.dto.AssignProfileRequest;
import com.estoquecentral.auth.adapter.in.dto.UserDTO;
import com.estoquecentral.auth.application.ProfileService;
import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Profile;
import com.estoquecentral.auth.domain.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UsuarioController - REST API for user management
 *
 * <p><strong>Security:</strong> All endpoints require ADMIN role
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>GET /api/usuarios - List all users for current tenant</li>
 *   <li>GET /api/usuarios/{id} - Get user by ID</li>
 *   <li>PUT /api/usuarios/{id}/status - Activate/deactivate user</li>
 *   <li>PUT /api/usuarios/{id}/profile - Assign profile to user</li>
 * </ul>
 *
 * @see UserService
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "User management API")
public class UsuarioController {

    private final UserService userService;
    private final ProfileService profileService;

    @Autowired
    public UsuarioController(UserService userService, ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    /**
     * Lists all users for the current tenant.
     *
     * @return list of users with profile info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users", description = "Lists all users for the current tenant")
    public ResponseEntity<List<UserDTO>> listar() {
        List<Usuario> usuarios = userService.listAll();

        // Build a map of profileId -> profileNome for efficiency
        Map<UUID, String> profileNames = new HashMap<>();
        List<Profile> profiles = profileService.listActive();
        for (Profile profile : profiles) {
            profileNames.put(profile.getId(), profile.getNome());
        }

        List<UserDTO> userDTOs = usuarios.stream()
                .map(u -> UserDTO.fromEntityWithProfile(u,
                        u.getProfileId() != null ? profileNames.get(u.getProfileId()) : null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Gets a user by ID.
     *
     * @param id the user ID
     * @return the user with profile info
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a user with profile information")
    public ResponseEntity<UserDTO> getById(@PathVariable UUID id) {
        Usuario usuario = userService.getUserById(id);

        String profileNome = null;
        if (usuario.getProfileId() != null) {
            try {
                Profile profile = profileService.getById(usuario.getProfileId());
                profileNome = profile.getNome();
            } catch (Exception e) {
                // Profile may have been deleted
            }
        }

        return ResponseEntity.ok(UserDTO.fromEntityWithProfile(usuario, profileNome));
    }

    /**
     * Activates or deactivates a user.
     *
     * @param id     the user ID
     * @param ativo  the new status
     * @return no content
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Activates or deactivates a user")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean ativo) {
        if (ativo) {
            userService.activateUser(id);
        } else {
            userService.deactivateUser(id);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Assigns a profile to a user.
     *
     * @param id      the user ID
     * @param request the profile assignment request
     * @return no content
     */
    @PutMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign profile to user", description = "Assigns a profile to a user")
    public ResponseEntity<Void> assignProfile(
            @PathVariable UUID id,
            @Valid @RequestBody AssignProfileRequest request) {
        userService.assignProfile(id, request.getProfileId());
        return ResponseEntity.noContent().build();
    }
}
