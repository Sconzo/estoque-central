package com.estoquecentral.auth.adapter.in;

import com.estoquecentral.auth.adapter.in.dto.ProfileCreateRequest;
import com.estoquecentral.auth.adapter.in.dto.ProfileDTO;
import com.estoquecentral.auth.adapter.in.dto.RoleDTO;
import com.estoquecentral.auth.adapter.in.dto.AssignProfileRequest;
import com.estoquecentral.auth.application.ProfileService;
import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Profile;
import com.estoquecentral.auth.domain.Role;
import com.estoquecentral.shared.tenant.TenantContext;
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
 * ProfileController - REST API for profile management
 *
 * <p><strong>Security:</strong> All endpoints require ADMIN role
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>GET /api/profiles - List profiles for current tenant</li>
 *   <li>GET /api/profiles/{id} - Get profile by ID</li>
 *   <li>GET /api/profiles/{id}/roles - Get roles for a profile</li>
 *   <li>POST /api/profiles - Create new profile</li>
 *   <li>PUT /api/profiles/{id} - Update profile</li>
 *   <li>PUT /api/profiles/{id}/roles - Update profile roles</li>
 *   <li>DELETE /api/profiles/{id} - Deactivate profile</li>
 * </ul>
 *
 * @see ProfileService
 */
@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profiles", description = "Profile management API (RBAC)")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    /**
     * Lists all active profiles for the current tenant.
     *
     * @return list of profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List profiles", description = "Lists all active profiles for the current tenant")
    public ResponseEntity<List<ProfileDTO>> listar() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        List<Profile> profiles = profileService.listByTenant(tenantId);

        List<ProfileDTO> profileDTOs = profiles.stream()
                .map(ProfileDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(profileDTOs);
    }

    /**
     * Gets a profile by ID with roles.
     *
     * @param id the profile ID
     * @return the profile with roles
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get profile by ID", description = "Retrieves a profile with its roles")
    public ResponseEntity<ProfileDTO> getById(@PathVariable UUID id) {
        Profile profile = profileService.getById(id);
        List<Role> roles = profileService.getRolesByProfile(id);
        List<RoleDTO> roleDTOs = roles.stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());

        ProfileDTO dto = ProfileDTO.fromEntityWithRoles(profile, roleDTOs);
        return ResponseEntity.ok(dto);
    }

    /**
     * Gets all roles for a profile.
     *
     * @param id the profile ID
     * @return list of roles
     */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get profile roles", description = "Lists all roles assigned to a profile")
    public ResponseEntity<List<RoleDTO>> getRoles(@PathVariable UUID id) {
        List<Role> roles = profileService.getRolesByProfile(id);
        List<RoleDTO> roleDTOs = roles.stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roleDTOs);
    }

    /**
     * Creates a new profile.
     *
     * @param request the profile creation request
     * @return the created profile
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create profile", description = "Creates a new profile with roles")
    public ResponseEntity<ProfileDTO> criar(@Valid @RequestBody ProfileCreateRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Profile profile = profileService.create(
                tenantId,
                request.getNome(),
                request.getDescricao(),
                request.getRoleIds()
        );

        // Load roles for response
        List<Role> roles = profileService.getRolesByProfile(profile.getId());
        List<RoleDTO> roleDTOs = roles.stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());

        ProfileDTO dto = ProfileDTO.fromEntityWithRoles(profile, roleDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Updates a profile's metadata.
     *
     * @param id      the profile ID
     * @param request the update request
     * @return the updated profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update profile", description = "Updates profile name and/or description")
    public ResponseEntity<ProfileDTO> atualizar(
            @PathVariable UUID id,
            @RequestBody ProfileCreateRequest request) {
        Profile profile = profileService.update(id, request.getNome(), request.getDescricao());
        return ResponseEntity.ok(ProfileDTO.fromEntity(profile));
    }

    /**
     * Updates profile roles.
     *
     * @param id      the profile ID
     * @param request the role IDs
     * @return no content
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update profile roles", description = "Replaces all roles for a profile")
    public ResponseEntity<Void> atualizarRoles(
            @PathVariable UUID id,
            @Valid @RequestBody ProfileCreateRequest request) {
        profileService.updateRoles(id, request.getRoleIds());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivates a profile (soft delete).
     *
     * @param id the profile ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate profile", description = "Soft deletes a profile (sets ativo=false)")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        profileService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assigns a profile to a user.
     *
     * @param userId  the user ID
     * @param request the profile assignment request
     * @return no content
     */
    @PutMapping("/users/{userId}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign profile to user", description = "Assigns a profile to a user")
    public ResponseEntity<Void> assignProfileToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignProfileRequest request) {
        userService.assignProfile(userId, request.getProfileId());
        return ResponseEntity.noContent().build();
    }
}
