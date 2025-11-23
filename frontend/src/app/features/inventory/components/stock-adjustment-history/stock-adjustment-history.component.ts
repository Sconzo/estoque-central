import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chip';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import {
  StockAdjustmentService,
  StockAdjustmentResponse,
  AdjustmentType,
  AdjustmentReasonCode
} from '../../services/stock-adjustment.service';

/**
 * StockAdjustmentHistoryComponent - History and list of stock adjustments
 * Story 3.5: Stock Adjustment (AC10)
 */
@Component({
  selector: 'app-stock-adjustment-history',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  template: `
    <div class="history-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            Histórico de Ajustes de Estoque
            <button mat-raised-button color="primary" (click)="createNew()" style="float: right;">
              <mat-icon>add</mat-icon>
              Novo Ajuste
            </button>
          </mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <!-- Filters -->
          <form [formGroup]="filterForm" class="filters">
            <mat-form-field appearance="outline">
              <mat-label>Produto</mat-label>
              <input matInput formControlName="productId" placeholder="ID do produto">
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Local</mat-label>
              <mat-select formControlName="stockLocationId">
                <mat-option value="">Todos</mat-option>
                <mat-option value="loc1">Depósito Central</mat-option>
                <mat-option value="loc2">Loja Principal</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Tipo</mat-label>
              <mat-select formControlName="adjustmentType">
                <mat-option value="">Todos</mat-option>
                <mat-option [value]="AdjustmentType.INCREASE">Entrada</mat-option>
                <mat-option [value]="AdjustmentType.DECREASE">Saída</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Motivo</mat-label>
              <mat-select formControlName="reasonCode">
                <mat-option value="">Todos</mat-option>
                <mat-option [value]="AdjustmentReasonCode.INVENTORY">Inventário</mat-option>
                <mat-option [value]="AdjustmentReasonCode.LOSS">Perda</mat-option>
                <mat-option [value]="AdjustmentReasonCode.DAMAGE">Dano</mat-option>
                <mat-option [value]="AdjustmentReasonCode.THEFT">Furto</mat-option>
                <mat-option [value]="AdjustmentReasonCode.ERROR">Erro</mat-option>
                <mat-option [value]="AdjustmentReasonCode.OTHER">Outros</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-raised-button (click)="applyFilters()">Filtrar</button>
            <button mat-button (click)="clearFilters()">Limpar</button>
          </form>

          <!-- Table -->
          <table mat-table [dataSource]="adjustments" class="adjustment-table">
            <!-- Number Column -->
            <ng-container matColumnDef="adjustmentNumber">
              <th mat-header-cell *matHeaderCellDef>Número</th>
              <td mat-cell *matCellDef="let row">{{ row.adjustmentNumber }}</td>
            </ng-container>

            <!-- Date Column -->
            <ng-container matColumnDef="adjustmentDate">
              <th mat-header-cell *matHeaderCellDef>Data</th>
              <td mat-cell *matCellDef="let row">{{ row.adjustmentDate | date:'dd/MM/yyyy' }}</td>
            </ng-container>

            <!-- Product Column -->
            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>Produto</th>
              <td mat-cell *matCellDef="let row">
                <div>{{ row.productName }}</div>
                <small style="color: #666;">{{ row.productSku }}</small>
              </td>
            </ng-container>

            <!-- Location Column -->
            <ng-container matColumnDef="location">
              <th mat-header-cell *matHeaderCellDef>Local</th>
              <td mat-cell *matCellDef="let row">{{ row.stockLocationName }}</td>
            </ng-container>

            <!-- Type Column -->
            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>Tipo</th>
              <td mat-cell *matCellDef="let row">
                <mat-chip [class]="row.adjustmentType === 'INCREASE' ? 'chip-increase' : 'chip-decrease'">
                  {{ getAdjustmentTypeLabel(row.adjustmentType) }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Quantity Column -->
            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef>Quantidade</th>
              <td mat-cell *matCellDef="let row">{{ row.quantity }}</td>
            </ng-container>

            <!-- Balance Column -->
            <ng-container matColumnDef="balance">
              <th mat-header-cell *matHeaderCellDef>Estoque</th>
              <td mat-cell *matCellDef="let row">
                {{ row.balanceBefore }} → {{ row.balanceAfter }}
              </td>
            </ng-container>

            <!-- Reason Column -->
            <ng-container matColumnDef="reason">
              <th mat-header-cell *matHeaderCellDef>Motivo</th>
              <td mat-cell *matCellDef="let row">
                <mat-chip [class]="getReasonChipClass(row.reasonCode)">
                  {{ getReasonCodeLabel(row.reasonCode) }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Actions Column -->
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Ações</th>
              <td mat-cell *matCellDef="let row">
                <button mat-icon-button (click)="viewDetails(row)">
                  <mat-icon>visibility</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>

          <!-- Paginator -->
          <mat-paginator
            [length]="totalElements"
            [pageSize]="pageSize"
            [pageSizeOptions]="[10, 20, 50]"
            (page)="onPageChange($event)">
          </mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .history-container { padding: 24px; }
    .filters { display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; align-items: center; }
    .filters mat-form-field { min-width: 200px; }
    .adjustment-table { width: 100%; }
    .chip-increase { background-color: #4caf50 !important; color: white !important; }
    .chip-decrease { background-color: #f44336 !important; color: white !important; }
    .chip-critical { background-color: #ff9800 !important; color: white !important; }
  `]
})
export class StockAdjustmentHistoryComponent implements OnInit {
  adjustments: StockAdjustmentResponse[] = [];
  displayedColumns: string[] = [
    'adjustmentNumber',
    'adjustmentDate',
    'product',
    'location',
    'type',
    'quantity',
    'balance',
    'reason',
    'actions'
  ];

  filterForm: FormGroup;
  totalElements = 0;
  pageSize = 20;
  currentPage = 0;

  // Expose enums to template
  AdjustmentType = AdjustmentType;
  AdjustmentReasonCode = AdjustmentReasonCode;

  constructor(
    private adjustmentService: StockAdjustmentService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.filterForm = this.fb.group({
      productId: [''],
      stockLocationId: [''],
      adjustmentType: [''],
      reasonCode: ['']
    });
  }

  ngOnInit(): void {
    this.loadAdjustments();
  }

  loadAdjustments(): void {
    const tenantId = sessionStorage.getItem('tenantId') || 'default-tenant';
    const filters = {
      ...this.filterForm.value,
      page: this.currentPage,
      size: this.pageSize
    };

    this.adjustmentService.getAdjustmentHistory(tenantId, filters).subscribe({
      next: (response) => {
        this.adjustments = response.content || [];
        this.totalElements = response.totalElements || 0;
      },
      error: (error) => {
        console.error('Error loading adjustments:', error);
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadAdjustments();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.applyFilters();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAdjustments();
  }

  viewDetails(adjustment: StockAdjustmentResponse): void {
    // Navigate to details or open modal
    alert(`Detalhes: ${adjustment.adjustmentNumber}\n${adjustment.reasonDescription}`);
  }

  createNew(): void {
    this.router.navigate(['/inventory/adjustments/new']);
  }

  getAdjustmentTypeLabel(type: string): string {
    return type === 'INCREASE' ? 'Entrada' : 'Saída';
  }

  getReasonCodeLabel(code: string): string {
    return this.adjustmentService.getReasonCodeLabel(code as AdjustmentReasonCode);
  }

  getReasonChipClass(code: string): string {
    return (code === 'THEFT' || code === 'LOSS') ? 'chip-critical' : '';
  }
}
