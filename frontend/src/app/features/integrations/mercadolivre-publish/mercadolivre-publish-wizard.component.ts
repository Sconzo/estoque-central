import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatStepperModule } from '@angular/material/stepper';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MercadoLivreService, PublishProductResponse, CategorySuggestion } from '../services/mercadolivre.service';
import { environment } from '../../../../environments/environment';
import { forkJoin } from 'rxjs';

/**
 * MercadoLivrePublishWizardComponent - 4-step wizard for publishing products
 * Story 5.3: Publish Products to Mercado Livre - AC5 (Complete)
 * Refactored with Material Stepper
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
  imports: [
    CommonModule,
    FormsModule,
    MatStepperModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatChipsModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatExpansionModule,
    MatTooltipModule,
    MatDividerModule
  ],
  templateUrl: './mercadolivre-publish-wizard.component.html',
  styleUrls: ['./mercadolivre-publish-wizard.component.scss']
})
export class MercadoLivrePublishWizardComponent implements OnInit {
  private http = inject(HttpClient);
  private mlService = inject(MercadoLivreService);

  currentStep = 1;
  products: Product[] = [];
  filteredProducts: Product[] = [];
  selectedProducts: Product[] = [];
  selectedProductIds: string[] = [];
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
    // Update selected products based on selectedProductIds
    this.selectedProducts = this.products.filter(p => this.selectedProductIds.includes(p.id));
    return this.selectedProducts.length > 0;
  }

  goToStep2() {
    this.selectedProducts = this.products.filter(p => this.selectedProductIds.includes(p.id));
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
    return this.selectedProducts.length > 0 &&
           this.selectedProducts.every(p => p.mlCategory && !p.categoryLoading);
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

    // Simulate progress for better UX
    const progressInterval = setInterval(() => {
      if (this.publishProgress < 90) {
        this.publishProgress += 10;
      }
    }, 500);

    this.mlService.publishProducts({ productIds }).subscribe({
      next: (result) => {
        clearInterval(progressInterval);
        this.publishProgress = 100;
        this.publishResult = result;
        this.publishing = false;
      },
      error: (err) => {
        clearInterval(progressInterval);
        this.error = 'Erro ao publicar produtos: ' + (err.error?.message || err.message);
        this.publishing = false;
      }
    });
  }

  resetWizard() {
    this.currentStep = 1;
    this.selectedProducts = [];
    this.selectedProductIds = [];
    this.publishResult = null;
    this.publishProgress = 0;
    this.loadProducts();
  }
}
