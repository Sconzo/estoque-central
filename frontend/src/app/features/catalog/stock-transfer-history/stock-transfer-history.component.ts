import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { StockTransferService } from '../services/stock-transfer.service';
import { LocationService } from '../../estoque/services/location.service';
import { TenantService } from '../../../core/services/tenant.service';
import { StockTransferResponse, StockTransferFilters } from '../../../shared/models/stock.model';
import { Location } from '../../estoque/models/location.model';

/**
 * StockTransferHistoryComponent - Display transfer history with filters
 * Story 2.9: Stock Transfer Between Locations - AC5
 */
@Component({
  selector: 'app-stock-transfer-history',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './stock-transfer-history.component.html',
  styleUrls: ['./stock-transfer-history.component.css']
})
export class StockTransferHistoryComponent implements OnInit {
  private fb = inject(FormBuilder);
  private transferService = inject(StockTransferService);
  private locationService = inject(LocationService);
  private tenantService = inject(TenantService);

  filterForm!: FormGroup;
  loading = signal(false);
  transfers = signal<StockTransferResponse[]>([]);
  locations = signal<Location[]>([]);

  displayedColumns: string[] = [
    'createdAt',
    'productInfo',
    'originLocation',
    'destinationLocation',
    'quantity',
    'userName',
    'reason'
  ];

  ngOnInit(): void {
    this.initFilterForm();
    this.loadLocations();
    this.loadTransfers();
  }

  initFilterForm(): void {
    this.filterForm = this.fb.group({
      productId: [''],
      variantId: [''],
      originLocationId: [''],
      destinationLocationId: [''],
      startDate: [''],
      endDate: [''],
      userId: ['']
    });
  }

  loadLocations(): void {
    const tenantId = this.tenantService.currentTenant$();
    if (!tenantId) return;
    this.locationService.listAll(tenantId, false).subscribe({
      next: (locations) => this.locations.set(locations),
      error: (err) => console.error('Error loading locations:', err)
    });
  }

  loadTransfers(): void {
    this.loading.set(true);
    const filters = this.buildFilters();

    this.transferService.getTransferHistory(filters).subscribe({
      next: (transfers) => {
        this.transfers.set(transfers);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading transfers:', err);
        this.loading.set(false);
      }
    });
  }

  buildFilters(): StockTransferFilters {
    const formValue = this.filterForm.value;
    const filters: StockTransferFilters = {};

    if (formValue.productId) filters.productId = formValue.productId;
    if (formValue.variantId) filters.variantId = formValue.variantId;
    if (formValue.originLocationId) filters.originLocationId = formValue.originLocationId;
    if (formValue.destinationLocationId) filters.destinationLocationId = formValue.destinationLocationId;
    if (formValue.startDate) filters.startDate = formValue.startDate.toISOString();
    if (formValue.endDate) filters.endDate = formValue.endDate.toISOString();
    if (formValue.userId) filters.userId = formValue.userId;

    return filters;
  }

  applyFilters(): void {
    this.loadTransfers();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.loadTransfers();
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('pt-BR');
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'primary';
      case 'PENDING': return 'accent';
      case 'CANCELLED': return 'warn';
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'Concluída';
      case 'PENDING': return 'Pendente';
      case 'CANCELLED': return 'Cancelada';
      default: return status;
    }
  }
}
