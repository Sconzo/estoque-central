package com.estoquecentral.shared.tenant.config;

import com.estoquecentral.common.CurrentUserArgumentResolver;
import com.estoquecentral.shared.tenant.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * WebMvcConfig - Registers TenantInterceptor globally
 *
 * <p>This configuration registers the {@link TenantInterceptor} to intercept
 * ALL HTTP requests and extract tenant identification before request processing.
 *
 * <p>The interceptor runs for ALL paths by default, including:
 * <ul>
 *   <li>/api/** - API endpoints (tenant-specific data)</li>
 *   <li>/actuator/** - Health checks (uses public schema)</li>
 *   <li>Any other endpoints</li>
 * </ul>
 *
 * <p>If a request doesn't have tenant identification (no X-Tenant-ID header
 * or subdomain), the system defaults to the "public" schema, which is appropriate
 * for tenant creation and health check endpoints.
 *
 * @see TenantInterceptor
 * @see com.estoquecentral.shared.tenant.TenantContext
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Registers the TenantInterceptor to intercept all HTTP requests.
     *
     * <p>The interceptor is registered with default path pattern "/**",
     * meaning it intercepts ALL requests.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor())
                .addPathPatterns("/**"); // Intercept all requests
    }

    /**
     * Registers custom argument resolvers for controller methods.
     *
     * <p>Adds the CurrentUserArgumentResolver to automatically inject
     * CurrentUser instances in controller method parameters.
     *
     * @param resolvers the list of argument resolvers
     */
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}
