import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LocationService } from '../../services/location.service';
import { Location, LocationType, LOCATION_TYPE_LABELS } from '../../models/location.model';
import { TenantService } from '../../../../core/services/tenant.service';
import { FeedbackService } from '../../../../shared/services/feedback.service';
import { ConfirmDialogService } from '../../../../shared/services/confirm-dialog.service';

@Component({
  selector: 'app-location-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './location-list.component.html',
  styleUrls: ['./location-list.component.scss']
})
export class LocationListComponent implements OnInit {
  locations: Location[] = [];
  loading = false;
  errorMessage: string | null = null;
  includeInactive = false;

  readonly locationTypeLabels = LOCATION_TYPE_LABELS;
  readonly displayedColumns = ['code', 'name', 'type', 'address', 'city', 'status', 'actions'];

  constructor(
    private locationService: LocationService,
    private tenantService: TenantService,
    private router: Router,
    private feedback: FeedbackService,
    private confirmDialog: ConfirmDialogService
  ) {}

  ngOnInit(): void {
    this.loadLocations();
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

    this.locationService.listAll(tenantId, this.includeInactive).subscribe({
      next: (locations) => {
        this.locations = locations;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar locais de estoque';
        this.loading = false;
        console.error('Error loading locations:', error);
      }
    });
  }

  onIncludeInactiveChange(): void {
    this.loadLocations();
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
    return ativo ? 'badge-success' : 'badge-secondary';
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
