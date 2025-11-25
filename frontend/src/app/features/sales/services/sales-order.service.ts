import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/**
 * Sales Order Service - API client for B2B sales orders
 * Story 4.5: Sales Order B2B Interface
 */
@Injectable({
  providedIn: 'root'
})
export class SalesOrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/sales-orders`;

  /**
   * Create a new sales order
   */
  createSalesOrder(request: SalesOrderRequest): Observable<SalesOrderResponse> {
    return this.http.post<SalesOrderResponse>(this.apiUrl, request);
  }

  /**
   * Get sales order by ID
   */
  getSalesOrderById(id: string): Observable<SalesOrderResponse> {
    return this.http.get<SalesOrderResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Search/list sales orders with filters and pagination
   */
  getSalesOrders(filters?: SalesOrderFilters): Observable<PagedSalesOrderResponse> {
    let params = new HttpParams();

    if (filters) {
      if (filters.customer_id) params = params.set('customer_id', filters.customer_id);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.order_date_from) params = params.set('order_date_from', filters.order_date_from);
      if (filters.order_date_to) params = params.set('order_date_to', filters.order_date_to);
      if (filters.order_number) params = params.set('order_number', filters.order_number);
      if (filters.page !== undefined) params = params.set('page', filters.page.toString());
      if (filters.size !== undefined) params = params.set('size', filters.size.toString());
      if (filters.sort) params = params.set('sort', filters.sort.join(','));
    }

    return this.http.get<PagedSalesOrderResponse>(this.apiUrl, { params });
  }

  /**
   * Update a sales order (only DRAFT)
   */
  updateSalesOrder(id: string, request: SalesOrderRequest): Observable<SalesOrderResponse> {
    return this.http.put<SalesOrderResponse>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Confirm sales order (DRAFT → CONFIRMED)
   */
  confirmSalesOrder(id: string): Observable<SalesOrderResponse> {
    return this.http.put<SalesOrderResponse>(`${this.apiUrl}/${id}/confirm`, {});
  }

  /**
   * Cancel sales order
   */
  cancelSalesOrder(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get stock availability for a product at a location
   */
  getStockAvailability(
    productId: string | null,
    variantId: string | null,
    locationId: string
  ): Observable<StockAvailability> {
    let params = new HttpParams().set('locationId', locationId);

    if (productId) {
      params = params.set('productId', productId);
    }
    if (variantId) {
      params = params.set('variantId', variantId);
    }

    return this.http.get<StockAvailability>(`${this.apiUrl}/stock/availability`, { params });
  }
}

// Interfaces

export interface SalesOrderRequest {
  customerId: string;
  stockLocationId: string;
  orderDate?: string; // yyyy-MM-dd
  deliveryDateExpected?: string; // yyyy-MM-dd
  paymentTerms?: PaymentTerms;
  notes?: string;
  items: SalesOrderItemRequest[];
}

export interface SalesOrderItemRequest {
  productId?: string | null;
  variantId?: string | null;
  quantity: number;
  unitPrice: number;
}

export interface SalesOrderResponse {
  id: string;
  orderNumber: string;
  status: SalesOrderStatus;
  orderDate: string;
  deliveryDateExpected?: string;
  paymentTerms?: PaymentTerms;
  totalAmount: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  customer?: CustomerSummary;
  location?: LocationSummary;
  items?: SalesOrderItemResponse[];
}

export interface SalesOrderItemResponse {
  id: string;
  productId?: string;
  variantId?: string;
  productName?: string;
  productSku?: string;
  quantityOrdered: number;
  quantityReserved: number;
  unitPrice: number;
  totalPrice: number;
  stockInfo?: StockInfo;
}

export interface CustomerSummary {
  id: string;
  name: string;
  documentNumber?: string;
}

export interface LocationSummary {
  id: string;
  name: string;
}

export interface StockInfo {
  available: number;
  reserved: number;
  forSale: number;
}

export interface PagedSalesOrderResponse {
  content: SalesOrderResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface SalesOrderFilters {
  customer_id?: string;
  status?: string;
  order_date_from?: string;
  order_date_to?: string;
  order_number?: string;
  page?: number;
  size?: number;
  sort?: string[];
}

export interface StockAvailability {
  available: number;
  reserved: number;
  forSale: number;
  inStock: boolean;
}

export enum SalesOrderStatus {
  DRAFT = 'DRAFT',
  CONFIRMED = 'CONFIRMED',
  INVOICED = 'INVOICED',
  CANCELLED = 'CANCELLED'
}

export enum PaymentTerms {
  A_VISTA = 'A_VISTA',
  DIAS_7 = 'DIAS_7',
  DIAS_14 = 'DIAS_14',
  DIAS_30 = 'DIAS_30',
  DIAS_60 = 'DIAS_60',
  DIAS_90 = 'DIAS_90'
}

// Story 4.6: Expiring Sales Orders
export interface ExpiringSalesOrder {
  id: string;
  orderNumber: string;
  customerId: string;
  customerName: string;
  stockLocationId: string;
  locationName: string;
  orderDate: string;
  deliveryDateExpected: string | null;
  totalAmount: number;
  status: SalesOrderStatus;
  daysUntilExpiration: number;
  createdAt: string;
}

/**
 * Get sales orders expiring soon (Story 4.6 - AC9)
 * @param days Days until expiration threshold (default: 2)
 */
export function getExpiringSoon(http: HttpClient, days: number = 2): Observable<ExpiringSalesOrder[]> {
  const apiUrl = `${environment.apiUrl}/sales-orders`;
  return http.get<ExpiringSalesOrder[]>(`${apiUrl}/expiring-soon`, {
    params: { days: days.toString() }
  });
}

/**
 * Extend sales order expiration by N days (Story 4.6 - AC10)
 * @param http HttpClient instance
 * @param orderId Order ID
 * @param days Days to extend (default: 7)
 */
export function extendOrderExpiration(
  http: HttpClient,
  orderId: string,
  days: number = 7
): Observable<{ message: string }> {
  const apiUrl = `${environment.apiUrl}/sales-orders`;
  return http.put<{ message: string }>(`${apiUrl}/${orderId}/extend`, null, {
    params: { days: days.toString() }
  });
}
