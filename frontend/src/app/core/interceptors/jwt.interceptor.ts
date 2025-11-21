import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * JWT Interceptor - Adds Authorization header with JWT token to all HTTP requests
 *
 * This interceptor automatically includes the JWT token in the Authorization
 * header for all outgoing HTTP requests (except public endpoints).
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

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

    if (token && !isPublicEndpoint) {
      // Clone request and add Authorization header
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
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
