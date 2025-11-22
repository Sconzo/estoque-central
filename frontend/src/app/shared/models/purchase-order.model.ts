/**
 * Purchase Order models
 * Story 3.2: Purchase Order Creation
 */

export enum PurchaseOrderStatus {
  DRAFT = 'DRAFT',
  PENDING_APPROVAL = 'PENDING_APPROVAL',
  APPROVED = 'APPROVED',
  SENT_TO_SUPPLIER = 'SENT_TO_SUPPLIER',
  PARTIALLY_RECEIVED = 'PARTIALLY_RECEIVED',
  RECEIVED = 'RECEIVED',
  CANCELLED = 'CANCELLED',
  CLOSED = 'CLOSED'
}

export interface PurchaseOrderItemRequest {
  productId: string;
  variantId?: string;
  quantityOrdered: number;
  unitCost: number;
  notes?: string;
}

export interface CreatePurchaseOrderRequest {
  supplierId: string;
  stockLocationId: string;
  orderDate?: string;
  expectedDeliveryDate?: string;
  notes?: string;
  items: PurchaseOrderItemRequest[];
}

export interface UpdateStatusRequest {
  status: PurchaseOrderStatus;
}

export interface ProductSummary {
  id: string;
  sku: string;
  name: string;
}

export interface SupplierSummary {
  id: string;
  companyName: string;
}

export interface LocationSummary {
  id: string;
  name: string;
}

export interface UserSummary {
  id: string;
  name: string;
}

export interface PurchaseOrderItemResponse {
  id: string;
  product: ProductSummary;
  quantityOrdered: number;
  quantityReceived: number;
  unitCost: number;
  totalCost: number;
  notes?: string;
}

export interface PurchaseOrderResponse {
  id: string;
  poNumber: string;
  supplier?: SupplierSummary;
  stockLocation?: LocationSummary;
  status: PurchaseOrderStatus;
  orderDate: string;
  expectedDeliveryDate?: string;
  totalAmount: number;
  notes?: string;
  items?: PurchaseOrderItemResponse[];
  createdBy?: UserSummary;
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseOrderPage {
  content: PurchaseOrderResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface PurchaseOrderFilters {
  supplier_id?: string;
  status?: string;
  order_date_from?: string;
  order_date_to?: string;
  order_number?: string;
  page?: number;
  size?: number;
  sort?: string;
}
