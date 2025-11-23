import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/**
 * Interface for receiving item in local queue
 */
export interface ReceivingItem {
  purchaseOrderItemId: string;
  productId: string;
  productName: string;
  sku: string;
  barcode: string;
  quantityToReceive: number;
  unitCost: number;
}

/**
 * Interface for receiving order summary
 */
export interface ReceivingOrderSummary {
  id: string;
  orderNumber: string;
  supplierName: string;
  orderDate: string;
  status: string;
  itemsSummary: {
    totalItems: number;
    totalReceived: number;
    totalPending: number;
    totalAmount: number;
  };
}

/**
 * Interface for receiving order details
 */
export interface ReceivingOrderDetail {
  id: string;
  orderNumber: string;
  supplierName: string;
  stockLocationName: string;
  items: ReceivingItemDetail[];
}

/**
 * Interface for receiving item detail
 */
export interface ReceivingItemDetail {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  barcode: string;
  quantityOrdered: number;
  quantityReceived: number;
  quantityPending: number;
  unitCost: number;
}

/**
 * Service for managing receiving operations
 * Story 3.3: Mobile Receiving with Barcode Scanner
 */
@Injectable({
  providedIn: 'root'
})
export class ReceivingService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080';
  private receivingQueue$ = new BehaviorSubject<ReceivingItem[]>([]);

  constructor(private http: HttpClient) {}

  /**
   * Get pending receipt orders
   */
  getPendingOrders(tenantId: string, supplierId?: string): Observable<ReceivingOrderSummary[]> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId
    });

    let url = `${this.apiUrl}/api/purchase-orders/pending-receipt`;
    if (supplierId) {
      url += `?supplier_id=${supplierId}`;
    }

    return this.http.get<ReceivingOrderSummary[]>(url, { headers });
  }

  /**
   * Get receiving details for a specific order
   */
  getReceivingDetails(tenantId: string, orderId: string): Observable<ReceivingOrderDetail> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId
    });

    return this.http.get<ReceivingOrderDetail>(
      `${this.apiUrl}/api/purchase-orders/${orderId}/receiving-details`,
      { headers }
    );
  }

  /**
   * Add item to receiving queue
   */
  addItem(item: ReceivingItem): void {
    const current = this.receivingQueue$.value;

    // Check if item already exists in queue
    const existingIndex = current.findIndex(
      i => i.purchaseOrderItemId === item.purchaseOrderItemId
    );

    if (existingIndex >= 0) {
      // Add to existing quantity
      current[existingIndex].quantityToReceive += item.quantityToReceive;
    } else {
      // Add new item
      current.push(item);
    }

    this.receivingQueue$.next([...current]);
  }

  /**
   * Remove item from receiving queue
   */
  removeItem(purchaseOrderItemId: string): void {
    const current = this.receivingQueue$.value.filter(
      i => i.purchaseOrderItemId !== purchaseOrderItemId
    );
    this.receivingQueue$.next(current);
  }

  /**
   * Get receiving queue as observable
   */
  getQueue(): Observable<ReceivingItem[]> {
    return this.receivingQueue$.asObservable();
  }

  /**
   * Get current queue value
   */
  getQueueValue(): ReceivingItem[] {
    return this.receivingQueue$.value;
  }

  /**
   * Get total value of items in queue
   */
  getTotalValue(): number {
    return this.receivingQueue$.value.reduce(
      (sum, item) => sum + (item.quantityToReceive * item.unitCost),
      0
    );
  }

  /**
   * Get total item count in queue
   */
  getItemCount(): number {
    return this.receivingQueue$.value.length;
  }

  /**
   * Clear receiving queue
   */
  clearQueue(): void {
    this.receivingQueue$.next([]);
  }

  /**
   * Find item in order by barcode
   */
  findItemByBarcode(orderDetail: ReceivingOrderDetail, barcode: string): ReceivingItemDetail | null {
    return orderDetail.items.find(item => item.barcode === barcode) || null;
  }

  /**
   * Finalize receiving - submit to backend (Story 3.4 AC8)
   */
  finalizeReceiving(
    tenantId: string,
    purchaseOrderId: string,
    receivingDate: string,
    notes: string | null
  ): Observable<any> {
    const headers = new HttpHeaders({
      'X-Tenant-ID': tenantId,
      'Content-Type': 'application/json'
    });

    const items = this.receivingQueue$.value.map(item => ({
      purchaseOrderItemId: item.purchaseOrderItemId,
      quantityReceived: item.quantityToReceive,
      notes: null
    }));

    const request = {
      purchaseOrderId,
      receivingDate,
      notes,
      items
    };

    return this.http.post(`${this.apiUrl}/api/receivings`, request, { headers });
  }
}
