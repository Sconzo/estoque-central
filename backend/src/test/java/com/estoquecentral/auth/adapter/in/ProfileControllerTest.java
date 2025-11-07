package com.estoquecentral.auth.adapter.in;

import com.estoquecentral.auth.application.ProfileService;
import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Profile;
import com.estoquecentral.auth.domain.Role;
import com.estoquecentral.shared.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProfileController
 *
 * <p>Tests REST API endpoints for profile management:
 * <ul>
 *   <li>GET /api/profiles - List profiles</li>
 *   <li>GET /api/profiles/{id} - Get profile by ID</li>
 *   <li>GET /api/profiles/{id}/roles - Get roles for profile</li>
 *   <li>POST /api/profiles - Create profile</li>
 *   <li>PUT /api/profiles/{id} - Update profile</li>
 *   <li>PUT /api/profiles/{id}/roles - Update profile roles</li>
 *   <li>DELETE /api/profiles/{id} - Deactivate profile</li>
 *   <li>PUT /api/profiles/users/{userId}/profile - Assign profile to user</li>
 * </ul>
 */
@WebMvcTest(ProfileController.class)
@DisplayName("ProfileController Integration Tests")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private UserService userService;

    private UUID tenantId;
    private UUID profileId;
    private UUID roleAdminId;
    private UUID roleGerenteId;
    private Profile adminProfile;
    private Role adminRole;
    private Role gerenteRole;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        roleAdminId = UUID.randomUUID();
        roleGerenteId = UUID.randomUUID();

        // Set TenantContext for all tests
        TenantContext.setTenantId(tenantId.toString());

        adminProfile = new Profile(
                profileId,
                tenantId,
                "Administrador",
                "Perfil administrativo"
        );

        adminRole = new Role(roleAdminId, "ADMIN", "Admin", "SISTEMA");
        gerenteRole = new Role(roleGerenteId, "GERENTE", "Gerente", "GESTAO");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should list profiles for current tenant")
    @WithMockUser(roles = "ADMIN")
    void shouldListProfilesForCurrentTenant() throws Exception {
        // Given
        Profile profile2 = new Profile(UUID.randomUUID(), tenantId, "Gerente", "Perfil gerente");
        when(profileService.listByTenant(tenantId)).thenReturn(List.of(adminProfile, profile2));

        // When/Then
        mockMvc.perform(get("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Administrador")))
                .andExpect(jsonPath("$[1].nome", is("Gerente")));

        verify(profileService, times(1)).listByTenant(tenantId);
    }

    @Test
    @DisplayName("Should get profile by ID with roles")
    @WithMockUser(roles = "ADMIN")
    void shouldGetProfileByIdWithRoles() throws Exception {
        // Given
        when(profileService.getById(profileId)).thenReturn(adminProfile);
        when(profileService.getRolesByProfile(profileId)).thenReturn(List.of(adminRole, gerenteRole));

        // When/Then
        mockMvc.perform(get("/api/profiles/{id}", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(profileId.toString())))
                .andExpect(jsonPath("$.nome", is("Administrador")))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles[*].nome", containsInAnyOrder("ADMIN", "GERENTE")));

        verify(profileService, times(1)).getById(profileId);
        verify(profileService, times(1)).getRolesByProfile(profileId);
    }

    @Test
    @DisplayName("Should get roles for profile")
    @WithMockUser(roles = "ADMIN")
    void shouldGetRolesForProfile() throws Exception {
        // Given
        when(profileService.getRolesByProfile(profileId)).thenReturn(List.of(adminRole));

        // When/Then
        mockMvc.perform(get("/api/profiles/{id}/roles", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("ADMIN")));

        verify(profileService, times(1)).getRolesByProfile(profileId);
    }

    @Test
    @DisplayName("Should create profile with roles")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProfileWithRoles() throws Exception {
        // Given
        when(profileService.create(eq(tenantId), anyString(), anyString(), anyList()))
                .thenReturn(adminProfile);
        when(profileService.getRolesByProfile(profileId)).thenReturn(List.of(adminRole));

        String requestBody = String.format("""
                {
                    "nome": "Administrador",
                    "descricao": "Perfil administrativo",
                    "roleIds": ["%s"]
                }
                """, roleAdminId);

        // When/Then
        mockMvc.perform(post("/api/profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Administrador")))
                .andExpect(jsonPath("$.roles", hasSize(1)));

        verify(profileService, times(1)).create(eq(tenantId), eq("Administrador"),
                eq("Perfil administrativo"), anyList());
    }

    @Test
    @DisplayName("Should return 400 when creating profile with invalid data")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenCreatingProfileWithInvalidData() throws Exception {
        // Given
        String requestBody = """
                {
                    "nome": "",
                    "descricao": "Invalid",
                    "roleIds": []
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(profileService, never()).create(any(), anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("Should update profile metadata")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateProfileMetadata() throws Exception {
        // Given
        Profile updatedProfile = new Profile(profileId, tenantId, "Novo Nome", "Nova Descrição");
        when(profileService.update(eq(profileId), anyString(), anyString())).thenReturn(updatedProfile);

        String requestBody = """
                {
                    "nome": "Novo Nome",
                    "descricao": "Nova Descrição"
                }
                """;

        // When/Then
        mockMvc.perform(put("/api/profiles/{id}", profileId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Novo Nome")))
                .andExpect(jsonPath("$.descricao", is("Nova Descrição")));

        verify(profileService, times(1)).update(eq(profileId), anyString(), anyString());
    }

    @Test
    @DisplayName("Should update profile roles")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateProfileRoles() throws Exception {
        // Given
        doNothing().when(profileService).updateRoles(eq(profileId), anyList());

        String requestBody = String.format("""
                {
                    "roleIds": ["%s", "%s"]
                }
                """, roleAdminId, roleGerenteId);

        // When/Then
        mockMvc.perform(put("/api/profiles/{id}/roles", profileId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        verify(profileService, times(1)).updateRoles(eq(profileId), anyList());
    }

    @Test
    @DisplayName("Should deactivate profile")
    @WithMockUser(roles = "ADMIN")
    void shouldDeactivateProfile() throws Exception {
        // Given
        doNothing().when(profileService).deactivate(profileId);

        // When/Then
        mockMvc.perform(delete("/api/profiles/{id}", profileId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(profileService, times(1)).deactivate(profileId);
    }

    @Test
    @DisplayName("Should assign profile to user")
    @WithMockUser(roles = "ADMIN")
    void shouldAssignProfileToUser() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).assignProfile(userId, profileId);

        String requestBody = String.format("""
                {
                    "profileId": "%s"
                }
                """, profileId);

        // When/Then
        mockMvc.perform(put("/api/profiles/users/{userId}/profile", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).assignProfile(userId, profileId);
    }

    @Test
    @DisplayName("Should return 400 when assigning profile without profileId")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenAssigningProfileWithoutProfileId() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        String requestBody = """
                {
                    "profileId": null
                }
                """;

        // When/Then
        mockMvc.perform(put("/api/profiles/users/{userId}/profile", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(userService, never()).assignProfile(any(), any());
    }

    @Test
    @DisplayName("Should deny access without ADMIN role")
    @WithMockUser(roles = "VENDEDOR")
    void shouldDenyAccessWithoutAdminRole() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(profileService, never()).listByTenant(any());
    }

    @Test
    @DisplayName("Should deny access without authentication")
    void shouldDenyAccessWithoutAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(profileService, never()).listByTenant(any());
    }

    @Test
    @DisplayName("Should return 404 when profile not found")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenProfileNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(profileService.getById(nonExistentId))
                .thenThrow(new IllegalArgumentException("Profile not found"));

        // When/Then
        mockMvc.perform(get("/api/profiles/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(profileService, times(1)).getById(nonExistentId);
    }
}
