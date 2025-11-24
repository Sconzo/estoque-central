import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SaleManagementService, SaleResponse, NfceStatus } from '../../services/sale-management.service';
import { Page } from '../../../produtos/models/product.model';
import { CancelSaleDialogComponent } from '../cancel-sale-dialog/cancel-sale-dialog.component';

/**
 * PendingSalesComponent - NFCe Retry Queue Management
 * Story 4.4: NFCe Retry Queue Management
 *
 * Features:
 * - Display sales with PENDING or FAILED NFCe status
 * - Filter by status (ALL, PENDING, FAILED, EMITTED)
 * - Retry NFCe emission
 * - Cancel sale with refund
 * - Pagination
 */
@Component({
  selector: 'app-pending-sales',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatPaginatorModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  template: `
    <mat-card class="pending-sales-card">
      <mat-card-header>
        <mat-card-title>Vendas Pendentes NFCe</mat-card-title>
        <mat-card-subtitle>Gerenciamento de fila de emissão de NFCe</mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <!-- Filter Section -->
        <div class="filter-section">
          <mat-form-field appearance="outline">
            <mat-label>Filtrar por Status</mat-label>
            <mat-select [(ngModel)]="selectedStatus" (selectionChange)="onStatusChange()">
              <mat-option value="">Todos</mat-option>
              <mat-option value="PENDING">Pendente</mat-option>
              <mat-option value="FAILED">Falha</mat-option>
              <mat-option value="EMITTED">Emitida</mat-option>
            </mat-select>
          </mat-form-field>

          <button mat-raised-button color="primary" (click)="loadPendingSales()" [disabled]="loading">
            <mat-icon>refresh</mat-icon>
            Atualizar
          </button>
        </div>

        <!-- Loading Spinner -->
        <div *ngIf="loading" class="loading-container">
          <mat-spinner></mat-spinner>
          <p>Carregando vendas...</p>
        </div>

        <!-- Error Message -->
        <div *ngIf="error" class="error-message">
          <mat-icon>error</mat-icon>
          <p>{{ error }}</p>
        </div>

        <!-- Sales Table -->
        <div *ngIf="!loading && !error" class="table-container">
          <table mat-table [dataSource]="sales?.content || []" class="sales-table">
            <!-- Sale Number Column -->
            <ng-container matColumnDef="saleNumber">
              <th mat-header-cell *matHeaderCellDef>Número da Venda</th>
              <td mat-cell *matCellDef="let sale">{{ sale.saleNumber }}</td>
            </ng-container>

            <!-- Customer Column -->
            <ng-container matColumnDef="customer">
              <th mat-header-cell *matHeaderCellDef>Cliente</th>
              <td mat-cell *matCellDef="let sale">{{ sale.customerName || sale.customerId }}</td>
            </ng-container>

            <!-- Date Column -->
            <ng-container matColumnDef="date">
              <th mat-header-cell *matHeaderCellDef>Data</th>
              <td mat-cell *matCellDef="let sale">{{ formatDate(sale.saleDate) }}</td>
            </ng-container>

            <!-- Total Column -->
            <ng-container matColumnDef="total">
              <th mat-header-cell *matHeaderCellDef>Total</th>
              <td mat-cell *matCellDef="let sale">{{ formatCurrency(sale.totalAmount) }}</td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let sale">
                <mat-chip [ngClass]="getStatusChipClass(sale.nfceStatus)">
                  {{ getStatusLabel(sale.nfceStatus) }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Attempts Column -->
            <ng-container matColumnDef="attempts">
              <th mat-header-cell *matHeaderCellDef>Tentativas</th>
              <td mat-cell *matCellDef="let sale">
                <span [matTooltip]="sale.lastErrorMessage || 'Nenhum erro'">
                  {{ sale.emissionAttempts || 0 }}
                </span>
              </td>
            </ng-container>

            <!-- Actions Column -->
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Ações</th>
              <td mat-cell *matCellDef="let sale">
                <div class="action-buttons">
                  <button
                    mat-icon-button
                    color="primary"
                    (click)="retrySale(sale.id)"
                    [disabled]="sale.nfceStatus === 'EMITTED' || sale.nfceStatus === 'CANCELLED'"
                    matTooltip="Retentar emissão">
                    <mat-icon>refresh</mat-icon>
                  </button>
                  <button
                    mat-icon-button
                    color="warn"
                    (click)="cancelSale(sale.id)"
                    [disabled]="sale.nfceStatus === 'CANCELLED'"
                    matTooltip="Cancelar com reembolso">
                    <mat-icon>cancel</mat-icon>
                  </button>
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

            <!-- No Data Row -->
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell" [attr.colspan]="displayedColumns.length">
                <div class="no-data">
                  <mat-icon>inbox</mat-icon>
                  <p>Nenhuma venda pendente encontrada</p>
                </div>
              </td>
            </tr>
          </table>

          <!-- Paginator -->
          <mat-paginator
            *ngIf="sales && sales.totalElements > 0"
            [length]="sales.totalElements"
            [pageSize]="pageSize"
            [pageIndex]="pageIndex"
            [pageSizeOptions]="[10, 20, 50]"
            (page)="onPageChange($event)"
            showFirstLastButtons>
          </mat-paginator>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .pending-sales-card {
      margin: 20px;
      max-width: 1400px;

      mat-card-header {
        margin-bottom: 20px;

        mat-card-title {
          font-size: 24px;
          font-weight: 500;
          color: #333;
        }

        mat-card-subtitle {
          font-size: 14px;
          color: #666;
          margin-top: 4px;
        }
      }
    }

    .filter-section {
      display: flex;
      gap: 16px;
      align-items: center;
      margin-bottom: 20px;

      mat-form-field {
        width: 200px;
      }

      button {
        height: 40px;
        mat-icon {
          margin-right: 8px;
        }
      }
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;

      mat-spinner {
        margin-bottom: 16px;
      }

      p {
        color: #666;
        font-size: 14px;
      }
    }

    .error-message {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background-color: #ffebee;
      border-radius: 4px;
      color: #c62828;

      mat-icon {
        color: #c62828;
      }

      p {
        margin: 0;
      }
    }

    .table-container {
      overflow-x: auto;
    }

    .sales-table {
      width: 100%;
      background-color: white;

      th {
        font-weight: 600;
        color: #333;
        background-color: #f5f5f5;
      }

      td {
        color: #666;
      }

      .action-buttons {
        display: flex;
        gap: 4px;
      }
    }

    .no-data {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
      color: #999;

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        margin-bottom: 12px;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }

    /* Status Chip Styles */
    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;

      &.status-pending {
        background-color: #fff3cd;
        color: #856404;
      }

      &.status-failed {
        background-color: #f8d7da;
        color: #721c24;
      }

      &.status-emitted {
        background-color: #d4edda;
        color: #155724;
      }

      &.status-cancelled {
        background-color: #e2e3e5;
        color: #383d41;
      }
    }

    mat-paginator {
      margin-top: 16px;
      background-color: transparent;
    }
  `]
})
export class PendingSalesComponent implements OnInit {
  // Data
  sales: Page<SaleResponse> | null = null;

