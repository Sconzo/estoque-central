import { Component, inject, model } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { Router } from '@angular/router';
import {
  StockMovementResponse,
  MOVEMENT_TYPE_INFO
} from '../../../shared/models/stock.model';

/**
 * MovementDetailsModal - Modal component for detailed stock movement view
 * Story 2.8: Stock Movement History - AC7
 *
 * Features:
 * - Complete movement details display
 * - Link to source document (sale, purchase, etc.)
 * - Export to PDF functionality
 * - Visual indicators for entry/exit
 */
@Component({
  selector: 'app-movement-details-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule
  ],
  templateUrl: './movement-details-modal.component.html',
  styleUrls: ['./movement-details-modal.component.css']
})
export class MovementDetailsModalComponent {
  private dialogRef = inject(MatDialogRef<MovementDetailsModalComponent>);
  private router = inject(Router);

  // Model input from dialog data
  movement = model.required<StockMovementResponse>();

  movementTypeInfo = MOVEMENT_TYPE_INFO;

  close(): void {
    this.dialogRef.close();
  }

  getMovementColor(): string {
    return this.movementTypeInfo[this.movement().type]?.color || '#757575';
  }

  isEntry(): boolean {
    return this.movement().quantity > 0;
  }

  isExit(): boolean {
    return this.movement().quantity < 0;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(date);
  }

  formatQuantity(quantity: number): string {
    const abs = Math.abs(quantity);
    return new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 3
    }).format(abs);
  }

  goToDocument(): void {
    const movement = this.movement();
    if (!movement.documentType || !movement.documentId) {
      return;
    }

    // Close modal first
    this.dialogRef.close();

    // Navigate to document based on type
    switch (movement.documentType) {
      case 'SALE':
        this.router.navigate(['/sales', movement.documentId]);
        break;
      case 'PURCHASE':
        this.router.navigate(['/purchases', movement.documentId]);
        break;
      case 'TRANSFER':
        this.router.navigate(['/transfers', movement.documentId]);
        break;
      default:
        console.warn('Unknown document type:', movement.documentType);
    }
  }

  exportToPdf(): void {
    // TODO: Implement PDF export
    // This would call a backend endpoint to generate a PDF receipt
    console.log('Export to PDF functionality to be implemented');

    // Future implementation:
    // this.stockMovementService.exportMovementToPdf(this.movement().id)
    //   .subscribe(blob => {
    //     const url = window.URL.createObjectURL(blob);
    //     const link = document.createElement('a');
    //     link.href = url;
    //     link.download = `movement-${this.movement().id}.pdf`;
    //     link.click();
    //   });
  }

  hasDocument(): boolean {
    const movement = this.movement();
    return !!(movement.documentType && movement.documentId);
  }
}
