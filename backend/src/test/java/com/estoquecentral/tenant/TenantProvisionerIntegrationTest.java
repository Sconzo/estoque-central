package com.estoquecentral.tenant;

import com.estoquecentral.integration.BaseIntegrationTest;
import com.estoquecentral.shared.tenant.TenantContext;
import com.estoquecentral.tenant.application.TenantProvisioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for TenantProvisioner
 *
 * <p>Tests the complete tenant provisioning flow:
 * <ul>
 *   <li>Schema creation</li>
 *   <li>Flyway migrations application</li>
 *   <li>Default profiles seeding (Admin, Gerente, Vendedor)</li>
 *   <li>Performance validation (< 30s as per NFR2)</li>
 * </ul>
 *
 * Story: 7-2 PostgreSQL Multi-Tenant Schema-per-Tenant
 */
class TenantProvisionerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TenantProvisioner tenantProvisioner;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String createdSchemaName;

    @AfterEach
    void cleanup() {
        // Clean up created tenant schema after each test
        if (createdSchemaName != null) {
            tenantProvisioner.dropTenantSchema(createdSchemaName);
            createdSchemaName = null;
        }
        TenantContext.clear();
    }

    /**
     * AC2: Test complete tenant provisioning flow
     * - Creates schema dynamically
     * - Applies Flyway migrations
     * - Seeds default profiles (Admin, Gerente, Vendedor)
     * - Completes in < 30 seconds (NFR2)
     */
    @Test
    void shouldProvisionCompleteTenantSchema() {
        // Given
        UUID tenantId = UUID.randomUUID();

        // When
        long startTime = System.currentTimeMillis();
        String schemaName = tenantProvisioner.provisionTenantSchema(tenantId);
        long duration = System.currentTimeMillis() - startTime;

        createdSchemaName = schemaName;

        // Then - Schema created
        assertThat(schemaName).isNotNull();
        assertThat(schemaName).matches("tenant_[a-f0-9]{32}");

        boolean schemaExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)",
                Boolean.class,
                schemaName
        );
        assertThat(schemaExists).isTrue();

        // Then - Migrations applied (check for profiles table)
        Integer profilesTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = ? AND table_name = 'profiles'",
                Integer.class,
                schemaName
        );
        assertThat(profilesTableCount).isEqualTo(1);

        // Then - profile_roles table exists
        Integer profileRolesTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = ? AND table_name = 'profile_roles'",
                Integer.class,
                schemaName
        );
        assertThat(profileRolesTableCount).isEqualTo(1);

        // Then - Default profiles seeded (Admin, Gerente, Vendedor)
        List<Map<String, Object>> profiles = jdbcTemplate.queryForList(
                "SELECT nome FROM " + schemaName + ".profiles ORDER BY nome"
        );
        assertThat(profiles).hasSize(3);
        assertThat(profiles.get(0).get("nome")).isEqualTo("Admin");
        assertThat(profiles.get(1).get("nome")).isEqualTo("Gerente");
        assertThat(profiles.get(2).get("nome")).isEqualTo("Vendedor");

        // Then - NFR2: Performance < 30 seconds
        assertThat(duration).isLessThan(30000);
        System.out.printf("✅ Tenant provisioned in %dms (NFR2: < 30s)%n", duration);
    }

    /**
     * AC2: Test that default profiles have roles assigned
     */
    @Test
    void shouldSeedDefaultProfilesWithRoles() {
        // Given
        UUID tenantId = UUID.randomUUID();
        String schemaName = tenantProvisioner.provisionTenantSchema(tenantId);
        createdSchemaName = schemaName;

        // When - Check Admin profile has ADMIN role
        Integer adminRoleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + schemaName + ".profile_roles pr " +
                        "JOIN " + schemaName + ".profiles p ON pr.profile_id = p.id " +
                        "WHERE p.nome = 'Admin'",
                Integer.class
        );

        // Then
        assertThat(adminRoleCount).isGreaterThan(0);
    }

    /**
     * AC2: Test schema is isolated (independent of other tenants)
     */
    @Test
    void shouldIsolateTenantSchemas() {
        // Given - Create two tenants
        UUID tenant1Id = UUID.randomUUID();
        UUID tenant2Id = UUID.randomUUID();

        String schema1 = tenantProvisioner.provisionTenantSchema(tenant1Id);
        String schema2 = tenantProvisioner.provisionTenantSchema(tenant2Id);

        try {
            // When - Add custom profile to tenant1 only
            jdbcTemplate.update(
                    "INSERT INTO " + schema1 + ".profiles (id, nome, descricao, ativo) " +
                            "VALUES (?, 'Custom Profile', 'Test', true)",
                    UUID.randomUUID()
            );

            // Then - Tenant1 has 4 profiles (3 default + 1 custom)
            Integer tenant1ProfileCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + schema1 + ".profiles",
                    Integer.class
            );
            assertThat(tenant1ProfileCount).isEqualTo(4);

            // Then - Tenant2 still has only 3 default profiles (isolation confirmed)
            Integer tenant2ProfileCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + schema2 + ".profiles",
                    Integer.class
            );
            assertThat(tenant2ProfileCount).isEqualTo(3);

        } finally {
            tenantProvisioner.dropTenantSchema(schema1);
            tenantProvisioner.dropTenantSchema(schema2);
            createdSchemaName = null; // Already cleaned up
        }
    }

    /**
     * Test schema naming convention
     */
    @Test
    void shouldFollowSchemaNamingConvention() {
        // Given
        UUID tenantId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

        // When
        String schemaName = tenantProvisioner.provisionTenantSchema(tenantId);
        createdSchemaName = schemaName;

        // Then - Format: tenant_{uuid_without_hyphens}
        String expectedSchema = "tenant_a1b2c3d4e5f67890abcdef1234567890";
        assertThat(schemaName).isEqualTo(expectedSchema);
    }
}
