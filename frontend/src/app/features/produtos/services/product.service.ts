import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Product,
  ProductDTO,
  ProductCreateRequest,
  ProductUpdateRequest,
  ProductSearchFilters,
  Page,
  ProductStatus
} from '../models/product.model';
import { environment } from '../../../../environments/environment';

/**
 * ProductService - HTTP client for product API
 *
 * Provides methods for managing products (CRUD, search, filters).
 * All requests are tenant-scoped via JWT token (added by JwtInterceptor).
 */
@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly apiUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) {}

  /**
   * Lists all active products with pagination
   *
   * @param page page number (default 0)
   * @param size page size (default 20)
   * @returns Observable of paginated products
   */
  listAll(page: number = 0, size: number = 20): Observable<Page<ProductDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<ProductDTO>>(this.apiUrl, { params });
  }

  /**
   * Gets product by ID
   *
   * @param id product ID
   * @returns Observable of product
   */
  getById(id: string): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.apiUrl}/${id}`);
  }

  /**
   * Gets product by SKU
   *
   * @param sku product SKU
   * @returns Observable of product
   */
  getBySku(sku: string): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.apiUrl}/sku/${sku}`);
  }

  /**
   * Gets product by barcode
   *
   * @param barcode product barcode
   * @returns Observable of product
   */
  getByBarcode(barcode: string): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.apiUrl}/barcode/${barcode}`);
  }

  /**
   * Searches products by query (name, SKU, barcode)
   *
   * @param query search query
   * @param page page number (default 0)
   * @param size page size (default 20)
   * @returns Observable of paginated products
   */
  search(query: string, page: number = 0, size: number = 20): Observable<Page<ProductDTO>> {
    const params = new HttpParams()
      .set('q', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<ProductDTO>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Lists products by category
   *
   * @param categoryId category ID
   * @param page page number (default 0)
   * @param size page size (default 20)
   * @returns Observable of paginated products
   */
  listByCategory(categoryId: string, page: number = 0, size: number = 20): Observable<Page<ProductDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<ProductDTO>>(`${this.apiUrl}/category/${categoryId}`, { params });
  }

  /**
   * Advanced search with multiple filters
   *
   * @param filters search filters
   * @returns Observable of paginated products
   */
  searchWithFilters(filters: ProductSearchFilters): Observable<Page<ProductDTO>> {
    let params = new HttpParams()
      .set('page', (filters.page || 0).toString())
      .set('size', (filters.size || 20).toString());

    if (filters.query) {
      params = params.set('q', filters.query);
    }

    if (filters.categoryId) {
      return this.listByCategory(filters.categoryId, filters.page, filters.size);
    }

    if (filters.query) {
      return this.search(filters.query, filters.page, filters.size);
    }

    return this.listAll(filters.page, filters.size);
  }

  /**
   * Creates new product
   *
   * @param request product creation data
   * @returns Observable of created product
   */
  create(request: ProductCreateRequest): Observable<ProductDTO> {
    return this.http.post<ProductDTO>(this.apiUrl, request);
  }

  /**
   * Updates product
   *
   * @param id product ID
   * @param request update data
   * @returns Observable of updated product
   */
  update(id: string, request: ProductUpdateRequest): Observable<ProductDTO> {
    return this.http.put<ProductDTO>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Updates product status
   *
   * @param id product ID
   * @param status new status
   * @returns Observable of updated product
   */
  updateStatus(id: string, status: ProductStatus): Observable<ProductDTO> {
    return this.http.patch<ProductDTO>(`${this.apiUrl}/${id}/status`, { status });
  }

  /**
   * Deletes product (soft delete)
   *
   * @param id product ID
   * @returns Observable of void
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activates previously deactivated product
   *
   * @param id product ID
   * @returns Observable of activated product
   */
  activate(id: string): Observable<ProductDTO> {
    return this.http.put<ProductDTO>(`${this.apiUrl}/${id}/activate`, {});
  }
}
