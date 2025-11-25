import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/**
 * MercadoLivreService - API client for Mercado Livre integration
 * Story 5.1: Mercado Livre OAuth2 Authentication
 */

export interface ConnectionStatusResponse {
  connected: boolean;
  status: string;
  userIdMarketplace?: string;
  lastSyncAt?: string;
  tokenExpiresAt?: string;
  errorMessage?: string;
}

export interface ListingPreview {
  listingId: string;
  title: string;
  price: number;
  quantity: number;
  thumbnail: string;
  alreadyImported: boolean;
  hasVariations: boolean;
}

export interface ImportListingsRequest {
  listingIds: string[];
}

export interface ImportListingsResponse {
  imported: number;
  skipped: number;
  errors: string[];
}

@Injectable({
  providedIn: 'root'
})
export class MercadoLivreService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/integrations/mercadolivre`;

  /**
   * Initialize OAuth2 flow - Get authorization URL
   * AC2: OAuth2 Flow - Initialize Authentication
   */
  initAuth(): Observable<{ authorization_url: string; message: string }> {
    return this.http.get<{ authorization_url: string; message: string }>(
      `${this.apiUrl}/auth/init`
    );
  }

  /**
   * Get current connection status
   */
  getStatus(): Observable<ConnectionStatusResponse> {
    return this.http.get<ConnectionStatusResponse>(`${this.apiUrl}/status`);
  }

  /**
   * Disconnect Mercado Livre integration
   * AC7: Disconnect and revoke tokens
   */
  disconnect(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/disconnect`, {});
  }

  /**
   * Test connection (fetch user info from ML)
   */
  testConnection(): Observable<string> {
    return this.http.get(`${this.apiUrl}/test`, { responseType: 'text' });
  }

  /**
   * Get listings preview for import
   * Story 5.2: Import Products from Mercado Livre - AC2
   */
  getListings(): Observable<ListingPreview[]> {
    return this.http.get<ListingPreview[]>(`${this.apiUrl}/listings`);
  }

  /**
   * Import selected listings
   * Story 5.2: Import Products from Mercado Livre - AC3
   */
  importListings(request: ImportListingsRequest): Observable<ImportListingsResponse> {
    return this.http.post<ImportListingsResponse>(`${this.apiUrl}/import-listings`, request);
  }
}
