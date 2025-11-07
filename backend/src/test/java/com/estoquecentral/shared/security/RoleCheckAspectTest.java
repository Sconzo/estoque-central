package com.estoquecentral.shared.security;

import com.estoquecentral.auth.application.UserService;
import com.estoquecentral.auth.domain.Role;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleCheckAspect
 *
 * <p>Tests the AOP aspect that enforces @RequiresRole annotation:
 * <ul>
 *   <li>Access granted when user has required role</li>
 *   <li>Access denied when user lacks required role</li>
 *   <li>OR logic (default) - user needs at least one role</li>
 *   <li>AND logic - user needs all roles</li>
 *   <li>Access denied when user not authenticated</li>
 *   <li>Access denied when user has no profile</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleCheckAspect Unit Tests")
class RoleCheckAspectTest {

    @Mock
    private UserService userService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private RoleCheckAspect roleCheckAspect;

    private UUID userId;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                Collections.emptyList()
        );

        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));
        when(joinPoint.getSignature().getName()).thenReturn("testMethod");
    }

    @Test
    @DisplayName("Should grant access when user has required role (single role)")
    void shouldGrantAccessWhenUserHasRequiredRole() {
        // Given
        Role adminRole = new Role(UUID.randomUUID(), "ADMIN", "Admin", "SISTEMA");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(List.of(adminRole));

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN"}, false);

        // When/Then
        assertThatCode(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .doesNotThrowAnyException();

        verify(userService, times(1)).getUserRoles(userId);
    }

    @Test
    @DisplayName("Should grant access when user has one of required roles (OR logic)")
    void shouldGrantAccessWhenUserHasOneOfRequiredRoles() {
        // Given
        Role gerenteRole = new Role(UUID.randomUUID(), "GERENTE", "Gerente", "GESTAO");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(List.of(gerenteRole));

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN", "GERENTE"}, false);

        // When/Then
        assertThatCode(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .doesNotThrowAnyException();

        verify(userService, times(1)).getUserRoles(userId);
    }

    @Test
    @DisplayName("Should grant access when user has all required roles (AND logic)")
    void shouldGrantAccessWhenUserHasAllRequiredRoles() {
        // Given
        Role adminRole = new Role(UUID.randomUUID(), "ADMIN", "Admin", "SISTEMA");
        Role fiscalRole = new Role(UUID.randomUUID(), "FISCAL", "Fiscal", "OPERACIONAL");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(List.of(adminRole, fiscalRole));

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN", "FISCAL"}, true);

        // When/Then
        assertThatCode(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .doesNotThrowAnyException();

        verify(userService, times(1)).getUserRoles(userId);
    }

    @Test
    @DisplayName("Should deny access when user lacks required role")
    void shouldDenyAccessWhenUserLacksRequiredRole() {
        // Given
        Role vendedorRole = new Role(UUID.randomUUID(), "VENDEDOR", "Vendedor", "OPERACIONAL");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(List.of(vendedorRole));

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN"}, false);

        // When/Then
        assertThatThrownBy(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User lacks required role");

        verify(userService, times(1)).getUserRoles(userId);
    }

    @Test
    @DisplayName("Should deny access when user lacks all required roles (OR logic)")
    void shouldDenyAccessWhenUserLacksAllRequiredRoles() {
        // Given
        Role vendedorRole = new Role(UUID.randomUUID(), "VENDEDOR", "Vendedor", "OPERACIONAL");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(List.of(vendedorRole));

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN", "GERENTE"}, false);

        // When/Then
        assertThatThrownBy(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User lacks required role");

        verify(userService, times(1)).getUserRoles(userId);
    }

    @Test
    @DisplayName("Should deny access when user lacks one required role (AND logic)")
    void shouldDenyAccessWhenUserLacksOneRequiredRole() {
        // Given
        Role adminRole = new Role(UUID.randomUUID(), "ADMIN", "Admin", "SISTEMA");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(List.of(adminRole));

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN", "FISCAL"}, true);

        // When/Then
        assertThatThrownBy(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User lacks required role");

        verify(userService, times(1)).getUserRoles(userId);
    }

    @Test
    @DisplayName("Should deny access when user not authenticated")
    void shouldDenyAccessWhenUserNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN"}, false);

        // When/Then
        assertThatThrownBy(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User not authenticated");

        verify(userService, never()).getUserRoles(any());
    }

    @Test
    @DisplayName("Should deny access when authentication is not authenticated")
    void shouldDenyAccessWhenAuthenticationNotAuthenticated() {
        // Given
        Authentication unauthenticated = mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(unauthenticated);

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN"}, false);

        // When/Then
        assertThatThrownBy(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User not authenticated");

        verify(userService, never()).getUserRoles(any());
    }

    @Test
    @DisplayName("Should deny access when user ID is invalid")
    void shouldDenyAccessWhenUserIdIsInvalid() {
        // Given
        Authentication invalidAuth = new UsernamePasswordAuthenticationToken(
                "invalid-uuid",
                null,
                Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(invalidAuth);

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN"}, false);

        // When/Then
        assertThatThrownBy(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userService, never()).getUserRoles(any());
    }

    @Test
    @DisplayName("Should deny access when user has no profile (empty roles)")
    void shouldDenyAccessWhenUserHasNoProfile() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(Collections.emptyList());

        RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN"}, false);

        // When/Then
        assertThatThrownBy(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User lacks required role");

        verify(userService, times(1)).getUserRoles(userId);
    }

    @Test
    @DisplayName("Should grant access when user has extra roles beyond required")
    void shouldGrantAccessWhenUserHasExtraRoles() {
        // Given
        Role adminRole = new Role(UUID.randomUUID(), "ADMIN", "Admin", "SISTEMA");
        Role gerenteRole = new Role(UUID.randomUUID(), "GERENTE", "Gerente", "GESTAO");
        Role vendedorRole = new Role(UUID.randomUUID(), "VENDEDOR", "Vendedor", "OPERACIONAL");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserRoles(userId)).thenReturn(List.of(adminRole, gerenteRole, vendedorRole));

        RequiresRole requiresRole = createRequiresRole(new String[]{"GERENTE"}, false);

        // When/Then
        assertThatCode(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
                .doesNotThrowAnyException();

        verify(userService, times(1)).getUserRoles(userId);
    }

    /**
     * Helper method to create a RequiresRole annotation mock
     */
    private RequiresRole createRequiresRole(String[] roles, boolean requireAll) {
        RequiresRole requiresRole = mock(RequiresRole.class);
        when(requiresRole.value()).thenReturn(roles);
        when(requiresRole.requireAll()).thenReturn(requireAll);
        return requiresRole;
    }
}
