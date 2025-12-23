package com.estoquecentral.shared.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TenantCacheKeyGenerator
 *
 * <p>Tests key generation logic with different tenant contexts and method parameters.
 */
@DisplayName("TenantCacheKeyGenerator Unit Tests")
class TenantCacheKeyGeneratorTest {

    private TenantCacheKeyGenerator keyGenerator;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        keyGenerator = new TenantCacheKeyGenerator();
        tenantId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should generate key with tenant prefix")
    void shouldGenerateKeyWithTenantPrefix() throws Exception {
        // Given
        TenantContext.setTenantId(tenantId.toString());
        Method method = TestService.class.getMethod("findById", UUID.class);
        UUID productId = UUID.randomUUID();

        // When
        Object key = keyGenerator.generate(new TestService(), method, productId);

        // Then
        assertThat(key).isInstanceOf(String.class);
        String keyStr = (String) key;
        assertThat(keyStr).startsWith("tenant:" + tenantId);
        assertThat(keyStr).contains(":TestService:");
        assertThat(keyStr).contains(":findById:");
        assertThat(keyStr).endsWith(":" + productId);
    }

    @Test
    @DisplayName("Should generate key with public prefix when no tenant context")
    void shouldGenerateKeyWithPublicPrefixWhenNoTenantContext() throws Exception {
        // Given - no tenant context
        Method method = TestService.class.getMethod("findById", UUID.class);
        UUID productId = UUID.randomUUID();

        // When
        Object key = keyGenerator.generate(new TestService(), method, productId);

        // Then
        String keyStr = (String) key;
        assertThat(keyStr).startsWith("public:");
        assertThat(keyStr).contains(":TestService:findById:");
    }

    @Test
    @DisplayName("Should generate key with multiple parameters")
    void shouldGenerateKeyWithMultipleParameters() throws Exception {
        // Given
        TenantContext.setTenantId(tenantId.toString());
        Method method = TestService.class.getMethod("findByRange", String.class, Integer.class);

        // When
        Object key = keyGenerator.generate(new TestService(), method, "category-A", 100);

        // Then
        String keyStr = (String) key;
        assertThat(keyStr).contains(":findByRange:");
        assertThat(keyStr).contains(":category-A:");
        assertThat(keyStr).endsWith(":100");
    }

    @Test
    @DisplayName("Should generate key with no parameters")
    void shouldGenerateKeyWithNoParameters() throws Exception {
        // Given
        TenantContext.setTenantId(tenantId.toString());
        Method method = TestService.class.getMethod("findAll");

        // When
        Object key = keyGenerator.generate(new TestService(), method);

        // Then
        String keyStr = (String) key;
        assertThat(keyStr).isEqualTo("tenant:" + tenantId + ":TestService:findAll");
    }

    @Test
    @DisplayName("Should handle null parameter")
    void shouldHandleNullParameter() throws Exception {
        // Given
        TenantContext.setTenantId(tenantId.toString());
        Method method = TestService.class.getMethod("findById", UUID.class);

        // When
        Object key = keyGenerator.generate(new TestService(), method, (Object) null);

        // Then
        String keyStr = (String) key;
        assertThat(keyStr).endsWith(":null");
    }

    @Test
    @DisplayName("Static method - Should generate simple key")
    void staticMethodShouldGenerateSimpleKey() {
        // Given
        TenantContext.setTenantId(tenantId.toString());

        // When
        String key = TenantCacheKeyGenerator.generateKey("products", "product-123");

        // Then
        assertThat(key).isEqualTo("tenant:" + tenantId + ":products:product-123");
    }

    @Test
    @DisplayName("Static method - Should generate key with multiple components")
    void staticMethodShouldGenerateKeyWithMultipleComponents() {
        // Given
        TenantContext.setTenantId(tenantId.toString());

        // When
        String key = TenantCacheKeyGenerator.generateKey("products", "category-A", "status-active", "page-1");

        // Then
        assertThat(key).isEqualTo("tenant:" + tenantId + ":products:category-A:status-active:page-1");
    }

    @Test
    @DisplayName("Static method - Should generate pattern for cache eviction")
    void staticMethodShouldGeneratePatternForCacheEviction() {
        // Given
        TenantContext.setTenantId(tenantId.toString());

        // When
        String pattern = TenantCacheKeyGenerator.generatePattern("products");

        // Then
        assertThat(pattern).isEqualTo("tenant:" + tenantId + ":products:*");
    }

    @Test
    @DisplayName("Static method - Should generate wildcard pattern")
    void staticMethodShouldGenerateWildcardPattern() {
        // Given
        TenantContext.setTenantId(tenantId.toString());

        // When
        String pattern = TenantCacheKeyGenerator.generatePattern("*");

        // Then
        assertThat(pattern).isEqualTo("tenant:" + tenantId + ":*");
    }

    @Test
    @DisplayName("Should generate different keys for different tenants")
    void shouldGenerateDifferentKeysForDifferentTenants() throws Exception {
        // Given
        UUID tenant1 = UUID.randomUUID();
        UUID tenant2 = UUID.randomUUID();
        Method method = TestService.class.getMethod("findById", UUID.class);
        UUID productId = UUID.randomUUID();

        // When
        TenantContext.setTenantId(tenant1.toString());
        String key1 = (String) keyGenerator.generate(new TestService(), method, productId);

        TenantContext.setTenantId(tenant2.toString());
        String key2 = (String) keyGenerator.generate(new TestService(), method, productId);

        // Then
        assertThat(key1).isNotEqualTo(key2);
        assertThat(key1).startsWith("tenant:" + tenant1);
        assertThat(key2).startsWith("tenant:" + tenant2);
    }

    @Test
    @DisplayName("Should generate same key for same tenant and parameters")
    void shouldGenerateSameKeyForSameTenantAndParameters() throws Exception {
        // Given
        TenantContext.setTenantId(tenantId.toString());
        Method method = TestService.class.getMethod("findById", UUID.class);
        UUID productId = UUID.randomUUID();

        // When
        String key1 = (String) keyGenerator.generate(new TestService(), method, productId);
        String key2 = (String) keyGenerator.generate(new TestService(), method, productId);

        // Then
        assertThat(key1).isEqualTo(key2);
    }

    /**
     * Mock service class for testing method reflection
     */
    static class TestService {
        public Object findById(UUID id) {
            return null;
        }

        public Object findByRange(String category, Integer limit) {
            return null;
        }

        public Object findAll() {
            return null;
        }
    }
}
