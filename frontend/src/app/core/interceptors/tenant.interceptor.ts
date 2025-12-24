import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from '../services/tenant.service';

/**
 * Tenant Interceptor - Adds X-Tenant-ID header to all HTTP requests
 * Story 9.4: Frontend - Persistência de Contexto em Sessão (AC3)
 *
 * This interceptor automatically includes the current tenant/company ID
 * in the X-Tenant-ID header for all outgoing HTTP requests (ARCH22).
 *
 * The backend uses this header to route database queries to the correct
 * tenant schema via the backend TenantInterceptor.
 *
 * Multi-Tenant Flow:
 * 1. User logs in and selects a company
 * 2. TenantService stores tenantId (UUID) in localStorage (AC1)
 * 3. This interceptor reads tenantId from TenantService
 * 4. X-Tenant-ID header is added to all HTTP requests (AC3)
 * 5. Backend TenantInterceptor routes to correct schema
 *
 * @see TenantService
 * @since Epic 9 - Story 9.4
 */
@Injectable()
export class TenantInterceptor implements HttpInterceptor {

  constructor(private tenantService: TenantService) {}

  /**
   * Intercepts HTTP requests and adds X-Tenant-ID header (AC3).
   *
   * The header is only added if:
   * - A tenant/company is currently selected (stored in localStorage)
   * - The request is not to a public/tenant-agnostic endpoint
   *
   * @param request The outgoing HTTP request
   * @param next The HTTP handler
   * @returns Observable of HTTP event
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // AC3: Get current tenantId (UUID string) from localStorage via TenantService
    const tenantId = this.tenantService.getCurrentTenant();

    // Skip adding tenant header for public endpoints
    const isPublicEndpoint = this.isPublicUrl(request.url);

    // DEBUG: Log interceptor execution
    console.log('🏢 Tenant Interceptor called for:', request.url);
    console.log('   - Current tenant ID:', tenantId);
    console.log('   - Is public endpoint:', isPublicEndpoint);

    if (tenantId !== null && !isPublicEndpoint) {
      // AC3: Clone request and add X-Tenant-ID header with UUID (ARCH22)
      request = request.clone({
        setHeaders: {
          'X-Tenant-ID': tenantId
        }
      });
      console.log('   ✅ X-Tenant-ID header added:', tenantId);
    } else {
      console.log('   ❌ X-Tenant-ID header NOT added - tenant:', tenantId, 'public:', isPublicEndpoint);
    }

    return next.handle(request);
  }

  /**
   * Checks if URL is a public endpoint (no tenant context required).
   *
   * Public endpoints:
   * - /api/auth/* - Authentication endpoints
   * - /api/tenants - Tenant creation (public schema)
   * - /api/users/me/companies - List user's companies (uses JWT user ID)
   * - /api/users/me/context - Context switching (tenantId in body)
   * - /actuator/* - Health check and monitoring
   *
   * @param url The request URL
   * @returns true if public endpoint, false otherwise
   */
  private isPublicUrl(url: string): boolean {
    const publicEndpoints = [
      '/api/auth/',
      '/api/tenants',
      '/api/users/me/companies',
      '/api/users/me/context',
      '/actuator/'
    ];

    return publicEndpoints.some(endpoint => url.includes(endpoint));
  }
}
