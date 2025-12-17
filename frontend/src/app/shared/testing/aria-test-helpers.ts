/**
 * ARIA Test Helpers
 * Utilities for testing WCAG 2.1 Level AA compliance in Angular components
 */

import { DebugElement } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

/**
 * Verifies that a form field has proper ARIA attributes
 */
export function expectFormFieldToHaveAria(
  fixture: ComponentFixture<any>,
  selector: string,
  options: {
    label?: string;
    required?: boolean;
    describedBy?: string;
  } = {}
): void {
  const element = fixture.debugElement.query(By.css(selector));

  if (!element) {
    fail(`Element with selector "${selector}" not found`);
    return;
  }

  const nativeElement = element.nativeElement;

  // Check for aria-label or aria-labelledby
  if (options.label) {
    const ariaLabel = nativeElement.getAttribute('aria-label');
    expect(ariaLabel).toBeTruthy(`Expected aria-label on ${selector}`);
    if (ariaLabel) {
      expect(ariaLabel.toLowerCase()).toContain(
        options.label.toLowerCase(),
        `Expected aria-label to contain "${options.label}"`
      );
    }
  }

  // Check for aria-required
  if (options.required) {
    const ariaRequired = nativeElement.getAttribute('aria-required');
    expect(ariaRequired).toBe('true', `Expected aria-required="true" on ${selector}`);
  }

  // Check for aria-describedby
  if (options.describedBy) {
    const ariaDescribedBy = nativeElement.getAttribute('aria-describedby');
    expect(ariaDescribedBy).toBeTruthy(`Expected aria-describedby on ${selector}`);
  }
}

/**
 * Verifies that a button has proper ARIA attributes
 */
export function expectButtonToHaveAria(
  fixture: ComponentFixture<any>,
  selector: string,
  expectedLabel: string
): void {
  const button = fixture.debugElement.query(By.css(selector));

  if (!button) {
    fail(`Button with selector "${selector}" not found`);
    return;
  }

  const ariaLabel = button.nativeElement.getAttribute('aria-label');
  expect(ariaLabel).toBeTruthy(`Expected aria-label on button ${selector}`);

  if (ariaLabel && expectedLabel) {
    expect(ariaLabel.toLowerCase()).toContain(
      expectedLabel.toLowerCase(),
      `Expected button aria-label to contain "${expectedLabel}"`
    );
  }
}

/**
 * Verifies that error messages have role="alert"
 */
export function expectErrorMessagesToHaveAlertRole(
  fixture: ComponentFixture<any>,
  errorSelector: string = 'mat-error'
): void {
  const errors = fixture.debugElement.queryAll(By.css(errorSelector));

  errors.forEach((error, index) => {
    const role = error.nativeElement.getAttribute('role');
    expect(role).toBe('alert', `Expected role="alert" on error message ${index + 1}`);

    const id = error.nativeElement.getAttribute('id');
    expect(id).toBeTruthy(`Expected unique ID on error message ${index + 1}`);
  });
}

/**
 * Verifies that icons are marked as decorative
 */
export function expectIconsToBeDecorative(
  fixture: ComponentFixture<any>,
  iconSelector: string = 'mat-icon'
): void {
  const icons = fixture.debugElement.queryAll(By.css(iconSelector));

  icons.forEach((icon, index) => {
    const ariaHidden = icon.nativeElement.getAttribute('aria-hidden');
    // Only check icons that are purely decorative (not inside buttons without text)
    const parentButton = icon.nativeElement.closest('button');
    if (parentButton && parentButton.textContent.trim()) {
      expect(ariaHidden).toBe('true',
        `Expected aria-hidden="true" on decorative icon ${index + 1}`);
    }
  });
}

/**
 * Verifies that a table has proper ARIA label
 */
export function expectTableToHaveAria(
  fixture: ComponentFixture<any>,
  tableSelector: string = 'table[mat-table]',
  expectedLabel?: string
): void {
  const table = fixture.debugElement.query(By.css(tableSelector));

  if (!table) {
    fail(`Table with selector "${tableSelector}" not found`);
    return;
  }

  const ariaLabel = table.nativeElement.getAttribute('aria-label');
  expect(ariaLabel).toBeTruthy('Expected aria-label on table');

  if (ariaLabel && expectedLabel) {
    expect(ariaLabel.toLowerCase()).toContain(
      expectedLabel.toLowerCase(),
      `Expected table aria-label to contain "${expectedLabel}"`
    );
  }
}

/**
 * Verifies that required field indicators are present
 */
export function expectRequiredIndicators(
  fixture: ComponentFixture<any>,
  requiredFieldCount: number
): void {
  const requiredSpans = fixture.debugElement.queryAll(By.css('.required'));
  expect(requiredSpans.length).toBeGreaterThanOrEqual(
    requiredFieldCount,
    `Expected at least ${requiredFieldCount} required field indicators`
  );
}

/**
 * Verifies that buttons have minimum touch target size (48px)
 */
export function expectButtonsToHaveTouchTargets(
  fixture: ComponentFixture<any>
): void {
  const buttons = fixture.debugElement.queryAll(By.css('button'));

  buttons.forEach((button, index) => {
    const styles = window.getComputedStyle(button.nativeElement);
    const minHeight = parseInt(styles.minHeight, 10);

    expect(minHeight).toBeGreaterThanOrEqual(
      48,
      `Expected button ${index + 1} to have min-height >= 48px for touch target`
    );
  });
}

/**
 * Verifies that a form has proper aria-labelledby or aria-label
 */
export function expectFormToHaveAria(
  fixture: ComponentFixture<any>,
  formSelector: string = 'form'
): void {
  const form = fixture.debugElement.query(By.css(formSelector));

  if (!form) {
    fail(`Form with selector "${formSelector}" not found`);
    return;
  }

  const ariaLabelledBy = form.nativeElement.getAttribute('aria-labelledby');
  const ariaLabel = form.nativeElement.getAttribute('aria-label');

  expect(ariaLabelledBy || ariaLabel).toBeTruthy(
    'Expected form to have aria-labelledby or aria-label'
  );
}

/**
 * Runs a comprehensive ARIA check on a component
 */
export function runComprehensiveAriaCheck(
  fixture: ComponentFixture<any>,
  options: {
    formSelector?: string | false;
    requiredFieldCount?: number;
    hasTable?: boolean;
    tableLabel?: string;
    checkTouchTargets?: boolean;
  } = {}
): void {
  fixture.detectChanges();

  // Check form ARIA
  if (options.formSelector !== false) {
    expectFormToHaveAria(fixture, options.formSelector);
  }

  // Check required indicators
  if (options.requiredFieldCount) {
    expectRequiredIndicators(fixture, options.requiredFieldCount);
  }

  // Check error messages
  expectErrorMessagesToHaveAlertRole(fixture);

  // Check decorative icons
  expectIconsToBeDecorative(fixture);

  // Check table if present
  if (options.hasTable) {
    expectTableToHaveAria(fixture, 'table[mat-table]', options.tableLabel);
  }

  // Check touch targets
  if (options.checkTouchTargets) {
    expectButtonsToHaveTouchTargets(fixture);
  }
}
