import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  StockAdjustmentService,
  AdjustmentType,
  AdjustmentReasonCode,
  StockAdjustmentRequest
} from '../../services/stock-adjustment.service';
import {
  StockAdjustmentConfirmationModalComponent,
  AdjustmentConfirmationData
} from '../stock-adjustment-confirmation-modal/stock-adjustment-confirmation-modal.component';

/**
 * StockAdjustmentFormComponent - Form for creating stock adjustments
 * Story 3.5: Stock Adjustment (AC8)
 */
@Component({
  selector: 'app-stock-adjustment-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatRadioModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatDialogModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Ajuste de Estoque</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="adjustmentForm" (ngSubmit)="onSubmit()">
            <!-- Product Selection -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Produto *</mat-label>
              <input matInput formControlName="productId" placeholder="Digite nome ou SKU">
              <mat-error *ngIf="adjustmentForm.get('productId')?.hasError('required')">
                Produto é obrigatório
              </mat-error>
            </mat-form-field>

            <!-- Location Selection -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Local de Estoque *</mat-label>
              <mat-select formControlName="stockLocationId">
                <mat-option value="loc1">Depósito Central</mat-option>
                <mat-option value="loc2">Loja Principal</mat-option>
              </mat-select>
              <mat-error *ngIf="adjustmentForm.get('stockLocationId')?.hasError('required')">
                Local é obrigatório
              </mat-error>
            </mat-form-field>

            <!-- Adjustment Type -->
            <div class="form-field-label">Tipo de Ajuste *</div>
            <mat-radio-group formControlName="adjustmentType" class="radio-group">
              <mat-radio-button [value]="AdjustmentType.INCREASE">
                Entrada Manual
              </mat-radio-button>
              <mat-radio-button [value]="AdjustmentType.DECREASE">
                Saída Manual
              </mat-radio-button>
            </mat-radio-group>

            <!-- Quantity -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Quantidade *</mat-label>
              <input matInput type="number" formControlName="quantity" min="0.01" step="0.01">
              <mat-error *ngIf="adjustmentForm.get('quantity')?.hasError('required')">
                Quantidade é obrigatória
              </mat-error>
              <mat-error *ngIf="adjustmentForm.get('quantity')?.hasError('min')">
                Quantidade deve ser maior que zero
              </mat-error>
            </mat-form-field>

            <!-- Reason Code -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Motivo *</mat-label>
              <mat-select formControlName="reasonCode">
                <mat-option [value]="AdjustmentReasonCode.INVENTORY">Inventário</mat-option>
                <mat-option [value]="AdjustmentReasonCode.LOSS">Perda</mat-option>
                <mat-option [value]="AdjustmentReasonCode.DAMAGE">Dano</mat-option>
                <mat-option [value]="AdjustmentReasonCode.THEFT">Furto</mat-option>
                <mat-option [value]="AdjustmentReasonCode.ERROR">Erro de Lançamento</mat-option>
                <mat-option [value]="AdjustmentReasonCode.OTHER">Outros</mat-option>
              </mat-select>
              <mat-error *ngIf="adjustmentForm.get('reasonCode')?.hasError('required')">
                Motivo é obrigatório
              </mat-error>
            </mat-form-field>

            <!-- Reason Description -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Descrição/Justificativa *</mat-label>
              <textarea matInput formControlName="reasonDescription" rows="3"
                placeholder="Descreva detalhadamente o motivo do ajuste (mínimo 10 caracteres)"></textarea>
              <mat-hint>{{ adjustmentForm.get('reasonDescription')?.value?.length || 0 }} / 10 mínimo</mat-hint>
              <mat-error *ngIf="adjustmentForm.get('reasonDescription')?.hasError('required')">
                Descrição é obrigatória
              </mat-error>
              <mat-error *ngIf="adjustmentForm.get('reasonDescription')?.hasError('minlength')">
                Descrição deve ter no mínimo 10 caracteres
              </mat-error>
            </mat-form-field>

            <!-- Adjustment Date -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Data do Ajuste *</mat-label>
              <input matInput [matDatepicker]="picker" formControlName="adjustmentDate">
              <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
              <mat-datepicker #picker></mat-datepicker>
            </mat-form-field>

            <!-- Action Buttons -->
            <div class="actions">
              <button mat-raised-button type="button" (click)="onCancel()">
                Cancelar
              </button>
              <button mat-raised-button color="primary" type="submit"
                [disabled]="adjustmentForm.invalid || isSubmitting">
                @if (isSubmitting) {
                  <mat-spinner diameter="20" style="display: inline-block; margin-right: 8px;"></mat-spinner>
                }
                {{ isSubmitting ? 'Processando...' : 'Criar Ajuste' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-container { padding: 24px; max-width: 800px; margin: 0 auto; }
    .full-width { width: 100%; margin-bottom: 16px; }
    .form-field-label { margin-bottom: 8px; font-weight: 500; }
    .radio-group { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
    .actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
  `]
})
export class StockAdjustmentFormComponent implements OnInit {
  adjustmentForm: FormGroup;
  isSubmitting = false;

  // Expose enums to template
  AdjustmentType = AdjustmentType;
  AdjustmentReasonCode = AdjustmentReasonCode;

  constructor(
    private fb: FormBuilder,
    private adjustmentService: StockAdjustmentService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router
  ) {
    this.adjustmentForm = this.fb.group({
      productId: ['', Validators.required],
      variantId: [''],
      stockLocationId: ['', Validators.required],
      adjustmentType: [AdjustmentType.INCREASE, Validators.required],
      quantity: [null, [Validators.required, Validators.min(0.01)]],
      reasonCode: ['', Validators.required],
      reasonDescription: ['', [Validators.required, Validators.minLength(10)]],
      adjustmentDate: [new Date(), Validators.required]
    });
  }

  ngOnInit(): void {
    // Initialize form
  }

  onSubmit(): void {
    if (this.adjustmentForm.invalid || this.isSubmitting) {
      return;
    }

    const formValue = this.adjustmentForm.value;

    // Calculate new stock for confirmation
    const currentStock = 100; // TODO: fetch from backend
    const quantity = formValue.quantity;
    const newStock = formValue.adjustmentType === AdjustmentType.INCREASE
      ? currentStock + quantity
      : currentStock - quantity;

    // Open confirmation modal (AC9)
    const confirmationData: AdjustmentConfirmationData = {
      adjustmentType: formValue.adjustmentType,
      productName: 'Produto Exemplo', // TODO: get from product selection
      productSku: formValue.productId, // TODO: get actual SKU
      locationName: 'Local Exemplo', // TODO: get from location selection
      quantity: quantity,
      currentStock: currentStock,
      newStock: newStock,
      reasonCode: formValue.reasonCode,
      reasonDescription: formValue.reasonDescription.trim()
    };

    const dialogRef = this.dialog.open(StockAdjustmentConfirmationModalComponent, {
      width: '600px',
      data: confirmationData,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.submitAdjustment();
      }
    });
  }

  private submitAdjustment(): void {
    this.isSubmitting = true;

    const formValue = this.adjustmentForm.value;
    const request: StockAdjustmentRequest = {
      productId: formValue.productId,
      variantId: formValue.variantId || undefined,
      stockLocationId: formValue.stockLocationId,
      adjustmentType: formValue.adjustmentType,
      quantity: formValue.quantity,
      reasonCode: formValue.reasonCode,
      reasonDescription: formValue.reasonDescription.trim(),
      adjustmentDate: formValue.adjustmentDate?.toISOString().split('T')[0]
    };

    // Get tenantId from session (TODO: proper auth context)
    const tenantId = sessionStorage.getItem('tenantId') || 'default-tenant';

    this.adjustmentService.createAdjustment(tenantId, request).subscribe({
      next: (response) => {
        this.snackBar.open(
          `Ajuste ${response.adjustmentNumber} criado com sucesso!`,
          'Fechar',
          { duration: 3000, panelClass: 'success-snackbar' }
        );
        this.router.navigate(['/inventory/adjustments']);
      },
      error: (error) => {
        this.isSubmitting = false;
        const errorMessage = error.error?.message || 'Erro ao criar ajuste. Tente novamente.';
        this.snackBar.open(errorMessage, 'Fechar', {
          duration: 5000,
          panelClass: 'error-snackbar'
        });
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/inventory/adjustments']);
  }
}
