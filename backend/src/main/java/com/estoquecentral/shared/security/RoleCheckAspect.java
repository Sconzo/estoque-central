package com.estoquecentral.shared.security;

import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Role;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RoleCheckAspect - AOP aspect for enforcing @RequiresRole annotation
 *
 * <p>This aspect intercepts methods annotated with {@link RequiresRole} and
 * validates that the authenticated user has the required roles.
 *
 * <p><strong>How it works:</strong>
 * <ol>
 *   <li>Intercepts method calls with @RequiresRole annotation</li>
 *   <li>Extracts userId from SecurityContext (set by JwtAuthenticationFilter)</li>
 *   <li>Fetches user's roles via UserService.getUserRoles()</li>
 *   <li>Compares user roles against required roles (OR or AND logic)</li>
 *   <li>Throws AccessDeniedException if validation fails</li>
 * </ol>
 *
 * <p><strong>Prerequisites:</strong>
 * <ul>
 *   <li>Spring Security must be configured</li>
 *   <li>JwtAuthenticationFilter must set Authentication in SecurityContext</li>
 *   <li>Authentication.getName() must return user ID as String</li>
 *   <li>AspectJ dependency must be in pom.xml</li>
 * </ul>
 *
 * @see RequiresRole
 * @see UserService
 */
@Aspect
@Component
public class RoleCheckAspect {

    private static final Logger logger = LoggerFactory.getLogger(RoleCheckAspect.class);

    private final UserService userService;

    @Autowired
    public RoleCheckAspect(UserService userService) {
        this.userService = userService;
    }

    /**
     * Before advice that checks role requirements before method execution.
     *
     * <p>This method runs before any method annotated with @RequiresRole.
     * It validates the user's roles against the annotation's requirements.
     *
     * @param joinPoint the join point representing the intercepted method
     * @param requiresRole the RequiresRole annotation instance
     * @throws AccessDeniedException if user is not authenticated or lacks required roles
     */
    @Before("@annotation(requiresRole)")
    public void checkRole(JoinPoint joinPoint, RequiresRole requiresRole) {
        logger.debug("Checking role requirements for method: {}", joinPoint.getSignature().getName());

        // 1. Get authenticated user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Access denied: User not authenticated");
            throw new AccessDeniedException("User not authenticated");
        }

        // 2. Extract userId from authentication (set by JwtAuthenticationFilter)
        String userIdStr = authentication.getName();
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user ID in authentication: {}", userIdStr);
            throw new AccessDeniedException("Invalid user ID in authentication");
        }

        // 3. Fetch user's roles from profile
        List<Role> userRoles = userService.getUserRoles(userId);
        List<String> roleNames = userRoles.stream()
                .map(Role::getNome)
                .collect(Collectors.toList());

        logger.debug("User {} has roles: {}", userId, roleNames);

        // 4. Get required roles from annotation
        String[] requiredRoles = requiresRole.value();
        logger.debug("Required roles: {} (requireAll={})", Arrays.toString(requiredRoles), requiresRole.requireAll());

        // 5. Check if user has required roles
        boolean hasAccess;

        if (requiresRole.requireAll()) {
            // AND logic: user must have ALL required roles
            hasAccess = roleNames.containsAll(Arrays.asList(requiredRoles));
            logger.debug("AND logic: User has all required roles? {}", hasAccess);
        } else {
            // OR logic (default): user must have AT LEAST ONE required role
            hasAccess = Arrays.stream(requiredRoles)
                    .anyMatch(roleNames::contains);
            logger.debug("OR logic: User has at least one required role? {}", hasAccess);
        }

        // 6. Deny access if user lacks required roles
        if (!hasAccess) {
            logger.warn("Access denied for user {}: Missing required role(s): {}",
                    userId, Arrays.toString(requiredRoles));
            throw new AccessDeniedException("User lacks required role(s): " + Arrays.toString(requiredRoles));
        }

        logger.debug("Access granted for user {}", userId);
    }
}
