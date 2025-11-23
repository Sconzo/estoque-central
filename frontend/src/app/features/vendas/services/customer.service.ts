import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Customer,
  CustomerRequest,
  CustomerQuick,
  PagedCustomers,
  CustomerType
} from '../models/customer.model';
import { environment } from '../../../../environments/environment';

/**
 * CustomerService - HTTP client for customer API
 *
 * Story 4.1: Customer Management
 *
 * Provides methods for managing customers (CRUD, search, filters).
 * All requests are tenant-scoped via JWT token (added by JwtInterceptor).
 */
@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private readonly apiUrl = `${environment.apiUrl}/api/customers`;

  constructor(private http: HttpClient) {}

  /**
   * Lists all customers with optional filters and pagination
   *
   * @param customerType optional customer type filter (INDIVIDUAL or BUSINESS)
   * @param ativo optional active status filter (default true)
   * @param page page number (default 0)
   * @param size page size (default 20, max 100)
   * @returns Observable of paginated customers
   */
  listAll(
    customerType?: CustomerType,
    ativo: boolean = true,
    page: number = 0,
    size: number = 20
  ): Observable<PagedCustomers> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('ativo', ativo.toString());

    if (customerType) {
      params = params.set('customerType', customerType);
    }

    return this.http.get<PagedCustomers>(this.apiUrl, { params });
  }

  /**
   * Gets customer by ID
   *
   * @param id customer ID
   * @returns Observable of customer
   */
  getById(id: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/${id}`);
  }

  /**
   * Creates a new customer
   *
   * @param customer customer data
   * @returns Observable of created customer
   */
  create(customer: CustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(this.apiUrl, customer);
  }

  /**
   * Updates an existing customer
   *
   * @param id customer ID
   * @param customer updated customer data
   * @returns Observable of updated customer
   */
  update(id: string, customer: CustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/${id}`, customer);
  }

  /**
   * Soft deletes a customer (marks as inactive)
   *
   * @param id customer ID
   * @returns Observable of void
   */
  softDelete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Quick search for customers (autocomplete)
   * Performance target: < 500ms (NFR3)
   *
   * @param query search query
   * @returns Observable of matching customers (max 10)
   */
  quickSearch(query: string): Observable<CustomerQuick[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<CustomerQuick[]>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Gets the default "Consumidor Final" customer for PDV
   *
   * @returns Observable of default consumer customer
   */
  getDefaultConsumer(): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/default-consumer`);
  }
}
