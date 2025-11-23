import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Cart, CartItem } from '../models/pdv.model';

/**
 * CartService - Manages shopping cart state
 * Story 4.2: PDV Touchscreen Interface
 */
@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<Cart>(this.getEmptyCart());
  public cart$: Observable<Cart> = this.cartSubject.asObservable();

  constructor() {}

  private getEmptyCart(): Cart {
    return {
      items: [],
      subtotal: 0,
      discount: 0,
      total: 0
    };
  }

  addItem(product: any): void {
    const cart = this.cartSubject.value;
    const existingItem = cart.items.find(i => i.product_id === product.id);

    if (existingItem) {
      existingItem.quantity++;
      existingItem.subtotal = existingItem.quantity * existingItem.unit_price;
    } else {
      cart.items.push({
        product_id: product.id,
        product_name: product.name,
        barcode: product.barcode,
        quantity: 1,
        unit_price: product.price || 0,
        subtotal: product.price || 0
      });
    }

    this.recalculateTotals(cart);
    this.cartSubject.next(cart);
  }

  updateQuantity(productId: string, quantity: number): void {
    const cart = this.cartSubject.value;
    const item = cart.items.find(i => i.product_id === productId);

    if (item && quantity > 0) {
      item.quantity = quantity;
      item.subtotal = item.quantity * item.unit_price;
      this.recalculateTotals(cart);
      this.cartSubject.next(cart);
    }
  }

  removeItem(productId: string): void {
    const cart = this.cartSubject.value;
    cart.items = cart.items.filter(i => i.product_id !== productId);
    this.recalculateTotals(cart);
    this.cartSubject.next(cart);
  }

  clearCart(): void {
    this.cartSubject.next(this.getEmptyCart());
  }

  private recalculateTotals(cart: Cart): void {
    cart.subtotal = cart.items.reduce((sum, item) => sum + item.subtotal, 0);
    cart.total = cart.subtotal - cart.discount;
  }

  getCart(): Cart {
    return this.cartSubject.value;
  }
}
