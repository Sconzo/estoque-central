import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ReceivingItemDetail, ReceivingOrderDetail } from '../../services/receiving.service';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

export interface ManualEntryModalData {
  orderDetail: ReceivingOrderDetail;
}

@Component({
  selector: 'app-manual-entry-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule
  ],
  template: `
    <h2 mat-dialog-title>Entrada Manual</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="search-field">
        <mat-label>Buscar produto (Nome ou SKU)</mat-label>
        <input
          matInput
          [(ngModel)]="searchText"
          [matAutocomplete]="auto"
          (ngModelChange)="onSearchChange()"
          placeholder="Digite para buscar...">
        <mat-autocomplete #auto="matAutocomplete" (optionSelected)="onProductSelected($event.option.value)">
          @for (item of filteredItems; track item.id) {
            <mat-option [value]="item">
              <div class="option-content">
                <strong>{{ item.productName }}</strong>
                <span class="sku">SKU: {{ item.sku }}</span>
                <span class="pending">Pendente: {{ item.quantityPending }}</span>
              </div>
            </mat-option>
          }
        </mat-autocomplete>
      </mat-form-field>

      @if (selectedItem) {
        <div class="selected-product">
          <h3>{{ selectedItem.productName }}</h3>
          <p>SKU: {{ selectedItem.sku }} | Pendente: {{ selectedItem.quantityPending }}</p>

          <mat-form-field appearance="outline" class="quantity-field">
            <mat-label>Quantidade</mat-label>
            <input
              matInput
              type="number"
              inputmode="decimal"
              [(ngModel)]="quantity"
              [max]="selectedItem.quantityPending"
              min="1"
              step="1">
            <mat-error *ngIf="quantity > selectedItem.quantityPending">
              Máximo: {{ selectedItem.quantityPending }}
            </mat-error>
          </mat-form-field>
        </div>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="add()"
        [disabled]="!canAdd()">
        Adicionar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content {
      min-height: 200px;
      padding: 20px 24px;
    }

    .search-field {
      width: 100%;
      margin-bottom: 16px;
    }

    .option-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 4px 0;

      .sku, .pending {
        font-size: 12px;
        color: #666;
      }
    }

    .selected-product {
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
      margin-top: 16px;

      h3 {
        margin: 0 0 8px 0;
        font-size: 16px;
      }

      p {
        margin: 0 0 16px 0;
        font-size: 14px;
        color: #666;
      }

      .quantity-field {
        width: 100%;
      }
    }

    mat-dialog-actions {
      padding: 16px 24px;
      gap: 8px;
    }
  `]
})
export class ManualEntryModalComponent implements OnInit {
  searchText: string = '';
  filteredItems: ReceivingItemDetail[] = [];
  selectedItem: ReceivingItemDetail | null = null;
  quantity: number = 1;

  constructor(
    public dialogRef: MatDialogRef<ManualEntryModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ManualEntryModalData
  ) {}

  ngOnInit(): void {
    this.filteredItems = this.data.orderDetail.items;
  }

  onSearchChange(): void {
    if (!this.searchText || this.searchText.trim() === '') {
      this.filteredItems = this.data.orderDetail.items;
      return;
    }

    const search = this.searchText.toLowerCase();
    this.filteredItems = this.data.orderDetail.items.filter(item =>
      item.productName.toLowerCase().includes(search) ||
      item.sku.toLowerCase().includes(search)
    );
  }

  onProductSelected(item: ReceivingItemDetail): void {
    this.selectedItem = item;
    this.quantity = Math.min(1, item.quantityPending);
  }

  canAdd(): boolean {
    return this.selectedItem !== null &&
           this.quantity > 0 &&
           this.quantity <= this.selectedItem.quantityPending;
  }

  add(): void {
    if (this.canAdd() && this.selectedItem) {
      this.dialogRef.close({
        item: this.selectedItem,
        quantity: this.quantity
      });
    }
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
