import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  StockMovementResponse,
  CreateStockMovementRequest,
  StockMovementFilters
} from '../../../shared/models/stock.model';

/**
 * StockMovementService - HTTP service for stock movement history
 * Story 2.8: Stock Movement History
 *
 * Provides methods to:
 * - Create manual stock movements
 * - Query movement history with filters
 * - Get movement timeline for audit trail
 * - Validate balance consistency
 */
@Injectable({
  providedIn: 'root'
})
export class StockMovementService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/stock-movements`;

  // ============================================================
  // AC1: Create Stock Movement
  // ============================================================

  /**
   * Create a manual stock movement (ENTRY, EXIT, ADJUSTMENT, etc.)
   * POST /api/stock-movements
   */
  createMovement(request: CreateStockMovementRequest): Observable<StockMovementResponse> {
    return this.http.post<StockMovementResponse>(this.apiUrl, request);
  }

  // ============================================================
  // AC2: Query Movements with Filters
  // ============================================================

  /**
   * Get stock movements with flexible filtering
   * GET /api/stock-movements?productId=xxx&locationId=xxx&type=xxx
   */
  getMovements(filters?: StockMovementFilters): Observable<StockMovementResponse[]> {
    let params = new HttpParams();

    if (filters) {
      if (filters.productId) params = params.set('productId', filters.productId);
      if (filters.variantId) params = params.set('variantId', filters.variantId);
      if (filters.locationId) params = params.set('locationId', filters.locationId);
      if (filters.type) params = params.set('type', filters.type);
      if (filters.startDate) params = params.set('startDate', filters.startDate);
      if (filters.endDate) params = params.set('endDate', filters.endDate);
      if (filters.documentType) params = params.set('documentType', filters.documentType);
      if (filters.documentId) params = params.set('documentId', filters.documentId);
      if (filters.userId) params = params.set('userId', filters.userId);
      if (filters.page !== undefined) params = params.set('page', filters.page.toString());
      if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    }

    return this.http.get<StockMovementResponse[]>(this.apiUrl, { params });
  }

  // ============================================================
  // AC3: Movement Timeline (Audit Trail)
  // ============================================================

  /**
   * Get complete movement timeline for a product or variant
   * GET /api/stock-movements/timeline?productId=xxx&locationId=xxx
   */
  getMovementTimeline(params: {
    productId?: string;
    variantId?: string;
    locationId?: string;
  }): Observable<StockMovementResponse[]> {
    let httpParams = new HttpParams();

    if (params.productId) httpParams = httpParams.set('productId', params.productId);
    if (params.variantId) httpParams = httpParams.set('variantId', params.variantId);
    if (params.locationId) httpParams = httpParams.set('locationId', params.locationId);

    return this.http.get<StockMovementResponse[]>(`${this.apiUrl}/timeline`, { params: httpParams });
  }

  // ============================================================
  // AC4: Validate Balance Consistency
  // ============================================================

  /**
   * Validate that movement history balance matches current inventory
   * GET /api/stock-movements/validate-balance?productId=xxx&locationId=xxx
   *
   * Returns 200 OK if valid, 409 CONFLICT if inconsistent
   */
  validateBalance(params: {
    productId?: string;
    variantId?: string;
    locationId: string;
  }): Observable<void> {
    let httpParams = new HttpParams();
    httpParams = httpParams.set('locationId', params.locationId);

    if (params.productId) httpParams = httpParams.set('productId', params.productId);
    if (params.variantId) httpParams = httpParams.set('variantId', params.variantId);

    return this.http.get<void>(`${this.apiUrl}/validate-balance`, { params: httpParams });
  }

  // ============================================================
  // Convenience Methods
  // ============================================================

  /**
   * Get recent movements (last 50 by default)
   * GET /api/stock-movements/recent?limit=50
   */
  getRecentMovements(limit: number = 50): Observable<StockMovementResponse[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<StockMovementResponse[]>(`${this.apiUrl}/recent`, { params });
  }

  /**
   * Get all movements linked to a specific document (e.g., sale, purchase)
   * GET /api/stock-movements/by-document?documentType=SALE&documentId=xxx
   */
  getMovementsByDocument(documentType: string, documentId: string): Observable<StockMovementResponse[]> {
    const params = new HttpParams()
      .set('documentType', documentType)
      .set('documentId', documentId);

    return this.http.get<StockMovementResponse[]>(`${this.apiUrl}/by-document`, { params });
  }

  /**
   * Get movements for a product at a specific location
   * Convenience method combining filters
   */
  getProductMovements(productId: string, locationId?: string): Observable<StockMovementResponse[]> {
    const filters: StockMovementFilters = {
      productId,
      locationId
    };
    return this.getMovements(filters);
  }

  /**
   * Get movements for a variant at a specific location
   */
  getVariantMovements(variantId: string, locationId?: string): Observable<StockMovementResponse[]> {
    const filters: StockMovementFilters = {
      variantId,
      locationId
    };
    return this.getMovements(filters);
  }

  /**
   * Get movements by date range
   */
  getMovementsByDateRange(startDate: string, endDate: string): Observable<StockMovementResponse[]> {
    const filters: StockMovementFilters = {
      startDate,
      endDate
    };
    return this.getMovements(filters);
  }
}
