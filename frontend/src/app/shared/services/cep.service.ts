import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError, debounceTime } from 'rxjs/operators';
import { ViaCepResponse, AddressFromCep } from '../models/supplier.model';

/**
 * CepService - Integration with ViaCEP API for address lookup
 * Story 3.1: Supplier Management - AC6
 */
@Injectable({
  providedIn: 'root'
})
export class CepService {
  private http = inject(HttpClient);
  private readonly viaCepUrl = 'https://viacep.com.br/ws';

  /**
   * Search address by CEP (Brazilian postal code)
   * @param cep - CEP in format 00000-000 or 00000000
   * @returns Observable with address data
   */
  searchCep(cep: string): Observable<AddressFromCep> {
    // Clean CEP (remove any non-digit characters)
    const cepClean = this.cleanCep(cep);

    // Validate CEP length
    if (cepClean.length !== 8) {
      return throwError(() => new Error('CEP inválido. Deve conter 8 dígitos.'));
    }

    // Call ViaCEP API
    return this.http.get<ViaCepResponse>(`${this.viaCepUrl}/${cepClean}/json/`)
      .pipe(
        map(response => {
          // Check if CEP was found
          if (response.erro) {
            throw new Error('CEP não encontrado.');
          }

          // Map to our address interface
          return {
            street: response.logradouro || '',
            neighborhood: response.bairro || '',
            city: response.localidade || '',
            state: response.uf || ''
          };
        }),
        catchError(error => {
          console.error('Error fetching CEP:', error);
          if (error.message === 'CEP não encontrado.') {
            return throwError(() => error);
          }
          return throwError(() => new Error('Erro ao buscar CEP. Verifique sua conexão.'));
        })
      );
  }

  /**
   * Clean CEP removing formatting
   */
  cleanCep(cep: string): string {
    if (!cep) {
      return '';
    }
    return cep.replace(/\D/g, '');
  }

  /**
   * Format CEP to standard format 00000-000
   */
  formatCep(cep: string): string {
    const cepClean = this.cleanCep(cep);

    if (cepClean.length !== 8) {
      return cep; // Return original if invalid
    }

    return `${cepClean.substring(0, 5)}-${cepClean.substring(5)}`;
  }

  /**
   * Validate CEP format
   */
  isValidCep(cep: string): boolean {
    const cepClean = this.cleanCep(cep);
    return cepClean.length === 8 && /^\d{8}$/.test(cepClean);
  }
}
