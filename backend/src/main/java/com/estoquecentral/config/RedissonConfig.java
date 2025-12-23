package com.estoquecentral.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedissonConfig - Redis client configuration with TLS support
 * Story 4.4: NFCe Retry Queue and Failure Management
 * Story 7.5: Redis Cache with Tenant Isolation
 *
 * <p>Configures Redisson client for:
 * <ul>
 *   <li>Distributed delayed queues (NFCe retry mechanism)</li>
 *   <li>Spring Cache backend (tenant-isolated caching)</li>
 *   <li>Session management (future feature)</li>
 * </ul>
 *
 * <p><strong>Production Configuration:</strong>
 * <ul>
 *   <li>TLS 1.2+ enabled via rediss:// protocol</li>
 *   <li>Azure Cache for Redis compatible</li>
 *   <li>Connection pooling optimized for multi-tenant workload</li>
 *   <li>Automatic retry with exponential backoff</li>
 * </ul>
 *
 * <p><strong>Local Development:</strong>
 * Uses redis:// protocol (no TLS) for local Docker container.
 *
 * @see com.estoquecentral.config.CacheConfig
 * @see com.estoquecentral.shared.cache.CacheInvalidationService
 */
@Configuration
public class RedissonConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedissonConfig.class);

    @Value("${redis.address:redis://localhost:6379}")
    private String redisAddress;

    @Value("${redis.password:#{null}}")
    private String redisPassword;

    @Value("${redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${redis.database:0}")
    private int database;

    /**
     * Creates RedissonClient bean for distributed data structures and caching.
     *
     * <p><strong>TLS Support:</strong>
     * If redis.address starts with "rediss://", TLS is automatically enabled.
     * For Azure Cache for Redis, use: rediss://{name}.redis.cache.windows.net:6380
     *
     * <p><strong>Connection Pooling:</strong>
     * Configured for high-concurrency multi-tenant environment:
     * <ul>
     *   <li>Minimum idle: 5 connections</li>
     *   <li>Pool size: 20 connections</li>
     *   <li>Subscription pool: 10 connections</li>
     * </ul>
     *
     * @return configured RedissonClient
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // Detect TLS from address (rediss:// vs redis://)
        boolean useTls = redisAddress.startsWith("rediss://") || sslEnabled;

        // Single server configuration
        var singleServerConfig = config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(database)
                // Connection pool settings (optimized for multi-tenant)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20)
                .setSubscriptionConnectionMinimumIdleSize(2)
                .setSubscriptionConnectionPoolSize(10)
                // Timeout settings
                .setConnectTimeout(10000)    // 10 seconds
                .setTimeout(3000)            // 3 seconds
                .setIdleConnectionTimeout(30000)  // 30 seconds
                // Retry settings
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                // Keepalive
                .setKeepAlive(true)
                .setPingConnectionInterval(30000);  // Ping every 30s

        // Set password if provided
        if (redisPassword != null && !redisPassword.isEmpty()) {
            singleServerConfig.setPassword(redisPassword);
        }

        // Configure TLS if enabled
        if (useTls) {
            singleServerConfig.setSslEnableEndpointIdentification(true);
            singleServerConfig.setSslProvider(SslProvider.JDK);
            // Azure Cache for Redis requires TLS 1.2+
            singleServerConfig.setSslProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            logger.info("Redis TLS enabled (protocols: TLSv1.2, TLSv1.3)");
        }

        // Codec configuration (use default JSON codec for cache serialization)
        // config.setCodec(new JsonJacksonCodec());  // Optional: use Jackson for serialization

        logger.info("Redisson client configured: {} (TLS: {}, DB: {})",
                redisAddress, useTls, database);

        return Redisson.create(config);
    }
}
