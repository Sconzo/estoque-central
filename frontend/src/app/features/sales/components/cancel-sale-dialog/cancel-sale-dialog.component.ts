import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

/**
 * CancelSaleDialogComponent - Dialog for sale cancellation justification
 * Story 4.4: NFCe Retry Queue Management
 *
 * Features:
 * - Textarea input for cancellation justification
 * - Minimum 10 characters validation
 * - Character counter
 * - Returns justification on confirm or null on cancel
 */
@Component({
  selector: 'app-cancel-sale-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon class="dialog-icon">warning</mat-icon>
      Cancelar Venda com Reembolso
    </h2>

    <mat-dialog-content>
      <div class="warning-message">
        <mat-icon>info</mat-icon>
        <p>
          Esta ação irá cancelar a venda e reembolsar o estoque.
          Por favor, informe o motivo do cancelamento.
        </p>
      </div>

      <mat-form-field appearance="outline" class="justification-field">
        <mat-label>Justificativa (mínimo 10 caracteres)</mat-label>
        <textarea
          matInput
          [formControl]="justificationControl"
          rows="4"
          maxlength="500"
          placeholder="Exemplo: Cliente desistiu da compra, produto com defeito, erro no pedido..."
          autofocus>
        </textarea>
        <mat-hint align="end">{{ justificationControl.value?.length || 0 }}/500 caracteres</mat-hint>
        <mat-error *ngIf="justificationControl.hasError('required')">
          Justificativa é obrigatória
        </mat-error>
        <mat-error *ngIf="justificationControl.hasError('minlength')">
          Mínimo de 10 caracteres
        </mat-error>
      </mat-form-field>

      <div class="help-text">
        <mat-icon>lightbulb</mat-icon>
        <p>
          A justificativa será registrada no histórico da venda e
          poderá ser consultada posteriormente.
        </p>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button
        mat-button
        (click)="cancel()"
        color="secondary">
        <mat-icon>close</mat-icon>
        Cancelar
      </button>
      <button
        mat-raised-button
        (click)="confirm()"
        color="warn"
        [disabled]="!isValid()">
        <mat-icon>check</mat-icon>
        Confirmar Cancelamento
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #d32f2f;
      font-size: 20px;
      font-weight: 500;
      margin: 0 0 16px 0;

      .dialog-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
        color: #f57c00;
      }
    }

    mat-dialog-content {
      min-width: 450px;
      padding: 0 24px;
    }

    .warning-message {
      display: flex;
      gap: 12px;
      padding: 12px 16px;
      background-color: #fff3e0;
      border-left: 4px solid #f57c00;
      border-radius: 4px;
      margin-bottom: 20px;

      mat-icon {
        color: #f57c00;
        flex-shrink: 0;
      }

      p {
        margin: 0;
        color: #e65100;
        font-size: 14px;
        line-height: 1.5;
      }
    }

    .justification-field {
      width: 100%;
      margin-bottom: 16px;

      textarea {
        font-family: 'Roboto', sans-serif;
        font-size: 14px;
        line-height: 1.5;
        resize: vertical;
        min-height: 80px;
      }

      mat-hint {
        font-size: 12px;
      }
    }

    .help-text {
      display: flex;
      gap: 8px;
      padding: 8px 12px;
      background-color: #e3f2fd;
      border-radius: 4px;
      margin-bottom: 16px;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        color: #1976d2;
        flex-shrink: 0;
      }

      p {
        margin: 0;
        color: #0d47a1;
        font-size: 12px;
        line-height: 1.4;
      }
    }

    mat-dialog-actions {
      padding: 16px 24px;
      gap: 8px;
      margin: 0;

      button {
        min-width: 120px;

        mat-icon {
          margin-right: 4px;
          font-size: 18px;
          width: 18px;
          height: 18px;
        }
      }
    }
  `]
})
export class CancelSaleDialogComponent {
  /**
   * Form control for justification input
   * Validators:
   * - required: Justification is mandatory
   * - minLength(10): Minimum 10 characters
   */
  justificationControl = new FormControl('', [
    Validators.required,
    Validators.minLength(10)
  ]);

  constructor(
    public dialogRef: MatDialogRef<CancelSaleDialogComponent>
  ) {}

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return this.justificationControl.valid;
  }

  /**
   * Confirm and return justification
   */
  confirm(): void {
    if (this.isValid()) {
      const justification = this.justificationControl.value?.trim() || '';
      this.dialogRef.close(justification);
    }
  }

  /**
   * Cancel and close dialog without returning data
   */
  cancel(): void {
    this.dialogRef.close(null);
  }
}
