import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * Service for managing tenant context and switching between companies.
 *
 * Supports Epic 9 (Multi-company Context) by maintaining the current
 * company/tenant context throughout the application.
 *
 * @since 1.0
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService {
  private readonly STORAGE_KEY = 'current_company_id';
  private readonly apiUrl = `${environment.apiUrl}/api/companies`;

  // Signal for reactive current company ID
  currentCompanyId = signal<number | null>(this.loadCurrentCompanyId());

  constructor(private http: HttpClient) {}

  /**
   * Switches the current company context (Epic 9).
   * Stores the selection in localStorage for persistence across sessions.
   */
  switchCompany(companyId: number): void {
    this.currentCompanyId.set(companyId);
    localStorage.setItem(this.STORAGE_KEY, companyId.toString());
  }

  /**
   * Gets the current company ID from memory.
   */
  getCurrentCompanyId(): number | null {
    return this.currentCompanyId();
  }

  /**
   * Clears the current company context (logout).
   */
  clearContext(): void {
    this.currentCompanyId.set(null);
    localStorage.removeItem(this.STORAGE_KEY);
  }

  /**
   * Loads the current company ID from localStorage on service initialization.
   */
  private loadCurrentCompanyId(): number | null {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    return stored ? parseInt(stored, 10) : null;
  }

  /**
   * Fetches all companies accessible by the current user (Epic 9).
   */
  getUserCompanies(): Observable<Company[]> {
    return this.http.get<Company[]>(`${this.apiUrl}/my-companies`);
  }

  /**
   * Auto-detects and sets the company context if user has only one company.
   * Returns true if context was auto-set, false otherwise.
   */
  autoDetectCompany(): Observable<boolean> {
    return this.getUserCompanies().pipe(
      tap(companies => {
        if (companies.length === 1 && !this.currentCompanyId()) {
          this.switchCompany(companies[0].id);
        }
      }),
      tap(companies => companies.length === 1)
    );
  }
}

/**
 * Company interface matching backend CompanyDTO.
 */
export interface Company {
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
