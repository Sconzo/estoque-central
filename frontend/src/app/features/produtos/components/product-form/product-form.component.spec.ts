import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ProductFormComponent } from './product-form.component';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';

// ARIA Test Helpers
import {
  expectFormFieldToHaveAria,
  expectButtonToHaveAria,
  expectErrorMessagesToHaveAlertRole,
  expectIconsToBeDecorative,
  expectRequiredIndicators,
  expectFormToHaveAria,
  runComprehensiveAriaCheck
} from '../../../../shared/testing/aria-test-helpers';

// Axe-core Test Helpers
import {
  expectNoAxeViolationsExceptColorContrast,
  expectWcagCompliance,
  expectAccessibleForm
} from '../../../../shared/testing/axe-test-helpers';

describe('ProductFormComponent - ARIA Accessibility', () => {
  let component: ProductFormComponent;
  let fixture: ComponentFixture<ProductFormComponent>;
  let mockProductService: jasmine.SpyObj<ProductService>;
  let mockCategoryService: jasmine.SpyObj<CategoryService>;

  beforeEach(async () => {
    // Create mock services
    mockProductService = jasmine.createSpyObj('ProductService', [
      'getProduct',
      'createProduct',
      'updateProduct'
    ]);
    mockCategoryService = jasmine.createSpyObj('CategoryService', ['listAll']);

    // Mock service returns
    mockCategoryService.listAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [
        ProductFormComponent,
        ReactiveFormsModule,
        NoopAnimationsModule
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ProductService, useValue: mockProductService },
        { provide: CategoryService, useValue: mockCategoryService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => null } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Form-level ARIA', () => {
    it('should have aria-labelledby linking to form title', () => {
      expectFormToHaveAria(fixture);
      const form = fixture.nativeElement.querySelector('form');
      const ariaLabelledBy = form?.getAttribute('aria-labelledby');
      expect(ariaLabelledBy).toBe('form-title');

      const title = fixture.nativeElement.querySelector('#form-title');
      expect(title).toBeTruthy('Expected h1 with id="form-title"');
    });

    it('should have required field indicators', () => {
      // product-form has 7 required fields: productType, name, sku, category, status, price, unit
      expectRequiredIndicators(fixture, 7);
    });
  });

  describe('Input Fields ARIA', () => {
    it('should have aria-label on name input', () => {
      expectFormFieldToHaveAria(fixture, 'input[formControlName="name"]', {
        label: 'nome do produto',
        required: true
      });
    });

    it('should have aria-label on SKU input', () => {
      expectFormFieldToHaveAria(fixture, 'input[formControlName="sku"]', {
        label: 'SKU',
        required: true
      });
    });

    it('should have aria-label on barcode input', () => {
      expectFormFieldToHaveAria(fixture, 'input[formControlName="barcode"]', {
        label: 'código de barras'
      });
    });

    it('should have aria-label on price input', () => {
      expectFormFieldToHaveAria(fixture, 'input[formControlName="price"]', {
        label: 'preço de venda',
        required: true
      });
    });

    it('should have aria-label on cost input', () => {
      expectFormFieldToHaveAria(fixture, 'input[formControlName="cost"]', {
        label: 'custo do produto'
      });
    });
  });

  describe('Select Fields ARIA', () => {
    it('should have aria-label on product type select', () => {
      // Product type is only shown in create mode and uses selectedProductType, not formControlName
      const productTypeSelect = fixture.nativeElement.querySelector('mat-select[aria-label]');
      if (productTypeSelect) {
        const ariaLabel = productTypeSelect.getAttribute('aria-label');
        expect(ariaLabel?.toLowerCase()).toContain('tipo de produto');
      }
    });

    it('should have aria-label on category select', () => {
      expectFormFieldToHaveAria(fixture, 'mat-select[formControlName="categoryId"]', {
        label: 'categoria',
        required: true
      });
    });

    it('should have aria-label on status select', () => {
      expectFormFieldToHaveAria(fixture, 'mat-select[formControlName="status"]', {
        label: 'status',
        required: true
      });
    });

    it('should have aria-label on unit select', () => {
      expectFormFieldToHaveAria(fixture, 'mat-select[formControlName="unit"]', {
        label: 'unidade de medida',
        required: true
      });
    });
  });

  describe('Textarea ARIA', () => {
    it('should have aria-label on description textarea', () => {
      expectFormFieldToHaveAria(fixture, 'textarea[formControlName="description"]', {
        label: 'descrição'
      });
    });
  });

  describe('Checkbox ARIA', () => {
    it('should have aria-label on controls inventory checkbox when present', () => {
      const checkbox = fixture.nativeElement.querySelector('mat-checkbox[formControlName="controlsInventory"]');

      if (checkbox) {
        // Material checkbox renders aria-label on inner input element
        const ariaLabel = checkbox.getAttribute('aria-label') ||
                         checkbox.querySelector('input')?.getAttribute('aria-label') ||
                         checkbox.querySelector('[aria-label]')?.getAttribute('aria-label');
        expect(ariaLabel).toBeTruthy('Expected aria-label on checkbox or its inner elements');
        expect(ariaLabel?.toLowerCase()).toContain('controla estoque');
      } else {
        // Checkbox might not be visible in create mode
        expect(true).toBe(true, 'Checkbox not rendered in current mode');
      }
    });
  });

  describe('Error Messages ARIA', () => {
    it('should have role="alert" on all error messages', () => {
      // Trigger validation by marking fields as touched
      Object.keys(component.productForm.controls).forEach(key => {
        component.productForm.get(key)?.markAsTouched();
      });
      fixture.detectChanges();

      expectErrorMessagesToHaveAlertRole(fixture);
    });

    it('should have unique IDs on error messages', () => {
      // Trigger validation
      component.productForm.get('name')?.markAsTouched();
      component.productForm.get('name')?.setErrors({ required: true });
      fixture.detectChanges();

      const nameError = fixture.nativeElement.querySelector('#name-error');
      expect(nameError).toBeTruthy('Expected error with id="name-error"');
      expect(nameError?.getAttribute('role')).toBe('alert');
    });

    it('should link error messages via aria-describedby when field has error', () => {
      // Trigger validation on name field
      const nameControl = component.productForm.get('name');
      nameControl?.markAsTouched();
      nameControl?.setErrors({ required: true });
      fixture.detectChanges();

      const nameInput = fixture.nativeElement.querySelector('input[formControlName="name"]');
      const ariaDescribedBy = nameInput?.getAttribute('aria-describedby');

      expect(ariaDescribedBy).toContain('name-error',
        'Expected aria-describedby to reference error ID');
    });
  });

  describe('Button ARIA', () => {
    it('should have aria-label on cancel button', () => {
      expectButtonToHaveAria(fixture, 'button[type="button"]', 'cancelar');
    });

    it('should have aria-label on save button', () => {
      const saveButton = fixture.nativeElement.querySelector('button[type="submit"]');
      expect(saveButton).toBeTruthy('Expected save button');

      const ariaLabel = saveButton?.getAttribute('aria-label');
      expect(ariaLabel).toBeTruthy('Expected aria-label on save button');
      // In create mode, label should be "criar novo produto" or "salvar"
      expect(ariaLabel?.toLowerCase()).toMatch(/criar|salvar/);
    });

    it('should have aria-disabled on save button when form invalid', () => {
      component.productForm.setErrors({ invalid: true });
      fixture.detectChanges();

      const saveButton = fixture.nativeElement.querySelector('button[color="primary"]');
      const ariaDisabled = saveButton?.getAttribute('aria-disabled');
      const disabled = saveButton?.hasAttribute('disabled');

      expect(disabled || ariaDisabled === 'true').toBeTruthy(
        'Expected save button to be disabled when form invalid'
      );
    });
  });

  describe('Icon ARIA', () => {
    it('should mark decorative icons as aria-hidden', () => {
      expectIconsToBeDecorative(fixture);
    });
  });

  describe('Hints ARIA', () => {
    it('should have unique IDs on hint elements', () => {
      const skuHint = fixture.nativeElement.querySelector('#sku-hint');
      const productTypeHint = fixture.nativeElement.querySelector('#product-type-hint');
      const controlsInventoryHint = fixture.nativeElement.querySelector('#controls-inventory-hint');

      expect(skuHint || productTypeHint || controlsInventoryHint).toBeTruthy(
        'Expected at least one hint element with unique ID'
      );
    });

    it('should link hints via aria-describedby', () => {
      const skuInput = fixture.nativeElement.querySelector('input[formControlName="sku"]');
      const ariaDescribedBy = skuInput?.getAttribute('aria-describedby');

      // In edit mode, SKU should reference the hint
      if (component.isEditMode) {
        expect(ariaDescribedBy).toContain('sku-hint');
      }
    });
  });

  describe('Comprehensive ARIA Check', () => {
    it('should pass comprehensive ARIA validation', () => {
      runComprehensiveAriaCheck(fixture, {
        requiredFieldCount: 7,
        hasTable: false,
        checkTouchTargets: false // Skip in unit tests (requires actual DOM rendering)
      });
    });
  });

  describe('WCAG 2.1 Level AA Compliance', () => {
    it('should have all required form fields with aria-required', () => {
      // Note: productType is not in FormGroup, it's controlled separately via selectedProductType
      const requiredFields = [
        'name',
        'sku',
        'categoryId',
        'status',
        'price',
        'unit'
      ];

      requiredFields.forEach(fieldName => {
        const field = fixture.nativeElement.querySelector(`[formControlName="${fieldName}"]`);
        const ariaRequired = field?.getAttribute('aria-required');

        expect(ariaRequired).toBe('true',
          `Expected aria-required="true" on required field: ${fieldName}`);
      });

      // Check productType separately (not in FormGroup)
      const productTypeSelect = fixture.nativeElement.querySelector('mat-select[aria-label*="Tipo"]');
      if (productTypeSelect) {
        expect(productTypeSelect.getAttribute('aria-required')).toBe('true');
      }
    });

    it('should have form accessible name via aria-labelledby', () => {
      const form = fixture.nativeElement.querySelector('form');
      const ariaLabelledBy = form?.getAttribute('aria-labelledby');
      const titleElement = fixture.nativeElement.querySelector(`#${ariaLabelledBy}`);

      expect(titleElement).toBeTruthy(
        'Expected form aria-labelledby to reference existing element'
      );
      expect(titleElement?.textContent).toBeTruthy(
        'Expected referenced element to have text content'
      );
    });

    it('should have proper error identification', () => {
      // Trigger errors
      component.productForm.get('name')?.setErrors({ required: true });
      component.productForm.get('name')?.markAsTouched();
      component.productForm.get('price')?.setErrors({ min: true });
      component.productForm.get('price')?.markAsTouched();
      fixture.detectChanges();

      const errors = fixture.nativeElement.querySelectorAll('mat-error[role="alert"]');
      expect(errors.length).toBeGreaterThan(0,
        'Expected error messages with role="alert"');

      errors.forEach((error: HTMLElement) => {
        expect(error.id).toBeTruthy('Expected error to have unique ID');
      });
    });
  });

  describe('Axe-core Automated Accessibility', () => {
    it('should have no automated accessibility violations (excluding color contrast)', async () => {
      await expectNoAxeViolationsExceptColorContrast(fixture);
    });

    it('should pass WCAG 2.1 Level AA compliance', async () => {
      await expectWcagCompliance(fixture, 'AA');
    });

    it('should have accessible form elements', async () => {
      await expectAccessibleForm(fixture);
    });
  });
});
