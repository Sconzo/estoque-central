package com.estoquecentral.auth.adapter.in.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig - Spring Security configuration
 *
 * <p>This configuration:
 * <ul>
 *   <li>Disables CSRF (not needed for stateless JWT API)</li>
 *   <li>Configures CORS for frontend access</li>
 *   <li>Disables session management (stateless JWT)</li>
 *   <li>Configures public endpoints (no authentication required)</li>
 *   <li>Configures protected endpoints (authentication required)</li>
 *   <li>Adds JwtAuthenticationFilter to the security chain</li>
 * </ul>
 *
 * <p><strong>Public Endpoints (no authentication):</strong>
 * <ul>
 *   <li>POST /api/auth/google/callback</li>
 *   <li>GET /api/auth/health</li>
 *   <li>POST /api/tenants (tenant creation)</li>
 *   <li>GET /actuator/health</li>
 *   <li>GET /swagger-ui.html, /swagger-ui/**, /v3/api-docs/**</li>
 * </ul>
 *
 * <p><strong>Protected Endpoints (authentication required):</strong>
 * <ul>
 *   <li>All other /api/** endpoints</li>
 * </ul>
 *
 * @see JwtAuthenticationFilter
 * @see com.estoquecentral.auth.adapter.in.AuthController
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity object
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless JWT API)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable session management (stateless JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers(
                                "/api/auth/google/callback",
                                "/api/auth/health",
                                "/api/tenants", // Tenant creation (public)
                                "/actuator/health",
                                "/actuator/health/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Protected endpoints (authentication required)
                        .requestMatchers("/api/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS to allow frontend access.
     *
     * <p>In production, you should restrict allowed origins to your frontend domain only.
     * For example: http://localhost:4200, https://app.estoquecentral.com
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow frontend origins
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",     // Angular dev server
                "http://localhost:8080",     // Backend serving frontend
                "http://localhost:3000"      // Alternative frontend port
                // Add production frontend URLs here
        ));

        // Allow all HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache CORS preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
