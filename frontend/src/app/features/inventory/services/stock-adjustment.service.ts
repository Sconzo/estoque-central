import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/**
 * Interfaces for Stock Adjustment
 * Story 3.5: Stock Adjustment
 */
export enum AdjustmentType {
  INCREASE = 'INCREASE',
  DECREASE = 'DECREASE'
}

export enum AdjustmentReasonCode {
  INVENTORY = 'INVENTORY',
  LOSS = 'LOSS',
  DAMAGE = 'DAMAGE',
  THEFT = 'THEFT',
  ERROR = 'ERROR',
  OTHER = 'OTHER'
}

export interface StockAdjustmentRequest {
  productId: string;
  variantId?: string;
  stockLocationId: string;
  adjustmentType: AdjustmentType;
  quantity: number;
  reasonCode: AdjustmentReasonCode;
  reasonDescription: string;
  adjustmentDate?: string;
}

export interface StockAdjustmentResponse {
  id: string;
  adjustmentNumber: string;
  productId: string;
  productName: string;
  productSku: string;
  variantId?: string;
  variantName?: string;
  stockLocationId: string;
  stockLocationName: string;
  adjustmentType: AdjustmentType;
  quantity: number;
  reasonCode: AdjustmentReasonCode;
  reasonDescription: string;
  adjustedByUserId: string;
  adjustedByUserName?: string;
  adjustmentDate: string;
  balanceBefore: number;
  balanceAfter: number;
  createdAt: string;
}

export interface StockAdjustmentFilters {
  productId?: string;
  stockLocationId?: string;
  adjustmentType?: AdjustmentType;
  reasonCode?: AdjustmentReasonCode;
  adjustmentDateFrom?: string;
  adjustmentDateTo?: string;
  userId?: string;
  page?: number;
  size?: number;
}

/**
 * Service for managing stock adjustments
 * Story 3.5: Stock Adjustment (Ajuste de Estoque)
 */
@Injectable({
  providedIn: 'root'
})
export class StockAdjustmentService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Create stock adjustment (AC2)
   */
  createAdjustment(tenantId: string, request: StockAdjustmentRequest): Observable<StockAdjustmentResponse> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId,
      'Content-Type': 'application/json'
    });

    return this.http.post<StockAdjustmentResponse>(
      `${this.apiUrl}/api/stock/adjustments`,
      request,
      { headers }
    );
  }

  /**
   * Get adjustment history with filters (AC6)
   */
  getAdjustmentHistory(tenantId: string, filters: StockAdjustmentFilters = {}): Observable<any> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId
    });

    let params = new HttpParams();
    if (filters.productId) params = params.set('productId', filters.productId);
    if (filters.stockLocationId) params = params.set('stockLocationId', filters.stockLocationId);
    if (filters.adjustmentType) params = params.set('adjustmentType', filters.adjustmentType);
    if (filters.reasonCode) params = params.set('reasonCode', filters.reasonCode);
    if (filters.adjustmentDateFrom) params = params.set('adjustmentDateFrom', filters.adjustmentDateFrom);
    if (filters.adjustmentDateTo) params = params.set('adjustmentDateTo', filters.adjustmentDateTo);
    if (filters.userId) params = params.set('userId', filters.userId);
    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());

    return this.http.get<any>(
      `${this.apiUrl}/api/stock/adjustments`,
      { headers, params }
    );
  }

  /**
   * Get adjustment by ID (AC7)
   */
  getAdjustmentById(tenantId: string, adjustmentId: string): Observable<StockAdjustmentResponse> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId
    });

    return this.http.get<StockAdjustmentResponse>(
      `${this.apiUrl}/api/stock/adjustments/${adjustmentId}`,
      { headers }
    );
  }

  /**
   * Get reason code label for display
   */
  getReasonCodeLabel(code: AdjustmentReasonCode): string {
    const labels: Record<AdjustmentReasonCode, string> = {
      [AdjustmentReasonCode.INVENTORY]: 'Inventário',
      [AdjustmentReasonCode.LOSS]: 'Perda',
      [AdjustmentReasonCode.DAMAGE]: 'Dano',
      [AdjustmentReasonCode.THEFT]: 'Furto',
      [AdjustmentReasonCode.ERROR]: 'Erro de Lançamento',
      [AdjustmentReasonCode.OTHER]: 'Outros'
    };
    return labels[code] || code;
  }

  /**
   * Get adjustment type label for display
   */
  getAdjustmentTypeLabel(type: AdjustmentType): string {
    return type === AdjustmentType.INCREASE ? 'Entrada Manual' : 'Saída Manual';
  }

  /**
   * Get products with frequent adjustments (AC11)
   */
  getFrequentAdjustments(tenantId: string, daysBack: number = 30): Observable<any[]> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId
    });

    const params = new HttpParams().set('daysBack', daysBack.toString());

    return this.http.get<any[]>(
      `${this.apiUrl}/api/stock/adjustments/frequent-adjustments`,
      { headers, params }
    );
  }
}
