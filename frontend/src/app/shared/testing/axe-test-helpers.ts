/**
 * Axe-core Accessibility Test Helpers
 * Automated WCAG 2.1 compliance testing using axe-core
 */

import { ComponentFixture } from '@angular/core/testing';
import { axe, toHaveNoViolations } from 'jasmine-axe';
import type { Result as AxeResult } from 'axe-core';

// Add custom matcher to Jasmine
beforeEach(() => {
  jasmine.addMatchers(toHaveNoViolations);
});

/**
 * Runs axe-core accessibility audit on a component
 *
 * @param fixture - Angular component fixture to test
 * @param options - Optional axe configuration
 * @returns Promise<AxeResults>
 *
 * @example
 * ```typescript
 * it('should have no accessibility violations', async () => {
 *   await expectNoAxeViolations(fixture);
 * });
 * ```
 */
export async function expectNoAxeViolations(
  fixture: ComponentFixture<any>,
  options?: {
    rules?: Record<string, { enabled: boolean }>;
    exclude?: string[];
    include?: string[];
  }
): Promise<void> {
  fixture.detectChanges();

  const axeConfig = {
    rules: {
      // Enable all WCAG 2.1 Level A and AA rules
      ...options?.rules
    }
  };

  const context = options?.include || options?.exclude
    ? {
        include: options.include ? [options.include] : undefined,
        exclude: options.exclude ? [options.exclude] : undefined
      }
    : fixture.nativeElement;

  const results = await axe(context, axeConfig);
  expect(results).toHaveNoViolations();
}

/**
 * Runs axe-core audit and returns results for custom assertions
 *
 * @param fixture - Angular component fixture to test
 * @param options - Optional axe configuration
 * @returns Promise with axe results
 *
 * @example
 * ```typescript
 * it('should check specific accessibility rules', async () => {
 *   const results = await runAxeAudit(fixture, {
 *     rules: { 'color-contrast': { enabled: false } }
 *   });
 *   expect(results.violations.length).toBe(0);
 * });
 * ```
 */
export async function runAxeAudit(
  fixture: ComponentFixture<any>,
  options?: {
    rules?: Record<string, { enabled: boolean }>;
    exclude?: string[];
    include?: string[];
  }
): Promise<any> {
  fixture.detectChanges();

  const axeConfig = {
    rules: options?.rules || {}
  };

  const context = options?.include || options?.exclude
    ? {
        include: options.include ? [options.include] : undefined,
        exclude: options.exclude ? [options.exclude] : undefined
      }
    : fixture.nativeElement;

  return await axe(context, axeConfig);
}

/**
 * Runs axe-core audit with specific WCAG level
 *
 * @param fixture - Angular component fixture to test
 * @param level - WCAG level to test ('A', 'AA', or 'AAA')
 * @returns Promise<void>
 *
 * @example
 * ```typescript
 * it('should pass WCAG 2.1 Level AA', async () => {
 *   await expectWcagCompliance(fixture, 'AA');
 * });
 * ```
 */
export async function expectWcagCompliance(
  fixture: ComponentFixture<any>,
  level: 'A' | 'AA' | 'AAA' = 'AA'
): Promise<void> {
  fixture.detectChanges();

  const tags = [];
  if (level === 'A' || level === 'AA' || level === 'AAA') {
    tags.push('wcag2a');
  }
  if (level === 'AA' || level === 'AAA') {
    tags.push('wcag2aa');
  }
  if (level === 'AAA') {
    tags.push('wcag2aaa');
  }
  tags.push('wcag21a');
  if (level === 'AA' || level === 'AAA') {
    tags.push('wcag21aa');
  }

  const results = await axe(fixture.nativeElement, {
    runOnly: {
      type: 'tag',
      values: tags
    }
  });

  expect(results).toHaveNoViolations();
}

/**
 * Runs axe-core audit for form elements specifically
 *
 * @param fixture - Angular component fixture to test
 * @returns Promise<void>
 *
 * @example
 * ```typescript
 * it('should have accessible form elements', async () => {
 *   await expectAccessibleForm(fixture);
 * });
 * ```
 */
export async function expectAccessibleForm(
  fixture: ComponentFixture<any>
): Promise<void> {
  fixture.detectChanges();

  const results = await axe(fixture.nativeElement, {
    runOnly: {
      type: 'tag',
      values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'best-practice']
    },
    rules: {
      // Focus on form-related rules
      'label': { enabled: true },
      'label-title-only': { enabled: true },
      'aria-required-attr': { enabled: true },
      'aria-valid-attr': { enabled: true },
      'aria-valid-attr-value': { enabled: true },
      'button-name': { enabled: true },
      'form-field-multiple-labels': { enabled: true },
      'input-button-name': { enabled: true },
      'input-image-alt': { enabled: true },
      'select-name': { enabled: true }
    }
  });

  expect(results).toHaveNoViolations();
}

/**
 * Runs axe-core audit excluding color contrast checks
 * Useful when testing components with dynamic themes or in unit tests
 *
 * @param fixture - Angular component fixture to test
 * @returns Promise<void>
 *
 * @example
 * ```typescript
 * it('should have no violations except color contrast', async () => {
 *   await expectNoAxeViolationsExceptColorContrast(fixture);
 * });
 * ```
 */
export async function expectNoAxeViolationsExceptColorContrast(
  fixture: ComponentFixture<any>
): Promise<void> {
  fixture.detectChanges();

  const results = await axe(fixture.nativeElement, {
    rules: {
      'color-contrast': { enabled: false }
    }
  });

  expect(results).toHaveNoViolations();
}
