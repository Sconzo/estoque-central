import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CustomerService } from '../../services/customer.service';
import {
  Customer,
  PagedCustomers,
  CustomerType
} from '../../models/customer.model';
import { debounceTime, Subject } from 'rxjs';

/**
 * CustomerListComponent - Customer listing with filters and pagination
 *
 * Story 4.1: Customer Management - AC6 (Frontend Customer List)
 *
 * Features:
 * - Paginated customer table (20 items per page)
 * - Quick search (debounced 300ms)
 * - Filter by customer type (PF/PJ)
 * - Filter by status (active/inactive)
 * - Actions: View, Edit, Delete (soft delete)
 * - Navigate to create customer
 */
@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.scss']
})
export class CustomerListComponent implements OnInit {
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

  constructor(
    private customerService: CustomerService,
    private router: Router
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
   * Navigates to previous page
   */
  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadCustomers();
    }
  }

  /**
   * Navigates to next page
   */
  nextPage(): void {
    if (this.customers && this.currentPage < this.customers.totalPages - 1) {
      this.currentPage++;
      this.loadCustomers();
    }
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
      alert('Não é possível excluir o cliente padrão "Consumidor Final".');
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
          alert('Erro ao excluir cliente. Tente novamente.');
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
