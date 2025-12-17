import { Component, OnInit, ViewChild } from '@angular/core';
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
import { CustomerService } from '../../services/customer.service';
import {
  Customer,
  PagedCustomers,
  CustomerType
} from '../../models/customer.model';
import { debounceTime, Subject } from 'rxjs';
import { FeedbackService } from '../../../../shared/services/feedback.service';

/**
 * CustomerListComponent - Customer listing with filters and pagination
 *
 * Story 4.1: Customer Management - AC6 (Frontend Customer List)
 *
 * Features:
 * - Paginated customer table (20 items per page) using Material Table
 * - Quick search (debounced 300ms) with Material form field
 * - Filter by customer type (PF/PJ) with Material select
 * - Filter by status (active/inactive)
 * - Actions: View, Edit, Delete (soft delete)
 * - Navigate to create customer
 * - Material Design 3 components
 * - WCAG AA accessibility
 */
@Component({
  selector: 'app-customer-list',
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
    MatTooltipModule
  ],
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.scss']
})
export class CustomerListComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  // Data
  customers: PagedCustomers | null = null;

  // Loading states
  loading = false;
  error: string | null = null;

  // Filters
  searchQuery = '';
  selectedType: CustomerType | '' = '';
  selectedStatus: boolean | '' = true;
  currentPage = 0;
  pageSize = 20;

  // Search debounce
  private searchSubject = new Subject<string>();

  // Enum references for template
  readonly CustomerType = CustomerType;

  // Mat-table columns
  displayedColumns: string[] = ['type', 'taxId', 'name', 'tradeName', 'email', 'phone', 'status', 'actions'];

  constructor(
    private customerService: CustomerService,
    private router: Router,
    private feedback: FeedbackService
  ) {
    // Setup search debounce
    this.searchSubject
      .pipe(debounceTime(300))
      .subscribe(query => {
        this.searchQuery = query;
        this.currentPage = 0; // Reset to first page
        this.loadCustomers();
      });
  }

  ngOnInit(): void {
    this.loadCustomers();
  }

  /**
   * Loads customers with current filters and pagination
   */
  loadCustomers(): void {
    this.loading = true;
    this.error = null;

    const customerType = this.selectedType || undefined;
    const ativo = this.selectedStatus === '' ? true : this.selectedStatus;

    this.customerService
      .listAll(customerType, ativo, this.currentPage, this.pageSize)
      .subscribe({
        next: (data) => {
          this.customers = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error loading customers:', err);
          this.error = 'Erro ao carregar clientes. Tente novamente.';
          this.loading = false;
        }
      });
  }

  /**
   * Handles search input with debounce
   */
  onSearchChange(query: string): void {
    this.searchSubject.next(query);
  }

  /**
   * Handles filter changes
   */
  onFilterChange(): void {
    this.currentPage = 0;
    this.loadCustomers();
  }

  /**
   * Handles mat-paginator page change event
   */
  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCustomers();
  }

  /**
   * Navigates to create customer page
   */
  createCustomer(): void {
    this.router.navigate(['/customers/new']);
  }

  /**
   * Navigates to edit customer page
   */
  editCustomer(id: string): void {
    this.router.navigate(['/customers/edit', id]);
  }

  /**
   * Soft deletes a customer (marks as inactive)
   */
  deleteCustomer(customer: Customer): void {
    if (customer.isDefaultConsumer) {
      this.feedback.showWarning('Não é possível excluir o cliente padrão "Consumidor Final".');
      return;
    }

    const confirmMsg = `Tem certeza que deseja inativar o cliente ${customer.displayName}?`;
    if (confirm(confirmMsg)) {
      this.customerService.softDelete(customer.id).subscribe({
        next: () => {
          this.loadCustomers(); // Reload list
        },
        error: (err) => {
          console.error('Error deleting customer:', err);
          this.feedback.showError('Erro ao excluir cliente.', () => this.deleteCustomer(customer));
        }
      });
    }
  }

  /**
   * Returns badge class for customer type
   */
  getTypeBadgeClass(type: CustomerType): string {
    return type === CustomerType.INDIVIDUAL ? 'badge-pf' : 'badge-pj';
  }

  /**
   * Returns badge label for customer type
   */
  getTypeLabel(type: CustomerType): string {
    return type === CustomerType.INDIVIDUAL ? 'PF' : 'PJ';
  }

  /**
   * Returns badge class for status
   */
  getStatusBadgeClass(ativo: boolean): string {
    return ativo ? 'badge-active' : 'badge-inactive';
  }

  /**
   * Returns badge label for status
   */
  getStatusLabel(ativo: boolean): string {
    return ativo ? 'Ativo' : 'Inativo';
  }

  /**
   * Formats CPF/CNPJ for display
   */
  getTaxId(customer: Customer): string {
    if (customer.customerType === CustomerType.INDIVIDUAL && customer.cpf) {
      return customer.cpf;
    }
    if (customer.customerType === CustomerType.BUSINESS && customer.cnpj) {
      return customer.cnpj;
    }
    return '-';
  }
}
