import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ImportService } from '../../services/import.service';
import {
  ImportPreviewResponse,
  ImportConfirmResponse,
  ProductCsvRow,
  ImportStatus,
  IMPORT_STATUS_LABELS,
  IMPORT_STATUS_COLORS
} from '../../models/import.model';

/**
 * ProductImportComponent - CSV product import with preview and validation
 *
 * Workflow:
 * 1. User uploads CSV file
 * 2. Preview shows all rows with validation errors highlighted
 * 3. User reviews and confirms import
 * 4. Valid products are persisted
 */
@Component({
  selector: 'app-product-import',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './product-import.component.html',
  styleUrls: ['./product-import.component.scss']
})
export class ProductImportComponent {
  // Upload state
  selectedFile: File | null = null;
  uploadProgress = 0;
  isUploading = false;

  // Preview state
  preview: ImportPreviewResponse | null = null;
  showPreview = false;

  // Confirm state
  isConfirming = false;
  confirmResult: ImportConfirmResponse | null = null;

  // Error state
  errorMessage: string | null = null;

  // UI helpers
  IMPORT_STATUS_LABELS = IMPORT_STATUS_LABELS;
  IMPORT_STATUS_COLORS = IMPORT_STATUS_COLORS;

  constructor(
    private importService: ImportService,
    private router: Router
  ) {}

  /**
   * Handle file selection
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.errorMessage = null;
      this.preview = null;
      this.confirmResult = null;
      this.showPreview = false;
    }
  }

  /**
   * Upload and preview CSV
   */
  uploadAndPreview(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'Selecione um arquivo CSV';
      return;
    }

    this.isUploading = true;
    this.errorMessage = null;
    this.uploadProgress = 0;

    // TODO: Get tenantId from auth service
    const tenantId = '00000000-0000-0000-0000-000000000001';

    this.importService.preview(this.selectedFile, tenantId).subscribe({
      next: (response) => {
        this.preview = response;
        this.showPreview = true;
        this.isUploading = false;
        this.uploadProgress = 100;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erro ao processar arquivo CSV';
        this.isUploading = false;
        this.uploadProgress = 0;
      }
    });
  }

  /**
   * Confirm import and persist products
   */
  confirmImport(): void {
    if (!this.preview) {
      return;
    }

    this.isConfirming = true;
    this.errorMessage = null;

    // TODO: Get tenantId from auth service
    const tenantId = '00000000-0000-0000-0000-000000000001';

    this.importService.confirmImport(this.preview.importLogId, tenantId).subscribe({
      next: (response) => {
        this.confirmResult = response;
        this.isConfirming = false;

        // Navigate back to product list after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/produtos']);
        }, 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erro ao confirmar importação';
        this.isConfirming = false;
      }
    });
  }

  /**
   * Cancel import and go back
   */
  cancel(): void {
    this.router.navigate(['/produtos']);
  }

  /**
   * Download CSV template
   */
  downloadTemplate(): void {
    this.importService.downloadTemplate().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'template_importacao_produtos.csv';
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        this.errorMessage = 'Erro ao baixar template';
      }
    });
  }

  /**
   * Reset component state
   */
  reset(): void {
    this.selectedFile = null;
    this.uploadProgress = 0;
    this.isUploading = false;
    this.preview = null;
    this.showPreview = false;
    this.isConfirming = false;
    this.confirmResult = null;
    this.errorMessage = null;
  }

  /**
   * Get row CSS class based on validation
   */
  getRowClass(row: ProductCsvRow): string {
    return row.valid ? 'table-success' : 'table-danger';
  }

  /**
   * Get validation icon
   */
  getValidationIcon(row: ProductCsvRow): string {
    return row.valid ? '✓' : '✗';
  }
}
