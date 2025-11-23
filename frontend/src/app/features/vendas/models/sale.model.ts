export interface SaleItem {
  product_id: string;
  quantity: number;
  unit_price: number;
}

export interface Sale {
  id: string;
  tenant_id: string;
  customer_id: string;
  sale_date: string;
  total_amount: number;
  discount: number;
  payment_method: string;
  status: string;
  items: SaleItem[];
}

export interface SaleResponse {
  id: string;
  customer_id: string;
  sale_date: string;
  total_amount: number;
  discount: number;
  payment_method: string;
  status: string;
}
