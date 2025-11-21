import { Directive, Input, TemplateRef, ViewContainerRef, OnInit } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';

/**
 * HasRoleDirective - Conditionally shows/hides elements based on user roles
 *
 * Usage (single role):
 * ```html
 * <button *hasRole="'ADMIN'" (click)="deletar()">
 *   Deletar
 * </button>
 * ```
 *
 * Usage (multiple roles - OR logic):
 * ```html
 * <div *hasRole="['ADMIN', 'GERENTE']">
 *   Conteúdo visível para ADMIN ou GERENTE
 * </div>
 * ```
 *
 * The directive will hide the element if user doesn't have the required role(s).
 */
@Directive({
  selector: '[hasRole]',
  standalone: true
})
export class HasRoleDirective implements OnInit {
  private requiredRoles: string[] = [];

  @Input() set hasRole(roles: string | string[]) {
    this.requiredRoles = Array.isArray(roles) ? roles : [roles];
    this.updateView();
  }

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.updateView();
  }

  private updateView(): void {
    // Check if user is authenticated
    if (!this.authService.isAuthenticated()) {
      this.viewContainer.clear();
      return;
    }

    // Get user's roles from JWT token
    const userRoles = this.authService.getRolesFromToken();

    // Check if user has at least one of the required roles (OR logic)
    const hasRequiredRole = this.requiredRoles.some(role => userRoles.includes(role));

    if (hasRequiredRole) {
      // User has required role - show element
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      // User doesn't have role - hide element
      this.viewContainer.clear();
    }
  }
}
