import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MercadoLivreService, PublishProductResponse, CategorySuggestion } from '../services/mercadolivre.service';
import { environment } from '../../../../environments/environment';
import { forkJoin } from 'rxjs';

/**
 * MercadoLivrePublishWizardComponent - 4-step wizard for publishing products
 * Story 5.3: Publish Products to Mercado Livre - AC5 (Complete)
 *
 * Wizard Steps:
 * 1. Select Products - Choose products to publish
 * 2. Configure Categories - Set ML category for each product
 * 3. Preview - Review products before publishing
 * 4. Publish - Execute publish and show results
 */

interface Product {
  id: string;
  name: string;
  sku: string;
  price: number;
  type: string;
  description: string;
  selected?: boolean;
  alreadyPublished?: boolean;
  mlCategory?: CategorySuggestion;
  categoryLoading?: boolean;
}

@Component({
  selector: 'app-mercadolivre-publish-wizard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="wizard-container">
      <h2>Publicar Produtos no Mercado Livre</h2>

      <!-- Wizard Steps Indicator -->
      <div class="wizard-steps">
        <div class="step" [class.active]="currentStep === 1" [class.completed]="currentStep > 1">
          <div class="step-number">1</div>
          <div class="step-label">Selecionar Produtos</div>
        </div>
        <div class="step-divider"></div>
        <div class="step" [class.active]="currentStep === 2" [class.completed]="currentStep > 2">
          <div class="step-number">2</div>
          <div class="step-label">Configurar Categorias</div>
        </div>
        <div class="step-divider"></div>
        <div class="step" [class.active]="currentStep === 3" [class.completed]="currentStep > 3">
          <div class="step-number">3</div>
          <div class="step-label">Visualizar</div>
        </div>
        <div class="step-divider"></div>
        <div class="step" [class.active]="currentStep === 4" [class.completed]="currentStep > 4">
          <div class="step-number">4</div>
          <div class="step-label">Publicar</div>
        </div>
      </div>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error" class="error-message">
        <strong>Erro:</strong> {{ error }}
      </div>

      <!-- STEP 1: Select Products -->
      <div *ngIf="currentStep === 1 && !loading" class="step-content">
        <h3>Passo 1: Selecionar Produtos</h3>
        <p class="step-description">Escolha os produtos que deseja publicar no Mercado Livre</p>

        <div class="filters">
          <input
            type="text"
            [(ngModel)]="searchTerm"
            (input)="filterProducts()"
            placeholder="Buscar produtos..."
            class="search-input"
          />
        </div>

        <div class="products-list">
          <div class="product-item" *ngFor="let product of filteredProducts">
            <input
              type="checkbox"
              [(ngModel)]="product.selected"
              [disabled]="!!product.alreadyPublished"
            />
            <div class="product-info">
              <strong>{{ product.name }}</strong>
              <span class="sku">SKU: {{ product.sku }}</span>
              <span class="price">R$ {{ product.price | number:'1.2-2' }}</span>
              <span *ngIf="product.alreadyPublished" class="badge published">Já publicado</span>
            </div>
          </div>
        </div>

        <div class="wizard-actions">
          <button (click)="goToStep2()" [disabled]="!hasSelectedProducts()" class="btn-next">
            Próximo: Configurar Categorias →
          </button>
        </div>
      </div>

      <!-- STEP 2: Configure Categories -->
      <div *ngIf="currentStep === 2 && !loading" class="step-content">
        <h3>Passo 2: Configurar Categorias</h3>
        <p class="step-description">Configure a categoria do Mercado Livre para cada produto</p>

        <div class="category-config-list">
          <div class="category-item" *ngFor="let product of selectedProducts">
            <div class="product-summary">
              <strong>{{ product.name }}</strong>
              <span class="sku">{{ product.sku }}</span>
            </div>

            <div class="category-selector">
              <label>Categoria sugerida:</label>
              <div *ngIf="product.categoryLoading" class="mini-spinner"></div>
              <div *ngIf="!product.categoryLoading && product.mlCategory" class="category-display">
                <strong>{{ product.mlCategory.categoryName }}</strong>
                <small>{{ product.mlCategory.categoryPath }}</small>
              </div>
              <button
                (click)="refreshCategory(product)"
                [disabled]="product.categoryLoading"
                class="btn-refresh"
              >
                🔄 Atualizar
              </button>
            </div>
          </div>
        </div>

        <div class="wizard-actions">
          <button (click)="currentStep = 1" class="btn-back">← Voltar</button>
          <button (click)="goToStep3()" [disabled]="!allCategoriesConfigured()" class="btn-next">
            Próximo: Visualizar →
          </button>
        </div>
      </div>

      <!-- STEP 3: Preview -->
      <div *ngIf="currentStep === 3 && !loading" class="step-content">
        <h3>Passo 3: Visualizar Anúncios</h3>
        <p class="step-description">Revise os anúncios antes de publicar</p>

        <div class="preview-list">
          <div class="preview-item" *ngFor="let product of selectedProducts">
            <div class="preview-card">
              <div class="preview-image">
                <div class="placeholder-image">📦</div>
              </div>
              <div class="preview-details">
                <h4>{{ product.name }}</h4>
                <p class="preview-price">R$ {{ product.price | number:'1.2-2' }}</p>
                <p class="preview-category">
                  <strong>Categoria:</strong> {{ product.mlCategory?.categoryName }}
                </p>
                <p class="preview-description">{{ product.description || 'Sem descrição' }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="wizard-actions">
          <button (click)="currentStep = 2" class="btn-back">← Voltar</button>
          <button (click)="goToStep4()" class="btn-next">Próximo: Publicar →</button>
        </div>
      </div>

      <!-- STEP 4: Publish -->
      <div *ngIf="currentStep === 4 && !loading" class="step-content">
        <h3>Passo 4: Publicar</h3>

        <div *ngIf="!publishing && !publishResult" class="publish-confirmation">
          <p class="confirmation-text">
            Você está prestes a publicar <strong>{{ selectedProducts.length }}</strong> produto(s) no Mercado Livre.
          </p>
          <p class="warning-text">⚠️ Esta ação criará anúncios ativos no marketplace.</p>

          <div class="wizard-actions">
            <button (click)="currentStep = 3" class="btn-back">← Voltar</button>
            <button (click)="publishProducts()" class="btn-publish">✓ Confirmar e Publicar</button>
          </div>
        </div>

        <div *ngIf="publishing" class="publishing-state">
          <div class="spinner"></div>
          <p>Publicando produtos...</p>
          <div class="progress-bar">
            <div class="progress-fill" [style.width.%]="publishProgress"></div>
          </div>
        </div>

        <div *ngIf="publishResult && !publishing" class="publish-results">
          <div class="success-summary" *ngIf="publishResult.published > 0">
            <div class="success-icon">✓</div>
            <h4>{{ publishResult.published }} produto(s) publicado(s) com sucesso!</h4>
          </div>

          <div class="error-summary" *ngIf="publishResult.errors.length > 0">
            <h4>⚠️ Erros ao publicar ({{ publishResult.errors.length }}):</h4>
            <ul class="error-list">
              <li *ngFor="let error of publishResult.errors">
                <strong>{{ error.productName }}:</strong> {{ error.errorMessage }}
              </li>
            </ul>
          </div>

          <div class="wizard-actions">
            <button (click)="resetWizard()" class="btn-primary">Publicar Mais Produtos</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .wizard-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    h2 {
      margin-bottom: 30px;
      color: #333;
    }

    /* Wizard Steps */
    .wizard-steps {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40px;
      padding: 20px;
      background: #f8f9fa;
      border-radius: 8px;
    }

    .step {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
    }

    .step-number {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #e9ecef;
      color: #6c757d;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 18px;
    }

    .step.active .step-number {
      background: #007bff;
      color: white;
    }

    .step.completed .step-number {
      background: #28a745;
      color: white;
    }

    .step-label {
      font-size: 12px;
      color: #6c757d;
      text-align: center;
    }

    .step.active .step-label {
      color: #007bff;
      font-weight: bold;
    }

    .step-divider {
      flex: 1;
      height: 2px;
      background: #dee2e6;
      margin: 0 10px;
    }

    /* Step Content */
    .step-content {
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    h3 {
      margin: 0 0 10px 0;
      color: #333;
    }

    .step-description {
      color: #666;
      margin-bottom: 20px;
    }

    /* Common Elements */
    .loading, .publishing-state {
      text-align: center;
      padding: 40px;
    }

    .spinner, .mini-spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #007bff;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    .mini-spinner {
      width: 20px;
      height: 20px;
      border-width: 3px;
      display: inline-block;
      margin: 0;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .error-message {
      background-color: #ffe6e6;
      border: 1px solid #ff0000;
      color: #cc0000;
      padding: 15px;
      border-radius: 4px;
      margin-bottom: 20px;
    }

    /* Step 1: Product Selection */
    .filters {
      margin-bottom: 20px;
    }

    .search-input {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }

    .products-list {
      max-height: 400px;
      overflow-y: auto;
      border: 1px solid #ddd;
      border-radius: 4px;
      margin-bottom: 20px;
    }

    .product-item {
      display: flex;
      align-items: center;
      padding: 15px;
      border-bottom: 1px solid #eee;
    }

    .product-item:last-child {
      border-bottom: none;
    }

    .product-item:hover {
      background-color: #f8f9fa;
    }

    .product-item input[type="checkbox"] {
      margin-right: 15px;
      width: 18px;
      height: 18px;
      cursor: pointer;
    }

    .product-info {
      display: flex;
      flex-direction: column;
      gap: 5px;
      flex: 1;
    }

    .sku {
      color: #666;
      font-size: 13px;
    }

    .price {
      color: #28a745;
      font-weight: bold;
    }

    .badge {
      display: inline-block;
      padding: 3px 8px;
      border-radius: 3px;
      font-size: 12px;
      font-weight: bold;
    }

    .badge.published {
      background-color: #d4edda;
      color: #155724;
    }

    /* Step 2: Category Configuration */
    .category-config-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
      margin-bottom: 20px;
    }

    .category-item {
      padding: 20px;
      border: 1px solid #ddd;
      border-radius: 8px;
      background: #f8f9fa;
    }

    .product-summary {
      margin-bottom: 15px;
    }

    .product-summary strong {
      display: block;
      margin-bottom: 5px;
    }

    .category-selector {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .category-selector label {
      font-weight: bold;
      min-width: 150px;
    }

    .category-display {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 5px;
    }

    .category-display small {
      color: #666;
      font-size: 12px;
    }

    .btn-refresh {
      padding: 8px 16px;
      background: #6c757d;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .btn-refresh:hover:not(:disabled) {
      background: #5a6268;
    }

    /* Step 3: Preview */
    .preview-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 20px;
    }

    .preview-card {
      border: 1px solid #ddd;
      border-radius: 8px;
      overflow: hidden;
      background: white;
    }

    .preview-image {
      height: 150px;
      background: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .placeholder-image {
      font-size: 48px;
    }

    .preview-details {
      padding: 15px;
    }

    .preview-details h4 {
      margin: 0 0 10px 0;
      color: #333;
    }

    .preview-price {
      color: #28a745;
      font-size: 20px;
      font-weight: bold;
      margin: 10px 0;
    }

    .preview-category {
      font-size: 13px;
      color: #666;
      margin: 8px 0;
    }

    .preview-description {
      font-size: 13px;
      color: #999;
      margin-top: 10px;
    }

    /* Step 4: Publish */
    .publish-confirmation {
      text-align: center;
      padding: 40px;
    }

    .confirmation-text {
      font-size: 18px;
      margin-bottom: 20px;
    }

    .warning-text {
      color: #856404;
      background: #fff3cd;
      border: 1px solid #ffeeba;
      padding: 15px;
      border-radius: 4px;
      margin-bottom: 30px;
    }

    .progress-bar {
      width: 100%;
      height: 8px;
      background-color: #e9ecef;
      border-radius: 4px;
      overflow: hidden;
      margin-top: 20px;
    }

    .progress-fill {
      height: 100%;
      background-color: #007bff;
      transition: width 0.3s ease;
    }

    .publish-results {
      padding: 20px;
    }

    .success-summary {
      text-align: center;
      padding: 30px;
      background: #d4edda;
      border: 1px solid #c3e6cb;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .success-icon {
      font-size: 48px;
      color: #28a745;
      margin-bottom: 10px;
    }

    .error-summary {
      background: #f8d7da;
      border: 1px solid #f5c6cb;
      padding: 20px;
      border-radius: 8px;
    }

    .error-list {
      margin: 15px 0 0 0;
      padding-left: 20px;
    }

    .error-list li {
      margin-bottom: 10px;
      color: #721c24;
    }

    /* Wizard Actions */
    .wizard-actions {
      display: flex;
      justify-content: space-between;
      margin-top: 30px;
      gap: 10px;
    }

    .wizard-actions button {
      padding: 12px 24px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 16px;
      font-weight: bold;
    }

    .btn-back {
      background: #6c757d;
      color: white;
    }

    .btn-back:hover {
      background: #5a6268;
    }

    .btn-next {
      background: #007bff;
      color: white;
      margin-left: auto;
    }

    .btn-next:hover:not(:disabled) {
      background: #0056b3;
    }

    .btn-next:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    .btn-publish {
      background: #28a745;
      color: white;
      margin-left: auto;
    }

    .btn-publish:hover {
      background: #218838;
    }

    .btn-primary {
      background: #007bff;
      color: white;
      margin: 0 auto;
    }

    .btn-primary:hover {
      background: #0056b3;
    }
  `]
})
export class MercadoLivrePublishWizardComponent implements OnInit {
  private http = inject(HttpClient);
  private mlService = inject(MercadoLivreService);

  currentStep = 1;
  products: Product[] = [];
  filteredProducts: Product[] = [];
  selectedProducts: Product[] = [];
  searchTerm = '';
  loading = false;
  publishing = false;
  publishProgress = 0;
  error: string | null = null;
  publishResult: PublishProductResponse | null = null;

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.loading = true;
    this.error = null;

    this.http.get<any>(`${environment.apiUrl}/products`).subscribe({
      next: (response) => {
        const products = response.content || response;

        this.products = products.map((p: any) => ({
          ...p,
          selected: false,
          alreadyPublished: false,
          categoryLoading: false
        }));

        this.filteredProducts = [...this.products];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar produtos: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  filterProducts() {
    const term = this.searchTerm.toLowerCase();
    this.filteredProducts = this.products.filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.sku.toLowerCase().includes(term)
    );
  }

  hasSelectedProducts(): boolean {
    return this.products.some(p => p.selected);
  }

  goToStep2() {
    this.selectedProducts = this.products.filter(p => p.selected);
    this.currentStep = 2;

    // Load category suggestions for all selected products
    this.loadCategorySuggestions();
  }

  loadCategorySuggestions() {
    this.selectedProducts.forEach(product => {
      product.categoryLoading = true;
      this.mlService.getCategorySuggestion(product.name).subscribe({
        next: (category) => {
          product.mlCategory = category;
          product.categoryLoading = false;
        },
        error: (err) => {
          console.error('Error loading category for', product.name, err);
          product.categoryLoading = false;
        }
      });
    });
  }

  refreshCategory(product: Product) {
    product.categoryLoading = true;
    this.mlService.getCategorySuggestion(product.name).subscribe({
      next: (category) => {
        product.mlCategory = category;
        product.categoryLoading = false;
      },
      error: (err) => {
        console.error('Error refreshing category', err);
        product.categoryLoading = false;
      }
    });
  }

  allCategoriesConfigured(): boolean {
    return this.selectedProducts.every(p => p.mlCategory && !p.categoryLoading);
  }

  goToStep3() {
    this.currentStep = 3;
  }

  goToStep4() {
    this.currentStep = 4;
  }

  publishProducts() {
    const productIds = this.selectedProducts.map(p => p.id);

    this.publishing = true;
    this.publishProgress = 0;

    this.mlService.publishProducts({ productIds }).subscribe({
      next: (result) => {
        this.publishProgress = 100;
        this.publishResult = result;
        this.publishing = false;
      },
      error: (err) => {
        this.error = 'Erro ao publicar produtos: ' + (err.error?.message || err.message);
        this.publishing = false;
      }
    });
  }

  resetWizard() {
    this.currentStep = 1;
    this.selectedProducts = [];
    this.publishResult = null;
    this.publishProgress = 0;
    this.loadProducts();
  }
}
