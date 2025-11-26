import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MercadoLivreService, SyncLog, SyncLogsResponse } from '../services/mercadolivre.service';

/**
 * MercadoLivreSyncHistoryComponent - Displays sync history logs
 * Story 5.4: Stock Synchronization to Mercado Livre - AC6
 */
@Component({
  selector: 'app-mercadolivre-sync-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mercadolivre-sync-history.component.html',
  styleUrls: ['./mercadolivre-sync-history.component.scss']
})
export class MercadoLivreSyncHistoryComponent implements OnInit {
  private mlService = inject(MercadoLivreService);

  logs: SyncLog[] = [];
  loading = false;
  error: string | null = null;

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;

  // Filters
  selectedStatus = '';
  statuses = ['', 'SUCCESS', 'FAILED', 'PENDING', 'PROCESSING', 'ERROR'];

  ngOnInit() {
    this.loadSyncLogs();
  }

  loadSyncLogs() {
    this.loading = true;
    this.error = null;

    const statusFilter = this.selectedStatus || undefined;

    this.mlService.getSyncLogs(this.currentPage, this.pageSize, statusFilter).subscribe({
      next: (response: SyncLogsResponse) => {
        this.logs = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.currentPage;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar histórico de sincronização';
        console.error('Error loading sync logs:', err);
        this.loading = false;
      }
    });
  }

  onStatusFilterChange() {
    this.currentPage = 0;
    this.loadSyncLogs();
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadSyncLogs();
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.goToPage(this.currentPage - 1);
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.goToPage(this.currentPage + 1);
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'SUCCESS':
        return 'badge-success';
      case 'FAILED':
      case 'ERROR':
        return 'badge-error';
      case 'PENDING':
        return 'badge-warning';
      case 'PROCESSING':
        return 'badge-info';
      default:
        return 'badge-default';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('pt-BR');
  }

  formatValue(value: number | undefined): string {
    return value !== undefined && value !== null ? value.toString() : '-';
  }
}
