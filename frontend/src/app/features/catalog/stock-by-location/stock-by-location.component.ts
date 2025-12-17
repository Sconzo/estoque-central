import { Component, OnInit, signal, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { StockService } from '../services/stock.service';
import { StockByLocationResponse, LocationStock, SetMinimumQuantityRequest } from '../../../shared/models/stock.model';
import { FeedbackService } from '../../../shared/services/feedback.service';

/**
 * StockByLocationComponent - Stock drill-down by location
 * Story 2.7 - AC8: Frontend Stock by Location View
 */
@Component({
  selector: 'app-stock-by-location',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule
  ],
  templateUrl: './stock-by-location.component.html',
  styleUrl: './stock-by-location.component.scss'
})
export class StockByLocationComponent implements OnInit {
  private stockService = inject(StockService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private feedback = inject(FeedbackService);

  // State
  stockData = signal<StockByLocationResponse | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  // Editable minimum quantity
  editingMinimum = signal<string | null>(null);
  newMinimumValue = signal<number>(0);

  // Table columns
  displayedColumns = ['location', 'quantityAvailable', 'reserved', 'quantityForSale', 'minimum', 'status', 'actions'];

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const type = params['type']; // 'product' or 'variant'
      const id = params['id'];

      if (type === 'product') {
        this.loadStockByProduct(id);
      } else if (type === 'variant') {
        this.loadStockByVariant(id);
      }
    });
  }

  loadStockByProduct(productId: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.stockService.getStockByProductByLocation(productId).subscribe({
      next: (data) => {
        this.stockData.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar estoque');
        this.loading.set(false);
        console.error('Error loading stock:', err);
      }
    });
  }

  loadStockByVariant(variantId: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.stockService.getStockByVariantByLocation(variantId).subscribe({
      next: (data) => {
        this.stockData.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar estoque');
        this.loading.set(false);
        console.error('Error loading stock:', err);
      }
    });
  }

  startEditMinimum(locationId: string, currentMinimum?: number): void {
    this.editingMinimum.set(locationId);
    this.newMinimumValue.set(currentMinimum || 0);
  }

  cancelEditMinimum(): void {
    this.editingMinimum.set(null);
    this.newMinimumValue.set(0);
  }

  saveMinimum(locationId: string): void {
    const data = this.stockData();
    if (!data) return;

    const request: SetMinimumQuantityRequest = {
      stockLocationId: locationId,
      minimumQuantity: this.newMinimumValue()
    };

    const observable = data.productId
      ? this.stockService.setMinimumQuantityForProduct(data.productId, request)
      : this.stockService.setMinimumQuantityForVariant(data.variantId!, request);

    observable.subscribe({
      next: () => {
        this.editingMinimum.set(null);
        // Reload data
        if (data.productId) {
          this.loadStockByProduct(data.productId);
        } else if (data.variantId) {
          this.loadStockByVariant(data.variantId);
        }
      },
      error: (err) => {
        console.error('Error saving minimum quantity:', err);
        this.feedback.showError('Erro ao salvar estoque mínimo', () => this.saveMinimum(locationId));
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'OK':
        return 'primary';
      case 'LOW':
        return 'accent';
      case 'CRITICAL':
        return 'warn';
      default:
        return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'OK':
        return 'OK';
      case 'LOW':
        return 'Baixo';
      case 'CRITICAL':
        return 'Crítico';
      default:
        return status;
    }
  }

  goBack(): void {
    this.router.navigate(['/catalog/stock']);
  }

  openTimeline(): void {
    const data = this.stockData();
    if (!data) return;

    // Navigate to timeline view with product/variant ID
    if (data.productId) {
      this.router.navigate(['/catalog/stock/timeline', 'product', data.productId]);
    } else if (data.variantId) {
      this.router.navigate(['/catalog/stock/timeline', 'variant', data.variantId]);
    }
  }

  onTransfer(locationStock: LocationStock): void {
    // TODO: Navigate to stock transfer page
    console.log('Transfer stock from location:', locationStock.stockLocationId);
    this.feedback.showInfo('Funcionalidade de transferência será implementada na Story 2.9');
  }
}
