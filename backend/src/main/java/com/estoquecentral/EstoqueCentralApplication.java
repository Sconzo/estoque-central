package com.estoquecentral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

/**
 * Estoque Central - Sistema ERP Omnichannel Brasileiro
 *
 * Main application entry point.
 * Uses Spring Modulith for bounded context separation via packages.
 */
@SpringBootApplication(
        exclude = {
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class
        },
        excludeName = {
                "org.redisson.spring.starter.RedissonAutoConfiguration",
                "org.redisson.spring.starter.RedissonAutoConfigurationV2"
        }
)
public class EstoqueCentralApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstoqueCentralApplication.class, args);
    }
}
