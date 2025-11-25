import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { environment } from '../../../../../environments/environment';
import { Observable } from 'rxjs';

/**
 * StockAvailabilityComponent - Display stock levels with reserved quantity
 * Story 4.6: Stock Reservation and Automatic Release - AC7
 *
 * Shows inventory with reserved quantities and links to sales orders reserving stock
 */
export interface InventoryItem {
  id: string;
  productId: string;
  productName: string;
  variantId: string | null;
  variantName: string | null;
  locationId: string;
  locationName: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  minimumQuantity: number | null;
  maximumQuantity: number | null;
}

@Component({
  selector: 'app-stock-availability',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatTooltipModule,
    MatButtonModule,
    MatDialogModule
  ],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>
          <mat-icon>inventory_2</mat-icon>
          Disponibilidade de Estoque
        </mat-card-title>
        <mat-card-subtitle>
          Consulta de estoque com quantidade reservada por ordens de venda
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        @if (loading) {
          <div class="loading">Carregando...</div>
        }

        @if (!loading) {
          <table mat-table [dataSource]="inventory" class="inventory-table">
            <!-- Product Column -->
            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>Produto</th>
              <td mat-cell *matCellDef="let item">
                <div class="product-cell">
                  <strong>{{ item.productName }}</strong>
                  @if (item.variantName) {
                    <span class="variant">{{ item.variantName }}</span>
                  }
                </div>
              </td>
            </ng-container>

            <!-- Location Column -->
            <ng-container matColumnDef="location">
              <th mat-header-cell *matHeaderCellDef>Local</th>
              <td mat-cell *matCellDef="let item">{{ item.locationName }}</td>
            </ng-container>

            <!-- Quantity Column -->
            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef>Quantidade</th>
              <td mat-cell *matCellDef="let item">{{ item.quantity }}</td>
            </ng-container>

            <!-- Reserved Column (Story 4.6 - AC7) -->
            <ng-container matColumnDef="reserved">
              <th mat-header-cell *matHeaderCellDef>
                <div matTooltip="Quantidade reservada por Ordens de Venda pendentes">
                  Reservado
                  <mat-icon class="info-icon">info</mat-icon>
                </div>
              </th>
              <td mat-cell *matCellDef="let item">
                <div class="reserved-cell">
                  <span [class.has-reservation]="item.reservedQuantity > 0">
                    {{ item.reservedQuantity }}
                  </span>
                  @if (item.reservedQuantity > 0) {
                    <button mat-icon-button
                            (click)="viewReservingOrders(item)"
                            matTooltip="Ver ordens que reservam este estoque">
                      <mat-icon>search</mat-icon>
                    </button>
                  }
                </div>
              </td>
            </ng-container>

            <!-- Available Column -->
            <ng-container matColumnDef="available">
              <th mat-header-cell *matHeaderCellDef>
                <div matTooltip="Disponível para venda = Quantidade - Reservado">
                  Disponível
                  <mat-icon class="info-icon">info</mat-icon>
                </div>
              </th>
              <td mat-cell *matCellDef="let item">
                <span [class.low-stock]="item.availableQuantity < (item.minimumQuantity || 0)"
                      [class.zero-stock]="item.availableQuantity <= 0">
                  {{ item.availableQuantity }}
                </span>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        }
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .loading {
      text-align: center;
      padding: 32px;
      color: #666;
    }

    .inventory-table {
      width: 100%;
    }

    .product-cell {
      display: flex;
      flex-direction: column;
    }

    .variant {
      font-size: 12px;
      color: #666;
    }

    .info-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      vertical-align: middle;
      margin-left: 4px;
      color: #666;
    }

    .reserved-cell {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .has-reservation {
      font-weight: 600;
      color: #ff9800;
    }

    .low-stock {
      color: #ff9800;
      font-weight: 600;
    }

    .zero-stock {
      color: #f44336;
      font-weight: 600;
    }
  `]
})
export class StockAvailabilityComponent implements OnInit {
  private http = inject(HttpClient);
  private dialog = inject(MatDialog);

  inventory: InventoryItem[] = [];
  loading = false;
  displayedColumns = ['product', 'location', 'quantity', 'reserved', 'available'];

  ngOnInit(): void {
    this.loadInventory();
  }

  loadInventory(): void {
    this.loading = true;
    // This endpoint would need to be implemented to return inventory with reserved quantities
    const apiUrl = `${environment.apiUrl}/inventory/availability`;
    this.http.get<InventoryItem[]>(apiUrl).subscribe({
      next: (data) => {
        this.inventory = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading inventory:', error);
        this.loading = false;
      }
    });
  }

  viewReservingOrders(item: InventoryItem): void {
    // This would open a modal showing sales orders that reserve this stock
    // Implementation would require a new endpoint: GET /api/inventory/{productId}/reserving-orders
    console.log('View reserving orders for:', item);
  }
}
