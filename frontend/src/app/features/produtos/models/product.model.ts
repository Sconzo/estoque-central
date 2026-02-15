/**
 * Product Type Enum
 */
export enum ProductType {
  SIMPLE = 'SIMPLE',
  VARIANT_PARENT = 'VARIANT_PARENT',
  VARIANT = 'VARIANT',
  COMPOSITE = 'COMPOSITE'
}

/**
 * Product Status Enum
 */
export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
}

/**
 * Product - Main product entity
 */
export interface Product {
  id: string;
  tenantId: string;
  type: ProductType;
  bomType?: string; // BomType enum: VIRTUAL or PHYSICAL (only for COMPOSITE products)
  name: string;
  sku: string;
  barcode?: string;
  description?: string;
  categoryId: string;
  price: number;
  cost?: number;
  unit: string;
  controlsInventory: boolean;
  status: ProductStatus;
  ativo: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * ProductDTO - Product with category information
 */
export interface ProductDTO extends Product {
  categoryName?: string;
  categoryPath?: string;
}

/**
 * ProductAttribute - Descriptive key/value attribute
 */
export interface ProductAttribute {
  key: string;
  value: string;
}

/**
 * ProductCreateRequest - DTO for creating product
 */
export interface ProductCreateRequest {
  type: ProductType;
  bomType?: string; // Required for COMPOSITE products
  name: string;
  sku: string;
  barcode?: string;
  description?: string;
  categoryId: string;
  price: number;
  cost?: number;
  unit?: string;
  controlsInventory?: boolean;
  status?: ProductStatus;
  // Inventory fields (optional)
  locationId?: string;
  initialQuantity?: number;
  minimumQuantity?: number;
  maximumQuantity?: number;
  // Descriptive attributes (optional)
  attributes?: ProductAttribute[];
}

/**
 * ProductUpdateRequest - DTO for updating product
 */
export interface ProductUpdateRequest {
  name?: string;
  description?: string;
  categoryId?: string;
  price?: number;
  cost?: number;
  unit?: string;
  controlsInventory?: boolean;
  status?: ProductStatus;
}

/**
 * ProductSearchFilters - Filters for product search
 */
export interface ProductSearchFilters {
  query?: string;
  categoryId?: string;
  status?: ProductStatus;
  controlsInventory?: boolean;
  page?: number;
  size?: number;
}

/**
 * Page - Generic pagination wrapper
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * Unit of Measure options
 */
export const UNIT_OPTIONS = [
  { value: 'UN', label: 'Unidade' },
  { value: 'KG', label: 'Quilograma' },
  { value: 'G', label: 'Grama' },
  { value: 'L', label: 'Litro' },
  { value: 'ML', label: 'Mililitro' },
  { value: 'M', label: 'Metro' },
  { value: 'CM', label: 'Centímetro' },
  { value: 'CX', label: 'Caixa' },
  { value: 'PCT', label: 'Pacote' },
  { value: 'FD', label: 'Fardo' }
];

/**
 * Product Status display labels
 */
export const STATUS_LABELS: Record<ProductStatus, string> = {
  [ProductStatus.ACTIVE]: 'Ativo',
  [ProductStatus.INACTIVE]: 'Inativo',
  [ProductStatus.DISCONTINUED]: 'Descontinuado'
};

/**
 * Product Type display labels
 */
export const TYPE_LABELS: Record<ProductType, string> = {
  [ProductType.SIMPLE]: 'Simples',
  [ProductType.VARIANT_PARENT]: 'Com Variantes',
  [ProductType.VARIANT]: 'Variante',
  [ProductType.COMPOSITE]: 'Kit/Composto (BOM)'
};
