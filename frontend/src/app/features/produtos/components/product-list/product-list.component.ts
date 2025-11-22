import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
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

/**
 * ProductListComponent - Product listing with filters and pagination
 *
 * Features:
 * - Paginated product table (20 items per page)
 * - Quick search (debounced 300ms)
 * - Filter by category (dropdown)
 * - Filter by status (active/inactive/discontinued)
 * - Actions: View, Edit, Delete
 * - Navigate to create product
 */
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
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

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router
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
        alert('Erro ao deletar produto: ' + (err.error?.message || err.message || 'Erro desconhecido'));
        this.loading = false;
        console.error('Error deleting product:', err);
      }
    });
  }

  /**
   * Goes to previous page
   */
  previousPage(): void {
    if (this.products && !this.products.first) {
      this.filters.page = (this.filters.page || 0) - 1;
      this.loadProducts();
    }
  }

  /**
   * Goes to next page
   */
  nextPage(): void {
    if (this.products && !this.products.last) {
      this.filters.page = (this.filters.page || 0) + 1;
      this.loadProducts();
    }
  }

  /**
   * Goes to specific page
   */
  goToPage(page: number): void {
    this.filters.page = page;
    this.loadProducts();
  }

  /**
   * Gets page numbers for pagination
   */
  getPageNumbers(): number[] {
    if (!this.products) return [];

    const totalPages = this.products.totalPages;
    const currentPage = this.products.number;
    const pages: number[] = [];

    // Show max 5 page numbers
    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);

    // Adjust if at beginning or end
    if (currentPage < 2) {
      endPage = Math.min(totalPages - 1, 4);
    }
    if (currentPage > totalPages - 3) {
      startPage = Math.max(0, totalPages - 5);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
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
