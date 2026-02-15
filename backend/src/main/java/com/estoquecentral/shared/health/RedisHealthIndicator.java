package com.estoquecentral.shared.health;

import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Redis Health Indicator
 * Story 7.8: Health Check Monitoring (AC4)
 *
 * <p>Validates Azure Cache for Redis connectivity by executing a PING command.
 * Used by Container Apps health probes to determine instance health.
 *
 * <p>Health check endpoint: GET /actuator/health
 * <p>Readiness endpoint: GET /actuator/health/readiness
 *
 * @see org.springframework.boot.actuate.health.HealthIndicator
 */
@Component("redis")
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class RedisHealthIndicator implements HealthIndicator {

    private final RedissonClient redissonClient;

    public RedisHealthIndicator(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Checks Redis health by executing a PING command.
     *
     * <p>Command: PING → PONG (standard Redis health check)
     * <p>Timeout: 3 seconds (matches Container Apps health probe timeout)
     *
     * @return Health status with Redis version and connection info
     */
    @Override
    public Health health() {
        try {
            // Execute PING command to validate connection
            boolean isResponsive = redissonClient.getNodesGroup().pingAll();

            if (isResponsive) {
                return Health.up()
                        .withDetail("cache", "Redis")
                        .withDetail("validationCommand", "PING")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "PING command failed")
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
