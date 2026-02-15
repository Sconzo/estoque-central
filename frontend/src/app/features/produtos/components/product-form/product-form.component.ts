import { Component, OnInit, inject } from '@angular/core';
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
import { MatTableModule } from '@angular/material/table';
import { MatStepperModule } from '@angular/material/stepper';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { VariantService } from '../../services/variant.service';
import { LocationService } from '../../../estoque/services/location.service';
import { TenantService } from '../../../../core/services/tenant.service';
import {
  ProductType,
  ProductStatus,
  ProductCreateRequest,
  ProductUpdateRequest,
  ProductAttribute,
  UNIT_OPTIONS,
  STATUS_LABELS,
  ProductDTO
} from '../../models/product.model';
import { Category } from '../../models/category.model';
import { ProductVariant, VariantAttribute } from '../../models/variant.model';
import { BomComponent, AddBomComponentRequest } from '../../models/composite.model';
import { Location } from '../../../estoque/models/location.model';
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
 * ProductFormComponent - Create/Edit product form with 3-step wizard
 *
 * Steps:
 * 1. Dados Gerais - Type, Name, SKU, Barcode, Description, Category, Status, Price, Cost, Unit, ControlsInventory
 * 2. Estoque - Location, Initial Qty, Min, Max (visible when controlsInventory=true)
 * 3. Atributos - Descriptive attributes + type-specific (variant attrs / BOM components)
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
    MatTableModule,
    MatStepperModule,
    VariantMatrixComponent
  ],
  providers: [
    {
      provide: STEPPER_GLOBAL_OPTIONS,
      useValue: { showError: true }
    }
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  private _fb = inject(FormBuilder);

  // Form groups for each stepper step (initialized eagerly for template binding)
  generalForm: FormGroup = this._fb.group({
    type: [ProductType.SIMPLE],
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

  stockForm: FormGroup = this._fb.group({
    locationId: [''],
    initialQuantity: [0, Validators.min(0)],
    minimumQuantity: [null],
    maximumQuantity: [null]
  });

  categories: Category[] = [];
  locations: Location[] = [];
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

  // Descriptive product attributes (key/value)
  productAttributes: ProductAttribute[] = [];

  // Edit mode: existing data for type-specific products
  existingVariants: ProductVariant[] = [];
  existingBomComponents: BomComponent[] = [];
  loadingTypeData = false;

  // Type labels for display
  readonly TYPE_LABELS: Record<ProductType, string> = {
    [ProductType.SIMPLE]: 'Produto Simples',
    [ProductType.VARIANT_PARENT]: 'Produto com Variantes',
    [ProductType.VARIANT]: 'Variante',
    [ProductType.COMPOSITE]: 'Kit/Composto (BOM)'
  };
  readonly STATUS_LABELS = STATUS_LABELS;

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
    private productService: ProductService,
    private categoryService: CategoryService,
    private variantService: VariantService,
    private compositeService: CompositeProductService,
    private locationService: LocationService,
    private tenantService: TenantService,
    private feedbackService: FeedbackService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    // Set up form subscriptions
    this.initForms();

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
    this.loadCategories();
    this.loadLocations();
    this.checkEditMode();
  }

  /**
   * Sets up form subscriptions (called in constructor after forms are created)
   */
  initForms(): void {
    // Sync type form control with selectedProductType
    this.generalForm.get('type')?.valueChanges.subscribe(value => {
      if (value) {
        this.selectedProductType = value;

        const bomTypeControl = this.generalForm.get('bomType');
        if (value === ProductType.COMPOSITE) {
          bomTypeControl?.setValidators([Validators.required]);
          if (!bomTypeControl?.value) {
            bomTypeControl?.setValue('VIRTUAL');
          }
        } else {
          bomTypeControl?.clearValidators();
          bomTypeControl?.setValue('');
        }
        bomTypeControl?.updateValueAndValidity();

        if (value === ProductType.VARIANT_PARENT && this.variantAttributes.length === 0) {
          this.addAttribute();
        }
      }
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
   * Loads locations for stock step dropdown
   */
  loadLocations(): void {
    const tenantId = this.tenantService.getCurrentTenant();
    if (tenantId) {
      this.locationService.listAll(tenantId).subscribe({
        next: (locations) => {
          this.locations = locations;
        },
        error: (err) => {
          console.error('Error loading locations:', err);
        }
      });
    }
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
        this.loadTypeSpecificDataForEdit(product);
        this.loadProductAttributes(id);
      },
      error: (err) => {
        this.error = 'Erro ao carregar produto: ' + (err.message || 'Erro desconhecido');
        this.loading = false;
        console.error('Error loading product:', err);
      }
    });
  }

  /**
   * Loads descriptive attributes for edit mode
   */
  loadProductAttributes(productId: string): void {
    this.productService.getAttributes(productId).subscribe({
      next: (attrs) => {
        this.productAttributes = attrs;
      },
      error: (err) => {
        console.error('Error loading product attributes:', err);
      }
    });
  }

  /**
   * Loads type-specific data for edit mode (variants or BOM components)
   */
  loadTypeSpecificDataForEdit(product: ProductDTO): void {
    if (product.type === ProductType.VARIANT_PARENT) {
      this.loadingTypeData = true;
      this.variantService.listVariants(product.id).subscribe({
        next: (variants) => {
          this.existingVariants = variants;
          this.loadingTypeData = false;
        },
        error: (err) => {
          console.error('Error loading variants:', err);
          this.loadingTypeData = false;
        }
      });
    } else if (product.type === ProductType.COMPOSITE) {
      this.loadingTypeData = true;
      this.compositeService.listComponents(product.id).subscribe({
        next: (components) => {
          this.existingBomComponents = components;
          this.loadingTypeData = false;
        },
        error: (err) => {
          console.error('Error loading BOM components:', err);
          this.loadingTypeData = false;
        }
      });
    }
  }

  /**
   * Patches form with product data
   */
  patchFormWithProduct(product: ProductDTO): void {
    this.selectedProductType = product.type;

    this.generalForm.patchValue({
      type: product.type,
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

    // Disable SKU in edit mode
    this.generalForm.get('sku')?.disable();
  }

  /**
   * Submits form (create or update)
   */
  onSubmit(): void {
    if (this.generalForm.invalid) {
      this.markFormGroupTouched(this.generalForm);
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
    if (this.selectedProductType === ProductType.VARIANT_PARENT) {
      if (!this.validateVariantAttributes()) {
        return;
      }
    }

    if (this.selectedProductType === ProductType.COMPOSITE) {
      if (!this.validateBomComponents()) {
        return;
      }
    }

    const generalValue = this.generalForm.getRawValue();
    const stockValue = this.stockForm.getRawValue();

    const request: ProductCreateRequest = {
      type: generalValue.type || ProductType.SIMPLE,
      bomType: generalValue.type === ProductType.COMPOSITE ? generalValue.bomType : undefined,
      name: generalValue.name,
      sku: generalValue.sku,
      barcode: generalValue.barcode || undefined,
      description: generalValue.description || undefined,
      categoryId: generalValue.categoryId,
      price: generalValue.price,
      cost: generalValue.cost > 0 ? generalValue.cost : undefined,
      unit: generalValue.unit,
      controlsInventory: generalValue.controlsInventory,
      status: generalValue.status,
      // Inventory fields
      locationId: generalValue.controlsInventory && stockValue.locationId ? stockValue.locationId : undefined,
      initialQuantity: generalValue.controlsInventory && stockValue.locationId ? (stockValue.initialQuantity || 0) : undefined,
      minimumQuantity: generalValue.controlsInventory && stockValue.locationId && stockValue.minimumQuantity != null ? stockValue.minimumQuantity : undefined,
      maximumQuantity: generalValue.controlsInventory && stockValue.locationId && stockValue.maximumQuantity != null ? stockValue.maximumQuantity : undefined,
      // Descriptive attributes
      attributes: this.productAttributes.filter(a => a.key && a.value).length > 0
        ? this.productAttributes.filter(a => a.key && a.value)
        : undefined
    };

    this.saving = true;
    this.error = null;

    this.productService.create(request).subscribe({
      next: (product) => {
        console.log('Product created:', product);

        if (this.selectedProductType === ProductType.VARIANT_PARENT) {
          this.createdProductId = product.id;
          this.saving = false;
        } else if (this.selectedProductType === ProductType.COMPOSITE) {
          this.saveBomComponents(product.id);
        } else {
          this.feedbackService.showSuccess('Produto criado com sucesso!');
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

    const generalValue = this.generalForm.value;

    const request: ProductUpdateRequest = {
      name: generalValue.name,
      description: generalValue.description || undefined,
      categoryId: generalValue.categoryId,
      price: generalValue.price,
      cost: generalValue.cost > 0 ? generalValue.cost : undefined,
      unit: generalValue.unit,
      controlsInventory: generalValue.controlsInventory,
      status: generalValue.status
    };

    this.saving = true;
    this.error = null;

    // Save product + attributes in parallel
    this.productService.update(this.productId, request).subscribe({
      next: (product) => {
        // Save attributes
        const validAttrs = this.productAttributes.filter(a => a.key && a.value);
        this.productService.saveAttributes(this.productId!, validAttrs).subscribe({
          next: () => {
            this.feedbackService.showSuccess('Produto atualizado com sucesso!');
            this.router.navigate(['/produtos']);
          },
          error: (err) => {
            // Product was saved, but attributes failed
            console.error('Error saving attributes:', err);
            this.feedbackService.showSuccess('Produto atualizado, mas houve erro ao salvar atributos.');
            this.router.navigate(['/produtos']);
          }
        });
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
   * Marks all form fields as touched
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
  hasError(fieldName: string, form?: FormGroup): boolean {
    const f = form || this.generalForm;
    const field = f.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  /**
   * Gets error message for field
   */
  getErrorMessage(fieldName: string, form?: FormGroup): string {
    const f = form || this.generalForm;
    const field = f.get(fieldName);
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
    this.router.navigate(['/produtos']);
  }

  /**
   * Handles product type change
   */
  onProductTypeChange(event: any): void {
    // Logic moved to initForms() valueChanges subscription
  }

  // ==================== Descriptive Product Attributes ====================

  /**
   * Adds new descriptive attribute (max 30)
   */
  addProductAttribute(): void {
    if (this.productAttributes.length >= 30) {
      this.feedbackService.showWarning('Máximo de 30 atributos permitidos');
      return;
    }
    this.productAttributes.push({ key: '', value: '' });
  }

  /**
   * Removes descriptive attribute
   */
  removeProductAttribute(index: number): void {
    this.productAttributes.splice(index, 1);
  }

  // ==================== Variant Attribute Methods ====================

  /**
   * Adds new variant attribute (max 3)
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
   * Removes variant attribute
   */
  removeAttribute(index: number): void {
    this.variantAttributes.splice(index, 1);
    this.calculateEstimatedVariants();
  }

  /**
   * Adds value to variant attribute using input
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
   * Removes value from variant attribute
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
    return this.generalForm.valid &&
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
    if (this.bomComponents.some(c => c.product.id === product.id)) {
      this.feedbackService.showWarning('Este produto já foi adicionado');
      return;
    }

    this.bomComponents.push({
      product,
      quantityRequired: 1
    });

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
    return this.generalForm.valid && this.bomComponents.length > 0;
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

  // ==================== Edit Mode BOM Management ====================

  /**
   * Adds BOM component in edit mode
   */
  addBomComponentInEdit(product: ProductDTO): void {
    if (!this.productId) return;

    if (this.existingBomComponents.some(c => c.componentProductId === product.id)) {
      this.feedbackService.showWarning('Este produto já foi adicionado');
      return;
    }

    const request: AddBomComponentRequest = {
      componentProductId: product.id,
      quantityRequired: 1
    };

    this.compositeService.addComponent(this.productId, request).subscribe({
      next: (component) => {
        this.existingBomComponents.push(component);
        this.feedbackService.showSuccess('Componente adicionado');
        this.productSearchQuery = '';
        this.productSearchResults = [];
      },
      error: (err) => {
        this.feedbackService.showError('Erro ao adicionar componente: ' + (err.error?.message || err.message));
        console.error('Error adding BOM component:', err);
      }
    });
  }

  /**
   * Updates BOM component quantity in edit mode
   */
  updateBomComponentInEdit(component: BomComponent, quantity: number): void {
    if (!this.productId || quantity <= 0) return;

    const request: AddBomComponentRequest = {
      componentProductId: component.componentProductId,
      quantityRequired: quantity
    };

    this.compositeService.updateComponentQuantity(this.productId, component.componentProductId, request).subscribe({
      next: (updated) => {
        const index = this.existingBomComponents.findIndex(c => c.id === component.id);
        if (index !== -1) {
          this.existingBomComponents[index] = updated;
        }
      },
      error: (err) => {
        this.feedbackService.showError('Erro ao atualizar quantidade: ' + (err.error?.message || err.message));
        console.error('Error updating BOM component:', err);
      }
    });
  }

  /**
   * Removes BOM component in edit mode
   */
  removeBomComponentInEdit(component: BomComponent): void {
    if (!this.productId) return;

    this.compositeService.removeComponent(this.productId, component.componentProductId).subscribe({
      next: () => {
        this.existingBomComponents = this.existingBomComponents.filter(c => c.id !== component.id);
        this.feedbackService.showSuccess('Componente removido');
      },
      error: (err) => {
        this.feedbackService.showError('Erro ao remover componente: ' + (err.error?.message || err.message));
        console.error('Error removing BOM component:', err);
      }
    });
  }

  /**
   * Formats variant attributes for display
   */
  formatVariantAttributes(variant: ProductVariant): string {
    if (!variant.attributeCombination) return '-';
    return Object.entries(variant.attributeCombination)
      .map(([key, value]) => `${key}: ${value}`)
      .join(', ');
  }

  /**
   * Gets status label for display
   */
  getStatusLabel(status: string): string {
    return this.STATUS_LABELS[status as ProductStatus] || status;
  }

  /**
   * Returns whether controls inventory checkbox is checked
   */
  get controlsInventory(): boolean {
    return this.generalForm?.get('controlsInventory')?.value === true;
  }
}
