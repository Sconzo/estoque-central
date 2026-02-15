import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProductService } from '../../services/product.service';
import { VariantService } from '../../services/variant.service';
import { CompositeProductService } from '../../services/composite.service';
import { StockService } from '../../../catalog/services/stock.service';
import {
  ProductDTO,
  ProductType,
  ProductStatus,
  ProductAttribute,
  STATUS_LABELS,
  TYPE_LABELS,
  UNIT_OPTIONS
} from '../../models/product.model';
import { ProductVariant } from '../../models/variant.model';
import { BomComponent, BOM_TYPE_LABELS, BomType } from '../../models/composite.model';
import { StockByLocationResponse, LocationStock } from '../../../../shared/models/stock.model';

/**
 * ProductDetailComponent - Read-only product detail view
 *
 * Displays complete product information including:
 * - Basic info (SKU, name, category, status)
 * - Pricing (price, cost, margin)
 * - Inventory control settings
 * - Variants table (for VARIANT_PARENT)
 * - BOM components table (for COMPOSITE)
 * - Audit info (created/updated dates)
 */
@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatTooltipModule,
    DecimalPipe,
    DatePipe
  ],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  product: ProductDTO | null = null;
  variants: ProductVariant[] = [];
  bomComponents: BomComponent[] = [];
  productAttributes: ProductAttribute[] = [];
  stockData: StockByLocationResponse | null = null;
  loading = true;
  loadingVariants = false;
  loadingBom = false;
  loadingAttributes = false;
  loadingStock = false;
  error: string | null = null;

  // Variant stock data
  variantStockMap = new Map<string, StockByLocationResponse>();
  variantStockTotals = { available: 0, reserved: 0, forSale: 0 };
  loadingVariantStock = false;

  // Display columns for tables
  variantColumns = ['sku', 'name', 'attributes', 'price', 'stock', 'status'];
  bomColumns = ['sku', 'name', 'quantity', 'unit'];
  stockColumns = ['location', 'available', 'reserved', 'forSale', 'minimum', 'status'];

  // Labels for template
  readonly STATUS_LABELS = STATUS_LABELS;
  readonly TYPE_LABELS = TYPE_LABELS;
  readonly BOM_TYPE_LABELS = BOM_TYPE_LABELS;
  readonly ProductType = ProductType;
  readonly ProductStatus = ProductStatus;
  readonly BomType = BomType;

  constructor(
    private productService: ProductService,
    private variantService: VariantService,
    private compositeService: CompositeProductService,
    private stockService: StockService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProduct(id);
    } else {
      this.error = 'ID do produto não encontrado';
      this.loading = false;
    }
  }

  /**
   * Loads product and type-specific data
   */
  loadProduct(id: string): void {
    this.loading = true;
    this.error = null;

    this.productService.getById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
        this.loadTypeSpecificData(product);
        this.loadProductAttributes(product.id);
        if (product.controlsInventory && product.type !== ProductType.VARIANT_PARENT) {
          this.loadStockData(product.id);
        }
      },
      error: (err) => {
        this.error = 'Erro ao carregar produto: ' + (err.error?.message || err.message || 'Erro desconhecido');
        this.loading = false;
        console.error('Error loading product:', err);
      }
    });
  }

  /**
   * Loads variants or BOM components based on product type
   */
  loadTypeSpecificData(product: ProductDTO): void {
    if (product.type === ProductType.VARIANT_PARENT) {
      this.loadVariants(product.id);
    } else if (product.type === ProductType.COMPOSITE) {
      this.loadBomComponents(product.id);
    }
  }

  /**
   * Loads variants for VARIANT_PARENT products
   */
  loadVariants(productId: string): void {
    this.loadingVariants = true;
    this.variantService.listVariants(productId).subscribe({
      next: (variants) => {
        this.variants = variants;
        this.loadingVariants = false;
        if (this.product?.controlsInventory && variants.length > 0) {
          this.loadVariantStockData(variants);
        }
      },
      error: (err) => {
        console.error('Error loading variants:', err);
        this.loadingVariants = false;
      }
    });
  }

  /**
   * Loads BOM components for COMPOSITE products
   */
  loadBomComponents(productId: string): void {
    this.loadingBom = true;
    this.compositeService.listComponents(productId).subscribe({
      next: (components) => {
        this.bomComponents = components;
        this.loadingBom = false;
      },
      error: (err) => {
        console.error('Error loading BOM components:', err);
        this.loadingBom = false;
      }
    });
  }

  /**
   * Loads descriptive attributes for the product
   */
  loadProductAttributes(productId: string): void {
    this.loadingAttributes = true;
    this.productService.getAttributes(productId).subscribe({
      next: (attrs) => {
        this.productAttributes = attrs;
        this.loadingAttributes = false;
      },
      error: (err) => {
        console.error('Error loading product attributes:', err);
        this.loadingAttributes = false;
      }
    });
  }

  /**
   * Loads stock data for the product
   */
  loadStockData(productId: string): void {
    this.loadingStock = true;
    this.stockService.getStockByProductByLocation(productId).subscribe({
      next: (data) => {
        this.stockData = data;
        this.loadingStock = false;
      },
      error: (err) => {
        console.error('Error loading stock data:', err);
        this.stockData = {
          productName: this.product?.name || '',
          productSku: this.product?.sku || '',
          totalLocations: 0,
          totalQuantityAvailable: 0,
          totalReservedQuantity: 0,
          totalQuantityForSale: 0,
          byLocation: []
        };
        this.loadingStock = false;
      }
    });
  }

  /**
   * Loads stock data for each variant and calculates aggregated totals
   */
  loadVariantStockData(variants: ProductVariant[]): void {
    this.loadingVariantStock = true;
    this.variantStockMap.clear();
    this.variantStockTotals = { available: 0, reserved: 0, forSale: 0 };

    const emptyStock: StockByLocationResponse = {
      productName: '',
      productSku: '',
      totalLocations: 0,
      totalQuantityAvailable: 0,
      totalReservedQuantity: 0,
      totalQuantityForSale: 0,
      byLocation: []
    };

    const requests: Record<string, ReturnType<StockService['getStockByVariant']>> = {};
    for (const variant of variants) {
      requests[variant.id] = this.stockService.getStockByVariant(variant.id).pipe(
        catchError(() => of(emptyStock))
      );
    }

    forkJoin(requests).subscribe({
      next: (results) => {
        let totalAvailable = 0;
        let totalReserved = 0;
        let totalForSale = 0;

        for (const [variantId, stockData] of Object.entries(results)) {
          this.variantStockMap.set(variantId, stockData);
          totalAvailable += stockData.totalQuantityAvailable || 0;
          totalReserved += stockData.totalReservedQuantity || 0;
          totalForSale += stockData.totalQuantityForSale || 0;
        }

        this.variantStockTotals = {
          available: totalAvailable,
          reserved: totalReserved,
          forSale: totalForSale
        };
        this.loadingVariantStock = false;
      },
      error: (err) => {
        console.error('Error loading variant stock data:', err);
        this.loadingVariantStock = false;
      }
    });
  }

  /**
   * Gets total available stock for a variant
   */
  getVariantStockTotal(variantId: string): number {
    const stock = this.variantStockMap.get(variantId);
    return stock?.totalQuantityAvailable || 0;
  }

  /**
   * Gets stock status label
   */
  getStockStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'OK': 'Normal',
      'LOW': 'Baixo',
      'CRITICAL': 'Crítico'
    };
    return labels[status] || status;
  }

  /**
   * Formats variant attributes for display
   */
  formatAttributes(variant: ProductVariant): string {
    if (!variant.attributeCombination) return '-';
    return Object.entries(variant.attributeCombination)
      .map(([key, value]) => `${key}: ${value}`)
      .join(', ');
  }

  /**
   * Gets unit label from unit code
   */
  getUnitLabel(unitCode: string): string {
    const unit = UNIT_OPTIONS.find(u => u.value === unitCode);
    return unit ? unit.label : unitCode;
  }

  /**
   * Calculates profit margin percentage
   */
  calculateMargin(): number | null {
    if (!this.product || !this.product.cost || this.product.cost === 0) {
      return null;
    }
    return ((this.product.price - this.product.cost) / this.product.cost) * 100;
  }

  /**
   * Gets status label for display
   */
  getStatusLabel(status: string): string {
    return this.STATUS_LABELS[status as ProductStatus] || status;
  }

  /**
   * Gets BOM type label for display
   */
  getBomTypeLabel(bomType: string | undefined): string {
    if (!bomType) return '-';
    return this.BOM_TYPE_LABELS[bomType as BomType] || bomType;
  }

  /**
   * Navigates to edit page
   */
  editProduct(): void {
    if (this.product) {
      this.router.navigate(['/produtos', this.product.id, 'editar']);
    }
  }

  /**
   * Navigates back to product list
   */
  goBack(): void {
    this.router.navigate(['/produtos']);
  }
}
