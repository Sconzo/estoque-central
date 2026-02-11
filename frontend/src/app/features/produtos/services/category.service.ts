import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Category, CategoryTreeNode, CategoryCreateRequest, CategoryUpdateRequest } from '../models/category.model';
import { environment } from '../../../../environments/environment';

/**
 * CategoryService - HTTP client for category API
 *
 * Provides methods for managing hierarchical product categories.
 * All requests are tenant-scoped via JWT token (added by JwtInterceptor).
 */
@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly apiUrl = `${environment.apiUrl}/api/categories`;

  constructor(private http: HttpClient) {}

  /**
   * Lists all active categories (flat list)
   *
   * @returns Observable of category array
   */
  listAll(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl);
  }

  /**
   * Gets hierarchical category tree
   *
   * @returns Observable of tree structure (root categories with nested children)
   */
  getTree(): Observable<CategoryTreeNode[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tree`).pipe(
      map(nodes => this.mapToTreeNodes(nodes))
    );
  }

  private mapToTreeNodes(nodes: any[]): CategoryTreeNode[] {
    if (!nodes) return [];
    return nodes.map(node => ({
      category: {
        id: node.id,
        name: node.name,
        description: node.description,
        parentId: node.parentId,
        ativo: node.ativo ?? true,
        createdAt: node.createdAt ?? '',
        updatedAt: node.updatedAt ?? '',
      } as Category,
      children: this.mapToTreeNodes(node.children),
      expanded: false
    }));
  }

  /**
   * Gets category by ID
   *
   * @param id category ID
   * @returns Observable of category
   */
  getById(id: string): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`);
  }

  /**
   * Gets category path (breadcrumb) from root to category
   *
   * @param id category ID
   * @returns Observable of category array (ordered from root to current)
   */
  getPath(id: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/${id}/path`);
  }

  /**
   * Searches categories by name
   *
   * @param query search query (case-insensitive)
   * @returns Observable of matching categories
   */
  search(query: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/search`, {
      params: { q: query }
    });
  }

  /**
   * Gets root categories (no parent)
   *
   * @returns Observable of root categories
   */
  getRoots(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/roots`);
  }

  /**
   * Gets children of a category
   *
   * @param id parent category ID
   * @returns Observable of child categories
   */
  getChildren(id: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/${id}/children`);
  }

  /**
   * Creates new category
   *
   * @param request category creation data
   * @returns Observable of created category
   */
  create(request: CategoryCreateRequest): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, request);
  }

  /**
   * Updates category
   *
   * @param id category ID
   * @param request update data
   * @returns Observable of updated category
   */
  update(id: string, request: CategoryUpdateRequest): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Deletes category (soft delete)
   *
   * @param id category ID
   * @returns Observable of void
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activates previously deactivated category
   *
   * @param id category ID
   * @returns Observable of activated category
   */
  activate(id: string): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}/activate`, {});
  }
}
