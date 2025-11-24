package com.estoquecentral.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedissonConfig - Redis client configuration
 * Story 4.4: NFCe Retry Queue and Failure Management
 *
 * <p>Configures Redisson client for distributed delayed queue
 * Used for NFCe retry mechanism with exponential backoff
 */
@Configuration
public class RedissonConfig {

    @Value("${redis.address:redis://localhost:6379}")
    private String redisAddress;

    @Value("${redis.password:#{null}}")
    private String redisPassword;

    /**
     * Creates RedissonClient bean for distributed data structures
     * Single-server configuration for simplicity
     *
     * @return configured RedissonClient
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // Single server configuration
        config.useSingleServer()
                .setAddress(redisAddress)
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(10)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        // Set password if provided
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
