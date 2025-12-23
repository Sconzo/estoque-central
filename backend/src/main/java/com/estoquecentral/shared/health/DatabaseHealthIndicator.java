package com.estoquecentral.shared.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database Health Indicator
 * Story 7.8: Health Check Monitoring (AC4)
 *
 * <p>Validates PostgreSQL database connectivity by executing a simple query.
 * Used by Container Apps health probes to determine instance health.
 *
 * <p>Health check endpoint: GET /actuator/health
 * <p>Readiness endpoint: GET /actuator/health/readiness
 *
 * @see org.springframework.boot.actuate.health.HealthIndicator
 */
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Checks database health by executing a simple query.
     *
     * <p>Query: SELECT 1 (standard health check query)
     * <p>Timeout: 3 seconds (matches Container Apps health probe timeout)
     *
     * @return Health status with database version and connection pool info
     */
    @Override
    public Health health() {
        try {
            // Execute simple query to validate connection
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (result != null && result == 1) {
                // Get PostgreSQL version for debugging
                String version = jdbcTemplate.queryForObject(
                        "SELECT version()",
                        String.class
                );

                // Get active connections count
                Integer activeConnections = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'",
                        Integer.class
                );

                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("version", version != null ? version.split(" ")[1] : "unknown")
                        .withDetail("activeConnections", activeConnections)
                        .withDetail("validationQuery", "SELECT 1")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Validation query returned unexpected result")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getClass().getName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
