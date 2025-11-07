package com.estoquecentral.integration;

import com.estoquecentral.auth.adapter.out.RoleRepository;
import com.estoquecentral.auth.application.RoleService;
import com.estoquecentral.auth.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for RoleService with real PostgreSQL database
 *
 * <p>These tests use Testcontainers to spin up a real PostgreSQL instance
 * and verify the complete integration between:
 * <ul>
 *   <li>RoleService (application layer)</li>
 *   <li>RoleRepository (infrastructure layer)</li>
 *   <li>PostgreSQL database</li>
 *   <li>Flyway migrations</li>
 * </ul>
 *
 * <p>Tests are transactional and rolled back after each test to maintain
 * database isolation.
 */
@SpringBootTest
@Transactional
@DisplayName("RoleService Integration Tests (Real Database)")
class RoleServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Clean up any test data (transactional rollback will handle this)
    }

    @Test
    @DisplayName("Should list all default roles from migration")
    void shouldListAllDefaultRolesFromMigration() {
        // Given
        // Default roles from V004__create_rbac_tables.sql

        // When
        List<Role> roles = roleService.listAll();

        // Then
        assertThat(roles).isNotEmpty();
        assertThat(roles).hasSizeGreaterThanOrEqualTo(9); // 9 default roles
        assertThat(roles).extracting(Role::getNome)
                .contains("ADMIN", "GERENTE", "VENDEDOR", "ESTOQUISTA");
    }

    @Test
    @DisplayName("Should create and persist new role to database")
    void shouldCreateAndPersistNewRole() {
        // Given
        String roleName = "TEST_ROLE_" + UUID.randomUUID();

        // When
        Role createdRole = roleService.create(
                roleName,
                "Test role for integration testing",
                "OPERACIONAL"
        );

        // Then
        assertThat(createdRole).isNotNull();
        assertThat(createdRole.getId()).isNotNull();
        assertThat(createdRole.getNome()).isEqualTo(roleName);
        assertThat(createdRole.getAtivo()).isTrue();

        // Verify it was persisted
        Role foundRole = roleService.getByNome(roleName);
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getId()).isEqualTo(createdRole.getId());
    }

    @Test
    @DisplayName("Should update role in database")
    void shouldUpdateRoleInDatabase() {
        // Given
        String roleName = "UPDATE_TEST_" + UUID.randomUUID();
        Role role = roleService.create(roleName, "Original", "OPERACIONAL");

        // When
        Role updatedRole = roleService.update(
                role.getId(),
                "Updated description",
                "GESTAO"
        );

        // Then
        assertThat(updatedRole.getDescricao()).isEqualTo("Updated description");
        assertThat(updatedRole.getCategoria()).isEqualTo("GESTAO");

        // Verify persistence
        Role foundRole = roleService.getById(role.getId());
        assertThat(foundRole.getDescricao()).isEqualTo("Updated description");
        assertThat(foundRole.getCategoria()).isEqualTo("GESTAO");
    }

    @Test
    @DisplayName("Should deactivate role in database")
    void shouldDeactivateRoleInDatabase() {
        // Given
        String roleName = "DEACTIVATE_TEST_" + UUID.randomUUID();
        Role role = roleService.create(roleName, "Test", "OPERACIONAL");
        assertThat(role.getAtivo()).isTrue();

        // When
        roleService.deactivate(role.getId());

        // Then
        Role deactivatedRole = roleRepository.findById(role.getId()).orElseThrow();
        assertThat(deactivatedRole.getAtivo()).isFalse();

        // Verify it doesn't appear in listAll (only active roles)
        List<Role> activeRoles = roleService.listAll();
        assertThat(activeRoles).extracting(Role::getId)
                .doesNotContain(role.getId());
    }

    @Test
    @DisplayName("Should activate previously deactivated role")
    void shouldActivatePreviouslyDeactivatedRole() {
        // Given
        String roleName = "ACTIVATE_TEST_" + UUID.randomUUID();
        Role role = roleService.create(roleName, "Test", "OPERACIONAL");
        roleService.deactivate(role.getId());

        // When
        roleService.activate(role.getId());

        // Then
        Role activatedRole = roleRepository.findById(role.getId()).orElseThrow();
        assertThat(activatedRole.getAtivo()).isTrue();

        // Verify it appears in listAll again
        List<Role> activeRoles = roleService.listAll();
        assertThat(activeRoles).extracting(Role::getId)
                .contains(role.getId());
    }

    @Test
    @DisplayName("Should filter roles by category")
    void shouldFilterRolesByCategory() {
        // Given
        String role1Name = "GESTAO_TEST_" + UUID.randomUUID();
        String role2Name = "OPERACIONAL_TEST_" + UUID.randomUUID();
        roleService.create(role1Name, "Gestao role", "GESTAO");
        roleService.create(role2Name, "Operacional role", "OPERACIONAL");

        // When
        List<Role> gestaoRoles = roleService.listByCategoria("GESTAO");
        List<Role> operacionalRoles = roleService.listByCategoria("OPERACIONAL");

        // Then
        assertThat(gestaoRoles).extracting(Role::getNome).contains(role1Name);
        assertThat(operacionalRoles).extracting(Role::getNome).contains(role2Name);
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate role")
    void shouldThrowExceptionWhenCreatingDuplicateRole() {
        // Given
        String roleName = "DUPLICATE_TEST_" + UUID.randomUUID();
        roleService.create(roleName, "First", "OPERACIONAL");

        // When/Then
        assertThatThrownBy(() -> roleService.create(roleName, "Second", "GESTAO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role already exists");
    }

    @Test
    @DisplayName("Should throw exception when role not found")
    void shouldThrowExceptionWhenRoleNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> roleService.getById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("Should handle concurrent role creation")
    void shouldHandleConcurrentRoleCreation() {
        // Given
        String role1Name = "CONCURRENT_1_" + UUID.randomUUID();
        String role2Name = "CONCURRENT_2_" + UUID.randomUUID();

        // When
        Role role1 = roleService.create(role1Name, "First", "OPERACIONAL");
        Role role2 = roleService.create(role2Name, "Second", "GESTAO");

        // Then
        assertThat(role1.getId()).isNotEqualTo(role2.getId());

        List<Role> allRoles = roleService.listAll();
        assertThat(allRoles).extracting(Role::getNome)
                .contains(role1Name, role2Name);
    }
}
