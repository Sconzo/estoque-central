export interface DashboardSummary {
  dailyTotalSales: number;
  dailyOrderCount: number;
  dailyItemCount: number;
  totalInventoryValue: number;
  totalInventoryQuantity: number;
  totalUniqueProducts: number;
  outOfStockCount: number;
  criticalStockCount: number;
  lowStockCount: number;
  totalReplenishmentCost: number;
  pendingOrdersCount: number;
  pendingOrdersValue: number;
  overdueOrdersCount: number;
  snapshotTime: string;
}

export interface CriticalStockProduct {
  productId: string;
  sku: string;
  productName: string;
  categoryName: string;
  locationId: string;
  locationCode: string;
  locationName: string;
  currentQuantity: number;
  minimumQuantity: number;
  maximumQuantity: number;
  reorderPoint: number;
  quantityNeeded: number;
  alertLevel: string;
  unitCost: number;
  replenishmentCost: number;
  lastUpdated: string;
}

export interface TopProduct {
  productId: string;
  sku: string;
  productName: string;
  categoryName: string;
  orderCount: number;
  totalQuantitySold: number;
  totalRevenue: number;
  averagePrice: number;
  currentStock: number;
  rankPosition: number;
}

export interface MonthlySales {
  orderCount: number;
  totalSales: number;
  averageTicket: number;
}

export interface RecentActivity {
  tipo: string;
  descricao: string;
  timestamp: string;
}
