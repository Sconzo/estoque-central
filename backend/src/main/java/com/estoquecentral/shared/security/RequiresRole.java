package com.estoquecentral.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequiresRole - Custom annotation for method-level role-based authorization
 *
 * <p>This annotation can be applied to controller methods or service methods
 * to enforce role-based access control. It supports both OR and AND logic for
 * multiple roles.
 *
 * <p><strong>Usage Examples:</strong>
 * <pre>{@code
 * // Single role - user must have ESTOQUISTA role
 * @RequiresRole("ESTOQUISTA")
 * public ResponseEntity<?> listarMovimentacoes() { }
 *
 * // Multiple roles with OR logic (default) - user needs ADMIN OR GERENTE
 * @RequiresRole({"ADMIN", "GERENTE"})
 * public ResponseEntity<?> criarProduto() { }
 *
 * // Multiple roles with AND logic - user needs BOTH roles
 * @RequiresRole(value = {"ADMIN", "FISCAL"}, requireAll = true)
 * public ResponseEntity<?> emitirNFe() { }
 * }</pre>
 *
 * <p><strong>How it works:</strong>
 * <ul>
 *   <li>Validated by {@link RoleCheckAspect} using Spring AOP</li>
 *   <li>Fetches user roles from UserService.getUserRoles()</li>
 *   <li>OR logic (default): User needs at least ONE of the specified roles</li>
 *   <li>AND logic (requireAll=true): User needs ALL specified roles</li>
 *   <li>Throws AccessDeniedException if user lacks required roles</li>
 * </ul>
 *
 * @see RoleCheckAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    /**
     * Array of role names required to access the annotated method.
     *
     * <p>Role names should match the 'nome' field in the Role entity
     * (e.g., "ADMIN", "GERENTE", "VENDEDOR").
     *
     * @return array of role names
     */
    String[] value();

    /**
     * Determines the logic for evaluating multiple roles.
     *
     * <p>If false (default): OR logic - user needs at least ONE of the roles
     * <br>If true: AND logic - user needs ALL of the roles
     *
     * @return true for AND logic, false for OR logic (default)
     */
    boolean requireAll() default false;
}
