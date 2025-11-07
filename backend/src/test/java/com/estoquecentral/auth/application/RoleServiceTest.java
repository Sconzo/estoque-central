package com.estoquecentral.auth.application;

import com.estoquecentral.auth.adapter.out.RoleRepository;
import com.estoquecentral.auth.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleService
 *
 * <p>Tests role management operations:
 * <ul>
 *   <li>List all active roles</li>
 *   <li>List roles by category</li>
 *   <li>Get role by ID</li>
 *   <li>Get role by name</li>
 *   <li>Create new role</li>
 *   <li>Update role</li>
 *   <li>Activate/deactivate role</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
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
    @DisplayName("Should list all active roles")
    void shouldListAllActiveRoles() {
        // Given
        when(roleRepository.findByAtivoTrue()).thenReturn(List.of(adminRole, gerenteRole));

        // When
        List<Role> roles = roleService.listAll();

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Role::getNome).containsExactlyInAnyOrder("ADMIN", "GERENTE");
        verify(roleRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("Should list roles by category")
    void shouldListRolesByCategory() {
        // Given
        when(roleRepository.findByCategoria("GESTAO")).thenReturn(List.of(gerenteRole));

        // When
        List<Role> roles = roleService.listByCategoria("GESTAO");

        // Then
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getNome()).isEqualTo("GERENTE");
        assertThat(roles.get(0).getCategoria()).isEqualTo("GESTAO");
        verify(roleRepository, times(1)).findByCategoria("GESTAO");
    }

    @Test
    @DisplayName("Should get role by ID")
    void shouldGetRoleById() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));

        // When
        Role role = roleService.getById(roleId);

        // Then
        assertThat(role).isNotNull();
        assertThat(role.getNome()).isEqualTo("ADMIN");
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    @DisplayName("Should throw exception when role not found by ID")
    void shouldThrowExceptionWhenRoleNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(roleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.getById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");

        verify(roleRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should get role by name")
    void shouldGetRoleByName() {
        // Given
        when(roleRepository.findByNome("ADMIN")).thenReturn(Optional.of(adminRole));

        // When
        Role role = roleService.getByNome("ADMIN");

        // Then
        assertThat(role).isNotNull();
        assertThat(role.getNome()).isEqualTo("ADMIN");
        verify(roleRepository, times(1)).findByNome("ADMIN");
    }

    @Test
    @DisplayName("Should throw exception when role not found by name")
    void shouldThrowExceptionWhenRoleNotFoundByName() {
        // Given
        when(roleRepository.findByNome("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.getByNome("NONEXISTENT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");

        verify(roleRepository, times(1)).findByNome("NONEXISTENT");
    }

    @Test
    @DisplayName("Should create new role")
    void shouldCreateNewRole() {
        // Given
        when(roleRepository.findByNome("NEW_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Role newRole = roleService.create("NEW_ROLE", "Nova role", "OPERACIONAL");

        // Then
        assertThat(newRole).isNotNull();
        assertThat(newRole.getNome()).isEqualTo("NEW_ROLE");
        assertThat(newRole.getDescricao()).isEqualTo("Nova role");
        assertThat(newRole.getCategoria()).isEqualTo("OPERACIONAL");
        assertThat(newRole.getAtivo()).isTrue();

        verify(roleRepository, times(1)).findByNome("NEW_ROLE");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate role")
    void shouldThrowExceptionWhenCreatingDuplicateRole() {
        // Given
        when(roleRepository.findByNome("ADMIN")).thenReturn(Optional.of(adminRole));

        // When/Then
        assertThatThrownBy(() -> roleService.create("ADMIN", "Duplicate", "SISTEMA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role already exists");

        verify(roleRepository, times(1)).findByNome("ADMIN");
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating role with invalid category")
    void shouldThrowExceptionWhenCreatingRoleWithInvalidCategory() {
        // Given
        when(roleRepository.findByNome("NEW_ROLE")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roleService.create("NEW_ROLE", "Description", "INVALID_CATEGORY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid categoria");

        verify(roleRepository, times(1)).findByNome("NEW_ROLE");
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update role")
    void shouldUpdateRole() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Role updatedRole = roleService.update(roleId, "Nova descrição", "GESTAO");

        // Then
        assertThat(updatedRole).isNotNull();
        assertThat(updatedRole.getDescricao()).isEqualTo("Nova descrição");
        assertThat(updatedRole.getCategoria()).isEqualTo("GESTAO");

        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("Should deactivate role")
    void shouldDeactivateRole() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        roleService.deactivate(roleId);

        // Then
        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, times(1)).save(argThat(role -> !role.getAtivo()));
    }

    @Test
    @DisplayName("Should activate role")
    void shouldActivateRole() {
        // Given
        adminRole.deactivate(); // Start with inactive role
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        roleService.activate(roleId);

        // Then
        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, times(1)).save(argThat(Role::getAtivo));
    }

    @Test
    @DisplayName("Should return empty list when no roles found by category")
    void shouldReturnEmptyListWhenNoRolesFoundByCategory() {
        // Given
        when(roleRepository.findByCategoria("NONEXISTENT")).thenReturn(List.of());

        // When
        List<Role> roles = roleService.listByCategoria("NONEXISTENT");

        // Then
        assertThat(roles).isEmpty();
        verify(roleRepository, times(1)).findByCategoria("NONEXISTENT");
    }
}
