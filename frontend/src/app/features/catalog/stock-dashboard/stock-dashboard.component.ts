import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { StockService } from '../services/stock.service';
import { StockResponse } from '../../../shared/models/stock.model';

/**
 * StockDashboardComponent - Stock overview dashboard
 * Story 2.7 - AC7: Frontend Stock Dashboard
 */
@Component({
  selector: 'app-stock-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule
  ],
  templateUrl: './stock-dashboard.component.html',
  styleUrl: './stock-dashboard.component.scss'
})
export class StockDashboardComponent implements OnInit {
  private stockService = inject(StockService);
  private router = inject(Router);

  // State
  stockList = signal<StockResponse[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  // Filters
  selectedLocation = signal<string | null>(null);
  selectedStatus = signal<string | null>(null);
  searchTerm = signal('');

  // Computed values
  filteredStock = computed(() => {
    let stock = this.stockList();

    // Filter by location
    if (this.selectedLocation()) {
      stock = stock.filter(s => s.locationId === this.selectedLocation());
    }

    // Filter by status
    if (this.selectedStatus()) {
      if (this.selectedStatus() === 'RUPTURE') {
        stock = stock.filter(s => s.stockStatus === 'LOW' || s.stockStatus === 'CRITICAL');
      } else {
        stock = stock.filter(s => s.stockStatus === this.selectedStatus());
      }
    }

    // Filter by search term
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      stock = stock.filter(s =>
        s.productName.toLowerCase().includes(term) ||
        s.productSku.toLowerCase().includes(term)
      );
    }

    return stock;
  });

  totalProducts = computed(() => {
    const uniqueProducts = new Set(this.stockList().map(s => s.productId || s.variantId));
    return uniqueProducts.size;
  });

  productsInRupture = computed(() => {
    return this.stockList().filter(s => s.stockStatus === 'LOW' || s.stockStatus === 'CRITICAL').length;
  });

  totalInventoryValue = computed(() => {
    // This would require cost data - for now return 0
    // In real implementation, would sum (quantityAvailable * cost)
    return 0;
  });

  // Table columns
  displayedColumns = ['productName', 'sku', 'location', 'quantityForSale', 'minimumQuantity', 'status', 'actions'];

  ngOnInit(): void {
    this.loadStock();
  }

  loadStock(): void {
    this.loading.set(true);
    this.error.set(null);

    this.stockService.getAllStock().subscribe({
      next: (stock) => {
        this.stockList.set(stock);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar estoque');
        this.loading.set(false);
        console.error('Error loading stock:', err);
      }
    });
  }

  onProductClick(stock: StockResponse): void {
    const id = stock.productId || stock.variantId;
    const type = stock.productId ? 'product' : 'variant';
    this.router.navigate(['/catalog/stock', type, id]);
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
      case 'NOT_SET':
        return 'Não Definido';
      default:
        return status;
    }
  }

  clearFilters(): void {
    this.selectedLocation.set(null);
    this.selectedStatus.set(null);
    this.searchTerm.set('');
  }

  openRecentMovements(): void {
    // Navigate to recent movements page
    this.router.navigate(['/catalog/stock/movements']);
  }
}
