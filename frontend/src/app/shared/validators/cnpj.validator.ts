import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * CNPJ Validator - Validates Brazilian CNPJ with check digits
 * Story 3.1: Supplier Management
 */
export function cnpjValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const cnpj = control.value;

    if (!cnpj) {
      return null; // Don't validate empty values (use Validators.required for that)
    }

    // Remove formatting
    const cnpjClean = cnpj.replace(/\D/g, '');

    // Check length
    if (cnpjClean.length !== 14) {
      return { cnpj: { message: 'CNPJ deve ter 14 dígitos' } };
    }

    // Check if all digits are the same
    if (/^(\d)\1{13}$/.test(cnpjClean)) {
      return { cnpj: { message: 'CNPJ inválido' } };
    }

    // Validate check digits
    if (!isValidCnpj(cnpjClean)) {
      return { cnpj: { message: 'CNPJ inválido' } };
    }

    return null;
  };
}

function isValidCnpj(cnpj: string): boolean {
  try {
    // First check digit
    const weights1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
    let sum = 0;
    for (let i = 0; i < 12; i++) {
      sum += parseInt(cnpj.charAt(i)) * weights1[i];
    }
    const digit1 = (sum % 11 < 2) ? 0 : 11 - (sum % 11);

    if (parseInt(cnpj.charAt(12)) !== digit1) {
      return false;
    }

    // Second check digit
    const weights2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
    sum = 0;
    for (let i = 0; i < 13; i++) {
      sum += parseInt(cnpj.charAt(i)) * weights2[i];
    }
    const digit2 = (sum % 11 < 2) ? 0 : 11 - (sum % 11);

    return parseInt(cnpj.charAt(13)) === digit2;

  } catch (e) {
    return false;
  }
}

/**
 * Format CNPJ to standard format: 00.000.000/0000-00
 */
export function formatCnpj(cnpj: string): string {
  if (!cnpj) {
    return '';
  }

  const cnpjClean = cnpj.replace(/\D/g, '');

  if (cnpjClean.length !== 14) {
    return cnpj;
  }

  return `${cnpjClean.substring(0, 2)}.${cnpjClean.substring(2, 5)}.${cnpjClean.substring(5, 8)}/${cnpjClean.substring(8, 12)}-${cnpjClean.substring(12)}`;
}

/**
 * Clean CNPJ formatting
 */
export function cleanCnpj(cnpj: string): string {
  return cnpj ? cnpj.replace(/\D/g, '') : '';
}
