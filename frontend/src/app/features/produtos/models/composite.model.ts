/**
 * BomType - Bill of Materials type
 */
export enum BomType {
  VIRTUAL = 'VIRTUAL',
  PHYSICAL = 'PHYSICAL'
}

/**
 * BomComponent - Component in Bill of Materials
 */
export interface BomComponent {
  id: string;
  componentProductId: string;
  componentSku: string;
  componentName: string;
  quantityRequired: number;
  unit: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * AddBomComponentRequest - Request to add component to BOM
 */
export interface AddBomComponentRequest {
  componentProductId: string;
  quantityRequired: number;
}

/**
 * AvailableStockResponse - Response for available stock calculation
 */
export interface AvailableStockResponse {
  productId: string;
  availableQuantity: number;
  limitingComponentId?: string;
  message?: string;
}

/**
 * BOM Type labels for UI
 */
export const BOM_TYPE_LABELS: Record<BomType, string> = {
  [BomType.VIRTUAL]: 'Virtual (estoque calculado)',
  [BomType.PHYSICAL]: 'Físico (kits pré-montados)'
};
