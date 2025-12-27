import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { CompanyService, CompanyResponse, UpdateCompanyRequest } from '../../core/services/company.service';
import { AuthService } from '../../core/auth/auth.service';
import { HasRoleDirective } from '../../core/directives/has-role.directive';

/**
 * Company Settings Component (Story 10.10).
 *
 * Allows admins to:
 * - View company information (AC1)
 * - Edit company data with real-time validation (AC2)
 * - Save changes with loading feedback (AC3, AC4)
 * - Delete company with confirmation (AC5)
 *
 * @since Epic 10 - Gestão de Colaboradores e Permissões RBAC
 */
@Component({
  selector: 'app-company-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatIconModule,
    HasRoleDirective
  ],
  templateUrl: './company-settings.component.html',
  styleUrls: ['./company-settings.component.css']
})
export class CompanySettingsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private companyService = inject(CompanyService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);
  private router = inject(Router);

  companyForm!: FormGroup;
  loading = signal(false);
  saving = signal(false);
  company = signal<CompanyResponse | null>(null);

  ngOnInit(): void {
    this.buildForm();
    this.loadCompanyData();
  }

  /**
   * Builds the reactive form with validators (AC2).
   */
  buildForm(): void {
    this.companyForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      cnpj: [{ value: '', disabled: true }], // Read-only field
      email: ['', [Validators.email]],
      phone: ['', [Validators.maxLength(20)]]
    });
  }

  /**
   * Loads current company data (AC1).
   */
  loadCompanyData(): void {
    this.loading.set(true);

    this.companyService.getCurrentCompany().subscribe({
      next: (company) => {
        this.company.set(company);
        this.companyForm.patchValue({
          name: company.name,
          cnpj: company.cnpj,
          email: company.email,
          phone: company.phone
        });
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Erro ao carregar dados da empresa', 'OK', { duration: 5000 });
        this.loading.set(false);
      }
    });
  }

  /**
   * Saves company changes (AC3, AC4).
   */
  saveCompany(): void {
    if (this.companyForm.invalid) {
      return;
    }

    this.saving.set(true);

    const request: UpdateCompanyRequest = {
      name: this.companyForm.get('name')?.value,
      email: this.companyForm.get('email')?.value,
      phone: this.companyForm.get('phone')?.value
    };

    this.companyService.updateCurrentCompany(request).subscribe({
      next: (updatedCompany) => {
        this.company.set(updatedCompany);
        this.saving.set(false);
        // AC4: Success feedback
        this.snackBar.open('Dados atualizados com sucesso!', 'OK', { duration: 3000 });
      },
      error: (error) => {
        this.saving.set(false);
        const errorMessage = error.error?.message || error.message || 'Erro ao salvar';
        this.snackBar.open('Erro ao salvar: ' + errorMessage, 'OK', { duration: 5000 });
      }
    });
  }

  /**
   * Deletes the company (AC5).
   */
  deleteCompany(): void {
    const dialogRef = this.dialog.open(ConfirmDeleteCompanyDialog, {
      width: '500px'
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);

        this.companyService.deleteCurrentCompany().subscribe({
          next: () => {
            this.snackBar.open('Empresa deletada com sucesso', 'OK', { duration: 3000 });
            // Logout and redirect to login
            this.authService.logout();
            this.router.navigate(['/login']);
          },
          error: (error) => {
            this.loading.set(false);
            const errorMessage = error.error?.message || error.message || 'Erro ao deletar empresa';
            this.snackBar.open('Erro: ' + errorMessage, 'OK', { duration: 5000 });
          }
        });
      }
    });
  }

  /**
   * Checks if the save button should be disabled (AC2).
   */
  isSaveDisabled(): boolean {
    return this.companyForm.invalid || this.saving();
  }
}

/**
 * Confirmation dialog for company deletion (Story 10.10 - AC5).
 * Requires typing "DELETAR" to confirm.
 */
@Component({
  selector: 'confirm-delete-company-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="warn">warning</mat-icon>
      Deletar Empresa
    </h2>
    <mat-dialog-content>
      <p class="warning-text">
        <strong>Esta ação é IRREVERSÍVEL.</strong>
      </p>
      <p>
        Todos os dados da empresa serão perdidos, incluindo:
      </p>
      <ul>
        <li>Produtos e estoque</li>
        <li>Vendas e pedidos</li>
        <li>Colaboradores e permissões</li>
        <li>Todas as configurações</li>
      </ul>
      <p>
        Para confirmar, digite <strong>DELETAR</strong> no campo abaixo:
      </p>
      <form [formGroup]="confirmForm">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Digite DELETAR para confirmar</mat-label>
          <input matInput formControlName="confirmation" autocomplete="off">
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Cancelar</button>
      <button
        mat-raised-button
        color="warn"
        [disabled]="!isConfirmationValid()"
        [mat-dialog-close]="true">
        Confirmar Exclusão
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .warning-text {
      color: #f44336;
      font-size: 16px;
      margin-bottom: 16px;
    }

    ul {
      margin: 16px 0;
      padding-left: 24px;
    }

    li {
      margin: 8px 0;
    }

    .full-width {
      width: 100%;
    }

    mat-icon {
      vertical-align: middle;
      margin-right: 8px;
    }
  `]
})
export class ConfirmDeleteCompanyDialog {
  private fb = inject(FormBuilder);

  confirmForm: FormGroup;

  constructor() {
    this.confirmForm = this.fb.group({
      confirmation: ['']
    });
  }

  isConfirmationValid(): boolean {
    return this.confirmForm.get('confirmation')?.value === 'DELETAR';
  }
}
