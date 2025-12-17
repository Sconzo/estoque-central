import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';

/**
 * JWT Interceptor - Adds Authorization header with JWT token to all HTTP requests
 *
 * This interceptor automatically includes the JWT token in the Authorization
 * header for all outgoing HTTP requests (except public endpoints).
 *
 * Also handles 401/403 errors by redirecting to login page.
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * Intercepts HTTP requests and adds Authorization header.
   *
   * @param request The outgoing HTTP request
   * @param next The HTTP handler
   * @returns Observable of HTTP event
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();

    // Skip adding token for public endpoints
    const isPublicEndpoint = this.isPublicUrl(request.url);

    // DEBUG: Log interceptor execution
    console.log('🔍 JWT Interceptor called for:', request.url);
    console.log('   - Has token:', !!token);
    console.log('   - Is public endpoint:', isPublicEndpoint);
    console.log('   - Token value:', token ? `${token.substring(0, 20)}...` : 'null');

    if (token && !isPublicEndpoint) {
      // Clone request and add Authorization header
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('   ✅ Authorization header added');
    } else {
      console.log('   ❌ Authorization header NOT added - token:', !!token, 'public:', isPublicEndpoint);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized or 403 Forbidden
        if (error.status === 401 || error.status === 403) {
          console.error('❌ Authentication error:', error.status, error.message);
          console.error('   Request URL:', request.url);
          console.error('   Has token:', !!token);
          console.error('   Is public:', isPublicEndpoint);

          // Log user out and redirect to login
          this.authService.logout();
        }

        return throwError(() => error);
      })
    );
  }

  /**
   * Checks if URL is a public endpoint (no authentication required).
   *
   * @param url The request URL
   * @returns true if public endpoint, false otherwise
   */
  private isPublicUrl(url: string): boolean {
    const publicEndpoints = [
      '/api/auth/google/callback',
      '/api/auth/health',
      '/api/tenants',
      '/actuator/health'
    ];

    return publicEndpoints.some(endpoint => url.includes(endpoint));
  }
}
