import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { CompanyService, UserCompanyResponse, SwitchContextResponse } from '../../../core/services/company.service';
import { AuthService } from '../../../core/auth/auth.service';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * SelectCompanyComponent - Story 9.2
 *
 * Allows users with multiple companies to select which one to access.
 *
 * Story 9.2 - Frontend Company Selection:
 * - AC1: Automatic redirect after OAuth login (handled by AuthGuard)
 * - AC2: Company list display with MatCard components
 * - AC3: Company selection with loading spinner
 * - AC4: Success handling with JWT storage and redirect to dashboard
 * - AC5: Create new company button
 * - AC6: Responsive design (Desktop, Tablet, Mobile)
 *
 * @since Epic 9 - Story 9.2
 */
@Component({
  selector: 'app-select-company',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './select-company.component.html',
  styleUrls: ['./select-company.component.scss']
})
export class SelectCompanyComponent implements OnInit, OnDestroy {
  companies: UserCompanyResponse[] = [];
  isLoading = true;
  selectingTenantId: string | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private companyService: CompanyService,
    private authService: AuthService,
    private tenantService: TenantService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCompanies();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * AC2: Loads user's companies from /api/users/me/companies
   */
  private loadCompanies(): void {
    this.companyService.getUserCompanies()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (companies) => {
          this.companies = companies;
          this.isLoading = false;

          // Auto-select if user only has 1 company AND no current tenant context
          // (i.e., user is coming from login, not from "Gerenciar Empresas")
          const currentTenantId = this.tenantService.getCurrentTenant();
          if (companies.length === 1 && !currentTenantId) {
            this.selectCompany(companies[0]);
          }
        },
        error: (error) => {
          console.error('Error loading companies:', error);
          this.isLoading = false;
          this.showError('Erro ao carregar empresas. Tente novamente.');
        }
      });
  }

  /**
   * AC3: Selects a company and switches context
   * AC4: Success handling with JWT storage and redirect
   */
  selectCompany(company: UserCompanyResponse): void {
    // AC3: Show loading spinner
    this.selectingTenantId = company.tenantId;

    // AC3: PUT request to /api/users/me/context
    this.companyService.switchContext(company.tenantId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: SwitchContextResponse) => {
          // AC4: Store new JWT token
          if (response.token) {
            localStorage.setItem('jwt_token', response.token);
          }

          // AC4: Update tenant context
          this.tenantService.setCurrentTenant(response.tenantId);

          // AC4: Show success message
          this.snackBar.open(
            `Acessando ${response.companyName}...`,
            '',
            {
              duration: 2000,
              panelClass: ['success-snackbar'],
              horizontalPosition: 'center',
              verticalPosition: 'top'
            }
          );

          // AC4: Redirect to dashboard
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 500);
        },
        error: (error) => {
          console.error('Error switching context:', error);
          this.selectingTenantId = null;

          // Handle different error types
          let errorMessage = 'Erro ao acessar empresa. Tente novamente.';

          if (error.status === 403) {
            errorMessage = 'Você não tem permissão para acessar esta empresa.';
          } else if (error.status === 404) {
            errorMessage = 'Empresa não encontrada.';
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }

          this.showError(errorMessage);
        }
      });
  }

  /**
   * AC2: Formats CNPJ for display
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
   * Shows error message with snackbar
   */
  private showError(message: string): void {
    this.snackBar.open(
      message,
      'Fechar',
      {
        duration: 5000,
        panelClass: ['error-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'top'
      }
    );
  }
}
