import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { StockTransferService } from '../services/stock-transfer.service';
import { StockService } from '../services/stock.service';
import { CreateStockTransferRequest, StockResponse } from '../../../shared/models/stock.model';

/**
 * StockTransferFormComponent - Form for transferring stock between locations
 * Story 2.9: Stock Transfer Between Locations - AC6, AC7
 */
@Component({
  selector: 'app-stock-transfer-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDialogModule,
    MatSnackBarModule,
    MatAutocompleteModule
  ],
  templateUrl: './stock-transfer-form.component.html',
  styleUrls: ['./stock-transfer-form.component.css']
})
export class StockTransferFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private transferService = inject(StockTransferService);
  private stockService = inject(StockService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  transferForm!: FormGroup;
  loading = signal(false);
  stockItems = signal<StockResponse[]>([]);
  selectedStock = signal<StockResponse | null>(null);

  availableAtOrigin = computed(() => {
    const stock = this.selectedStock();
    return stock ? stock.quantityForSale : 0;
  });

  ngOnInit(): void {
    this.initForm();
    this.loadStockItems();
  }

  initForm(): void {
    this.transferForm = this.fb.group({
      stockId: ['', Validators.required],
      originLocationId: ['', Validators.required],
      destinationLocationId: ['', Validators.required],
      quantity: [0, [Validators.required, Validators.min(0.001)]],
      reason: ['']
    });
  }

  loadStockItems(): void {
    this.stockService.getAllStock().subscribe({
      next: (items) => this.stockItems.set(items),
      error: (err) => console.error('Error loading stock:', err)
    });
  }

  onStockSelected(stockId: string): void {
    const stock = this.stockItems().find(s => s.id === stockId);
    this.selectedStock.set(stock || null);
    if (stock) {
      this.transferForm.patchValue({
        originLocationId: stock.locationId
      });
    }
  }

  submitTransfer(): void {
    if (!this.transferForm.valid || !this.selectedStock()) return;

    const formValue = this.transferForm.value;
    const stock = this.selectedStock()!;

    const request: CreateStockTransferRequest = {
      productId: stock.productId,
      variantId: stock.variantId,
      originLocationId: formValue.originLocationId,
      destinationLocationId: formValue.destinationLocationId,
      quantity: formValue.quantity,
      reason: formValue.reason
    };

    this.loading.set(true);

    this.transferService.createTransfer(request).subscribe({
      next: () => {
        this.snackBar.open('Transferência realizada com sucesso!', 'Fechar', { duration: 3000 });
        this.transferForm.reset();
        this.selectedStock.set(null);
        this.loading.set(false);
      },
      error: (err) => {
        this.snackBar.open('Erro ao realizar transferência', 'Fechar', { duration: 5000 });
        this.loading.set(false);
        console.error('Transfer error:', err);
      }
    });
  }
}
