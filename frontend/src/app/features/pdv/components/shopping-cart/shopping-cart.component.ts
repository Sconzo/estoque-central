import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../services/cart.service';
import { Cart, CartItem } from '../../models/pdv.model';

/**
 * ShoppingCartComponent - Display and manage cart items
 * Story 4.2: PDV Touchscreen Interface - AC3
 */
@Component({
  selector: 'app-shopping-cart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="shopping-cart">
      <h3>Carrinho de Compras</h3>
      <div class="cart-items" *ngIf="cart.items.length > 0">
        <div *ngFor="let item of cart.items" class="cart-item">
          <div class="item-info">
            <strong class="item-name">{{ item.product_name }}</strong>
            <span class="item-price">R$ {{ item.unit_price | number:'1.2-2' }}</span>
          </div>
          <div class="item-controls">
            <button
              class="btn-quantity"
              (click)="decrementQuantity(item)"
              [disabled]="item.quantity <= 1"
            >
              -
            </button>
            <input
              type="number"
              [(ngModel)]="item.quantity"
              (change)="updateQuantity(item)"
              class="quantity-input"
              min="1"
            />
            <button
              class="btn-quantity"
              (click)="incrementQuantity(item)"
            >
              +
            </button>
            <button
              class="btn-remove"
              (click)="removeItem(item)"
              title="Remover item"
            >
              ×
            </button>
          </div>
          <div class="item-subtotal">
            Subtotal: <strong>R$ {{ item.subtotal | number:'1.2-2' }}</strong>
          </div>
        </div>
      </div>
      <div *ngIf="cart.items.length === 0" class="empty-cart">
        <p>Carrinho vazio</p>
        <p class="hint">Adicione produtos usando a busca</p>
      </div>
      <div class="cart-summary" *ngIf="cart.items.length > 0">
        <div class="summary-row">
          <span>Subtotal:</span>
          <strong>R$ {{ cart.subtotal | number:'1.2-2' }}</strong>
        </div>
        <div class="summary-row" *ngIf="cart.discount > 0">
          <span>Desconto:</span>
          <strong class="discount">-R$ {{ cart.discount | number:'1.2-2' }}</strong>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .shopping-cart {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    h3 {
      margin: 0 0 1rem 0;
    }
    .cart-items {
      flex: 1;
      overflow-y: auto;
      margin-bottom: 1rem;
    }
    .cart-item {
      padding: 1rem;
      border-bottom: 1px solid #eee;
      background: #f8f9fa;
      margin-bottom: 0.5rem;
      border-radius: 4px;
    }
    .item-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }
    .item-name {
      font-size: 1.1rem;
      color: #333;
    }
    .item-price {
      color: #666;
      font-size: 0.95rem;
    }
    .item-controls {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
    }
    .btn-quantity {
      width: 44px;
      height: 44px;
      font-size: 1.5rem;
      font-weight: bold;
      border: 1px solid #ddd;
      background: white;
      cursor: pointer;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .btn-quantity:hover:not(:disabled) {
      background: #e9ecef;
    }
    .btn-quantity:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .quantity-input {
      width: 80px;
      height: 44px;
      text-align: center;
      font-size: 1.2rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      padding: 0.5rem;
    }
    .btn-remove {
      width: 44px;
      height: 44px;
      margin-left: auto;
      font-size: 2rem;
      font-weight: bold;
      background: #dc3545;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      line-height: 1;
    }
    .btn-remove:hover {
      background: #c82333;
    }
    .item-subtotal {
      text-align: right;
      color: #28a745;
      font-size: 1.1rem;
    }
    .empty-cart {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #999;
      text-align: center;
    }
    .empty-cart p {
      margin: 0.5rem 0;
    }
    .hint {
      font-size: 0.9rem;
    }
    .cart-summary {
      border-top: 2px solid #dee2e6;
      padding-top: 1rem;
    }
    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
      font-size: 1.1rem;
    }
    .discount {
      color: #dc3545;
    }
  `]
})
export class ShoppingCartComponent implements OnInit {
  cart: Cart = { items: [], subtotal: 0, discount: 0, total: 0 };

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.cartService.cart$.subscribe(cart => {
      this.cart = cart;
    });
  }

  incrementQuantity(item: CartItem): void {
    this.cartService.updateQuantity(item.product_id, item.quantity + 1);
  }

  decrementQuantity(item: CartItem): void {
    if (item.quantity > 1) {
      this.cartService.updateQuantity(item.product_id, item.quantity - 1);
    }
  }

  updateQuantity(item: CartItem): void {
    const quantity = Math.max(1, Math.floor(item.quantity));
    this.cartService.updateQuantity(item.product_id, quantity);
  }

  removeItem(item: CartItem): void {
    if (confirm(`Remover ${item.product_name} do carrinho?`)) {
      this.cartService.removeItem(item.product_id);
    }
  }
}
