/**
 * Category Model
 *
 * Represents a product category in hierarchical tree structure.
 * Categories support unlimited depth via self-referencing parentId.
 */
export interface Category {
  id: string;
  name: string;
  description?: string;
  parentId?: string;
  ativo: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * CategoryTreeNode - Hierarchical tree structure
 *
 * Used by the tree component to display nested categories
 * with expand/collapse functionality.
 */
export interface CategoryTreeNode {
  category: Category;
  children: CategoryTreeNode[];
  expanded?: boolean; // UI state for expand/collapse
}

/**
 * CategoryCreateRequest - DTO for creating category
 */
export interface CategoryCreateRequest {
  name: string;
  description?: string;
  parentId?: string;
}

/**
 * CategoryUpdateRequest - DTO for updating category
 */
export interface CategoryUpdateRequest {
  name?: string;
  description?: string;
  parentId?: string;
}
