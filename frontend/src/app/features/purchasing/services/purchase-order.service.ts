import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  PurchaseOrderResponse,
  PurchaseOrderPage,
  CreatePurchaseOrderRequest,
  UpdateStatusRequest,
  PurchaseOrderFilters
} from '../../../shared/models/purchase-order.model';

/**
 * PurchaseOrderService - Service for purchase order operations
 * Story 3.2: Purchase Order Creation
 */
@Injectable({
  providedIn: 'root'
})
export class PurchaseOrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/purchase-orders`;

  /**
   * Get purchase orders with pagination and filters
   */
  getPurchaseOrders(filters: PurchaseOrderFilters = {}): Observable<PurchaseOrderPage> {
    let params = new HttpParams();

    if (filters.supplier_id) {
      params = params.set('supplier_id', filters.supplier_id);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.order_date_from) {
      params = params.set('order_date_from', filters.order_date_from);
    }
    if (filters.order_date_to) {
      params = params.set('order_date_to', filters.order_date_to);
    }
    if (filters.order_number) {
      params = params.set('order_number', filters.order_number);
    }
    if (filters.page !== undefined) {
      params = params.set('page', filters.page.toString());
    }
    if (filters.size !== undefined) {
      params = params.set('size', filters.size.toString());
    }
    if (filters.sort) {
      params = params.set('sort', filters.sort);
    }

    return this.http.get<PurchaseOrderPage>(this.apiUrl, { params });
  }

  /**
   * Get purchase order by ID
   */
  getPurchaseOrderById(id: string): Observable<PurchaseOrderResponse> {
    return this.http.get<PurchaseOrderResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Create new purchase order
   */
  createPurchaseOrder(request: CreatePurchaseOrderRequest): Observable<PurchaseOrderResponse> {
    return this.http.post<PurchaseOrderResponse>(this.apiUrl, request);
  }

  /**
   * Update purchase order status
   */
  updateStatus(id: string, request: UpdateStatusRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/status`, request);
  }

  /**
   * Delete purchase order (only DRAFT)
   */
  deletePurchaseOrder(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