  // Table columns
  displayedColumns: string[] = ['saleNumber', 'customer', 'date', 'total', 'status', 'attempts', 'actions'];

  // Loading and error states
  loading = false;
  error: string | null = null;

  // Filters
  selectedStatus = '';

  // Pagination
  pageIndex = 0;
  pageSize = 20;

  // Tenant ID (should come from auth service in real implementation)
  private tenantId = 'tenant-001';

  constructor(
    private saleManagementService: SaleManagementService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadPendingSales();
  }

  /**
   * Load pending sales with current filters and pagination (AC1)
   */
  loadPendingSales(): void {
    this.loading = true;
    this.error = null;

    this.saleManagementService.getPendingSales(
      this.tenantId,
      this.selectedStatus || undefined,
      this.pageIndex,
      this.pageSize
    ).subscribe({
      next: (page) => {
        this.sales = page;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar vendas pendentes: ' + (err.error?.message || err.message || 'Erro desconhecido');
        this.loading = false;
        console.error('Error loading pending sales:', err);
      }
    });
  }

  /**
   * Retry NFCe emission for a sale (AC2)
   */
  retrySale(saleId: string): void {
    if (!confirm('Deseja realmente retentar a emissão da NFCe para esta venda?')) {
      return;
    }

    this.loading = true;

    this.saleManagementService.retrySale(this.tenantId, saleId).subscribe({
      next: (response) => {
        this.loading = false;
        alert(`NFCe retentada com sucesso!\nStatus: ${response.nfceStatus}`);
        this.loadPendingSales();
      },
      error: (err) => {
        this.loading = false;
        alert('Erro ao retentar emissão: ' + (err.error?.message || err.message || 'Erro desconhecido'));
        console.error('Error retrying sale:', err);
      }
    });
  }

  /**
   * Cancel sale with refund (AC3)
   * Opens dialog to get justification
   */
  cancelSale(saleId: string): void {
    const dialogRef = this.dialog.open(CancelSaleDialogComponent, {
      width: '500px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.performCancellation(saleId, result);
      }
    });
  }

  /**
   * Perform the actual cancellation after getting justification
   */
  private performCancellation(saleId: string, justification: string): void {
    this.loading = true;

    this.saleManagementService.cancelSaleWithRefund(this.tenantId, saleId, justification).subscribe({
      next: (response) => {
        this.loading = false;
        alert(`Venda cancelada com sucesso!\nEstoque reembolsado.\nStatus: ${response.nfceStatus}`);
        this.loadPendingSales();
      },
      error: (err) => {
        this.loading = false;
        alert('Erro ao cancelar venda: ' + (err.error?.message || err.message || 'Erro desconhecido'));
        console.error('Error cancelling sale:', err);
      }
    });
  }

  /**
   * Handle status filter change
   */
  onStatusChange(): void {
    this.pageIndex = 0; // Reset to first page
    this.loadPendingSales();
  }

  /**
   * Handle pagination change
   */
  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPendingSales();
  }

  /**
   * Get status chip CSS class
   */
  getStatusChipClass(status: NfceStatus): string {
    switch (status) {
      case NfceStatus.PENDING:
        return 'status-pending';
      case NfceStatus.FAILED:
        return 'status-failed';
      case NfceStatus.EMITTED:
        return 'status-emitted';
      case NfceStatus.CANCELLED:
        return 'status-cancelled';
      default:
        return '';
    }
  }

  /**
   * Get status label in Portuguese
   */
  getStatusLabel(status: NfceStatus): string {
    switch (status) {
      case NfceStatus.PENDING:
        return 'Pendente';
      case NfceStatus.FAILED:
        return 'Falha';
      case NfceStatus.EMITTED:
        return 'Emitida';
      case NfceStatus.CANCELLED:
        return 'Cancelada';
      default:
        return status;
    }
  }

  /**
   * Format currency for display
   */
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }

  /**
   * Format date for display
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }
}
