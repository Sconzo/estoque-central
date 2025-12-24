import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from '../auth/auth.service';
import { CompanyService } from '../services/company.service';
import { TenantService } from '../services/tenant.service';

/**
 * AuthGuard - Protects routes requiring authentication and manages company context
 *
 * Story 8.3 - AC2, AC3, AC4:
 * - Redirects unauthenticated users to login page
 * - After login, checks user's companies:
 *   - 0 companies → redirect to /create-company (AC2)
 *   - 1 company → auto-select and redirect to /dashboard (AC3)
 *   - 2+ companies → redirect to /select-company (AC4)
 *
 * Story 9.4 - AC5: Context consistency across routes
 * - Maintains tenant context consistency across all route navigation
 * - Uses TenantService localStorage persistence to preserve context
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard {

  constructor(
    private authService: AuthService,
    private companyService: CompanyService,
    private tenantService: TenantService,
    private router: Router
  ) {}

  canActivate(): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    // Check if user is authenticated
    if (!this.authService.isAuthenticated()) {
      return this.router.createUrlTree(['/login']);
    }

    // Story 9.4 - AC5: Check if tenant context is already set (persistence)
    const currentTenantId = this.tenantService.getCurrentTenant();
    if (currentTenantId) {
      // User has already selected a company, allow access
      // Context is maintained consistently across all routes
      return true;
    }

    // AC1: Fetch user's companies from backend
    return this.companyService.getUserCompanies().pipe(
      map(companies => {
        if (companies.length === 0) {
          // AC2: Zero companies → redirect to create-company
          console.log('AuthGuard: User has no companies, redirecting to /create-company');
          return this.router.createUrlTree(['/create-company']);
        } else if (companies.length === 1) {
          // AC3: One company → auto-select and redirect to dashboard
          console.log('AuthGuard: User has 1 company, auto-selecting and redirecting to /dashboard');
          this.tenantService.setCurrentTenant(companies[0].tenantId);
          return this.router.createUrlTree(['/dashboard']);
        } else {
          // AC4: Multiple companies → redirect to select-company
          console.log(`AuthGuard: User has ${companies.length} companies, redirecting to /select-company`);
          return this.router.createUrlTree(['/select-company']);
        }
      }),
      catchError(error => {
        console.error('AuthGuard: Error fetching user companies', error);
        // On error, redirect to create-company as safe fallback
        return of(this.router.createUrlTree(['/create-company']));
      })
    );
  }
}
