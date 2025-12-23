import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from '../services/tenant.service';

/**
 * Tenant Interceptor - Adds X-Tenant-ID header to all HTTP requests
 * Story 7.6: Angular Material Frontend Base (AC5)
 *
 * <p>This interceptor automatically includes the current tenant/company ID
 * in the X-Tenant-ID header for all outgoing HTTP requests.
 *
 * <p>The backend uses this header to route database queries to the correct
 * tenant schema via {@link com.estoquecentral.shared.tenant.TenantInterceptor}.
 *
 * <p><strong>Multi-Tenant Flow:</strong>
 * <ol>
 *   <li>User logs in and selects a company</li>
 *   <li>TenantService stores company ID in localStorage</li>
 *   <li>This interceptor reads company ID from TenantService</li>
 *   <li>X-Tenant-ID header is added to all HTTP requests</li>
 *   <li>Backend TenantInterceptor routes to correct schema</li>
 * </ol>
 *
 * @see TenantService
 * @see com.estoquecentral.shared.tenant.TenantInterceptor (backend)
 * @see com.estoquecentral.shared.tenant.TenantContext (backend)
 */
@Injectable()
export class TenantInterceptor implements HttpInterceptor {

  constructor(private tenantService: TenantService) {}

  /**
   * Intercepts HTTP requests and adds X-Tenant-ID header.
   *
   * <p>The header is only added if:
   * <ul>
   *   <li>A tenant/company is currently selected</li>
   *   <li>The request is not to a public/tenant-agnostic endpoint</li>
   * </ul>
   *
   * @param request The outgoing HTTP request
   * @param next The HTTP handler
   * @returns Observable of HTTP event
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const companyId = this.tenantService.getCurrentCompanyId();

    // Skip adding tenant header for public endpoints
    const isPublicEndpoint = this.isPublicUrl(request.url);

    // DEBUG: Log interceptor execution
    console.log('🏢 Tenant Interceptor called for:', request.url);
    console.log('   - Current company ID:', companyId);
    console.log('   - Is public endpoint:', isPublicEndpoint);

    if (companyId !== null && !isPublicEndpoint) {
      // Clone request and add X-Tenant-ID header
      request = request.clone({
        setHeaders: {
          'X-Tenant-ID': companyId.toString()
        }
      });
      console.log('   ✅ X-Tenant-ID header added:', companyId);
    } else {
      console.log('   ❌ X-Tenant-ID header NOT added - company:', companyId, 'public:', isPublicEndpoint);
    }

    return next.handle(request);
  }

  /**
   * Checks if URL is a public endpoint (no tenant context required).
   *
   * <p>Public endpoints:
   * <ul>
   *   <li>/api/auth/* - Authentication endpoints</li>
   *   <li>/api/tenants - Tenant creation (public schema)</li>
   *   <li>/api/companies/my-companies - List user's companies (uses JWT user ID)</li>
   *   <li>/actuator/* - Health check and monitoring</li>
   * </ul>
   *
   * @param url The request URL
   * @returns true if public endpoint, false otherwise
   */
  private isPublicUrl(url: string): boolean {
    const publicEndpoints = [
      '/api/auth/',
      '/api/tenants',
      '/api/companies/my-companies',
      '/actuator/'
    ];

    return publicEndpoints.some(endpoint => url.includes(endpoint));
  }
}
