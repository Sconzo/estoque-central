import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProductService } from '../../services/product.service';
import { VariantService } from '../../services/variant.service';
import { CompositeProductService } from '../../services/composite.service';
import {
  ProductDTO,
  ProductType,
  ProductStatus,
  STATUS_LABELS,
  TYPE_LABELS,
  UNIT_OPTIONS
} from '../../models/product.model';
import { ProductVariant } from '../../models/variant.model';
import { BomComponent, BOM_TYPE_LABELS, BomType } from '../../models/composite.model';

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
  loading = true;
  loadingVariants = false;
  loadingBom = false;
  error: string | null = null;

  // Display columns for tables
  variantColumns = ['sku', 'name', 'attributes', 'price', 'status'];
  bomColumns = ['sku', 'name', 'quantity', 'unit'];

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
