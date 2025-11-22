/**
 * Stock Models - TypeScript interfaces for stock management
 * Story 2.7: Multi-Warehouse Stock Control
 */

export interface StockResponse {
  id: string;
  productId?: string;
  variantId?: string;
  locationId: string;
  locationName: string;
  locationCode: string;
  productName: string;
  productSku: string;
  quantityAvailable: number;
  reservedQuantity: number;
  quantityForSale: number;
  minimumQuantity?: number;
  maximumQuantity?: number;
  stockStatus: 'OK' | 'LOW' | 'CRITICAL' | 'NOT_SET';
  percentageOfMinimum?: number;
}

export interface StockByLocationResponse {
  productId?: string;
  variantId?: string;
  productName: string;
  productSku: string;
  totalLocations: number;
  totalQuantityAvailable: number;
  totalReservedQuantity: number;
  totalQuantityForSale: number;
  byLocation: LocationStock[];
}

export interface LocationStock {
  stockLocationId: string;
  locationName: string;
  locationCode: string;
  quantityAvailable: number;
  reservedQuantity: number;
  quantityForSale: number;
  minimumQuantity?: number;
  status: 'OK' | 'LOW' | 'CRITICAL';
}

export interface SetMinimumQuantityRequest {
  stockLocationId: string;
  minimumQuantity: number;
}

export interface BelowMinimumStockResponse {
  products: ProductBelowMinimum[];
  totalCount: number;
}

export interface ProductBelowMinimum {
  productId?: string;
  variantId?: string;
  productName: string;
  sku: string;
  stockLocationId: string;
  locationName: string;
  quantityForSale: number;
  minimumQuantity: number;
  percentageOfMinimum: number;
  severity: 'CRITICAL' | 'LOW';
}

export interface StockDashboardSummary {
  totalProducts: number;
  productsInRupture: number;
  totalInventoryValue: number;
}

// ============================================================
// Story 2.8: Stock Movement History
// ============================================================

export enum MovementType {
  ENTRY = 'ENTRY',
  EXIT = 'EXIT',
  TRANSFER_OUT = 'TRANSFER_OUT',
  TRANSFER_IN = 'TRANSFER_IN',
  ADJUSTMENT = 'ADJUSTMENT',
  SALE = 'SALE',
  PURCHASE = 'PURCHASE',
  RESERVE = 'RESERVE',
  RELEASE = 'RELEASE',
  BOM_ASSEMBLY = 'BOM_ASSEMBLY',
  BOM_DISASSEMBLY = 'BOM_DISASSEMBLY'
}

export interface MovementTypeInfo {
  type: MovementType;
  displayName: string;
  icon: string;
  color: string;
}

export const MOVEMENT_TYPE_INFO: Record<MovementType, Omit<MovementTypeInfo, 'type'>> = {
  [MovementType.ENTRY]: {
    displayName: 'Entrada Manual',
    icon: '📥',
    color: '#4caf50'
  },
  [MovementType.EXIT]: {
    displayName: 'Saída Manual',
    icon: '📤',
    color: '#f44336'
  },
  [MovementType.TRANSFER_OUT]: {
    displayName: 'Transferência - Saída',
    icon: '🔄',
    color: '#ff9800'
  },
  [MovementType.TRANSFER_IN]: {
    displayName: 'Transferência - Entrada',
    icon: '🔄',
    color: '#2196f3'
  },
  [MovementType.ADJUSTMENT]: {
    displayName: 'Ajuste de Inventário',
    icon: '⚖️',
    color: '#9c27b0'
  },
  [MovementType.SALE]: {
    displayName: 'Venda',
    icon: '🛒',
    color: '#e91e63'
  },
  [MovementType.PURCHASE]: {
    displayName: 'Compra',
    icon: '📦',
    color: '#00bcd4'
  },
  [MovementType.RESERVE]: {
    displayName: 'Reserva',
    icon: '🔒',
    color: '#795548'
  },
  [MovementType.RELEASE]: {
    displayName: 'Liberação de Reserva',
    icon: '🔓',
    color: '#607d8b'
  },
  [MovementType.BOM_ASSEMBLY]: {
    displayName: 'Montagem de Kit',
    icon: '🔧',
    color: '#3f51b5'
  },
  [MovementType.BOM_DISASSEMBLY]: {
    displayName: 'Desmontagem de Kit',
    icon: '🔨',
    color: '#673ab7'
  }
};

export interface StockMovementResponse {
  id: string;
  tenantId: string;
  productId?: string;
  variantId?: string;
  productName: string;
  productSku: string;
  stockLocationId: string;
  locationName: string;
  locationCode: string;
  type: MovementType;
  typeDisplayName: string;
  typeIcon: string;
  quantity: number;
  balanceBefore: number;
  balanceAfter: number;
  userId: string;
  userName?: string;
  documentType?: string;
  documentId?: string;
  reason?: string;
  createdAt: string;
}

export interface CreateStockMovementRequest {
  productId?: string;
  variantId?: string;
  stockLocationId: string;
  type: MovementType;
  quantity: number;
  reason?: string;
  documentType?: string;
  documentId?: string;
}

export interface StockMovementFilters {
  productId?: string;
  variantId?: string;
  locationId?: string;
  type?: MovementType;
  startDate?: string;
  endDate?: string;
  documentType?: string;
  documentId?: string;
  userId?: string;
  page?: number;
  size?: number;
}

// ============================================================
// Story 2.9: Stock Transfer Between Locations
// ============================================================

export interface StockTransferResponse {
  id: string;
  productId?: string;
  variantId?: string;
  productName: string;
  productSku: string;
  originLocationId: string;
  originLocationName: string;
  originLocationCode: string;
  destinationLocationId: string;
  destinationLocationName: string;
  destinationLocationCode: string;
  quantity: number;
  reason?: string;
  userId: string;
  userName?: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
}

export interface CreateStockTransferRequest {
  productId?: string;
  variantId?: string;
  originLocationId: string;
  destinationLocationId: string;
  quantity: number;
  reason?: string;
}

export interface StockTransferFilters {
  productId?: string;
  variantId?: string;
  originLocationId?: string;
  destinationLocationId?: string;
  startDate?: string;
  endDate?: string;
  userId?: string;
}
