import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { MercadoLivreService } from '../../../integrations/services/mercadolivre.service';
import {
  ProductDTO,
  ProductSearchFilters,
  ProductStatus,
  Page,
  STATUS_LABELS,
  TYPE_LABELS
} from '../../models/product.model';
import { Category } from '../../models/category.model';
import { debounceTime, Subject } from 'rxjs';
import { FeedbackService } from '../../../../shared/services/feedback.service';

/**
 * ProductListComponent - Product listing with filters and pagination
 *
 * Features:
 * - Paginated product table (20 items per page) using Material Table
 * - Quick search (debounced 300ms) with Material form field
 * - Filter by category (Material dropdown)
 * - Filter by status (active/inactive/discontinued)
 * - Actions: View, Edit, Delete, Sync Stock
 * - Navigate to create product
 * - Material Design 3 components
 * - WCAG AA accessibility
 */
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  // Data
  products: Page<ProductDTO> | null = null;
  categories: Category[] = [];

  // Loading states
  loading = false;
  error: string | null = null;

  // Filters
  filters: ProductSearchFilters = {
    query: '',
    categoryId: undefined,
    status: undefined,
    page: 0,
    size: 20
  };

  // Search debounce
  private searchSubject = new Subject<string>();

  // Enum references for template
  readonly ProductStatus = ProductStatus;
  readonly STATUS_LABELS = STATUS_LABELS;
  readonly TYPE_LABELS = TYPE_LABELS;

  // Mat-table columns
  displayedColumns: string[] = ['sku', 'name', 'category', 'price', 'cost', 'inventory', 'status', 'actions'];

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router,
    private mlService: MercadoLivreService,
    private feedback: FeedbackService
  ) {
    // Setup search debounce
    this.searchSubject
      .pipe(debounceTime(300))
      .subscribe(query => {
        this.filters.query = query;
        this.filters.page = 0; // Reset to first page
        this.loadProducts();
      });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  /**
   * Loads categories for filter dropdown
   */
  loadCategories(): void {
    this.categoryService.listAll().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
      }
    });
  }

  /**
   * Loads products with current filters
   */
  loadProducts(): void {
    this.loading = true;
    this.error = null;

    this.productService.searchWithFilters(this.filters).subscribe({
      next: (page) => {
        this.products = page;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar produtos: ' + (err.message || 'Erro desconhecido');
        this.loading = false;
        console.error('Error loading products:', err);
      }
    });
  }

  /**
   * Handles search input (debounced)
   */
  onSearchChange(query: string): void {
    this.searchSubject.next(query);
  }

  /**
   * Handles category filter change
   */
  onCategoryChange(): void {
    this.filters.page = 0; // Reset to first page
    this.loadProducts();
  }

  /**
   * Handles status filter change
   */
  onStatusChange(): void {
    this.filters.page = 0; // Reset to first page
    this.loadProducts();
  }

  /**
   * Clears all filters
   */
  clearFilters(): void {
    this.filters = {
      query: '',
      categoryId: undefined,
      status: undefined,
      page: 0,
      size: 20
    };
    this.loadProducts();
  }

  /**
   * Navigates to create product page
   */
  createProduct(): void {
    this.router.navigate(['/produtos/novo']);
  }

  /**
   * Navigates to import products page
   */
  importProducts(): void {
    this.router.navigate(['/produtos/importar']);
  }

  /**
   * Navigates to product detail page
   */
  viewProduct(product: ProductDTO): void {
    this.router.navigate(['/produtos', product.id]);
  }

  /**
   * Navigates to edit product page
   */
  editProduct(product: ProductDTO): void {
    this.router.navigate(['/produtos', product.id, 'editar']);
  }

  /**
   * Deletes product with confirmation
   */
  deleteProduct(product: ProductDTO, event: Event): void {
    event.stopPropagation(); // Prevent row click

    const confirmMessage = `Tem certeza que deseja excluir o produto "${product.name}"?\n\nSKU: ${product.sku}\n\nEsta ação marcará o produto como inativo (soft delete).`;

    if (!confirm(confirmMessage)) {
      return;
    }

    this.loading = true;

    this.productService.delete(product.id).subscribe({
      next: () => {
        this.loadProducts();
      },
      error: (err) => {
        const errorMessage = err.error?.message || err.message || 'Erro desconhecido';
        this.feedback.showError(`Erro ao deletar produto: ${errorMessage}`, () => this.deleteProduct(product, event));
        this.loading = false;
        console.error('Error deleting product:', err);
      }
    });
  }

  /**
   * Sync stock to marketplaces manually
   * Story 5.4: Manual stock synchronization
   */
  syncStockToMarketplace(product: ProductDTO): void {
    const confirmMessage = `Sincronizar estoque do produto "${product.name}" com os marketplaces agora?\n\nSKU: ${product.sku}\n\nEsta ação enviará o estoque atual para todos os marketplaces onde este produto está publicado.`;

    if (!confirm(confirmMessage)) {
      return;
    }

    this.mlService.syncStock(product.id).subscribe({
      next: (response) => {
        this.feedback.showSuccess(`Sincronização enfileirada com sucesso! ${response.message}`);
      },
      error: (err) => {
        const errorMessage = err.error?.error || err.message || 'Erro desconhecido';
        this.feedback.showError(`Erro ao sincronizar estoque: ${errorMessage}`, () => this.syncStockToMarketplace(product));
        console.error('Error syncing stock:', err);
      }
    });
  }

  /**
   * Handles mat-paginator page change event
   */
  onPageChange(event: PageEvent): void {
    this.filters.page = event.pageIndex;
    this.filters.size = event.pageSize;
    this.loadProducts();
  }

  /**
   * Formats currency for display
   */
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }

  /**
   * Gets status label
   */
  getStatusLabel(status: ProductStatus): string {
    return STATUS_LABELS[status] || 'Desconhecido';
  }

  /**
   * Gets status badge class
   */
  getStatusClass(status: ProductStatus): string {
    switch (status) {
      case ProductStatus.ACTIVE:
        return 'badge-success';
      case ProductStatus.INACTIVE:
        return 'badge-warning';
      case ProductStatus.DISCONTINUED:
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }
}
