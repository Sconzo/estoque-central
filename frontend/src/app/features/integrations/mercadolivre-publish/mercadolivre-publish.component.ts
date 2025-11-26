import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MercadoLivreService, PublishProductResponse } from '../services/mercadolivre.service';
import { environment } from '../../../../environments/environment';

/**
 * MercadoLivrePublishComponent - Publish products to Mercado Livre
 * Story 5.3: Publish Products to Mercado Livre - AC5
 */

interface Product {
  id: string;
  name: string;
  sku: string;
  price: number;
  type: string;
  status: string;
}

@Component({
  selector: 'app-mercadolivre-publish',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="publish-container">
      <h2>Publicar Produtos no Mercado Livre</h2>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando produtos...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error" class="error-message">
        <strong>Erro:</strong> {{ error }}
      </div>

      <!-- Products List -->
      <div *ngIf="!loading && !error" class="products-section">
        <div class="filters">
          <input
            type="text"
            [(ngModel)]="searchTerm"
            (input)="filterProducts()"
            placeholder="Buscar produtos..."
            class="search-input"
          />
          <button (click)="loadProducts()" class="refresh-btn">
            Atualizar
          </button>
        </div>

        <div class="products-list">
          <div class="product-item" *ngFor="let product of filteredProducts">
            <input
              type="checkbox"
              [(ngModel)]="product.selected"
              [disabled]="product.alreadyPublished"
            />
            <div class="product-info">
              <strong>{{ product.name }}</strong>
              <span class="sku">SKU: {{ product.sku }}</span>
              <span class="price">R$ {{ product.price | number:'1.2-2' }}</span>
              <span
                *ngIf="product.alreadyPublished"
                class="badge published"
              >
                Já publicado
              </span>
            </div>
          </div>

          <div *ngIf="filteredProducts.length === 0" class="empty-state">
            Nenhum produto encontrado
          </div>
        </div>

        <div class="actions">
          <button
            (click)="publishSelected()"
            [disabled]="!hasSelectedProducts() || publishing"
            class="publish-btn"
          >
            {{ publishing ? 'Publicando...' : 'Publicar Selecionados' }}
            <span *ngIf="hasSelectedProducts()">
              ({{ getSelectedCount() }})
            </span>
          </button>
        </div>

        <!-- Publishing Progress -->
        <div *ngIf="publishing" class="progress-bar">
          <div class="progress-fill" [style.width.%]="publishProgress"></div>
        </div>

        <!-- Publish Result -->
        <div *ngIf="publishResult" class="result-message">
          <div class="success" *ngIf="publishResult.published > 0">
            ✓ {{ publishResult.published }} produto(s) publicado(s) com sucesso!
          </div>
          <div class="errors" *ngIf="publishResult.errors.length > 0">
            <h4>Erros ao publicar:</h4>
            <ul>
              <li *ngFor="let error of publishResult.errors">
                <strong>{{ error.productName }}:</strong> {{ error.errorMessage }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .publish-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    h2 {
      margin-bottom: 20px;
      color: #333;
    }

    .loading {
      text-align: center;
      padding: 40px;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #3498db;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
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

    .filters {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
    }

    .search-input {
      flex: 1;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }

    .refresh-btn {
      padding: 10px 20px;
      background-color: #6c757d;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .refresh-btn:hover {
      background-color: #5a6268;
    }

    .products-list {
      max-height: 500px;
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

    .product-item input[type="checkbox"]:disabled {
      cursor: not-allowed;
      opacity: 0.5;
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

    .empty-state {
      text-align: center;
      padding: 40px;
      color: #999;
    }

    .actions {
      display: flex;
      justify-content: flex-end;
    }

    .publish-btn {
      padding: 12px 24px;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 16px;
      font-weight: bold;
    }

    .publish-btn:hover:not(:disabled) {
      background-color: #0056b3;
    }

    .publish-btn:disabled {
      background-color: #ccc;
      cursor: not-allowed;
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

    .result-message {
      margin-top: 20px;
    }

    .success {
      background-color: #d4edda;
      border: 1px solid #c3e6cb;
      color: #155724;
      padding: 15px;
      border-radius: 4px;
      margin-bottom: 10px;
    }

    .errors {
      background-color: #f8d7da;
      border: 1px solid #f5c6cb;
      color: #721c24;
      padding: 15px;
      border-radius: 4px;
    }

    .errors h4 {
      margin: 0 0 10px 0;
    }

    .errors ul {
      margin: 0;
      padding-left: 20px;
    }

    .errors li {
      margin-bottom: 5px;
    }
  `]
})
export class MercadoLivrePublishComponent implements OnInit {
  private http = inject(HttpClient);
  private mlService = inject(MercadoLivreService);

  products: any[] = [];
  filteredProducts: any[] = [];
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

    // Load products from catalog API
    this.http.get<any>(`${environment.apiUrl}/products`).subscribe({
      next: (response) => {
        // Handle paginated response
        const products = response.content || response;

        // Check which products are already published
        this.products = products.map((p: Product) => ({
          ...p,
          selected: false,
          alreadyPublished: false // TODO: Check marketplace_listings
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

  getSelectedCount(): number {
    return this.products.filter(p => p.selected).length;
  }

  publishSelected() {
    const selectedIds = this.products
      .filter(p => p.selected)
      .map(p => p.id);

    if (selectedIds.length === 0) {
      return;
    }

    this.publishing = true;
    this.publishProgress = 0;
    this.publishResult = null;

    this.mlService.publishProducts({ productIds: selectedIds }).subscribe({
      next: (result) => {
        this.publishProgress = 100;
        this.publishResult = result;
        this.publishing = false;

        // Reload products to update published status
        setTimeout(() => {
          this.loadProducts();
        }, 2000);
      },
      error: (err) => {
        this.error = 'Erro ao publicar produtos: ' + (err.error?.message || err.message);
        this.publishing = false;
      }
    });
  }
}
