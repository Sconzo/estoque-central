import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { VariantService } from '../../services/variant.service';
import {
  ProductType,
  ProductStatus,
  ProductCreateRequest,
  ProductUpdateRequest,
  UNIT_OPTIONS,
  STATUS_LABELS,
  ProductDTO
} from '../../models/product.model';
import { Category } from '../../models/category.model';
import { ProductVariant, VariantAttribute } from '../../models/variant.model';
import { VariantMatrixComponent } from '../variant-matrix/variant-matrix.component';

/**
 * ProductFormComponent - Create/Edit product form
 *
 * Features:
 * - Reactive form with validation
 * - Create mode (route: /produtos/novo)
 * - Edit mode (route: /produtos/:id/editar)
 * - Category dropdown (hierarchical)
 * - Unit of measure dropdown
 * - Status dropdown
 * - Price/Cost validation (>= 0)
 * - SKU uniqueness validation (backend)
 */
@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    VariantMatrixComponent
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  productForm!: FormGroup;
  categories: Category[] = [];
  isEditMode = false;
  productId: string | null = null;
  loading = false;
  saving = false;
  error: string | null = null;

  // Variant support
  selectedProductType: ProductType = ProductType.SIMPLE;
  createdProductId: string | null = null;
  variantsGenerated: ProductVariant[] = [];

  // Options for template
  readonly UNIT_OPTIONS = UNIT_OPTIONS;
  readonly STATUS_OPTIONS = Object.entries(STATUS_LABELS).map(([value, label]) => ({ value, label }));
  readonly PRODUCT_TYPE_OPTIONS = [
    { value: ProductType.SIMPLE, label: 'Produto Simples' },
    { value: ProductType.VARIANT_PARENT, label: 'Produto com Variantes (matriz)' },
    { value: ProductType.COMPOSITE, label: 'Kit/Composto (BOM)' }
  ];
  readonly BOM_TYPE_OPTIONS = [
    { value: 'VIRTUAL', label: 'Virtual (estoque calculado dos componentes)' },
    { value: 'PHYSICAL', label: 'Físico (kit pré-montado com estoque próprio)' }
  ];
  readonly ProductStatus = ProductStatus;
  readonly ProductType = ProductType;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService,
    private variantService: VariantService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();
    this.checkEditMode();
  }

  /**
   * Initializes reactive form
   */
  initForm(): void {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(200)]],
      sku: ['', [Validators.required, Validators.maxLength(100)]],
      barcode: ['', Validators.maxLength(100)],
      description: [''],
      categoryId: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      cost: [0, Validators.min(0)],
      unit: ['UN', Validators.required],
      controlsInventory: [true],
      status: [ProductStatus.ACTIVE, Validators.required],
      bomType: ['']
    });
  }

  /**
   * Loads categories for dropdown
   */
  loadCategories(): void {
    this.categoryService.listAll().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.error = 'Erro ao carregar categorias';
      }
    });
  }

  /**
   * Checks if in edit mode and loads product
   */
  checkEditMode(): void {
    this.productId = this.route.snapshot.paramMap.get('id');

    if (this.productId) {
      this.isEditMode = true;
      this.loadProduct(this.productId);
    }
  }

  /**
   * Loads product for editing
   */
  loadProduct(id: string): void {
    this.loading = true;
    this.error = null;

    this.productService.getById(id).subscribe({
      next: (product) => {
        this.patchFormWithProduct(product);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar produto: ' + (err.message || 'Erro desconhecido');
        this.loading = false;
        console.error('Error loading product:', err);
      }
    });
  }

  /**
   * Patches form with product data
   */
  patchFormWithProduct(product: ProductDTO): void {
    this.selectedProductType = product.type;

    this.productForm.patchValue({
      name: product.name,
      sku: product.sku,
      barcode: product.barcode || '',
      description: product.description || '',
      categoryId: product.categoryId,
      price: product.price,
      cost: product.cost || 0,
      unit: product.unit,
      controlsInventory: product.controlsInventory,
      status: product.status,
      bomType: product.bomType || ''
    });

    // Disable SKU in edit mode (cannot change)
    this.productForm.get('sku')?.disable();
  }

  /**
   * Submits form (create or update)
   */
  onSubmit(): void {
    if (this.productForm.invalid) {
      this.markFormGroupTouched(this.productForm);
      return;
    }

    if (this.isEditMode) {
      this.updateProduct();
    } else {
      this.createProduct();
    }
  }

  /**
   * Creates new product
   */
  createProduct(): void {
    const formValue = this.productForm.value;

    const request: ProductCreateRequest = {
      type: this.selectedProductType,
      bomType: this.selectedProductType === ProductType.COMPOSITE ? formValue.bomType : undefined,
      name: formValue.name,
      sku: formValue.sku,
      barcode: formValue.barcode || undefined,
      description: formValue.description || undefined,
      categoryId: formValue.categoryId,
      price: formValue.price,
      cost: formValue.cost > 0 ? formValue.cost : undefined,
      unit: formValue.unit,
      controlsInventory: formValue.controlsInventory,
      status: formValue.status
    };

    this.saving = true;
    this.error = null;

    this.productService.create(request).subscribe({
      next: (product) => {
        console.log('Product created:', product);

        // If it's a variant parent product, store the ID and wait for variant configuration
        if (this.selectedProductType === ProductType.VARIANT_PARENT) {
          this.createdProductId = product.id;
          this.saving = false;
          // Don't navigate yet - let user configure variants
        } else {
          // Simple or Composite product - navigate back to list
          // TODO: For COMPOSITE, consider showing component configuration screen
          this.router.navigate(['/produtos']);
        }
      },
      error: (err) => {
        this.saving = false;
        this.error = this.parseError(err);
        console.error('Error creating product:', err);
      }
    });
  }

  /**
   * Updates existing product
   */
  updateProduct(): void {
    if (!this.productId) return;

    const formValue = this.productForm.value;

    const request: ProductUpdateRequest = {
      name: formValue.name,
      description: formValue.description || undefined,
      categoryId: formValue.categoryId,
      price: formValue.price,
      cost: formValue.cost > 0 ? formValue.cost : undefined,
      unit: formValue.unit,
      controlsInventory: formValue.controlsInventory,
      status: formValue.status
    };

    this.saving = true;
    this.error = null;

    this.productService.update(this.productId, request).subscribe({
      next: (product) => {
        console.log('Product updated:', product);
        this.router.navigate(['/produtos']);
      },
      error: (err) => {
        this.saving = false;
        this.error = this.parseError(err);
        console.error('Error updating product:', err);
      }
    });
  }

  /**
   * Cancels and returns to product list
   */
  cancel(): void {
    this.router.navigate(['/produtos']);
  }

  /**
   * Marks all form fields as touched (shows validation errors)
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  /**
   * Parses error message from HTTP error
   */
  private parseError(err: any): string {
    if (err.error?.message) {
      return err.error.message;
    }
    if (err.message) {
      return err.message;
    }
    return 'Erro desconhecido ao salvar produto';
  }

  /**
   * Checks if field has error and is touched
   */
  hasError(fieldName: string): boolean {
    const field = this.productForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  /**
   * Gets error message for field
   */
  getErrorMessage(fieldName: string): string {
    const field = this.productForm.get(fieldName);
    if (!field || !field.errors) return '';

    if (field.errors['required']) return 'Este campo é obrigatório';
    if (field.errors['maxLength']) return `Máximo de ${field.errors['maxLength'].requiredLength} caracteres`;
    if (field.errors['min']) return `Valor mínimo: ${field.errors['min'].min}`;

    return 'Valor inválido';
  }

  /**
   * Handles when variants are generated and saved
   */
  onVariantsGenerated(variants: ProductVariant[]): void {
    this.variantsGenerated = variants;
    console.log('Variants generated:', variants);

    // Navigate to product list after variants are saved
    this.router.navigate(['/produtos']);
  }

  /**
   * Handles product type change
   */
  onProductTypeChange(event: any): void {
    this.selectedProductType = event.value as ProductType;

    // Update bomType validation based on product type
    const bomTypeControl = this.productForm.get('bomType');
    if (this.selectedProductType === ProductType.COMPOSITE) {
      bomTypeControl?.setValidators([Validators.required]);
      bomTypeControl?.setValue('VIRTUAL'); // Default to VIRTUAL
    } else {
      bomTypeControl?.clearValidators();
      bomTypeControl?.setValue('');
    }
    bomTypeControl?.updateValueAndValidity();
  }
}
