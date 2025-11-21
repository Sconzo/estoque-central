import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from './models/user.model';
import { LoginResponse } from './models/login-response.model';
import { GoogleCallbackRequest } from './models/google-callback-request.model';

/**
 * AuthService - Service for authentication and JWT token management
 *
 * Handles:
 * - Google OAuth 2.0 authentication flow
 * - JWT token storage in localStorage
 * - User authentication state
 * - Token validation
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'jwt_token';

  // BehaviorSubject to track authentication state
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Check token validity on service initialization
    this.checkTokenValidity();
  }

  /**
   * Authenticates user with Google OAuth ID token.
   *
   * @param idToken Google ID token
   * @param tenantId Tenant UUID
   * @returns Observable<string> JWT token
   */
  login(idToken: string, tenantId: string): Observable<LoginResponse> {
    const request: GoogleCallbackRequest = {
      idToken,
      tenantId
    };

    return this.http.post<LoginResponse>(`${this.API_URL}/google/callback`, request)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          this.isAuthenticatedSubject.next(true);
        })
      );
  }

  /**
   * Logs out the user by removing JWT token.
   */
  logout(): void {
    this.removeToken();
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  /**
   * Gets the stored JWT token.
   *
   * @returns JWT token or null if not found
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Stores JWT token in localStorage.
   *
   * @param token JWT token
   */
  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Removes JWT token from localStorage.
   */
  private removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  /**
   * Checks if user is authenticated (has valid token).
   *
   * @returns true if authenticated, false otherwise
   */
  isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  /**
   * Checks if token exists and is not expired.
   *
   * @returns true if token is valid, false otherwise
   */
  private hasValidToken(): boolean {
    const token = this.getToken();

    if (!token) {
      return false;
    }

    // Check if token is expired
    try {
      const payload = this.parseJwt(token);
      const expirationTime = payload.exp * 1000; // Convert to milliseconds
      const now = Date.now();

      return now < expirationTime;
    } catch (error) {
      console.error('Failed to parse JWT token', error);
      return false;
    }
  }

  /**
   * Parses JWT token payload.
   *
   * @param token JWT token
   * @returns Decoded payload
   */
  private parseJwt(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error('Invalid JWT token');
    }
  }

  /**
   * Checks token validity and updates authentication state.
   */
  private checkTokenValidity(): void {
    const isValid = this.hasValidToken();
    this.isAuthenticatedSubject.next(isValid);

    if (!isValid && this.getToken()) {
      // Token exists but is invalid/expired - remove it
      this.removeToken();
    }
  }

  /**
   * Gets current user information from backend.
   *
   * @returns Observable<User>
   */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me`);
  }

  /**
   * Extracts tenant ID from JWT token.
   *
   * @returns Tenant ID or null
   */
  getTenantIdFromToken(): string | null {
    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {
      const payload = this.parseJwt(token);
      return payload.tenantId || null;
    } catch (error) {
      return null;
    }
  }

  /**
   * Extracts user roles from JWT token.
   *
   * @returns Array of role strings
   */
  getRolesFromToken(): string[] {
    const token = this.getToken();

    if (!token) {
      return [];
    }

    try {
      const payload = this.parseJwt(token);
      return payload.roles || [];
    } catch (error) {
      return [];
    }
  }
}
