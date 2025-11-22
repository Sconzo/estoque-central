import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  StockTransferResponse,
  CreateStockTransferRequest,
  StockTransferFilters
} from '../../../shared/models/stock.model';

/**
 * StockTransferService - HTTP service for stock transfers
 * Story 2.9: Stock Transfer Between Locations
 */
@Injectable({
  providedIn: 'root'
})
export class StockTransferService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/stock-transfers`;

  /**
   * Create a stock transfer
   * POST /api/stock-transfers
   */
  createTransfer(request: CreateStockTransferRequest): Observable<StockTransferResponse> {
    return this.http.post<StockTransferResponse>(this.apiUrl, request);
  }

  /**
   * Get transfer history with filters
   * GET /api/stock-transfers?productId=xxx&originLocationId=xxx...
   */
  getTransferHistory(filters?: StockTransferFilters): Observable<StockTransferResponse[]> {
    let params = new HttpParams();

    if (filters) {
      if (filters.productId) params = params.set('productId', filters.productId);
      if (filters.variantId) params = params.set('variantId', filters.variantId);
      if (filters.originLocationId) params = params.set('originLocationId', filters.originLocationId);
      if (filters.destinationLocationId) params = params.set('destinationLocationId', filters.destinationLocationId);
      if (filters.startDate) params = params.set('startDate', filters.startDate);
      if (filters.endDate) params = params.set('endDate', filters.endDate);
      if (filters.userId) params = params.set('userId', filters.userId);
    }

    return this.http.get<StockTransferResponse[]>(this.apiUrl, { params });
  }

  /**
   * Get transfers for a specific product
   * GET /api/stock-transfers/product/{productId}
   */
  getTransfersForProduct(productId: string): Observable<StockTransferResponse[]> {
    return this.http.get<StockTransferResponse[]>(`${this.apiUrl}/product/${productId}`);
  }

  /**
   * Get transfers FROM a location
   * GET /api/stock-transfers/from/{locationId}
   */
  getTransfersFromLocation(locationId: string): Observable<StockTransferResponse[]> {
    return this.http.get<StockTransferResponse[]>(`${this.apiUrl}/from/${locationId}`);
  }

  /**
   * Get transfers TO a location
   * GET /api/stock-transfers/to/{locationId}
   */
  getTransfersToLocation(locationId: string): Observable<StockTransferResponse[]> {
    return this.http.get<StockTransferResponse[]>(`${this.apiUrl}/to/${locationId}`);
  }
}
