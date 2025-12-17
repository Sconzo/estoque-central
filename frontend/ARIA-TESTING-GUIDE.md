# ARIA Accessibility Testing Guide

## Overview

This guide explains how to write unit tests for ARIA attributes in Angular components to ensure WCAG 2.1 Level AA compliance.

## Test Helpers

All ARIA test utilities are located in:
```
frontend/src/app/shared/testing/aria-test-helpers.ts
```

## Quick Start

### 1. Import Test Helpers

```typescript
import {
  expectFormFieldToHaveAria,
  expectButtonToHaveAria,
  expectErrorMessagesToHaveAlertRole,
  expectIconsToBeDecorative,
  expectRequiredIndicators,
  expectFormToHaveAria,
  expectTableToHaveAria,
  runComprehensiveAriaCheck
} from '../../../shared/testing/aria-test-helpers';
```

### 2. Basic Test Structure

```typescript
describe('MyFormComponent - ARIA Accessibility', () => {
  let component: MyFormComponent;
  let fixture: ComponentFixture<MyFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyFormComponent, ReactiveFormsModule, NoopAnimationsModule],
      providers: [/* your providers */]
    }).compileComponents();

    fixture = TestBed.createComponent(MyFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Add your ARIA tests here
});
```

## Available Test Helpers

### 1. `expectFormFieldToHaveAria()`

Tests input/select/textarea fields for proper ARIA attributes.

```typescript
it('should have aria-label on name input', () => {
  expectFormFieldToHaveAria(fixture, 'input[formControlName="name"]', {
    label: 'nome do produto',  // Checks aria-label contains this text
    required: true,             // Checks aria-required="true"
    describedBy: 'name-hint'    // Optional: checks aria-describedby
  });
});
```

### 2. `expectButtonToHaveAria()`

Tests buttons for aria-label.

```typescript
it('should have aria-label on save button', () => {
  expectButtonToHaveAria(fixture, 'button[type="submit"]', 'salvar');
});
```

### 3. `expectErrorMessagesToHaveAlertRole()`

Tests that all error messages have `role="alert"` and unique IDs.

```typescript
it('should have role="alert" on error messages', () => {
  // Trigger validation first
  component.form.get('name')?.markAsTouched();
  component.form.get('name')?.setErrors({ required: true });
  fixture.detectChanges();

  expectErrorMessagesToHaveAlertRole(fixture);
});
```

### 4. `expectIconsToBeDecorative()`

Tests that decorative icons have `aria-hidden="true"`.

```typescript
it('should mark icons as decorative', () => {
  expectIconsToBeDecorative(fixture);
});
```

### 5. `expectRequiredIndicators()`

Tests that required fields have visual `<span class="required">*</span>` indicators.

```typescript
it('should have required field indicators', () => {
  expectRequiredIndicators(fixture, 5); // Expects at least 5 required indicators
});
```

### 6. `expectFormToHaveAria()`

Tests that the form has `aria-labelledby` or `aria-label`.

```typescript
it('should have aria-labelledby on form', () => {
  expectFormToHaveAria(fixture);
});
```

### 7. `expectTableToHaveAria()`

Tests that tables have `aria-label` for context.

```typescript
it('should have aria-label on table', () => {
  expectTableToHaveAria(fixture, 'table[mat-table]', 'pedidos de venda');
});
```

### 8. `runComprehensiveAriaCheck()`

Runs a complete ARIA validation suite.

```typescript
it('should pass comprehensive ARIA validation', () => {
  runComprehensiveAriaCheck(fixture, {
    requiredFieldCount: 5,      // Number of required fields
    hasTable: true,             // If component has a table
    tableLabel: 'pedidos',      // Expected table aria-label content
    checkTouchTargets: false    // Skip touch target check in unit tests
  });
});
```

## Test Suite Template

Here's a complete template for form ARIA tests:

```typescript
describe('MyFormComponent - ARIA Accessibility', () => {
  let component: MyFormComponent;
  let fixture: ComponentFixture<MyFormComponent>;

  beforeEach(async () => {
    // ... TestBed configuration
  });

  describe('Form-level ARIA', () => {
    it('should have aria-labelledby', () => {
      expectFormToHaveAria(fixture);
    });

    it('should have required indicators', () => {
      expectRequiredIndicators(fixture, 3); // Adjust count
    });
  });

  describe('Input Fields ARIA', () => {
    it('should have aria-label on each input', () => {
      expectFormFieldToHaveAria(fixture, 'input[formControlName="fieldName"]', {
        label: 'field description',
        required: true
      });
      // Repeat for each input
    });
  });

  describe('Select Fields ARIA', () => {
    it('should have aria-label on each select', () => {
      expectFormFieldToHaveAria(fixture, 'mat-select[formControlName="fieldName"]', {
        label: 'select description',
        required: true
      });
      // Repeat for each select
    });
  });

  describe('Error Messages ARIA', () => {
    it('should have role="alert" on errors', () => {
      // Trigger validation
      component.form.markAllAsTouched();
      fixture.detectChanges();

      expectErrorMessagesToHaveAlertRole(fixture);
    });

    it('should link errors via aria-describedby', () => {
      // Trigger error on specific field
      component.form.get('name')?.markAsTouched();
      component.form.get('name')?.setErrors({ required: true });
      fixture.detectChanges();

      const input = fixture.nativeElement.querySelector('input[formControlName="name"]');
      const ariaDescribedBy = input?.getAttribute('aria-describedby');
      expect(ariaDescribedBy).toContain('name-error');
    });
  });

  describe('Button ARIA', () => {
    it('should have aria-label on buttons', () => {
      expectButtonToHaveAria(fixture, 'button[type="submit"]', 'salvar');
    });

    it('should have aria-disabled when invalid', () => {
      component.form.setErrors({ invalid: true });
      fixture.detectChanges();

      const button = fixture.nativeElement.querySelector('button[type="submit"]');
      expect(button?.hasAttribute('disabled') ||
             button?.getAttribute('aria-disabled') === 'true').toBeTruthy();
    });
  });

  describe('Icon ARIA', () => {
    it('should mark icons as decorative', () => {
      expectIconsToBeDecorative(fixture);
    });
  });

  describe('Comprehensive Check', () => {
    it('should pass all ARIA validation', () => {
      runComprehensiveAriaCheck(fixture, {
        requiredFieldCount: 3,
        hasTable: false
      });
    });
  });
});
```

