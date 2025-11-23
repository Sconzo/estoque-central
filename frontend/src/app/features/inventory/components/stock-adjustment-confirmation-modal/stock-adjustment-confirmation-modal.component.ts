import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AdjustmentType } from '../../services/stock-adjustment.service';

/**
 * Confirmation modal data interface
 */
export interface AdjustmentConfirmationData {
  adjustmentType: AdjustmentType;
  productName: string;
  productSku: string;
  locationName: string;
  quantity: number;
  currentStock: number;
  newStock: number;
  reasonCode: string;
  reasonDescription: string;
}

/**
 * StockAdjustmentConfirmationModalComponent - Confirmation modal before submitting adjustment
 * Story 3.5: Stock Adjustment (AC9)
 */
@Component({
  selector: 'app-stock-adjustment-confirmation-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon [style.color]="getTypeColor()">{{ getTypeIcon() }}</mat-icon>
      Confirmar Ajuste de Estoque?
    </h2>

    <mat-dialog-content>
      <div class="confirmation-details">
        <!-- Adjustment Type -->
        <div class="detail-row">
          <span class="label">Tipo:</span>
          <span [class]="'value type-' + data.adjustmentType.toLowerCase()">
            {{ getTypeName() }}
          </span>
        </div>

        <!-- Product -->
        <div class="detail-row">
          <span class="label">Produto:</span>
          <span class="value">
            <strong>{{ data.productName }}</strong>
            <br>
            <small>SKU: {{ data.productSku }}</small>
          </span>
        </div>

        <!-- Location -->
        <div class="detail-row">
          <span class="label">Local:</span>
          <span class="value">{{ data.locationName }}</span>
        </div>

        <!-- Quantity -->
        <div class="detail-row">
          <span class="label">Quantidade:</span>
          <span class="value quantity">{{ data.quantity }} unidades</span>
        </div>

        <!-- Stock Change -->
        <div class="detail-row stock-change">
          <span class="label">Estoque:</span>
          <span class="value">
            <span class="stock-before">{{ data.currentStock }}</span>
            <mat-icon class="arrow">arrow_forward</mat-icon>
            <span [class]="'stock-after ' + (data.adjustmentType === 'INCREASE' ? 'increase' : 'decrease')">
              {{ data.newStock }}
            </span>
          </span>
        </div>

        <!-- Reason -->
        <div class="detail-row">
          <span class="label">Motivo:</span>
          <span class="value">
            <strong>{{ getReasonLabel() }}</strong>
            <br>
            <small class="reason-description">{{ data.reasonDescription }}</small>
          </span>
        </div>
      </div>

      <!-- Warning for DECREASE -->
      @if (data.adjustmentType === 'DECREASE') {
        <div class="warning-box">
          <mat-icon>warning</mat-icon>
          <span>
            Esta operação irá <strong>reduzir</strong> o estoque.
            Certifique-se de que as informações estão corretas.
          </span>
        </div>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        Cancelar
      </button>
      <button mat-raised-button
        [color]="data.adjustmentType === 'DECREASE' ? 'warn' : 'primary'"
        (click)="onConfirm()">
        <mat-icon>check</mat-icon>
        Confirmar Ajuste
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 {
      display: flex;
      align-items: center;
      gap: 12px;
      margin: 0;
    }

    mat-dialog-content {
      padding: 24px;
      min-width: 500px;
    }

    .confirmation-details {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 24px;
    }

    .detail-row {
      display: grid;
      grid-template-columns: 140px 1fr;
      gap: 16px;
      align-items: start;
    }

    .label {
      font-weight: 500;
      color: #666;
    }

    .value {
      color: #333;
    }

    .type-increase {
      color: #4caf50;
      font-weight: 600;
    }

    .type-decrease {
      color: #f44336;
      font-weight: 600;
    }

    .quantity {
      font-size: 1.1em;
      font-weight: 600;
    }

    .stock-change {
      background-color: #f5f5f5;
      padding: 12px;
      border-radius: 4px;
      margin: 8px 0;
    }

    .stock-change .value {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1.2em;
      font-weight: 600;
    }

    .stock-before {
      color: #666;
    }

    .arrow {
      color: #999;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .stock-after.increase {
      color: #4caf50;
    }

    .stock-after.decrease {
      color: #f44336;
    }

    .reason-description {
      color: #666;
      font-style: italic;
      display: block;
      margin-top: 4px;
      white-space: pre-wrap;
    }

    .warning-box {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background-color: #fff3e0;
      border-left: 4px solid #ff9800;
      border-radius: 4px;
      color: #e65100;
      margin-top: 16px;
    }

    .warning-box mat-icon {
      color: #ff9800;
    }

    mat-dialog-actions {
      padding: 16px 24px;
      gap: 12px;
    }
  `]
})
export class StockAdjustmentConfirmationModalComponent {

  constructor(
    public dialogRef: MatDialogRef<StockAdjustmentConfirmationModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdjustmentConfirmationData
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  getTypeName(): string {
    return this.data.adjustmentType === AdjustmentType.INCREASE
      ? 'Entrada Manual'
      : 'Saída Manual';
  }

  getTypeIcon(): string {
    return this.data.adjustmentType === AdjustmentType.INCREASE
      ? 'add_circle'
      : 'remove_circle';
  }

  getTypeColor(): string {
    return this.data.adjustmentType === AdjustmentType.INCREASE
      ? '#4caf50'
      : '#f44336';
  }

  getReasonLabel(): string {
    const labels: Record<string, string> = {
      'INVENTORY': 'Inventário',
      'LOSS': 'Perda',
      'DAMAGE': 'Dano',
      'THEFT': 'Furto',
      'ERROR': 'Erro de Lançamento',
      'OTHER': 'Outros'
    };
    return labels[this.data.reasonCode] || this.data.reasonCode;
  }
}
