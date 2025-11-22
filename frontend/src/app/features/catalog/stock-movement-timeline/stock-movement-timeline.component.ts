import { Component, OnInit, signal, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatDialog } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { StockMovementService } from '../services/stock-movement.service';
import { MovementDetailsModalComponent } from '../movement-details-modal/movement-details-modal.component';
import {
  StockMovementResponse,
  StockMovementFilters,
  MovementType,
  MOVEMENT_TYPE_INFO
} from '../../../shared/models/stock.model';

/**
 * StockMovementTimelineComponent - Timeline view for stock movement history
 * Story 2.8: Stock Movement History - AC5 (Frontend)
 *
 * Features:
 * - Chronological timeline of movements
 * - Visual representation of entries/exits
 * - Filtering by type, date range
 * - Balance tracking display
 * - Document linking
 */
@Component({
  selector: 'app-stock-movement-timeline',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    FormsModule
  ],
  templateUrl: './stock-movement-timeline.component.html',
  styleUrls: ['./stock-movement-timeline.component.css']
})
export class StockMovementTimelineComponent implements OnInit {
  private stockMovementService = inject(StockMovementService);
  private dialog = inject(MatDialog);

  // Input parameters
  productId = input<string>();
  variantId = input<string>();
  locationId = input<string>();

  // State
  movements = signal<StockMovementResponse[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  // Filters
  selectedType = signal<MovementType | null>(null);
  startDate = signal<Date | null>(null);
  endDate = signal<Date | null>(null);

  // Movement types for filter
  movementTypes = Object.values(MovementType);
  movementTypeInfo = MOVEMENT_TYPE_INFO;

  // Computed
  filteredMovements = computed(() => {
    let filtered = this.movements();

    if (this.selectedType()) {
      filtered = filtered.filter(m => m.type === this.selectedType());
    }

    if (this.startDate()) {
      const start = this.startDate()!;
      filtered = filtered.filter(m => new Date(m.createdAt) >= start);
    }

    if (this.endDate()) {
      const end = this.endDate()!;
      filtered = filtered.filter(m => new Date(m.createdAt) <= end);
    }

    return filtered;
  });

  totalEntries = computed(() => {
    return this.filteredMovements()
      .filter(m => m.quantity > 0)
      .reduce((sum, m) => sum + m.quantity, 0);
  });

  totalExits = computed(() => {
    return Math.abs(this.filteredMovements()
      .filter(m => m.quantity < 0)
      .reduce((sum, m) => sum + m.quantity, 0));
  });

  currentBalance = computed(() => {
    const movements = this.filteredMovements();
    if (movements.length === 0) return 0;
    return movements[0].balanceAfter; // First item is most recent
  });

  ngOnInit(): void {
    this.loadMovements();
  }

  loadMovements(): void {
    const productId = this.productId();
    const variantId = this.variantId();
    const locationId = this.locationId();

    if (!productId && !variantId) {
      this.error.set('Product ID or Variant ID is required');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.stockMovementService.getMovementTimeline({
      productId,
      variantId,
      locationId
    }).subscribe({
      next: (movements) => {
        this.movements.set(movements);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading movements:', err);
        this.error.set('Erro ao carregar histórico de movimentações');
        this.loading.set(false);
      }
    });
  }

  clearFilters(): void {
    this.selectedType.set(null);
    this.startDate.set(null);
    this.endDate.set(null);
  }

  getMovementColor(movement: StockMovementResponse): string {
    return this.movementTypeInfo[movement.type]?.color || '#757575';
  }

  isEntry(movement: StockMovementResponse): boolean {
    return movement.quantity > 0;
  }

  isExit(movement: StockMovementResponse): boolean {
    return movement.quantity < 0;
  }

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

  formatQuantity(quantity: number): string {
    const abs = Math.abs(quantity);
    return new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 3
    }).format(abs);
  }

  openMovementDetails(movement: StockMovementResponse): void {
    this.dialog.open(MovementDetailsModalComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: movement,
      panelClass: 'movement-details-dialog'
    });
  }
}
