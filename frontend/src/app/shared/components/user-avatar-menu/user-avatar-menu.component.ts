import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { CompanyService, UserCompanyResponse, SwitchContextResponse } from '../../../core/services/company.service';
import { AuthService } from '../../../core/auth/auth.service';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * UserAvatarMenuComponent - Story 9.3
 *
 * Avatar menu with company context switching functionality.
 *
 * Story 9.3 - User Avatar Menu with Context Switching:
 * - AC1: Avatar display with user initials or photo
 * - AC2: Menu content (MatMenu) with current company, other companies, settings, logout
 * - AC3: Company list with checkmark for current company
 * - AC4: Context switch via PUT /api/users/me/context
 * - AC5: Success feedback with snackbar and page refresh
 * - AC6: Error handling with snackbar
 *
 * @since Epic 9 - Story 9.3
 */
@Component({
  selector: 'app-user-avatar-menu',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './user-avatar-menu.component.html',
  styleUrl: './user-avatar-menu.component.scss'
})
export class UserAvatarMenuComponent implements OnInit, OnDestroy {
  currentUser = signal<any>(null);
  currentCompany: UserCompanyResponse | null = null;
  otherCompanies: UserCompanyResponse[] = [];
  isLoadingCompanies = false;
  switchingToTenantId: string | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private companyService: CompanyService,
    private tenantService: TenantService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Load current user
    const user = this.authService.getCurrentUser();
    this.currentUser.set(user);

    // Load companies when component initializes
    this.loadCompanies();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * AC1: Get user initials for avatar
   */
  getUserInitials(): string {
    const user = this.currentUser();
    if (!user || !user.nome) {
      return '?';
    }

    const names = user.nome.trim().split(' ');
    if (names.length === 1) {
      return names[0].substring(0, 2).toUpperCase();
    }

    return (names[0][0] + names[names.length - 1][0]).toUpperCase();
  }

  /**
   * AC1: Get avatar background color based on user name
   */
  getAvatarColor(): string {
    const user = this.currentUser();
    if (!user || !user.nome) {
      return '#0d9488'; // Default Teal
    }

    // Generate color from user name
    const colors = [
      '#0d9488', // Teal
      '#1976D2', // Blue
      '#388E3C', // Green
      '#D32F2F', // Red
      '#F57C00', // Orange
      '#7B1FA2', // Purple
      '#0097A7', // Cyan
      '#C2185B'  // Pink
    ];

    const charCode = user.nome.charCodeAt(0);
    return colors[charCode % colors.length];
  }

  /**
   * AC3: Load companies and separate current from others
   */
  private loadCompanies(): void {
    this.isLoadingCompanies = true;

    this.companyService.getUserCompanies()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (companies) => {
          const currentTenantId = this.tenantService.getCurrentTenant();

          // Separate current company from others
          this.currentCompany = companies.find(c => c.tenantId === currentTenantId) || null;
          this.otherCompanies = companies.filter(c => c.tenantId !== currentTenantId);

          this.isLoadingCompanies = false;
        },
        error: (error) => {
          console.error('Error loading companies:', error);
          this.isLoadingCompanies = false;
        }
      });
  }

  /**
   * AC4: Switch to a different company
   * AC5: Success feedback with snackbar and page refresh
   * AC6: Error handling
   * Story 9.5 - AC2, AC3: Navigation preservation + reactive data refresh (no page reload)
   */
  switchToCompany(company: UserCompanyResponse): void {
    const startTime = performance.now(); // AC5: Performance tracking

    // AC4: Show loading state
    this.switchingToTenantId = company.tenantId;

    // AC4: PUT request to /api/users/me/context
    this.companyService.switchContext(company.tenantId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: SwitchContextResponse) => {
          const duration = performance.now() - startTime;

          // AC5: Store new JWT token
          if (response.token) {
            localStorage.setItem('jwt_token', response.token);
          }

          // Story 9.5 - AC2: Update tenant context (triggers Signal update)
          // This will reactively update all components subscribed to currentTenant$
          this.tenantService.setCurrentTenant(response.tenantId);

          // AC5: Success feedback (UX14)
          this.snackBar.open(
            `Trocou para ${response.companyName}`,
            '',
            {
              duration: 3000,
              panelClass: ['success-snackbar'],
              horizontalPosition: 'center',
              verticalPosition: 'top'
            }
          );

          // Log performance (AC5: FR10 - should complete in < 500ms)
          if (duration > 500) {
            console.warn(`Context switch took ${duration.toFixed(0)}ms (exceeds 500ms target)`);
          } else {
            console.log(`Context switch completed in ${duration.toFixed(0)}ms`);
          }

          // Story 9.5 - AC3: Navigation preservation - NO page reload
          // Components reactively update via Signal subscription
          // Current route is preserved (e.g., stay on /products)
          this.switchingToTenantId = null;
          this.loadCompanies(); // Refresh company list to update UI
        },
        error: (error) => {
          console.error('Error switching context:', error);
          this.switchingToTenantId = null;

          // AC6: Error handling (UX25)
          let errorMessage = 'Erro ao trocar de empresa. Tente novamente.';

          if (error.status === 403) {
            errorMessage = 'Você não tem permissão para acessar esta empresa.';
          } else if (error.status === 404) {
            errorMessage = 'Empresa não encontrada.';
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }

          this.snackBar.open(
            errorMessage,
            'Fechar',
            {
              duration: 5000,
              panelClass: ['error-snackbar'],
              horizontalPosition: 'center',
              verticalPosition: 'top'
            }
          );
        }
      });
  }

  /**
   * Format CNPJ for display
   * Format: XX.XXX.XXX/XXXX-XX
   */
  formatCnpj(cnpj: string): string {
    if (!cnpj || cnpj.length !== 14) {
      return cnpj;
    }

    return cnpj.replace(
      /^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/,
      '$1.$2.$3/$4-$5'
    );
  }

  /**
   * Logout user
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
