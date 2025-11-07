package com.estoquecentral.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with real database
 *
 * <p>This class provides:
 * <ul>
 *   <li>PostgreSQL container via Testcontainers</li>
 *   <li>Full Spring Boot application context</li>
 *   <li>Database migrations via Flyway</li>
 *   <li>Shared container across all tests (performance)</li>
 * </ul>
 *
 * <p><strong>Usage:</strong>
 * <pre>{@code
 * @SpringBootTest
 * class MyIntegrationTest extends BaseIntegrationTest {
 *     @Autowired
 *     private MyService myService;
 *
 *     @Test
 *     void testWithRealDatabase() {
 *         // Test code with real PostgreSQL
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Container Lifecycle:</strong>
 * The PostgreSQL container is started once before all tests and shared
 * across test classes for better performance. The container is automatically
 * stopped after all tests complete.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    /**
     * Shared PostgreSQL container for all integration tests
     *
     * <p>Uses PostgreSQL 15 with the following configuration:
     * <ul>
     *   <li>Database: testdb</li>
     *   <li>Username: test</li>
     *   <li>Password: test</li>
     *   <li>Reusable: true (faster test execution)</li>
     * </ul>
     */
    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    /**
     * Configures Spring datasource properties to use the Testcontainers PostgreSQL
     *
     * <p>This method dynamically sets:
     * <ul>
     *   <li>spring.datasource.url - from container JDBC URL</li>
     *   <li>spring.datasource.username - test</li>
     *   <li>spring.datasource.password - test</li>
     * </ul>
     *
     * @param registry Spring's dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    /**
     * Ensures PostgreSQL container is started before any test
     */
    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }
}
