import { Component, OnInit, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SafetyMarginService, SafetyMarginRule, CreateSafetyMarginRuleRequest, UpdateSafetyMarginRuleRequest } from '../services/safety-margin.service';
import { ProductService } from '../../produtos/services/product.service';
import { CategoryService } from '../../produtos/services/category.service';
import { ProductDTO } from '../../produtos/models/product.model';
import { Category } from '../../produtos/models/category.model';

/**
 * SafetyMarginModalComponent - Modal for creating/editing safety margin rules
 * Story 5.7: Configurable Safety Stock Margin - AC7
 */
@Component({
  selector: 'app-safety-margin-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './safety-margin-modal.component.html',
  styleUrls: ['./safety-margin-modal.component.scss']
})
export class SafetyMarginModalComponent implements OnInit {
  private safetyMarginService = inject(SafetyMarginService);
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);

  @Input() editingRule: SafetyMarginRule | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  // Form data
  marketplace = 'MERCADO_LIVRE';
  priority: 'PRODUCT' | 'CATEGORY' | 'GLOBAL' = 'GLOBAL';
  selectedProductId: string | null = null;
  selectedCategoryId: string | null = null;
  marginPercentage = 100;

  // Data for dropdowns
  products: ProductDTO[] = [];
  categories: Category[] = [];

  // Search/filter for products
  productSearchTerm = '';
  filteredProducts: ProductDTO[] = [];

  // UI state
  loading = false;
  loadingProducts = false;
  loadingCategories = false;
  error: string | null = null;
  saving = false;

  ngOnInit() {
    this.loadCategories();
    this.loadProducts();

    // Pre-fill form if editing
    if (this.editingRule) {
      this.marketplace = this.editingRule.marketplace;
      this.priority = this.editingRule.priority;
      this.marginPercentage = this.editingRule.marginPercentage;
      this.selectedProductId = this.editingRule.productId || null;
      this.selectedCategoryId = this.editingRule.categoryId || null;
    }
  }

  loadProducts() {
    this.loadingProducts = true;
    this.productService.listAll(0, 1000).subscribe({
      next: (page) => {
        this.products = page.content;
        this.filteredProducts = this.products;
        this.loadingProducts = false;
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.loadingProducts = false;
      }
    });
  }

  loadCategories() {
    this.loadingCategories = true;
    this.categoryService.listAll().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loadingCategories = false;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.loadingCategories = false;
      }
    });
  }

  onProductSearchChange() {
    const term = this.productSearchTerm.toLowerCase();
    if (!term) {
      this.filteredProducts = this.products;
    } else {
      this.filteredProducts = this.products.filter(p =>
        p.name.toLowerCase().includes(term) ||
        (p.sku && p.sku.toLowerCase().includes(term))
      );
    }
  }

  onPriorityChange() {
    // Reset selections when priority changes
    this.selectedProductId = null;
    this.selectedCategoryId = null;
  }

  onCloseClick() {
    this.close.emit();
  }

  validateForm(): boolean {
    if (!this.marketplace) {
      this.error = 'Marketplace é obrigatório';
      return false;
    }

    if (!this.priority) {
      this.error = 'Nível/Prioridade é obrigatório';
      return false;
    }

    if (this.priority === 'PRODUCT' && !this.selectedProductId) {
      this.error = 'Produto é obrigatório para regra específica de produto';
      return false;
    }

    if (this.priority === 'CATEGORY' && !this.selectedCategoryId) {
      this.error = 'Categoria é obrigatória para regra de categoria';
      return false;
    }

    if (this.marginPercentage < 0 || this.marginPercentage > 100) {
      this.error = 'Margem deve estar entre 0% e 100%';
      return false;
    }

    return true;
  }

  onSaveClick() {
    this.error = null;

    if (!this.validateForm()) {
      return;
    }

    this.saving = true;

    if (this.editingRule) {
      // Update existing rule
      const request: UpdateSafetyMarginRuleRequest = {
        marginPercentage: this.marginPercentage
      };

      this.safetyMarginService.updateRule(this.editingRule.id, request).subscribe({
        next: () => {
          this.saving = false;
          this.saved.emit();
        },
        error: (err) => {
          this.error = 'Erro ao atualizar regra: ' + (err.error?.message || err.message);
          console.error('Error updating rule:', err);
          this.saving = false;
        }
      });
    } else {
      // Create new rule
      const request: CreateSafetyMarginRuleRequest = {
        marketplace: this.marketplace,
        priority: this.priority,
        marginPercentage: this.marginPercentage
      };

      if (this.priority === 'PRODUCT') {
        request.productId = this.selectedProductId!;
      } else if (this.priority === 'CATEGORY') {
        request.categoryId = this.selectedCategoryId!;
      }

      this.safetyMarginService.createRule(request).subscribe({
        next: () => {
          this.saving = false;
          this.saved.emit();
        },
        error: (err) => {
          this.error = 'Erro ao criar regra: ' + (err.error?.message || err.message);
          console.error('Error creating rule:', err);
          this.saving = false;
        }
      });
    }
  }

  getMarginLabel(): string {
    if (this.marginPercentage === 100) {
      return '100% (sem margem de segurança)';
    }
    const reduction = 100 - this.marginPercentage;
    return `${this.marginPercentage}% (reduz ${reduction}% do estoque anunciado)`;
  }

  getExampleText(): string {
    if (this.marginPercentage === 100) {
      return 'Exemplo: 100 unidades disponíveis → 100 unidades anunciadas no marketplace';
    }
    const published = Math.floor(100 * (this.marginPercentage / 100));
    return `Exemplo: 100 unidades disponíveis → ${published} unidades anunciadas no marketplace`;
  }
}
