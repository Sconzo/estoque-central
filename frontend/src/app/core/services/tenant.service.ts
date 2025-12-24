import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * Service for managing tenant context and switching between companies.
 *
 * Supports Epic 9 (Multi-company Context) by maintaining the current
 * company/tenant context throughout the application.
 *
 * Story 9.5 - AC1: Angular Signals for reactive state management
 * Components can subscribe to currentTenant$ signal for real-time updates
 *
 * @since 1.0
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService {
  private readonly STORAGE_KEY = 'current_company_id';
  private readonly TENANT_STORAGE_KEY = 'currentTenantId';
  private readonly apiUrl = `${environment.apiUrl}/api/companies`;

  // Signal for reactive current company ID (legacy)
  currentCompanyId = signal<number | null>(this.loadCurrentCompanyId());

  // Story 9.5 - AC1: Signal for reactive current tenant (UUID)
  // Components subscribe to this signal for real-time context updates
  currentTenant$ = signal<string | null>(this.loadCurrentTenantId());

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
   * Sets the current tenant using tenantId (UUID string) from Story 8.2 - AC6.
   * Stores the tenantId in localStorage for persistence (Story 9.4 - AC1).
   * Story 9.5 - AC1: Updates Signal for reactive component updates
   *
   * @param tenantId - The tenant UUID to set as current
   */
  setCurrentTenant(tenantId: string): void {
    localStorage.setItem(this.TENANT_STORAGE_KEY, tenantId);
    // AC1: Update signal to trigger reactive updates in subscribed components
    this.currentTenant$.set(tenantId);
    console.log('🔄 Tenant context updated (Signal):', tenantId);
  }

  /**
   * Gets the current tenant ID from localStorage (Story 9.4 - AC1).
   * Used for persistence - user remains in current company context after page refresh.
   * Story 9.5 - AC1: Use currentTenant$ signal for reactive subscriptions
   *
   * @returns Tenant ID (UUID string) or null if not set
   */
  getCurrentTenant(): string | null {
    return localStorage.getItem(this.TENANT_STORAGE_KEY);
  }

  /**
   * Clears the current tenant context (Story 9.4 - logout support).
   * Story 9.5 - AC1: Clears Signal to notify subscribed components
   *
   * Removes tenantId from localStorage.
   */
  clearTenantContext(): void {
    localStorage.removeItem(this.TENANT_STORAGE_KEY);
    // AC1: Clear signal to trigger reactive updates
    this.currentTenant$.set(null);
    console.log('🔄 Tenant context cleared (Signal)');
  }

  /**
   * Checks if a tenant context is currently set (Story 9.4 - AC2).
   *
   * @returns true if tenant context exists, false otherwise
   */
  hasTenantContext(): boolean {
    return this.getCurrentTenant() !== null;
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
   * Loads the current tenant ID from localStorage on service initialization.
   * Story 9.5 - AC1: Initializes Signal with persisted value
   */
  private loadCurrentTenantId(): string | null {
    return localStorage.getItem(this.TENANT_STORAGE_KEY);
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
      map(companies => companies.length === 1)
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
