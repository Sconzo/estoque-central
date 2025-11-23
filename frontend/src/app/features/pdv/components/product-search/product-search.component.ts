import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../produtos/services/product.service';
import { CartService } from '../../services/cart.service';

/**
 * ProductSearchComponent - Search with barcode scanner support
 * Story 4.2: PDV Touchscreen Interface - AC2
 */
@Component({
  selector: 'app-product-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="product-search">
      <h3>Buscar Produto</h3>
      <input
        #searchInput
        type="text"
        [(ngModel)]="searchQuery"
        (input)="onInput($event)"
        placeholder="Digite ou escaneie código de barras..."
        class="search-input"
        autofocus
      />
      <div *ngIf="searchResults.length > 0" class="results">
        <div
          *ngFor="let product of searchResults"
          (click)="addProduct(product)"
          class="product-item"
        >
          <strong>{{ product.name }}</strong>
          <span class="price">R$ {{ product.price | number:'1.2-2' }}</span>
        </div>
      </div>
      <div *ngIf="message" class="message" [class.error]="isError">
        {{ message }}
      </div>
    </div>
  `,
  styles: [`
    .product-search {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    h3 {
      margin: 0 0 1rem 0;
    }
    .search-input {
      padding: 1rem;
      font-size: 1.2rem;
      border: 2px solid #ddd;
      border-radius: 4px;
      margin-bottom: 1rem;
    }
    .results {
      flex: 1;
      overflow-y: auto;
    }
    .product-item {
      padding: 1rem;
      border-bottom: 1px solid #eee;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      min-height: 44px;
    }
    .product-item:hover {
      background: #f8f9fa;
    }
    .price {
      color: #28a745;
      font-weight: bold;
    }
    .message {
      padding: 0.5rem;
      margin-top: 0.5rem;
      border-radius: 4px;
      background: #d4edda;
      color: #155724;
    }
    .message.error {
      background: #f8d7da;
      color: #721c24;
    }
  `]
})
export class ProductSearchComponent implements OnInit {
  @ViewChild('searchInput') searchInput!: ElementRef;
  searchQuery = '';
  searchResults: any[] = [];
  message = '';
  isError = false;
  private scanBuffer = '';
  private scanTimeout: any;

  constructor(
    private productService: ProductService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {}

  onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    clearTimeout(this.scanTimeout);

    this.scanBuffer = value;

    this.scanTimeout = setTimeout(() => {
      if (this.scanBuffer.length >= 8) {
        // Likely a barcode scan
        this.searchByBarcode(this.scanBuffer);
      } else if (this.scanBuffer.length >= 2) {
        // Manual search
        this.search(this.scanBuffer);
      } else {
        this.searchResults = [];
      }
    }, 300);
  }

  searchByBarcode(barcode: string): void {
    // Simplified: search by SKU/name (would need specific barcode endpoint)
    this.productService.listAll(0, 10).subscribe({
      next: (response) => {
        const product = response.content.find(p => p.barcode === barcode || p.sku === barcode);
        if (product) {
          this.addProduct(product);
          this.showMessage('Produto adicionado!');
        } else {
          this.showMessage('Produto não encontrado', true);
        }
        this.clearSearch();
      },
      error: () => this.showMessage('Erro na busca', true)
    });
  }

  search(query: string): void {
    this.productService.listAll(0, 10).subscribe({
      next: (response) => {
        this.searchResults = response.content.filter(p =>
          p.name.toLowerCase().includes(query.toLowerCase()) ||
          p.sku?.toLowerCase().includes(query.toLowerCase())
        ).slice(0, 10);
      },
      error: () => this.showMessage('Erro na busca', true)
    });
  }

  addProduct(product: any): void {
    this.cartService.addItem(product);
    this.showMessage(`${product.name} adicionado!`);
    this.clearSearch();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults = [];
    if (this.searchInput) {
      this.searchInput.nativeElement.value = '';
      this.searchInput.nativeElement.focus();
    }
  }

  showMessage(msg: string, error = false): void {
    this.message = msg;
    this.isError = error;
    setTimeout(() => this.message = '', 3000);
  }
}
