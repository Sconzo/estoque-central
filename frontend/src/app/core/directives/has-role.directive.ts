import { Directive, Input, TemplateRef, ViewContainerRef, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';

/**
 * Structural directive for role-based UI rendering (Story 10.9 - AC1).
 *
 * Usage:
 * ```html
 * <!-- Show element only to ADMIN users -->
 * <div *appHasRole="'ADMIN'">Admin only content</div>
 *
 * <!-- Show element to multiple roles -->
 * <div *appHasRole="['ADMIN', 'GERENTE']">Admin or Manager content</div>
 * ```
 *
 * @since Epic 10 - Gestão de Colaboradores e Permissões RBAC
 */
@Directive({
  selector: '[appHasRole]',
  standalone: true
})
export class HasRoleDirective implements OnInit {
  @Input() appHasRole: string | string[] = [];

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.updateView();
  }

  /**
   * Updates the view based on user's roles.
   * Shows the element if user has any of the required roles.
   * Hides the element otherwise.
   */
  private updateView(): void {
    const userRoles = this.authService.getUserRoles();
    const requiredRoles = Array.isArray(this.appHasRole)
      ? this.appHasRole
      : [this.appHasRole];

    // Check if user has at least one of the required roles
    const hasRole = requiredRoles.some(role => userRoles.includes(role));

    if (hasRole) {
      // User has required role - show the element
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      // User doesn't have required role - hide the element
      this.viewContainer.clear();
    }
  }
}
