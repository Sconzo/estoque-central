import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LocationService } from '../../services/location.service';
import { Location, LocationType, LOCATION_TYPE_LABELS } from '../../models/location.model';
import { TenantService } from '../../../../core/services/tenant.service';
import { FeedbackService } from '../../../../shared/services/feedback.service';
import { ConfirmDialogService } from '../../../../shared/services/confirm-dialog.service';
import { LocationStockDialogComponent } from '../location-stock-dialog/location-stock-dialog.component';
import { debounceTime, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-location-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDialogModule,
    LocationStockDialogComponent
  ],
  templateUrl: './location-list.component.html',
  styleUrls: ['./location-list.component.scss']
})
export class LocationListComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  allLocations: Location[] = [];
  filteredLocations: Location[] = [];
  pagedLocations: Location[] = [];

  loading = false;
  errorMessage: string | null = null;

  // Filters
  searchQuery = '';
  selectedType: LocationType | '' = '';
  selectedStatus: boolean | '' = true;

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;

  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  readonly LocationType = LocationType;
  readonly locationTypeLabels = LOCATION_TYPE_LABELS;
  readonly displayedColumns = ['code', 'name', 'type', 'address', 'city', 'status', 'actions'];

  constructor(
    private locationService: LocationService,
    private tenantService: TenantService,
    private router: Router,
    private feedback: FeedbackService,
    private confirmDialog: ConfirmDialogService,
    private dialog: MatDialog
  ) {
    this.searchSubject
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(query => {
        this.searchQuery = query;
        this.currentPage = 0;
        this.applyFilters();
      });
  }

  ngOnInit(): void {
    this.loadLocations();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadLocations(): void {
    this.loading = true;
    this.errorMessage = null;

    const tenantId = this.tenantService.getCurrentTenant();
    if (!tenantId) {
      this.errorMessage = 'Nenhuma empresa selecionada';
      this.loading = false;
      return;
    }

    this.locationService.listAll(tenantId, true).subscribe({
      next: (locations) => {
        this.allLocations = locations;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar locais de estoque';
        this.loading = false;
        console.error('Error loading locations:', error);
      }
    });
  }

  applyFilters(): void {
    let filtered = this.allLocations;

    if (this.selectedStatus !== '') {
      filtered = filtered.filter(l => l.ativo === this.selectedStatus);
    }

    if (this.selectedType !== '') {
      filtered = filtered.filter(l => l.type === this.selectedType);
    }

    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase().trim();
      filtered = filtered.filter(l =>
        l.name.toLowerCase().includes(query) ||
        l.code.toLowerCase().includes(query)
      );
    }

    this.filteredLocations = filtered;
    this.totalElements = filtered.length;
    this.updatePagedLocations();
  }

  updatePagedLocations(): void {
    const start = this.currentPage * this.pageSize;
    this.pagedLocations = this.filteredLocations.slice(start, start + this.pageSize);
  }

  onSearchChange(query: string): void {
    this.searchSubject.next(query);
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.applyFilters();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updatePagedLocations();
  }

  viewLocationStock(location: Location): void {
    this.dialog.open(LocationStockDialogComponent, {
      data: { location },
      width: '900px',
      maxWidth: '95vw'
    });
  }

  createLocation(): void {
    this.router.navigate(['/estoque/locais/novo']);
  }

  editLocation(location: Location): void {
    this.router.navigate(['/estoque/locais', location.id, 'editar']);
  }

  deleteLocation(location: Location): void {
    this.confirmDialog.confirmDanger({
      title: 'Inativar Local de Estoque',
      message: `Tem certeza que deseja inativar "${location.name}"?\n\nCódigo: ${location.code}`
    }).subscribe(confirmed => {
      if (!confirmed) return;

      this.loading = true;

      this.locationService.delete(location.id).subscribe({
        next: () => {
          this.loadLocations();
        },
        error: (error) => {
          let errorMsg = 'Erro ao deletar local de estoque';

          if (error.status === 409) {
            errorMsg = 'Não é possível inativar local com estoque alocado. Transfira o estoque antes.';
          } else if (error.error?.message) {
            errorMsg = error.error.message;
          }

          this.feedback.showError(errorMsg, () => this.deleteLocation(location));
          this.loading = false;
          console.error('Error deleting location:', error);
        }
      });
    });
  }

  truncate(text: string | undefined, maxLength: number): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }

  getStatusLabel(ativo: boolean): string {
    return ativo ? 'Ativo' : 'Inativo';
  }

  getStatusClass(ativo: boolean): string {
    return ativo ? 'badge-active' : 'badge-inactive';
  }

  getTypeLabel(type: string): string {
    return this.locationTypeLabels[type as LocationType] || type;
  }

  getTypeClass(type: LocationType): string {
    const classMap: Record<string, string> = {
      'WAREHOUSE': 'type-warehouse',
      'STORE': 'type-store',
      'DISTRIBUTION_CENTER': 'type-distribution',
      'SUPPLIER': 'type-supplier',
      'CUSTOMER': 'type-customer',
      'TRANSIT': 'type-transit',
      'QUARANTINE': 'type-quarantine'
    };
    return classMap[type] || '';
  }
}
