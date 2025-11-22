import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  BomComponent,
  AddBomComponentRequest,
  AvailableStockResponse
} from '../models/composite.model';
import { environment } from '../../../../environments/environment';

/**
 * CompositeProductService - HTTP client for composite products/kits API
 *
 * Provides methods for managing Bill of Materials (BOM) for composite products.
 */
@Injectable({
  providedIn: 'root'
})
export class CompositeProductService {
  private readonly apiUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) {}

  /**
   * Adds component to composite product BOM
   *
   * @param productId composite product ID
   * @param request component to add
   * @returns Observable of created component
   */
  addComponent(productId: string, request: AddBomComponentRequest): Observable<BomComponent> {
    return this.http.post<BomComponent>(`${this.apiUrl}/${productId}/bom`, request);
  }

  /**
   * Lists all components of a composite product
   *
   * @param productId composite product ID
   * @returns Observable of component list
   */
  listComponents(productId: string): Observable<BomComponent[]> {
    return this.http.get<BomComponent[]>(`${this.apiUrl}/${productId}/bom`);
  }

  /**
   * Updates component quantity
   *
   * @param productId composite product ID
   * @param componentId component product ID
   * @param request updated quantity
   * @returns Observable of updated component
   */
  updateComponentQuantity(productId: string, componentId: string,
                         request: AddBomComponentRequest): Observable<BomComponent> {
    return this.http.put<BomComponent>(
      `${this.apiUrl}/${productId}/bom/${componentId}`,
      request
    );
  }

  /**
   * Removes component from BOM
   *
   * @param productId composite product ID
   * @param componentId component product ID
   * @returns Observable of void
   */
  removeComponent(productId: string, componentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}/bom/${componentId}`);
  }

  /**
   * Calculates available stock for virtual BOM
   *
   * @param productId composite product ID
   * @returns Observable of available stock response
   */
  getAvailableStock(productId: string): Observable<AvailableStockResponse> {
    return this.http.get<AvailableStockResponse>(`${this.apiUrl}/${productId}/available-stock`);
  }
}
