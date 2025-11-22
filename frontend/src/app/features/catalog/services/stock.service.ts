import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  StockResponse,
  StockByLocationResponse,
  SetMinimumQuantityRequest,
  BelowMinimumStockResponse
} from '../../../shared/models/stock.model';

/**
 * StockService - HTTP service for stock management
 * Story 2.7: Multi-Warehouse Stock Control
 */
@Injectable({
  providedIn: 'root'
})
export class StockService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/stock`;

  /**
   * AC2: Get all stock with optional filters
   */
  getAllStock(filters?: {
    productId?: string;
    variantId?: string;
    locationId?: string;
    belowMinimum?: boolean;
  }): Observable<StockResponse[]> {
    let params = new HttpParams();

    if (filters?.productId) {
      params = params.set('productId', filters.productId);
    }
    if (filters?.variantId) {
      params = params.set('variantId', filters.variantId);
    }
    if (filters?.locationId) {
      params = params.set('locationId', filters.locationId);
    }
    if (filters?.belowMinimum !== undefined) {
      params = params.set('belowMinimum', filters.belowMinimum.toString());
    }

    return this.http.get<StockResponse[]>(this.apiUrl, { params });
  }

  /**
   * AC2: Get stock by product (aggregated across all locations)
   */
  getStockByProduct(productId: string): Observable<StockByLocationResponse> {
    return this.http.get<StockByLocationResponse>(`${this.apiUrl}/product/${productId}`);
  }

  /**
   * AC2: Get stock by product with location drill-down
   */
  getStockByProductByLocation(productId: string): Observable<StockByLocationResponse> {
    return this.http.get<StockByLocationResponse>(`${this.apiUrl}/product/${productId}/by-location`);
  }

  /**
   * AC2: Get stock by variant (aggregated across all locations)
   */
  getStockByVariant(variantId: string): Observable<StockByLocationResponse> {
    return this.http.get<StockByLocationResponse>(`${this.apiUrl}/variant/${variantId}`);
  }

  /**
   * AC2: Get stock by variant with location drill-down
   */
  getStockByVariantByLocation(variantId: string): Observable<StockByLocationResponse> {
    return this.http.get<StockByLocationResponse>(`${this.apiUrl}/variant/${variantId}/by-location`);
  }

  /**
   * AC5: Set minimum quantity for product at a specific location
   */
  setMinimumQuantityForProduct(productId: string, request: SetMinimumQuantityRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/product/${productId}/minimum`, request);
  }

  /**
   * AC5: Set minimum quantity for variant at a specific location
   */
  setMinimumQuantityForVariant(variantId: string, request: SetMinimumQuantityRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/variant/${variantId}/minimum`, request);
  }

  /**
   * AC6: Get products below minimum stock (stock rupture alert)
   */
  getProductsBelowMinimum(locationId?: string): Observable<BelowMinimumStockResponse> {
    let params = new HttpParams();
    if (locationId) {
      params = params.set('locationId', locationId);
    }
    return this.http.get<BelowMinimumStockResponse>(`${this.apiUrl}/below-minimum`, { params });
  }
}
