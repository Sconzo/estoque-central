import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chip';
import { MatTableModule } from '@angular/material/table';
import { StockAdjustmentService } from '../../services/stock-adjustment.service';

/**
 * Frequent adjustment data interface
 */
export interface FrequentAdjustment {
  productId: string;
  productName: string;
  productSku: string;
  stockLocationId: string;
  stockLocationName: string;
  totalAdjustments: number;
  totalIncrease: number;
  totalDecrease: number;
}

/**
 * FrequentAdjustmentsWidgetComponent - Dashboard widget showing products with frequent adjustments
 * Story 3.5: Stock Adjustment (AC11 - Task 8)
 */
@Component({
  selector: 'app-frequent-adjustments-widget',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatTableModule
  ],
  template: `
    <mat-card class="widget-card">
      <mat-card-header>
        <mat-card-title>
          <mat-icon class="warning-icon">warning</mat-icon>
          Produtos com Ajustes Frequentes
        </mat-card-title>
        <mat-card-subtitle>
          Produtos com 3+ ajustes nos últimos 30 dias
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        @if (loading) {
          <div class="loading">Carregando...</div>
        } @else if (frequentAdjustments.length === 0) {
          <div class="empty-state">
            <mat-icon>check_circle</mat-icon>
            <p>Nenhum produto com ajustes frequentes!</p>
            <small>Isso é bom - indica estabilidade no estoque.</small>
          </div>
        } @else {
          <table mat-table [dataSource]="frequentAdjustments" class="frequent-table">
            <!-- Product Column -->
            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>Produto</th>
              <td mat-cell *matCellDef="let row">
                <div class="product-info">
                  <strong>{{ row.productName }}</strong>
                  <small>{{ row.productSku }}</small>
                </div>
              </td>
            </ng-container>

            <!-- Location Column -->
            <ng-container matColumnDef="location">
              <th mat-header-cell *matHeaderCellDef>Local</th>
              <td mat-cell *matCellDef="let row">{{ row.stockLocationName }}</td>
            </ng-container>

            <!-- Adjustments Column -->
            <ng-container matColumnDef="adjustments">
              <th mat-header-cell *matHeaderCellDef>Ajustes</th>
              <td mat-cell *matCellDef="let row">
                <mat-chip class="adjustment-chip">
                  {{ row.totalAdjustments }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Increase/Decrease Column -->
            <ng-container matColumnDef="balance">
              <th mat-header-cell *matHeaderCellDef>↑ / ↓</th>
              <td mat-cell *matCellDef="let row">
                <div class="balance-info">
                  <span class="increase">+{{ row.totalIncrease }}</span>
                  <span class="separator">/</span>
                  <span class="decrease">-{{ row.totalDecrease }}</span>
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"
                (click)="viewDetails(row)" class="clickable-row"></tr>
          </table>
        }
      </mat-card-content>

      @if (frequentAdjustments.length > 0) {
        <mat-card-actions>
          <button mat-button color="primary" (click)="viewAll()">
            Ver Histórico Completo
            <mat-icon>arrow_forward</mat-icon>
          </button>
        </mat-card-actions>
      }
    </mat-card>
  `,
  styles: [`
    .widget-card {
      margin: 16px;
      max-width: 100%;
    }

    mat-card-header {
      margin-bottom: 16px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1.2em;
    }

    .warning-icon {
      color: #ff9800;
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .loading {
      text-align: center;
      padding: 32px;
      color: #666;
    }

    .empty-state {
      text-align: center;
      padding: 32px;
      color: #666;
    }

    .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #4caf50;
      margin-bottom: 16px;
    }

    .empty-state p {
      margin: 8px 0;
      font-weight: 500;
    }

    .empty-state small {
      color: #999;
    }

    .frequent-table {
      width: 100%;
    }

    .product-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .product-info small {
      color: #666;
      font-size: 0.85em;
    }

    .adjustment-chip {
      background-color: #ff9800 !important;
      color: white !important;
      font-weight: 600;
    }

    .balance-info {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .increase {
      color: #4caf50;
      font-weight: 600;
    }

    .decrease {
      color: #f44336;
      font-weight: 600;
    }

    .separator {
      color: #999;
    }

    .clickable-row {
      cursor: pointer;
    }

    .clickable-row:hover {
      background-color: #f5f5f5;
    }

    mat-card-actions {
      display: flex;
      justify-content: flex-end;
      padding: 8px 16px;
    }
  `]
})
export class FrequentAdjustmentsWidgetComponent implements OnInit {
  frequentAdjustments: FrequentAdjustment[] = [];
  displayedColumns: string[] = ['product', 'location', 'adjustments', 'balance'];
  loading = false;

  constructor(
    private adjustmentService: StockAdjustmentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadFrequentAdjustments();
  }

  loadFrequentAdjustments(): void {
    this.loading = true;
    const tenantId = sessionStorage.getItem('tenantId') || 'default-tenant';

    // Using HttpClient directly for now
    this.adjustmentService.getFrequentAdjustments(tenantId, 30).subscribe({
      next: (adjustments) => {
        this.frequentAdjustments = adjustments;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading frequent adjustments:', error);
        this.loading = false;
      }
    });
  }

  viewDetails(adjustment: FrequentAdjustment): void {
    // Navigate to adjustment history filtered by product and location
    this.router.navigate(['/inventory/adjustments'], {
      queryParams: {
        productId: adjustment.productId,
        locationId: adjustment.stockLocationId
      }
    });
  }

  viewAll(): void {
    this.router.navigate(['/inventory/adjustments']);
  }
}
