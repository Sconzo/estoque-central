import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Page } from '../../produtos/models/product.model';

/**
 * Interface for Sale Item Response
 * Story 4.4: NFCe Retry Queue Management
 */
export interface SaleItemResponse {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

/**
 * NFCe Status Enum
 * Story 4.4: NFCe Retry Queue Management
 */
export enum NfceStatus {
  PENDING = 'PENDING',
  EMITTED = 'EMITTED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

/**
 * Sale Response Interface
 * Story 4.4: NFCe Retry Queue Management
 */
export interface SaleResponse {
  id: string;
  saleNumber: string;
  customerId: string;
  customerName?: string;
  totalAmount: number;
  changeAmount: number;
  nfceStatus: NfceStatus;
  nfceKey?: string;
  saleDate: string;
  items: SaleItemResponse[];
  emissionAttempts?: number;
  lastErrorMessage?: string;
}

/**
 * Cancel Refund Request
 * Story 4.4: NFCe Retry Queue Management
 */
export interface CancelRefundRequest {
  justification: string;
}

/**
 * Service for managing sales retry and cancellation operations
 * Story 4.4: NFCe Retry Queue Management
 */
@Injectable({
  providedIn: 'root'
})
export class SaleManagementService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Get pending sales for NFCe emission (AC1)
   * Retrieves sales with PENDING or FAILED status
   *
   * @param tenantId - Tenant identifier
   * @param status - Optional filter by status (PENDING, FAILED, EMITTED)
   * @param page - Page number (default: 0)
   * @param size - Page size (default: 20)
   * @returns Observable<Page<SaleResponse>>
   */
  getPendingSales(
    tenantId: string,
    status?: string,
    page: number = 0,
    size: number = 20
  ): Observable<Page<SaleResponse>> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId
    });

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<Page<SaleResponse>>(
      `${this.apiUrl}/api/sales/pending-fiscal`,
      { headers, params }
    );
  }

  /**
   * Retry NFCe emission for a sale (AC2)
   * Triggers a new emission attempt for a PENDING or FAILED sale
   *
   * @param tenantId - Tenant identifier
   * @param saleId - Sale ID to retry
   * @returns Observable<SaleResponse>
   */
  retrySale(tenantId: string, saleId: string): Observable<SaleResponse> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId,
      'Content-Type': 'application/json'
    });

    return this.http.post<SaleResponse>(
      `${this.apiUrl}/api/sales/${saleId}/retry`,
      {},
      { headers }
    );
  }

  /**
   * Cancel sale with refund (AC3)
   * Cancels a sale, refunds stock, and updates status to CANCELLED
   *
   * @param tenantId - Tenant identifier
   * @param saleId - Sale ID to cancel
   * @param justification - Cancellation justification (min 10 chars)
   * @returns Observable<SaleResponse>
   */
  cancelSaleWithRefund(
    tenantId: string,
    saleId: string,
    justification: string
  ): Observable<SaleResponse> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId,
      'Content-Type': 'application/json'
    });

    const request: CancelRefundRequest = { justification };

    return this.http.post<SaleResponse>(
      `${this.apiUrl}/api/sales/${saleId}/cancel-with-refund`,
      request,
      { headers }
    );
  }
}
