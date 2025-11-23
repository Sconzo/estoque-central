import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductSearchComponent } from '../product-search/product-search.component';
import { ShoppingCartComponent } from '../shopping-cart/shopping-cart.component';
import { PaymentModalComponent } from '../payment-modal/payment-modal.component';
import { CartService } from '../../services/cart.service';
import { CustomerService } from '../../../vendas/services/customer.service';
import { Customer } from '../../../vendas/models/customer.model';

/**
 * PDVLayoutComponent - Main PDV interface
 * Story 4.2: PDV Touchscreen Interface - AC1
 */
@Component({
  selector: 'app-pdv-layout',
  standalone: true,
  imports: [CommonModule, ProductSearchComponent, ShoppingCartComponent, PaymentModalComponent],
  templateUrl: './pdv-layout.component.html',
  styleUrls: ['./pdv-layout.component.scss']
})
export class PdvLayoutComponent implements OnInit {
  selectedCustomer: Customer | null = null;
  showPaymentModal = false;
  cartTotal = 0;

  constructor(
    private cartService: CartService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    this.cartService.cart$.subscribe(cart => {
      this.cartTotal = cart.total;
    });

    // Load default consumer
    this.customerService.getDefaultConsumer().subscribe({
      next: (customer) => {
        this.selectedCustomer = customer;
      },
      error: (err) => console.error('Error loading default consumer:', err)
    });
  }

  onFinalizeSale(): void {
    if (this.cartTotal > 0) {
      this.showPaymentModal = true;
    }
  }

  onPaymentComplete(): void {
    this.showPaymentModal = false;
    this.cartService.clearCart();
    // Reload default consumer
    this.ngOnInit();
  }

  onPaymentCancel(): void {
    this.showPaymentModal = false;
  }
}
