import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * CPF Validator - Validates Brazilian CPF with check digits
 * Story 3.1: Supplier Management
 */
export function cpfValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const cpf = control.value;

    if (!cpf) {
      return null; // Don't validate empty values (use Validators.required for that)
    }

    // Remove formatting
    const cpfClean = cpf.replace(/\D/g, '');

    // Check length
    if (cpfClean.length !== 11) {
      return { cpf: { message: 'CPF deve ter 11 dígitos' } };
    }

    // Check if all digits are the same
    if (/^(\d)\1{10}$/.test(cpfClean)) {
      return { cpf: { message: 'CPF inválido' } };
    }

    // Validate check digits
    if (!isValidCpf(cpfClean)) {
      return { cpf: { message: 'CPF inválido' } };
    }

    return null;
  };
}

function isValidCpf(cpf: string): boolean {
  try {
    // First check digit
    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += parseInt(cpf.charAt(i)) * (10 - i);
    }
    let digit1 = 11 - (sum % 11);
    digit1 = (digit1 >= 10) ? 0 : digit1;

    if (parseInt(cpf.charAt(9)) !== digit1) {
      return false;
    }

    // Second check digit
    sum = 0;
    for (let i = 0; i < 10; i++) {
      sum += parseInt(cpf.charAt(i)) * (11 - i);
    }
    let digit2 = 11 - (sum % 11);
    digit2 = (digit2 >= 10) ? 0 : digit2;

    return parseInt(cpf.charAt(10)) === digit2;

  } catch (e) {
    return false;
  }
}

/**
 * Format CPF to standard format: 000.000.000-00
 */
export function formatCpf(cpf: string): string {
  if (!cpf) {
    return '';
  }

  const cpfClean = cpf.replace(/\D/g, '');

  if (cpfClean.length !== 11) {
    return cpf;
  }

  return `${cpfClean.substring(0, 3)}.${cpfClean.substring(3, 6)}.${cpfClean.substring(6, 9)}-${cpfClean.substring(9)}`;
}

/**
 * Clean CPF formatting
 */
export function cleanCpf(cpf: string): string {
  return cpf ? cpf.replace(/\D/g, '') : '';
}
