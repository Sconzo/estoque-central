import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ReceivingItemDetail } from '../../services/receiving.service';

export interface QuantityModalData {
  item: ReceivingItemDetail;
}

@Component({
  selector: 'app-receiving-quantity-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title>Confirmar Quantidade</h2>
    <mat-dialog-content>
      <div class="product-info">
        <h3>{{ data.item.productName }}</h3>
        <p class="sku-barcode">SKU: {{ data.item.sku }} | Código: {{ data.item.barcode }}</p>
        <p class="pending-qty">Quantidade pendente: <strong>{{ data.item.quantityPending }}</strong> unidades</p>
      </div>

      <mat-form-field appearance="outline" class="quantity-field">
        <mat-label>Quantidade recebida</mat-label>
        <input
          matInput
          type="number"
          inputmode="decimal"
          [(ngModel)]="quantity"
          [max]="data.item.quantityPending"
          min="0"
          step="1"
          (keyup.enter)="confirm()"
          autofocus>
        <mat-error *ngIf="quantity > data.item.quantityPending">
          Quantidade não pode exceder {{ data.item.quantityPending }}
        </mat-error>
      </mat-form-field>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()" color="secondary">
        Cancelar
      </button>
      <button
        mat-raised-button
        (click)="confirm()"
        color="primary"
        [disabled]="!isValid()">
        Confirmar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .product-info {
      margin-bottom: 24px;

      h3 {
        margin: 0 0 8px 0;
        font-size: 18px;
        font-weight: 500;
      }

      .sku-barcode {
        color: #666;
        font-size: 14px;
        margin: 4px 0;
      }

      .pending-qty {
        color: #333;
        font-size: 16px;
        margin: 12px 0 0 0;

        strong {
          color: #1976d2;
        }
      }
    }

    .quantity-field {
      width: 100%;
      font-size: 20px;

      ::ng-deep input {
        font-size: 24px;
        text-align: center;
      }
    }

    mat-dialog-actions {
      padding: 16px 24px;
      gap: 8px;

      button {
        min-width: 100px;
      }
    }
  `]
})
export class ReceivingQuantityModalComponent {
  quantity: number;

  constructor(
    public dialogRef: MatDialogRef<ReceivingQuantityModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: QuantityModalData
  ) {
    // Default quantity: min between 1 and quantity pending
    this.quantity = Math.min(1, data.item.quantityPending);
  }

  isValid(): boolean {
    return this.quantity > 0 && this.quantity <= this.data.item.quantityPending;
  }

  confirm(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.quantity);
    }
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
