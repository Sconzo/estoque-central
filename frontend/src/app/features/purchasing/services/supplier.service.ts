import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  SupplierResponse,
  CreateSupplierRequest,
  UpdateSupplierRequest,
  SupplierFilters
} from '../../../shared/models/supplier.model';

/**
 * SupplierService - HTTP service for supplier management
 * Story 3.1: Supplier Management
 */
@Injectable({
  providedIn: 'root'
})
export class SupplierService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/suppliers`;

  /**
   * Create a new supplier
   * POST /api/suppliers
   */
  createSupplier(request: CreateSupplierRequest): Observable<SupplierResponse> {
    return this.http.post<SupplierResponse>(this.apiUrl, request);
  }

  /**
   * Update existing supplier
   * PUT /api/suppliers/{id}
   */
  updateSupplier(id: string, request: UpdateSupplierRequest): Observable<SupplierResponse> {
    return this.http.put<SupplierResponse>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Get supplier by ID
   * GET /api/suppliers/{id}
   */
  getSupplierById(id: string): Observable<SupplierResponse> {
    return this.http.get<SupplierResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all suppliers with pagination and filters
   * GET /api/suppliers?search=&status=&ativo=&page=&size=&sort=
   */
  getSuppliers(filters?: SupplierFilters): Observable<{ content: SupplierResponse[], totalElements: number, totalPages: number }> {
    let params = new HttpParams();

    if (filters) {
      if (filters.search) params = params.set('search', filters.search);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.ativo !== undefined) params = params.set('ativo', filters.ativo.toString());
      if (filters.page !== undefined) params = params.set('page', filters.page.toString());
      if (filters.size !== undefined) params = params.set('size', filters.size.toString());
      if (filters.sort) params = params.set('sort', filters.sort);
    }

    return this.http.get<any>(this.apiUrl, { params });
  }

  /**
   * Delete supplier (soft delete)
   * DELETE /api/suppliers/{id}
   */
  deleteSupplier(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activate supplier
   * POST /api/suppliers/{id}/activate
   */
  activateSupplier(id: string): Observable<SupplierResponse> {
    return this.http.post<SupplierResponse>(`${this.apiUrl}/${id}/activate`, {});
  }

  /**
   * Get preferred suppliers
   * GET /api/suppliers/preferred
   */
  getPreferredSuppliers(): Observable<SupplierResponse[]> {
    return this.http.get<SupplierResponse[]>(`${this.apiUrl}/preferred`);
  }
}
