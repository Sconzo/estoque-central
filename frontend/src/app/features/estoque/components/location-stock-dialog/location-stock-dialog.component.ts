import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Location, LOCATION_TYPE_LABELS, LocationType } from '../../models/location.model';
import { StockService } from '../../../catalog/services/stock.service';
import { StockResponse } from '../../../../shared/models/stock.model';

export interface LocationStockDialogData {
  location: Location;
}

@Component({
  selector: 'app-location-stock-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './location-stock-dialog.component.html',
  styleUrls: ['./location-stock-dialog.component.scss']
})
export class LocationStockDialogComponent implements OnInit {
  stock: StockResponse[] = [];
  loading = false;
  error: string | null = null;

  readonly displayedColumns = ['productName', 'sku', 'quantityAvailable', 'reserved', 'quantityForSale', 'status'];
  readonly locationTypeLabels = LOCATION_TYPE_LABELS;

  constructor(
    public dialogRef: MatDialogRef<LocationStockDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: LocationStockDialogData,
    private stockService: StockService
  ) {}

  ngOnInit(): void {
    this.loadStock();
  }

  loadStock(): void {
    this.loading = true;
    this.error = null;

    this.stockService.getAllStock({ locationId: this.data.location.id }).subscribe({
      next: (stock) => {
        this.stock = stock;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erro ao carregar estoque do local.';
        this.loading = false;
      }
    });
  }

  getTypeLabel(type: string): string {
    return this.locationTypeLabels[type as LocationType] || type;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'OK': 'status-ok',
      'LOW': 'status-low',
      'CRITICAL': 'status-critical',
      'NOT_SET': 'status-not-set'
    };
    return map[status] || '';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      'OK': 'OK',
      'LOW': 'Baixo',
      'CRITICAL': 'Crítico',
      'NOT_SET': 'S/ Mínimo'
    };
    return map[status] || status;
  }

  getTotalQuantity(): number {
    return this.stock.reduce((sum, s) => sum + s.quantityAvailable, 0);
  }

  close(): void {
    this.dialogRef.close();
  }
}
