import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ExpiringSalesOrder, getExpiringSoon, extendOrderExpiration } from '../services/sales-order.service';

/**
 * ExpiringSalesOrdersCardComponent - Dashboard card showing orders expiring soon
 * Story 4.6: Stock Reservation and Automatic Release - AC8
 *
 * Displays CONFIRMED sales orders that will expire within 2 days
 * Actions: Navigate to invoice or extend expiration by 7 days
 */
@Component({
  selector: 'app-expiring-orders-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  template: `
    <mat-card class="expiring-orders-card">
      <mat-card-header>
        <mat-card-title>
          <mat-icon>schedule</mat-icon>
          Ordens Próximas à Expiração
        </mat-card-title>
        <mat-card-subtitle>
          Ordens CONFIRMED que expiram em até 2 dias (liberação automática de estoque)
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        @if (loading) {
          <div class="loading">
            <mat-icon>refresh</mat-icon>
            Carregando...
          </div>
        }

        @if (!loading && expiringOrders.length === 0) {
          <div class="empty-state">
            <mat-icon>check_circle</mat-icon>
            <p>Nenhuma ordem próxima à expiração</p>
          </div>
        }

        @if (!loading && expiringOrders.length > 0) {
          <table mat-table [dataSource]="expiringOrders" class="expiring-table">
            <!-- Order Number Column -->
            <ng-container matColumnDef="orderNumber">
              <th mat-header-cell *matHeaderCellDef>Número</th>
              <td mat-cell *matCellDef="let order">{{ order.orderNumber }}</td>
            </ng-container>

            <!-- Customer Column -->
            <ng-container matColumnDef="customer">
              <th mat-header-cell *matHeaderCellDef>Cliente</th>
              <td mat-cell *matCellDef="let order">{{ order.customerName }}</td>
            </ng-container>

            <!-- Total Column -->
            <ng-container matColumnDef="total">
              <th mat-header-cell *matHeaderCellDef>Valor</th>
              <td mat-cell *matCellDef="let order">
                {{ order.totalAmount | currency: 'BRL' }}
              </td>
            </ng-container>

            <!-- Days Until Expiration Column -->
            <ng-container matColumnDef="daysUntilExpiration">
              <th mat-header-cell *matHeaderCellDef>Expira Em</th>
              <td mat-cell *matCellDef="let order">
                <mat-chip [class.urgent]="order.daysUntilExpiration === 0"
                          [class.warning]="order.daysUntilExpiration === 1">
                  @if (order.daysUntilExpiration === 0) {
                    <mat-icon>error</mat-icon> Hoje
                  } @else if (order.daysUntilExpiration === 1) {
                    <mat-icon>warning</mat-icon> Amanhã
                  } @else {
                    {{ order.daysUntilExpiration }} dias
                  }
                </mat-chip>
              </td>
            </ng-container>

            <!-- Actions Column -->
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Ações</th>
              <td mat-cell *matCellDef="let order">
                <button mat-button color="primary"
                        (click)="navigateToInvoice(order)"
                        matTooltip="Emitir NFe e faturar ordem">
                  <mat-icon>receipt</mat-icon>
                  Faturar
                </button>
                <button mat-button color="accent"
                        (click)="extendOrder(order)"
                        matTooltip="Prorrogar por +7 dias">
                  <mat-icon>update</mat-icon>
                  Prorrogar
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        }
      </mat-card-content>

      <mat-card-actions>
        <button mat-button (click)="refresh()">
          <mat-icon>refresh</mat-icon>
          Atualizar
        </button>
        <button mat-button (click)="viewAll()">
          Ver Todas as Ordens
          <mat-icon>arrow_forward</mat-icon>
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .expiring-orders-card {
      margin: 16px 0;
    }

    mat-card-header {
      margin-bottom: 16px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
    }

    mat-card-subtitle {
      margin-top: 8px;
      font-size: 12px;
    }

    .loading, .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 32px;
      color: #666;
    }

    .loading mat-icon, .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }

    .expiring-table {
      width: 100%;
    }

    mat-chip {
      font-weight: 500;
    }

    mat-chip.urgent {
      background-color: #f44336;
      color: white;
    }

    mat-chip.warning {
      background-color: #ff9800;
      color: white;
    }

    mat-chip mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }

    mat-card-actions {
      display: flex;
      justify-content: space-between;
      padding: 8px 16px;
    }

    button mat-icon {
      margin-right: 4px;
    }
  `]
})
export class ExpiringOrdersCardComponent implements OnInit {
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  expiringOrders: ExpiringSalesOrder[] = [];
  loading = false;
  displayedColumns = ['orderNumber', 'customer', 'total', 'daysUntilExpiration', 'actions'];

  ngOnInit(): void {
    this.loadExpiringOrders();
  }

  loadExpiringOrders(): void {
    this.loading = true;
    getExpiringSoon(this.http, 2).subscribe({
      next: (orders) => {
        this.expiringOrders = orders.sort((a, b) => a.daysUntilExpiration - b.daysUntilExpiration);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading expiring orders:', error);
        this.snackBar.open('Erro ao carregar ordens próximas à expiração', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  refresh(): void {
    this.loadExpiringOrders();
  }

  navigateToInvoice(order: ExpiringSalesOrder): void {
    // Navigate to invoice creation page for this order
    // This would be implemented in a future story for invoicing
    this.snackBar.open(
      `Faturamento de ordem ${order.orderNumber} será implementado em story futura`,
      'OK',
      { duration: 3000 }
    );
  }

  extendOrder(order: ExpiringSalesOrder): void {
    extendOrderExpiration(this.http, order.id, 7).subscribe({
      next: (response) => {
        this.snackBar.open(
          `Ordem ${order.orderNumber} prorrogada por +7 dias`,
          'Fechar',
          { duration: 3000 }
        );
        this.loadExpiringOrders();
      },
      error: (error) => {
        console.error('Error extending order:', error);
        this.snackBar.open('Erro ao prorrogar ordem', 'Fechar', { duration: 3000 });
      }
    });
  }

  viewAll(): void {
    this.router.navigate(['/sales/orders']);
  }
}
