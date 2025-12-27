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
  private readonly publicApiUrl = `${environment.apiUrl}/api/public/companies`;

  constructor(private http: HttpClient) {}

  /**
   * Creates a new company (Epic 8 - Story 8.2 - Self-service registration).
   * This is a public endpoint that doesn't require tenant context.
   *
   * AC4: POST /api/public/companies with userId from auth context
   * AC6: Returns JWT with tenantId (stored by component)
   *
   * @param request Company creation data including userId
   * @returns Observable<CreateCompanyResponse> with tenantId, schemaName, and JWT token
   */
  createCompany(request: CreateCompanyRequest): Observable<CreateCompanyResponse> {
    return this.http.post<CreateCompanyResponse>(this.publicApiUrl, request);
  }

  /**
   * Gets all companies linked to the current authenticated user (Story 8.4 - AC1, AC2).
   *
   * Uses the /api/users/me/companies endpoint which:
   * - Extracts userId from JWT token
   * - Returns companies with user's role in each company
   * - Returns empty array if user has no companies (AC3)
   *
   * @returns Observable<UserCompanyResponse[]> List of companies with role information
   */
  getUserCompanies(): Observable<UserCompanyResponse[]> {
    return this.http.get<UserCompanyResponse[]>(`${environment.apiUrl}/api/users/me/companies`);
  }

  /**
   * Switches user's company context (Story 9.1/9.2 - AC3).
   *
   * Sends PUT request to /api/users/me/context with target tenantId.
   * Returns new JWT token with updated context.
   *
   * @param tenantId Target company's tenant ID
   * @returns Observable<SwitchContextResponse> New JWT and company info
   */
  switchContext(tenantId: string): Observable<SwitchContextResponse> {
    return this.http.put<SwitchContextResponse>(
      `${environment.apiUrl}/api/users/me/context`,
      { tenantId }
    );
  }

  /**
   * Gets all companies for the current user (Epic 9).
   * @deprecated Use getUserCompanies() instead for Story 8.3+
   */
  getMyCompanies(): Observable<CompanyResponse[]> {
    return this.http.get<CompanyResponse[]>(`${this.apiUrl}/my-companies`);
  }

  /**
   * Gets current company information (Story 10.10 - AC1).
   * Uses JWT context to determine current company.
   *
   * @returns Observable<CompanyResponse> Current company data
   */
  getCurrentCompany(): Observable<CompanyResponse> {
    return this.http.get<CompanyResponse>(`${this.apiUrl}/current`);
  }

  /**
   * Updates current company information (Story 10.5 - AC1, AC2).
   * Uses JWT context to determine which company to update.
   * Requires ADMIN role.
   *
   * @param request Company update data
   * @returns Observable<CompanyResponse> Updated company data
   */
  updateCurrentCompany(request: UpdateCompanyRequest): Observable<CompanyResponse> {
    return this.http.put<CompanyResponse>(`${this.apiUrl}/current`, request);
  }

  /**
   * Deletes (deactivates) current company (Story 10.6 - AC1, AC2, AC3).
   * Uses JWT context to determine which company to delete.
   * Requires ADMIN role.
   * Performs soft delete with orphan user protection.
   *
   * @returns Observable<void>
   */
  deleteCurrentCompany(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/current`);
  }

  /**
   * Updates company information (Epic 10).
   * @deprecated Use updateCurrentCompany() for Story 10.5+
   */
  updateCompany(companyId: number, request: UpdateCompanyRequest): Observable<CompanyResponse> {
    return this.http.put<CompanyResponse>(`${this.apiUrl}/${companyId}`, request);
  }

  /**
   * Deletes (deactivates) a company (Epic 10).
   * @deprecated Use deleteCurrentCompany() for Story 10.6+
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
 * Request interface for creating a company (Story 8.2 - AC2, AC4).
 */
export interface CreateCompanyRequest {
  nome: string;
  cnpj?: string;
  email: string;
  telefone?: string;
  userId: number;
}

/**
 * Response interface for company creation (Story 8.2 - AC5, AC6).
 * Returns tenantId, schemaName, and JWT token.
 */
export interface CreateCompanyResponse {
  tenantId: string;
  nome: string;
  schemaName: string;
  token: string;
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

/**
 * Response interface for user's companies list (Story 8.4 - AC2).
 * Matches backend UserCompanyResponse DTO.
 */
export interface UserCompanyResponse {
  tenantId: string;
  nome: string;
  cnpj: string;
  profileId: string | null;
  profileName: string;
}

/**
 * Response interface for context switch (Story 9.1 - AC3).
 * Matches backend SwitchContextResponse DTO.
 */
export interface SwitchContextResponse {
  token: string;
  tenantId: string;
  companyName: string;
  roles: string[];
}
