import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { CompanyService, CreateCompanyRequest } from '../../../core/services/company.service';
import { AuthService } from '../../../core/auth/auth.service';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * CreateCompanyComponent - Story 8.2 + 8.6
 *
 * Story 8.2:
 * - AC2: Company registration form with reactive forms
 * - AC3: Form validation with inline error messages
 * - AC4: Form submission to /api/public/companies
 * - AC5: Loading state during provisioning (15-30 seconds)
 * - AC6: Success handling with redirect to /dashboard
 * - AC7: Error handling with retry button
 *
 * Story 8.6:
 * - AC1: Button loading state with spinner and "Criando..." text
 * - AC2: Full-screen loading overlay with message
 * - AC3: Success feedback with MatSnackBar (3s auto-dismiss)
 * - AC4: Error feedback with retry action (persists until dismissed)
 * - AC5: Design principles (Deep Purple theme, 44x44px touch targets)
 */
@Component({
  selector: 'app-create-company',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './create-company.component.html',
  styleUrls: ['./create-company.component.scss']
})
export class CreateCompanyComponent implements OnInit, OnDestroy {
  companyForm!: FormGroup;
  isLoading = false;
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private companyService: CompanyService,
    private authService: AuthService,
    private tenantService: TenantService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * AC2: Initialize reactive form with validators
   * AC3: Required fields and format validation
   */
  private initializeForm(): void {
    this.companyForm = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(255)]],
      cnpj: ['', [Validators.pattern(/^\d{14}$/)]],
      email: ['', [Validators.required, Validators.email]],
      telefone: ['']
    });
  }

  /**
   * AC4: Submit form to /api/public/companies
   * AC5: Display loading state during provisioning
   * AC6: Success handling with JWT storage and redirect
   * AC7: Error handling with retry option
   */
  onSubmit(): void {
    if (this.companyForm.invalid) {
      this.companyForm.markAllAsTouched();
      return;
    }

    // AC4: Get userId from JWT token
    const token = this.authService.getToken();
    if (!token) {
      this.showError('Usuário não autenticado. Faça login novamente.');
      this.router.navigate(['/login']);
      return;
    }

    const payload = this.parseJwt(token);
    const userId = parseInt(payload.sub, 10);

    if (!userId) {
      this.showError('Token inválido. Faça login novamente.');
      this.router.navigate(['/login']);
      return;
    }

    // AC4: Build request with form data + userId
    const request: CreateCompanyRequest = {
      ...this.companyForm.value,
      userId
    };

    // AC5: Enable loading state and disable form
    this.isLoading = true;
    this.companyForm.disable();

    // AC4: POST to /api/public/companies
    this.companyService.createCompany(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          // Story 8.2 - AC6 + Story 8.6 - AC3: Success handling
          this.isLoading = false;

          // AC6: Store JWT token in localStorage
          if (response.token) {
            localStorage.setItem('jwt_token', response.token);
          }

          // AC6: Update tenant context with new tenantId
          this.tenantService.setCurrentTenant(response.tenantId);

          // Story 8.6 - AC3: Success feedback with auto-dismiss (3 seconds)
          this.snackBar.open(
            'Empresa criada com sucesso! Redirecionando...',
            '',
            {
              duration: 3000, // AC3: Auto-dismiss after 3 seconds
              panelClass: ['success-snackbar'],
              horizontalPosition: 'center',
              verticalPosition: 'top'
            }
          );

          // AC6 + AC3: Redirect to dashboard smoothly after snackbar appears
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 500);
        },
        error: (error) => {
          // Story 8.2 - AC7 + Story 8.6 - AC4: Error handling
          this.isLoading = false;
          this.companyForm.enable();

          // Extract clear error message from backend response
          let errorMessage = 'Erro ao criar empresa. Por favor, tente novamente.';

          if (error.error?.message) {
            errorMessage = error.error.message;
          } else if (error.error?.fieldErrors) {
            // Handle validation errors (Story 8.5 - AC1)
            const fieldErrors = Object.entries(error.error.fieldErrors)
              .map(([field, message]) => `${field}: ${message}`)
              .join(', ');
            errorMessage = `Erro de validação: ${fieldErrors}`;
          } else if (error.status === 503) {
            errorMessage = 'Sistema temporariamente indisponível. Tente novamente em instantes.';
          } else if (error.status === 500) {
            errorMessage = 'Falha ao criar empresa. Nossa equipe foi notificada.';
          }

          this.showError(errorMessage);
        }
      });
  }

  /**
   * Story 8.2 - AC7 + Story 8.6 - AC4: Show error message with retry action
   * AC4: Error persists until user dismisses or retries
   */
  private showError(message: string): void {
    const snackBarRef = this.snackBar.open(
      message,
      'Tentar Novamente',
      {
        duration: 0, // AC4: Persists until user dismisses or retries
        panelClass: ['error-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'top'
      }
    );

    // AC4: Retry action button
    snackBarRef.onAction().subscribe(() => {
      // Re-enable form and allow user to edit
      this.companyForm.markAsUntouched();
      // Dismiss snackbar
      snackBarRef.dismiss();
    });
  }

  /**
   * Helper to parse JWT token payload
   */
  private parseJwt(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      return {};
    }
  }

  /**
   * AC3: Get error message for form field
   */
  getErrorMessage(fieldName: string): string {
    const field = this.companyForm.get(fieldName);

    if (!field || !field.errors || !field.touched) {
      return '';
    }

    if (field.errors['required']) {
      return 'Este campo é obrigatório';
    }

    if (field.errors['email']) {
      return 'Email inválido';
    }

    if (field.errors['maxlength']) {
      const maxLength = field.errors['maxlength'].requiredLength;
      return `Máximo de ${maxLength} caracteres`;
    }

    if (field.errors['pattern'] && fieldName === 'cnpj') {
      return 'CNPJ deve ter 14 dígitos (apenas números)';
    }

    return 'Valor inválido';
  }
}
