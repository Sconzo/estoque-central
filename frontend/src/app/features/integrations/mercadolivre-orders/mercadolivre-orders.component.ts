import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MercadoLivreService, OrderPreview } from '../services/mercadolivre.service';
import { Router } from '@angular/router';

/**
 * MercadoLivreOrdersComponent - Display imported orders from Mercado Livre
 * Story 5.5: Import and Process Orders from Mercado Livre - AC6
 * Story 5.6: Process Mercado Livre Cancellations - AC6
 */
@Component({
  selector: 'app-mercadolivre-orders',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatButtonToggleModule
  ],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>
          <mat-icon>shopping_cart</mat-icon>
          Pedidos do Mercado Livre
        </mat-card-title>
        <mat-card-subtitle>
          Pedidos importados automaticamente do Mercado Livre
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <!-- Story 5.6: AC6 - Status Filter -->
        <div class="filter-container">
          <mat-button-toggle-group [(value)]="statusFilter" (change)="applyFilter()">
            <mat-button-toggle value="all">Todos</mat-button-toggle>
            <mat-button-toggle value="active">Ativos</mat-button-toggle>
            <mat-button-toggle value="cancelled">Cancelados</mat-button-toggle>
          </mat-button-toggle-group>
        </div>

        @if (loading) {
          <div class="loading-container">
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
            <p>Carregando pedidos...</p>
          </div>
        }

        @if (!loading && filteredOrders.length === 0) {
          <div class="empty-state">
            <mat-icon>inventory_2</mat-icon>
            @if (orders.length === 0) {
              <p>Nenhum pedido importado ainda</p>
              <small>Os pedidos serão importados automaticamente quando houver vendas no Mercado Livre</small>
            } @else {
              <p>Nenhum pedido encontrado com este filtro</p>
              <small>Tente alterar o filtro de status</small>
            }
          </div>
        }

        @if (!loading && filteredOrders.length > 0) {
          <table mat-table [dataSource]="filteredOrders" class="orders-table">
            <!-- Order ID Column -->
            <ng-container matColumnDef="orderId">
              <th mat-header-cell *matHeaderCellDef>Pedido ML</th>
              <td mat-cell *matCellDef="let order">
                #{{ order.orderIdMarketplace }}
              </td>
            </ng-container>

            <!-- Customer Column -->
            <ng-container matColumnDef="customer">
              <th mat-header-cell *matHeaderCellDef>Cliente</th>
              <td mat-cell *matCellDef="let order">
                <div class="customer-info">
                  <strong>{{ order.customerName }}</strong>
                  @if (order.customerEmail) {
                    <small>{{ order.customerEmail }}</small>
                  }
                </div>
              </td>
            </ng-container>

            <!-- Total Column -->
            <ng-container matColumnDef="total">
              <th mat-header-cell *matHeaderCellDef>Valor</th>
              <td mat-cell *matCellDef="let order">
                {{ order.totalAmount | currency: 'BRL' }}
              </td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let order">
                <mat-chip [class]="'status-' + order.status.toLowerCase()">
                  {{ getStatusLabel(order.status) }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Payment Status Column -->
            <ng-container matColumnDef="payment">
              <th mat-header-cell *matHeaderCellDef>Pagamento</th>
              <td mat-cell *matCellDef="let order">
                @if (order.paymentStatus) {
                  <span class="payment-status">{{ order.paymentStatus }}</span>
                } @else {
                  <span class="payment-status">-</span>
                }
              </td>
            </ng-container>

            <!-- Internal Record Column -->
            <ng-container matColumnDef="internal">
              <th mat-header-cell *matHeaderCellDef>Venda Interna</th>
              <td mat-cell *matCellDef="let order">
                @if (order.hasInternalRecord) {
                  <mat-icon class="success-icon" matTooltip="Venda criada no sistema">check_circle</mat-icon>
                } @else {
                  <mat-icon class="warning-icon" matTooltip="Aguardando processamento">pending</mat-icon>
                }
              </td>
            </ng-container>

            <!-- Imported At Column -->
            <ng-container matColumnDef="importedAt">
              <th mat-header-cell *matHeaderCellDef>Importado em</th>
              <td mat-cell *matCellDef="let order">
                {{ order.importedAt | date: 'dd/MM/yyyy HH:mm' }}
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        }
      </mat-card-content>

      <mat-card-actions>
        <button mat-raised-button color="primary" (click)="loadOrders()" [disabled]="loading">
          <mat-icon>refresh</mat-icon>
          Atualizar
        </button>
        <button mat-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
          Voltar
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    mat-card {
      margin: 16px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .filter-container {
      margin-bottom: 24px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }

    .loading-container {
      padding: 40px 20px;
      text-align: center;
    }

    .loading-container mat-progress-bar {
      margin-bottom: 16px;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 60px 20px;
      color: #666;
      text-align: center;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
      color: #999;
    }

    .orders-table {
      width: 100%;
      margin-top: 16px;
    }

    .customer-info {
      display: flex;
      flex-direction: column;
    }

    .customer-info small {
      color: #666;
      font-size: 12px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .status-pending {
      background-color: #ff9800;
      color: white;
    }

    .status-paid {
      background-color: #4caf50;
      color: white;
    }

    .status-shipped {
      background-color: #2196f3;
      color: white;
    }

    .status-delivered {
      background-color: #9c27b0;
      color: white;
    }

    .status-cancelled {
      background-color: #f44336;
      color: white;
    }

    .payment-status {
      font-size: 12px;
      color: #666;
    }

    .success-icon {
      color: #4caf50;
    }

    .warning-icon {
      color: #ff9800;
    }

    mat-card-actions {
      display: flex;
      gap: 8px;
      padding: 16px;
    }
  `]
})
export class MercadoLivreOrdersComponent implements OnInit {
  private mlService = inject(MercadoLivreService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  orders: OrderPreview[] = [];
  filteredOrders: OrderPreview[] = [];
  statusFilter: 'all' | 'active' | 'cancelled' = 'all';
  displayedColumns = ['orderId', 'customer', 'total', 'status', 'payment', 'internal', 'importedAt'];
  loading = false;

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.mlService.getOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.snackBar.open('Erro ao carregar pedidos', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.statusFilter === 'all') {
      this.filteredOrders = this.orders;
    } else if (this.statusFilter === 'cancelled') {
      this.filteredOrders = this.orders.filter(o => o.status === 'CANCELLED');
    } else {
      // active = not cancelled
      this.filteredOrders = this.orders.filter(o => o.status !== 'CANCELLED');
    }
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'PENDING': 'Pendente',
      'PAID': 'Pago',
      'SHIPPED': 'Enviado',
      'DELIVERED': 'Entregue',
      'CANCELLED': 'Cancelado'
    };
    return labels[status] || status;
  }

  goBack(): void {
    this.router.navigate(['/integracoes']);
  }
}
