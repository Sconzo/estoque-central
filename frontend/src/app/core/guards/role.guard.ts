import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * RoleGuard - Protects routes based on required roles
 *
 * Usage in routes:
 * ```typescript
 * {
 *   path: 'estoque',
 *   component: EstoqueComponent,
 *   canActivate: [RoleGuard],
 *   data: { requiredRole: 'ESTOQUE' }
 * }
 * ```
 *
 * Or with multiple roles (OR logic):
 * ```typescript
 * data: { requiredRoles: ['ADMIN', 'GERENTE'] }
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuard {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    // Check if user is authenticated first
    if (!this.authService.isAuthenticated()) {
      return this.router.createUrlTree(['/login']);
    }

    // Get required role(s) from route data
    const requiredRole = route.data['requiredRole'] as string;
    const requiredRoles = route.data['requiredRoles'] as string[];

    // If no role requirement, allow access
    if (!requiredRole && (!requiredRoles || requiredRoles.length === 0)) {
      return true;
    }

    // Get user's roles from JWT token
    const userRoles = this.authService.getRolesFromToken();

    // Check single required role
    if (requiredRole && userRoles.includes(requiredRole)) {
      return true;
    }

    // Check multiple required roles (OR logic - user needs at least one)
    if (requiredRoles && requiredRoles.some(role => userRoles.includes(role))) {
      return true;
    }

    // User doesn't have required role(s) - redirect to 403
    console.warn(`Access denied: User doesn't have required role(s)`, { requiredRole, requiredRoles, userRoles });
    return this.router.createUrlTree(['/403']);
  }
}
