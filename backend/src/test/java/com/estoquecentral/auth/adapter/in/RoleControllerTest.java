package com.estoquecentral.auth.adapter.in;

import com.estoquecentral.auth.application.RoleService;
import com.estoquecentral.auth.domain.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Integration tests for RoleController
 *
 * <p>Tests REST API endpoints for role management:
 * <ul>
 *   <li>GET /api/roles - List all roles</li>
 *   <li>GET /api/roles?categoria=GESTAO - List roles by category</li>
 *   <li>GET /api/roles/{id} - Get role by ID</li>
 *   <li>POST /api/roles - Create new role</li>
 *   <li>PUT /api/roles/{id} - Update role</li>
 *   <li>DELETE /api/roles/{id} - Deactivate role</li>
 * </ul>
 */
@WebMvcTest(RoleController.class)
@DisplayName("RoleController Integration Tests")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    private Role adminRole;
    private Role gerenteRole;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();

        adminRole = new Role(
                roleId,
                "ADMIN",
                "Administrador do sistema",
                "SISTEMA"
        );

        gerenteRole = new Role(
                UUID.randomUUID(),
                "GERENTE",
                "Gerente de loja",
                "GESTAO"
        );
    }

    @Test
    @DisplayName("Should list all roles")
    @WithMockUser(roles = "ADMIN")
    void shouldListAllRoles() throws Exception {
        // Given
        when(roleService.listAll()).thenReturn(List.of(adminRole, gerenteRole));

        // When/Then
        mockMvc.perform(get("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("ADMIN")))
                .andExpect(jsonPath("$[1].nome", is("GERENTE")));

        verify(roleService, times(1)).listAll();
    }

    @Test
    @DisplayName("Should list roles by category")
    @WithMockUser(roles = "ADMIN")
    void shouldListRolesByCategory() throws Exception {
        // Given
        when(roleService.listByCategoria("GESTAO")).thenReturn(List.of(gerenteRole));

        // When/Then
        mockMvc.perform(get("/api/roles")
                        .param("categoria", "GESTAO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("GERENTE")))
                .andExpect(jsonPath("$[0].categoria", is("GESTAO")));

        verify(roleService, times(1)).listByCategoria("GESTAO");
    }

    @Test
    @DisplayName("Should get role by ID")
    @WithMockUser(roles = "ADMIN")
    void shouldGetRoleById() throws Exception {
        // Given
        when(roleService.getById(roleId)).thenReturn(adminRole);

        // When/Then
        mockMvc.perform(get("/api/roles/{id}", roleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(roleId.toString())))
                .andExpect(jsonPath("$.nome", is("ADMIN")))
                .andExpect(jsonPath("$.descricao", is("Administrador do sistema")))
                .andExpect(jsonPath("$.categoria", is("SISTEMA")));

        verify(roleService, times(1)).getById(roleId);
    }

    @Test
    @DisplayName("Should return 404 when role not found")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenRoleNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(roleService.getById(nonExistentId))
                .thenThrow(new IllegalArgumentException("Role not found"));

        // When/Then
        mockMvc.perform(get("/api/roles/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(roleService, times(1)).getById(nonExistentId);
    }

    @Test
    @DisplayName("Should create new role")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateNewRole() throws Exception {
        // Given
        Role newRole = new Role(
                UUID.randomUUID(),
                "FISCAL",
                "Operador fiscal",
                "OPERACIONAL"
        );
        when(roleService.create(anyString(), anyString(), anyString())).thenReturn(newRole);

        String requestBody = """
                {
                    "nome": "FISCAL",
                    "descricao": "Operador fiscal",
                    "categoria": "OPERACIONAL"
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("FISCAL")))
                .andExpect(jsonPath("$.descricao", is("Operador fiscal")))
                .andExpect(jsonPath("$.categoria", is("OPERACIONAL")));

        verify(roleService, times(1)).create("FISCAL", "Operador fiscal", "OPERACIONAL");
    }

    @Test
    @DisplayName("Should return 400 when creating role with invalid data")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenCreatingRoleWithInvalidData() throws Exception {
        // Given
        String requestBody = """
                {
                    "nome": "",
                    "descricao": "Invalid",
                    "categoria": "INVALID"
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(roleService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should update role")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateRole() throws Exception {
        // Given
        Role updatedRole = new Role(roleId, "ADMIN", "Nova descrição", "GESTAO");
        when(roleService.update(eq(roleId), anyString(), anyString())).thenReturn(updatedRole);

        String requestBody = """
                {
                    "descricao": "Nova descrição",
                    "categoria": "GESTAO"
                }
                """;

        // When/Then
        mockMvc.perform(put("/api/roles/{id}", roleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", is("Nova descrição")))
                .andExpect(jsonPath("$.categoria", is("GESTAO")));

        verify(roleService, times(1)).update(eq(roleId), anyString(), anyString());
    }

    @Test
    @DisplayName("Should deactivate role")
    @WithMockUser(roles = "ADMIN")
    void shouldDeactivateRole() throws Exception {
        // Given
        doNothing().when(roleService).deactivate(roleId);

        // When/Then
        mockMvc.perform(delete("/api/roles/{id}", roleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(roleService, times(1)).deactivate(roleId);
    }

    @Test
    @DisplayName("Should deny access without ADMIN role")
    @WithMockUser(roles = "VENDEDOR")
    void shouldDenyAccessWithoutAdminRole() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(roleService, never()).listAll();
    }

    @Test
    @DisplayName("Should deny access without authentication")
    void shouldDenyAccessWithoutAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(roleService, never()).listAll();
    }
}
