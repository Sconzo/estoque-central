package com.estoquecentral.integration;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import com.estoquecentral.auth.application.JwtService;
import com.estoquecentral.auth.application.ProfileService;
import com.estoquecentral.auth.application.RoleService;
import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Profile;
import com.estoquecentral.auth.domain.Role;
import com.estoquecentral.auth.domain.Tenant;
import com.estoquecentral.auth.domain.Usuario;
import com.estoquecentral.shared.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End Integration Test for complete RBAC flow
 *
 * <p>This test validates the entire RBAC system from start to finish:
 * <ol>
 *   <li>Create tenant</li>
 *   <li>Create roles (using default migrations)</li>
 *   <li>Create profile with multiple roles</li>
 *   <li>Create user and assign profile</li>
 *   <li>Generate JWT with roles</li>
 *   <li>Validate JWT contains correct roles</li>
 *   <li>Verify user role resolution</li>
 *   <li>Test profile updates</li>
 * </ol>
 *
 * <p>This test uses a real PostgreSQL database via Testcontainers and
 * validates the complete integration of all RBAC components.
 */
@SpringBootTest
@Transactional
@DisplayName("RBAC End-to-End Integration Test")
class RBACEndToEndTest extends BaseIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Complete RBAC flow: Tenant → Profile → User → JWT → Roles")
    void completeRBACFlow() {
        // ==========================================
        // STEP 1: Create Tenant
        // ==========================================
        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                "test-tenant-" + System.currentTimeMillis(),
                "Test Tenant LTDA",
                "@POLY"
        );
        tenant = tenantRepository.save(tenant);
        assertThat(tenant.getId()).isNotNull();

        // Set tenant context for subsequent operations
        TenantContext.setTenantId(tenant.getId().toString());

        // ==========================================
        // STEP 2: Get Default Roles from Migration
        // ==========================================
        Role adminRole = roleService.getByNome("ADMIN");
        Role gerenteRole = roleService.getByNome("GERENTE");
        Role vendedorRole = roleService.getByNome("VENDEDOR");

        assertThat(adminRole).isNotNull();
        assertThat(gerenteRole).isNotNull();
        assertThat(vendedorRole).isNotNull();

        // ==========================================
        // STEP 3: Create Profile with Multiple Roles
        // ==========================================
        List<UUID> roleIds = List.of(
                adminRole.getId(),
                gerenteRole.getId()
        );

        Profile adminProfile = profileService.create(
                "Administrador Completo",
                "Perfil com acesso administrativo e gerencial",
                roleIds
        );

        assertThat(adminProfile).isNotNull();
        assertThat(adminProfile.getId()).isNotNull();
        assertThat(adminProfile.getNome()).isEqualTo("Administrador Completo");

        // Verify profile has correct roles
        List<Role> profileRoles = profileService.getRolesByProfile(adminProfile.getId());
        assertThat(profileRoles).hasSize(2);
        assertThat(profileRoles).extracting(Role::getNome)
                .containsExactlyInAnyOrder("ADMIN", "GERENTE");

        // ==========================================
        // STEP 4: Create User WITHOUT Profile
        // ==========================================
        Usuario usuario = userService.findOrCreateUser(
                "google-id-12345",
                "admin@testcompany.com",
                "Admin User",
                "https://example.com/avatar.jpg",
                tenant.getId()
        );

        assertThat(usuario).isNotNull();
        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.hasProfile()).isFalse();
        assertThat(usuario.getAtivo()).isTrue();

        // Verify user has no roles initially
        List<Role> userRolesBeforeProfile = userService.getUserRoles(usuario.getId());
        assertThat(userRolesBeforeProfile).isEmpty();

        // ==========================================
        // STEP 5: Assign Profile to User
        // ==========================================
        userService.assignProfile(usuario.getId(), adminProfile.getId());

        // Reload user to verify profile assignment
        Usuario updatedUsuario = userService.getUserById(usuario.getId());
        assertThat(updatedUsuario.hasProfile()).isTrue();
        assertThat(updatedUsuario.getProfileId()).isEqualTo(adminProfile.getId());

        // ==========================================
        // STEP 6: Verify User Has Roles from Profile
        // ==========================================
        List<Role> userRoles = userService.getUserRoles(usuario.getId());
        assertThat(userRoles).hasSize(2);
        assertThat(userRoles).extracting(Role::getNome)
                .containsExactlyInAnyOrder("ADMIN", "GERENTE");

        // ==========================================
        // STEP 7: Generate JWT with Roles
        // ==========================================
        String jwt = jwtService.generateToken(updatedUsuario);
        assertThat(jwt).isNotBlank();

        // ==========================================
        // STEP 8: Validate JWT Contains Correct Data
        // ==========================================
        Claims claims = jwtService.validateToken(jwt);
        assertThat(claims).isNotNull();

        // Verify JWT claims
        assertThat(claims.getSubject()).isEqualTo(usuario.getId().toString());
        assertThat(claims.get("tenantId", String.class)).isEqualTo(tenant.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo("admin@testcompany.com");
        assertThat(claims.get("profileId", String.class)).isEqualTo(adminProfile.getId().toString());

        // Verify JWT contains roles
        @SuppressWarnings("unchecked")
        List<String> jwtRoles = claims.get("roles", List.class);
        assertThat(jwtRoles).hasSize(2);
        assertThat(jwtRoles).containsExactlyInAnyOrder("ADMIN", "GERENTE");

        // ==========================================
        // STEP 9: Extract Roles from JWT
        // ==========================================
        List<String> extractedRoles = jwtService.getRolesFromToken(jwt);
        assertThat(extractedRoles).hasSize(2);
        assertThat(extractedRoles).containsExactlyInAnyOrder("ADMIN", "GERENTE");

        // ==========================================
        // STEP 10: Update Profile Roles
        // ==========================================
        List<UUID> newRoleIds = List.of(
                adminRole.getId(),
                vendedorRole.getId()
        );
        profileService.updateRoles(adminProfile.getId(), newRoleIds);

        // Verify user now has updated roles
        List<Role> updatedUserRoles = userService.getUserRoles(usuario.getId());
        assertThat(updatedUserRoles).hasSize(2);
        assertThat(updatedUserRoles).extracting(Role::getNome)
                .containsExactlyInAnyOrder("ADMIN", "VENDEDOR");

        // ==========================================
        // STEP 11: Generate New JWT with Updated Roles
        // ==========================================
        String newJwt = jwtService.generateToken(updatedUsuario);
        List<String> newJwtRoles = jwtService.getRolesFromToken(newJwt);
        assertThat(newJwtRoles).hasSize(2);
        assertThat(newJwtRoles).containsExactlyInAnyOrder("ADMIN", "VENDEDOR");

        // ==========================================
        // STEP 12: Test User Deactivation
        // ==========================================
        userService.deactivateUser(usuario.getId());
        Usuario deactivatedUser = userService.getUserById(usuario.getId());
        assertThat(deactivatedUser.getAtivo()).isFalse();

        // ==========================================
        // STEP 13: Test Profile Deactivation
        // ==========================================
        profileService.deactivate(adminProfile.getId());
        Profile deactivatedProfile = profileService.getById(adminProfile.getId());
        assertThat(deactivatedProfile.getAtivo()).isFalse();
    }

    @Test
    @DisplayName("User without profile should have empty roles in JWT")
    void userWithoutProfileShouldHaveEmptyRolesInJWT() {
        // Given
        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                "test-tenant-no-profile",
                "Test Tenant",
                "@POLY"
        );
        tenant = tenantRepository.save(tenant);
        TenantContext.setTenantId(tenant.getId().toString());

        Usuario usuario = userService.findOrCreateUser(
                "google-id-no-profile",
                "user@example.com",
                "User Without Profile",
                "https://example.com/avatar.jpg",
                tenant.getId()
        );

        // When
        String jwt = jwtService.generateToken(usuario);
        List<String> roles = jwtService.getRolesFromToken(jwt);

        // Then
        assertThat(usuario.hasProfile()).isFalse();
        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("Multiple users in same tenant can have different profiles")
    void multipleUsersInSameTenantCanHaveDifferentProfiles() {
        // Given
        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                "test-tenant-multi-users",
                "Test Tenant",
                "@POLY"
        );
        tenant = tenantRepository.save(tenant);
        TenantContext.setTenantId(tenant.getId().toString());

        Role adminRole = roleService.getByNome("ADMIN");
        Role vendedorRole = roleService.getByNome("VENDEDOR");

        // Create two different profiles
        Profile adminProfile = profileService.create(
                "Admin Profile",
                "Admin access",
                List.of(adminRole.getId())
        );

        Profile vendedorProfile = profileService.create(
                "Vendedor Profile",
                "Vendedor access",
                List.of(vendedorRole.getId())
        );

        // Create two users
        Usuario admin = userService.findOrCreateUser(
                "google-admin",
                "admin@company.com",
                "Admin",
                null,
                tenant.getId()
        );

        Usuario vendedor = userService.findOrCreateUser(
                "google-vendedor",
                "vendedor@company.com",
                "Vendedor",
                null,
                tenant.getId()
        );

        // Assign different profiles
        userService.assignProfile(admin.getId(), adminProfile.getId());
        userService.assignProfile(vendedor.getId(), vendedorProfile.getId());

        // When
        List<Role> adminRoles = userService.getUserRoles(admin.getId());
        List<Role> vendedorRoles = userService.getUserRoles(vendedor.getId());

        // Then
        assertThat(adminRoles).extracting(Role::getNome).containsExactly("ADMIN");
        assertThat(vendedorRoles).extracting(Role::getNome).containsExactly("VENDEDOR");
    }
}
