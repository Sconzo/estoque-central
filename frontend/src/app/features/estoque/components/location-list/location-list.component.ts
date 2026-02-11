import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LocationService } from '../../services/location.service';
import { Location, LOCATION_TYPE_LABELS, LOCATION_TYPE_COLORS } from '../../models/location.model';
import { FeedbackService } from '../../../../shared/services/feedback.service';
import { ConfirmDialogService } from '../../../../shared/services/confirm-dialog.service';

/**
 * LocationListComponent - List and manage stock locations
 */
@Component({
  selector: 'app-location-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './location-list.component.html',
  styleUrls: ['./location-list.component.scss']
})
export class LocationListComponent implements OnInit {
  locations: Location[] = [];
  loading = false;
  errorMessage: string | null = null;
  includeInactive = false;

  // UI helpers
  locationTypeLabels = LOCATION_TYPE_LABELS;
  locationTypeColors = LOCATION_TYPE_COLORS;

  constructor(
    private locationService: LocationService,
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

    const tenantId = '00000000-0000-0000-0000-000000000001'; // TODO: Get from auth service

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

  getStatusBadgeClass(ativo: boolean): string {
    return ativo ? 'badge bg-success' : 'badge bg-secondary';
  }

  getStatusLabel(ativo: boolean): string {
    return ativo ? 'Ativo' : 'Inativo';
  }

  getTypeBadgeClass(type: string): string {
    const colorMap: Record<string, string> = {
      'primary': 'badge bg-primary',
      'success': 'badge bg-success',
      'info': 'badge bg-info',
      'warning': 'badge bg-warning',
      'secondary': 'badge bg-secondary',
      'dark': 'badge bg-dark',
      'danger': 'badge bg-danger'
    };

    const color = (this.locationTypeColors as any)[type] || 'secondary';
    return colorMap[color] || 'badge bg-secondary';
  }
}
