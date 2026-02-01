import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
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
import { BomComponent, AddBomComponentRequest } from '../../models/composite.model';
import { CompositeProductService } from '../../services/composite.service';
import { VariantMatrixComponent } from '../variant-matrix/variant-matrix.component';
import { FeedbackService } from '../../../../shared/services/feedback.service';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';

/**
 * Interface for BOM component during creation (before product is saved)
 */
interface BomComponentDraft {
  product: ProductDTO;
  quantityRequired: number;
}

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
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatChipsModule,
    MatAutocompleteModule,
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

  // Variant attributes (for VARIANT_PARENT type during creation)
  variantAttributes: VariantAttribute[] = [];
  newAttributeValue = '';
  estimatedVariantCount = 0;

  // BOM components (for COMPOSITE type during creation)
  bomComponents: BomComponentDraft[] = [];
  productSearchResults: ProductDTO[] = [];
  productSearchQuery = '';
  searchingProducts = false;
  private productSearchSubject = new Subject<string>();

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
    private compositeService: CompositeProductService,
    private feedbackService: FeedbackService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    // Set up product search with debounce
    this.productSearchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        if (!query || query.length < 2) {
          return [];
        }
        this.searchingProducts = true;
        return this.productService.search(query);
      })
    ).subscribe({
      next: (results) => {
        // Filter out composite products and already added components
        const addedIds = this.bomComponents.map(c => c.product.id);
        this.productSearchResults = results.content
          .filter(p => p.type !== ProductType.COMPOSITE && !addedIds.includes(p.id));
        this.searchingProducts = false;
      },
      error: () => {
        this.productSearchResults = [];
        this.searchingProducts = false;
      }
    });
  }

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
    // For VARIANT_PARENT, validate attributes first
    if (this.selectedProductType === ProductType.VARIANT_PARENT) {
      if (!this.validateVariantAttributes()) {
        return;
      }
    }

    // For COMPOSITE, validate BOM components first
    if (this.selectedProductType === ProductType.COMPOSITE) {
      if (!this.validateBomComponents()) {
        return;
      }
    }

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

        // If it's a variant parent product, store the ID and show variant matrix for generation
        if (this.selectedProductType === ProductType.VARIANT_PARENT) {
          this.createdProductId = product.id;
          this.saving = false;
          // The VariantMatrixComponent will now be shown with pre-configured attributes
        } else if (this.selectedProductType === ProductType.COMPOSITE) {
          // Save BOM components after composite product is created
          this.saveBomComponents(product.id);
        } else {
          // Simple product - navigate back to list
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

    // Initialize attributes array for VARIANT_PARENT
    if (this.selectedProductType === ProductType.VARIANT_PARENT && this.variantAttributes.length === 0) {
      this.addAttribute();
    }
  }

  // ==================== Variant Attribute Methods ====================

  /**
   * Adds new attribute (max 3)
   */
  addAttribute(): void {
    if (this.variantAttributes.length >= 3) {
      this.feedbackService.showWarning('Máximo de 3 atributos permitidos');
      return;
    }

    this.variantAttributes.push({
      name: '',
      values: []
    });
  }

  /**
   * Removes attribute
   */
  removeAttribute(index: number): void {
    this.variantAttributes.splice(index, 1);
    this.calculateEstimatedVariants();
  }

  /**
   * Adds value to attribute using input
   */
  addValueToAttribute(index: number, inputElement: HTMLInputElement): void {
    const attribute = this.variantAttributes[index];
    const value = inputElement.value?.trim();

    if (!attribute.name || attribute.name.trim() === '') {
      this.feedbackService.showWarning('Por favor, defina o nome do atributo primeiro');
      return;
    }

    if (!value) {
      return;
    }

    if (attribute.values.includes(value)) {
      this.feedbackService.showWarning('Este valor já existe');
      return;
    }

    attribute.values.push(value);
    inputElement.value = '';
    this.calculateEstimatedVariants();
  }

  /**
   * Handles Enter key in value input
   */
  onValueInputKeydown(event: KeyboardEvent, index: number, inputElement: HTMLInputElement): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addValueToAttribute(index, inputElement);
    }
  }

  /**
   * Removes value from attribute
   */
  removeValue(attributeIndex: number, valueIndex: number): void {
    this.variantAttributes[attributeIndex].values.splice(valueIndex, 1);
    this.calculateEstimatedVariants();
  }

  /**
   * Calculates estimated number of variants
   */
  calculateEstimatedVariants(): void {
    const validAttributes = this.variantAttributes.filter(attr => attr.name && attr.values.length > 0);
    if (validAttributes.length === 0) {
      this.estimatedVariantCount = 0;
      return;
    }
    this.estimatedVariantCount = validAttributes.reduce((acc, attr) => acc * attr.values.length, 1);
  }

  /**
   * Validates variant attributes before creating product
   */
  validateVariantAttributes(): boolean {
    if (this.variantAttributes.length === 0) {
      this.error = 'Adicione pelo menos um atributo para produtos com variantes';
      return false;
    }

    for (const attr of this.variantAttributes) {
      if (!attr.name || attr.name.trim() === '') {
        this.error = 'Todos os atributos devem ter um nome';
        return false;
      }

      if (attr.values.length === 0) {
        this.error = `Atributo "${attr.name}" não tem valores`;
        return false;
      }
    }

    if (this.estimatedVariantCount > 100) {
      this.error = `Máximo de 100 variantes permitidas. Esta combinação geraria ${this.estimatedVariantCount} variantes.`;
      return false;
    }

    return true;
  }

  /**
   * Checks if can proceed with variant product creation
   */
  canCreateVariantProduct(): boolean {
    return this.productForm.valid &&
           this.variantAttributes.length > 0 &&
           this.variantAttributes.every(attr => attr.name && attr.values.length > 0) &&
           this.estimatedVariantCount > 0 &&
           this.estimatedVariantCount <= 100;
  }

  // ==================== BOM Component Methods ====================

  /**
   * Handles product search input
   */
  onProductSearch(query: string): void {
    this.productSearchQuery = query;
    this.productSearchSubject.next(query);
  }

  /**
   * Adds product to BOM components
   */
  addBomComponent(product: ProductDTO): void {
    // Check if already added
    if (this.bomComponents.some(c => c.product.id === product.id)) {
      this.feedbackService.showWarning('Este produto já foi adicionado');
      return;
    }

    this.bomComponents.push({
      product,
      quantityRequired: 1
    });

    // Clear search
    this.productSearchQuery = '';
    this.productSearchResults = [];
  }

  /**
   * Removes component from BOM
   */
  removeBomComponent(index: number): void {
    this.bomComponents.splice(index, 1);
  }

  /**
   * Updates component quantity
   */
  updateComponentQuantity(index: number, quantity: number): void {
    if (quantity > 0) {
      this.bomComponents[index].quantityRequired = quantity;
    }
  }

  /**
   * Validates BOM components before creating product
   */
  validateBomComponents(): boolean {
    if (this.bomComponents.length === 0) {
      this.error = 'Adicione pelo menos um componente para produtos compostos (Kit/BOM)';
      return false;
    }

    for (const component of this.bomComponents) {
      if (component.quantityRequired <= 0) {
        this.error = `Quantidade inválida para o componente "${component.product.name}"`;
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if can proceed with composite product creation
   */
  canCreateCompositeProduct(): boolean {
    return this.productForm.valid && this.bomComponents.length > 0;
  }

  /**
   * Saves BOM components after composite product is created
   */
  private saveBomComponents(productId: string): void {
    if (this.bomComponents.length === 0) {
      this.router.navigate(['/produtos']);
      return;
    }

    const saveRequests = this.bomComponents.map(component => {
      const request: AddBomComponentRequest = {
        componentProductId: component.product.id,
        quantityRequired: component.quantityRequired
      };
      return this.compositeService.addComponent(productId, request).toPromise();
    });

    Promise.all(saveRequests)
      .then(() => {
        this.feedbackService.showSuccess('Produto composto criado com sucesso!');
        this.router.navigate(['/produtos']);
      })
      .catch((err) => {
        this.error = 'Produto criado, mas houve erro ao salvar componentes: ' + (err.message || 'Erro desconhecido');
        console.error('Error saving BOM components:', err);
      });
  }

  /**
   * Displays product in autocomplete
   */
  displayProduct(product: ProductDTO): string {
    return product ? `${product.name} (${product.sku})` : '';
  }
}
