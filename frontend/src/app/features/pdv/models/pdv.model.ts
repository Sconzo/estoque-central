/**
 * PDV Models - Story 4.2
 */

export interface CartItem {
  id?: string;
  product_id: string;
  product_name: string;
  barcode?: string;
  quantity: number;
  unit_price: number;
  subtotal: number;
}

export interface Cart {
  items: CartItem[];
  subtotal: number;
  discount: number;
  total: number;
}

export type PaymentMethod = 'DINHEIRO' | 'DEBITO' | 'CREDITO' | 'PIX';

export interface SaleRequest {
  customer_id: string;
  payment_method: PaymentMethod;
  items: SaleItemRequest[];
  discount?: number;
  amount_received?: number;
}

export interface SaleItemRequest {
  product_id?: string;
  variant_id?: string;
  quantity: number;
  unit_price: number;
}
