import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/**
 * Enums and Interfaces for Sales
 * Story 4.3: NFCe Emission and Stock Decrease
 */

export enum PaymentMethod {
  DINHEIRO = 'DINHEIRO',
  DEBITO = 'DEBITO',
  CREDITO = 'CREDITO',
  PIX = 'PIX'
}

export enum NfceStatus {
  PENDING = 'PENDING',
  EMITTED = 'EMITTED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export interface SaleItemRequest {
  productId: string;
  variantId?: string;
  quantity: number;
  unitPrice: number;
}

export interface SaleRequest {
  customerId: string;
  stockLocationId: string;
  paymentMethod: PaymentMethod;
  paymentAmountReceived: number;
  items: SaleItemRequest[];
}

export interface SaleItemResponse {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface SaleResponse {
  id: string;
  saleNumber: string;
  customerId: string;
  totalAmount: number;
  changeAmount: number;
  nfceStatus: NfceStatus;
  nfceKey?: string;
  saleDate: string;
  items: SaleItemResponse[];
}

/**
 * Service for managing sales and NFCe emission
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Injectable({
  providedIn: 'root'
})
export class SaleService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Process sale with NFCe emission and stock decrease (AC1, AC2, AC3)
   */
  processSale(tenantId: string, request: SaleRequest): Observable<SaleResponse> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId,
      'Content-Type': 'application/json'
    });

    return this.http.post<SaleResponse>(
      `${this.apiUrl}/api/sales`,
      request,
      { headers }
    );
  }
}
