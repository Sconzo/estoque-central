import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ProductVariant,
  CreateVariantProductRequest,
  VariantAttribute
} from '../models/variant.model';
import { ProductDTO } from '../models/product.model';
import { environment } from '../../../../environments/environment';

/**
 * VariantService - HTTP client for product variant API
 *
 * Provides methods for managing product variants (matrix products).
 */
@Injectable({
  providedIn: 'root'
})
export class VariantService {
  private readonly apiUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) {}

  /**
   * Creates variant parent product with attributes
   *
   * @param request variant product creation data
   * @returns Observable of created parent product
   */
  createVariantProduct(request: CreateVariantProductRequest): Observable<ProductDTO> {
    return this.http.post<ProductDTO>(`${this.apiUrl}/variants`, request);
  }

  /**
   * Generates all variant combinations (cartesian product)
   *
   * @param parentProductId parent product ID
   * @param attributes list of attributes with values
   * @returns Observable of generated variants
   */
  generateVariants(parentProductId: string, attributes: VariantAttribute[]): Observable<ProductVariant[]> {
    return this.http.post<ProductVariant[]>(
      `${this.apiUrl}/${parentProductId}/variants/generate`,
      attributes
    );
  }

  /**
   * Lists all variants for a parent product
   *
   * @param parentProductId parent product ID
   * @returns Observable of variant list
   */
  listVariants(parentProductId: string): Observable<ProductVariant[]> {
    return this.http.get<ProductVariant[]>(`${this.apiUrl}/${parentProductId}/variants`);
  }

  /**
   * Updates variant
   *
   * @param parentProductId parent product ID
   * @param variantId variant ID
   * @param variant updated variant data
   * @returns Observable of updated variant
   */
  updateVariant(
    parentProductId: string,
    variantId: string,
    variant: Partial<ProductVariant>
  ): Observable<ProductVariant> {
    return this.http.put<ProductVariant>(
      `${this.apiUrl}/${parentProductId}/variants/${variantId}`,
      variant
    );
  }

  /**
   * Deletes variant (soft delete)
   *
   * @param parentProductId parent product ID
   * @param variantId variant ID
   * @returns Observable of void
   */
  deleteVariant(parentProductId: string, variantId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${parentProductId}/variants/${variantId}`);
  }
}
