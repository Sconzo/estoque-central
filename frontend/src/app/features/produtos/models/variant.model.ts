/**
 * VariantAttribute - Attribute definition (e.g., "Color")
 */
export interface VariantAttribute {
  name: string;
  values: string[];
}

/**
 * ProductVariant - Individual variant
 */
export interface ProductVariant {
  id: string;
  parentProductId: string;
  sku: string;
  barcode?: string;
  name: string;
  price?: number;
  cost?: number;
  status: string;
  ativo: boolean;
  attributeCombination?: Record<string, string>; // e.g., { "Color": "Red", "Size": "M" }
}

/**
 * CreateVariantProductRequest - Request to create variant parent product
 */
export interface CreateVariantProductRequest {
  name: string;
  baseSku: string;
  description?: string;
  categoryId: string;
  price: number;
  cost?: number;
  unit?: string;
  attributes: VariantAttribute[];
}

/**
 * GenerateVariantsRequest - Request to generate variant matrix
 */
export interface GenerateVariantsRequest {
  attributes: VariantAttribute[];
}

/**
 * VariantMatrixRow - Row in variant matrix (for UI display)
 */
export interface VariantMatrixRow {
  sku: string;
  combination: Record<string, string>;
  price?: number;
  cost?: number;
  initialQuantity?: number;
  minimumQuantity?: number;
  maximumQuantity?: number;
  editable: boolean;
}