## Testing Patterns by Component Type

### Data Entry Forms (e.g., product-form, customer-form)

Test:
- ✅ Form `aria-labelledby`
- ✅ All inputs have `aria-label` and `aria-required`
- ✅ Error messages have `role="alert"` and unique IDs
- ✅ Error messages linked via `aria-describedby`
- ✅ Buttons have `aria-label`
- ✅ Icons have `aria-hidden="true"`
- ✅ Required field indicators (`.required`)
- ✅ Hints have unique IDs and linked via `aria-describedby`

### List/Filter Forms (e.g., sales-order-list, supplier-list)

Test:
- ✅ Form `aria-labelledby`
- ✅ Filter inputs have `aria-label`
- ✅ Table has `aria-label`
- ✅ Action buttons/menus have dynamic `aria-label`
- ✅ Loading states have `role="status"` and `aria-live="polite"`
- ✅ Empty states have `role="status"`
- ✅ Paginator has `aria-label`

### Master-Detail Forms (e.g., purchase-order-form, sales-order-form)

Test everything from Data Entry Forms, plus:
- ✅ FormArray items have dynamic `aria-label` based on index
- ✅ Add/Remove item buttons have `aria-label`
- ✅ Calculated totals have appropriate ARIA
- ✅ Table columns have proper header association

## Running Tests

```bash
# Run all tests
npm test

# Run tests for specific component
npm test -- --include='**/product-form.component.spec.ts'

# Run with coverage
npm test -- --code-coverage

# Run in watch mode
npm test -- --watch
```

## WCAG 2.1 Level AA Requirements Covered

These test helpers validate compliance with:

- **1.3.1 Info and Relationships**: Form structure and labels
- **2.4.6 Headings and Labels**: Descriptive labels via `aria-label`
- **3.3.1 Error Identification**: Errors with `role="alert"` and `aria-describedby`
- **3.3.2 Labels or Instructions**: All form controls labeled
- **4.1.2 Name, Role, Value**: Proper ARIA attributes on all interactive elements
- **4.1.3 Status Messages**: Loading/error states with `role="status"`

## Best Practices

1. **Always trigger validation before testing errors**
   ```typescript
   component.form.markAllAsTouched();
   fixture.detectChanges();
   ```

2. **Test both valid and invalid states**
   ```typescript
   it('should have aria-describedby only when error present', () => {
     // Test without error
     const input1 = fixture.nativeElement.querySelector('input');
     expect(input1?.getAttribute('aria-describedby')).toBeFalsy();

     // Test with error
     component.form.get('field')?.setErrors({ required: true });
     fixture.detectChanges();
     const input2 = fixture.nativeElement.querySelector('input');
     expect(input2?.getAttribute('aria-describedby')).toContain('error');
   });
   ```

3. **Test dynamic ARIA attributes**
   ```typescript
   it('should update aria-label based on loading state', () => {
     component.loading = true;
     fixture.detectChanges();
     const button = fixture.nativeElement.querySelector('button');
     expect(button?.getAttribute('aria-label')).toContain('carregando');
   });
   ```

4. **Use NoopAnimationsModule in tests**
   ```typescript
   imports: [NoopAnimationsModule]  // Prevents animation delays
   ```

## Next Steps

1. Copy the test template for each form component
2. Adjust the field names and counts based on your form
3. Run tests to verify ARIA compliance
4. Add to CI/CD pipeline to prevent regressions

## Example Components with ARIA Tests

- ✅ `product-form.component.spec.ts` - Complete example with all patterns
- 📝 `customer-form.component.spec.ts` - TODO
- 📝 `sales-order-form.component.spec.ts` - TODO
- 📝 `sales-order-list.component.spec.ts` - TODO

## References

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [ARIA Authoring Practices Guide](https://www.w3.org/WAI/ARIA/apg/)
- [Angular Testing Guide](https://angular.io/guide/testing)
