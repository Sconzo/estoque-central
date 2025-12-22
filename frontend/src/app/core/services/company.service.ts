import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Service for company management operations.
 *
 * Supports Epic 8 (Self-service Company Creation) and
 * Epic 10 (Company Management and Collaborators).
 *
 * @since 1.0
 */
@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private readonly apiUrl = `${environment.apiUrl}/api/companies`;

  constructor(private http: HttpClient) {}

  /**
   * Creates a new company (Epic 8 - Self-service registration).
   * This is a public endpoint that doesn't require tenant context.
   */
  createCompany(request: CreateCompanyRequest): Observable<CompanyResponse> {
    return this.http.post<CompanyResponse>(`${this.apiUrl}/register`, request);
  }

  /**
   * Gets all companies for the current user (Epic 9).
   */
  getMyCompanies(): Observable<CompanyResponse[]> {
    return this.http.get<CompanyResponse[]>(`${this.apiUrl}/my-companies`);
  }

  /**
   * Updates company information (Epic 10).
   */
  updateCompany(companyId: number, request: UpdateCompanyRequest): Observable<CompanyResponse> {
    return this.http.put<CompanyResponse>(`${this.apiUrl}/${companyId}`, request);
  }

  /**
   * Deletes (deactivates) a company (Epic 10).
   */
  deleteCompany(companyId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${companyId}`);
  }

  /**
   * Checks if a CNPJ is already registered.
   */
  checkCnpjExists(cnpj: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check-cnpj/${cnpj}`);
  }
}

/**
 * Request interface for creating a company.
 */
export interface CreateCompanyRequest {
  name: string;
  cnpj: string;
  email: string;
  phone?: string;
}

/**
 * Request interface for updating a company.
 */
export interface UpdateCompanyRequest {
  name: string;
  email: string;
  phone?: string;
}

/**
 * Response interface matching backend CompanyDTO.
 */
export interface CompanyResponse {
  id: number;
  name: string;
  cnpj: string;
  email: string;
  phone: string;
  ownerUserId: number;
  createdAt: string;
  updatedAt: string;
  active: boolean;
}
