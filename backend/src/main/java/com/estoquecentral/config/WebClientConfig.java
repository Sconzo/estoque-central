package com.estoquecentral.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web client configuration
 * Provides RestTemplate bean for HTTP client operations
 */
@Configuration
public class WebClientConfig {

    /**
     * RestTemplate bean for HTTP client operations
     * Used by marketplace integrations and external API calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
