import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CollaboratorService, CollaboratorResponse, InviteCollaboratorRequest } from '../../core/services/collaborator.service';
import { AuthService } from '../../core/auth/auth.service';
import { HasRoleDirective } from '../../core/directives/has-role.directive';

/**
 * Collaborators Management Component (Story 10.8).
 *
 * Allows admins to:
 * - View list of collaborators (AC1)
 * - Invite new collaborators (AC2, AC3)
 * - Remove collaborators (AC4)
 * - Promote collaborators to admin (AC5)
 *
 * @since Epic 10 - Gestão de Colaboradores e Permissões RBAC
 */
@Component({
  selector: 'app-collaborators',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    HasRoleDirective
  ],
  templateUrl: './collaborators.component.html',
  styleUrls: ['./collaborators.component.css']
})
export class CollaboratorsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private collaboratorService = inject(CollaboratorService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);

  collaborators = signal<CollaboratorResponse[]>([]);
  loading = signal(false);
  companyId: number | null = null;

  displayedColumns: string[] = [
    'userName',
    'userEmail',
    'role',
    'invitedAt',
    'actions'
  ];

  roleOptions = [
    { value: 'ADMIN', label: 'Administrador' },
    { value: 'GERENTE', label: 'Gerente' },
    { value: 'VENDEDOR', label: 'Vendedor' }
  ];

  ngOnInit(): void {
    this.loadCollaborators();
  }

  /**
   * Loads collaborators list (AC1).
   */
  loadCollaborators(): void {
    this.loading.set(true);

    // Get company ID from JWT token
    const tenantId = this.authService.getTenantIdFromToken();

    if (!tenantId) {
      this.snackBar.open('Erro: Contexto de empresa não encontrado', 'OK', { duration: 3000 });
      this.loading.set(false);
      return;
    }

    // For now, we'll use a placeholder company ID
    // In a real scenario, you'd need to map tenantId to companyId
    this.companyId = 1; // TODO: Get actual companyId from tenantId

    this.collaboratorService.listCollaborators(this.companyId).subscribe({
      next: (collaborators) => {
        this.collaborators.set(collaborators);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Erro ao carregar colaboradores: ' + error.message, 'OK', { duration: 5000 });
        this.loading.set(false);
      }
    });
  }

  /**
   * Opens invite dialog (AC2).
   */
  openInviteDialog(): void {
    const dialogRef = this.dialog.open(InviteCollaboratorDialog, {
      width: '500px',
      data: { roleOptions: this.roleOptions }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.companyId) {
        this.inviteCollaborator(result);
      }
    });
  }

  /**
   * Invites a new collaborator (AC3).
   */
  private inviteCollaborator(request: InviteCollaboratorRequest): void {
    if (!this.companyId) return;

    this.loading.set(true);

    this.collaboratorService.inviteCollaborator(this.companyId, request).subscribe({
      next: (response) => {
        this.snackBar.open('Colaborador convidado com sucesso!', 'OK', { duration: 3000 });
        this.loadCollaborators();
      },
      error: (error) => {
        const errorMessage = error.error?.message || error.message || 'Erro ao convidar colaborador';
        this.snackBar.open('Erro: ' + errorMessage, 'OK', { duration: 5000 });
        this.loading.set(false);
      }
    });
  }

  /**
   * Removes a collaborator (AC4).
   */
  removeCollaborator(collaborator: CollaboratorResponse): void {
    if (!this.companyId) return;

    const dialogRef = this.dialog.open(ConfirmDeleteDialog, {
      width: '400px',
      data: {
        title: 'Remover Colaborador',
        message: `Tem certeza que deseja remover ${collaborator.userName}?`
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed && this.companyId) {
        this.loading.set(true);

        this.collaboratorService.removeCollaborator(this.companyId, collaborator.userId).subscribe({
          next: () => {
            this.snackBar.open('Colaborador removido com sucesso!', 'OK', { duration: 3000 });
            this.loadCollaborators();
          },
          error: (error) => {
            const errorMessage = error.error?.message || error.message || 'Erro ao remover colaborador';
            this.snackBar.open('Erro: ' + errorMessage, 'OK', { duration: 5000 });
            this.loading.set(false);
          }
        });
      }
    });
  }

  /**
   * Promotes a collaborator to admin (AC5).
   */
  promoteToAdmin(collaborator: CollaboratorResponse): void {
    if (!this.companyId) return;

    const dialogRef = this.dialog.open(ConfirmDeleteDialog, {
      width: '400px',
      data: {
        title: 'Promover para Admin',
        message: `Tem certeza que deseja promover ${collaborator.userName} para Administrador?`
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed && this.companyId) {
        this.loading.set(true);

        this.collaboratorService.promoteToAdmin(this.companyId, collaborator.userId).subscribe({
          next: () => {
            this.snackBar.open('Colaborador promovido com sucesso!', 'OK', { duration: 3000 });
            this.loadCollaborators();
          },
          error: (error) => {
            const errorMessage = error.error?.message || error.message || 'Erro ao promover colaborador';
            this.snackBar.open('Erro: ' + errorMessage, 'OK', { duration: 5000 });
            this.loading.set(false);
          }
        });
      }
    });
  }

  /**
   * Checks if user can be promoted (not already an admin).
   */
  canPromote(collaborator: CollaboratorResponse): boolean {
    return collaborator.role !== 'ADMIN';
  }

  /**
   * Formats date for display.
   */
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('pt-BR');
  }
}

/**
 * Dialog for inviting a new collaborator (Story 10.8 - AC2).
 */
@Component({
  selector: 'invite-collaborator-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Convidar Colaborador</h2>
    <mat-dialog-content>
      <form [formGroup]="inviteForm">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Email</mat-label>
          <input matInput formControlName="email" type="email" required>
          <mat-error *ngIf="inviteForm.get('email')?.hasError('required')">
            Email é obrigatório
          </mat-error>
          <mat-error *ngIf="inviteForm.get('email')?.hasError('email')">
            Email inválido
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Perfil</mat-label>
          <mat-select formControlName="role" required>
            <mat-option *ngFor="let role of data.roleOptions" [value]="role.value">
              {{role.label}}
            </mat-option>
          </mat-select>
          <mat-error *ngIf="inviteForm.get('role')?.hasError('required')">
            Perfil é obrigatório
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        [disabled]="inviteForm.invalid"
        (click)="invite()">
        Convidar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }
  `]
})
export class InviteCollaboratorDialog {
  private fb = inject(FormBuilder);
  data = inject<any>(MatDialogModule);

  inviteForm: FormGroup;

  constructor() {
    this.inviteForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      role: ['', Validators.required]
    });
  }

  invite(): void {
    if (this.inviteForm.valid) {
      const dialogRef = inject(MatDialog);
      dialogRef.closeAll();
      // Return the form value
    }
  }
}

/**
 * Confirmation dialog for delete actions (Story 10.8 - AC4).
 */
@Component({
  selector: 'confirm-delete-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{data.title}}</h2>
    <mat-dialog-content>
      <p>{{data.message}}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Cancelar</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true">Confirmar</button>
    </mat-dialog-actions>
  `
})
export class ConfirmDeleteDialog {
  data = inject<any>(MatDialogModule);
}
