import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SaleService } from '../../../vendas/services/sale.service';
import { CartService } from '../../services/cart.service';
import { PaymentMethod, SaleRequest } from '../../models/pdv.model';
import { CustomerService } from '../../../vendas/services/customer.service';

/**
 * PaymentModalComponent - Payment processing modal
 * Story 4.2: PDV Touchscreen Interface - AC4
 */
@Component({
  selector: 'app-payment-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal-overlay" (click)="onCancel()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <h2>Finalizar Venda</h2>

        <div class="total-section">
          <span>Total a Pagar:</span>
          <strong class="total">R$ {{ total | number:'1.2-2' }}</strong>
        </div>

        <div class="payment-methods">
          <h3>Forma de Pagamento</h3>
          <div class="methods-grid">
            <button
              *ngFor="let method of paymentMethods"
              class="btn-method"
              [class.selected]="selectedMethod === method.value"
              (click)="selectMethod(method.value)"
            >
              {{ method.label }}
            </button>
          </div>
        </div>

        <div class="cash-section" *ngIf="selectedMethod === 'DINHEIRO'">
          <h3>Valor Recebido</h3>
          <input
            type="number"
            [(ngModel)]="amountReceived"
            (input)="calculateChange()"
            class="cash-input"
            placeholder="0,00"
            step="0.01"
            min="0"
          />
          <div class="change-display" *ngIf="change >= 0">
            <span>Troco:</span>
            <strong [class.error]="change < 0">R$ {{ change | number:'1.2-2' }}</strong>
          </div>
          <div class="error-msg" *ngIf="amountReceived > 0 && change < 0">
            Valor insuficiente!
          </div>
        </div>

        <div class="message" *ngIf="message" [class.error]="isError">
          {{ message }}
        </div>

        <div class="modal-actions">
          <button
            class="btn-cancel"
            (click)="onCancel()"
            [disabled]="processing"
          >
            Cancelar (Esc)
          </button>
          <button
            class="btn-confirm"
            (click)="confirmPayment()"
            [disabled]="!canConfirm() || processing"
          >
            {{ processing ? 'Processando...' : 'Confirmar (Enter)' }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }
    .modal-content {
      background: white;
      border-radius: 8px;
      padding: 2rem;
      width: 90%;
      max-width: 600px;
      max-height: 90vh;
      overflow-y: auto;
    }
    h2 {
      margin: 0 0 1.5rem 0;
      text-align: center;
      color: #333;
    }
    h3 {
      margin: 1rem 0 0.5rem 0;
      color: #666;
      font-size: 1.1rem;
    }
    .total-section {
      text-align: center;
      padding: 1.5rem;
      background: #f8f9fa;
      border-radius: 8px;
      margin-bottom: 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .total-section span {
      font-size: 1.1rem;
      color: #666;
    }
    .total {
      font-size: 2.5rem;
      color: #28a745;
    }
    .payment-methods {
      margin-bottom: 1.5rem;
    }
    .methods-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }
    .btn-method {
      padding: 1.5rem;
      font-size: 1.2rem;
      font-weight: bold;
      border: 2px solid #ddd;
      background: white;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      min-height: 80px;
    }
    .btn-method:hover {
      border-color: #0d6efd;
      background: #f8f9fa;
    }
    .btn-method.selected {
      border-color: #0d6efd;
      background: #0d6efd;
      color: white;
    }
    .cash-section {
      background: #fff3cd;
      padding: 1.5rem;
      border-radius: 8px;
      margin-bottom: 1.5rem;
    }
    .cash-input {
      width: 100%;
      padding: 1rem;
      font-size: 1.5rem;
      text-align: center;
      border: 2px solid #ddd;
      border-radius: 4px;
      margin-bottom: 1rem;
    }
    .change-display {
      text-align: center;
      font-size: 1.5rem;
      padding: 1rem;
      background: white;
      border-radius: 4px;
    }
    .change-display strong {
      color: #28a745;
      margin-left: 0.5rem;
    }
    .change-display strong.error {
      color: #dc3545;
    }
    .error-msg {
      color: #dc3545;
      text-align: center;
      font-weight: bold;
      margin-top: 0.5rem;
    }
    .message {
      padding: 1rem;
      margin-bottom: 1rem;
      border-radius: 4px;
      background: #d4edda;
      color: #155724;
      text-align: center;
    }
    .message.error {
      background: #f8d7da;
      color: #721c24;
    }
    .modal-actions {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-top: 1.5rem;
    }
    .btn-cancel,
    .btn-confirm {
      padding: 1.5rem;
      font-size: 1.2rem;
      font-weight: bold;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      min-height: 60px;
    }
    .btn-cancel {
      background: #6c757d;
      color: white;
    }
    .btn-cancel:hover:not(:disabled) {
      background: #5a6268;
    }
    .btn-confirm {
      background: #28a745;
      color: white;
    }
    .btn-confirm:hover:not(:disabled) {
      background: #218838;
    }
    .btn-cancel:disabled,
    .btn-confirm:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `]
})
export class PaymentModalComponent implements OnInit {
  @Input() total: number = 0;
  @Output() paymentComplete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  paymentMethods = [
    { value: 'DINHEIRO' as PaymentMethod, label: 'Dinheiro' },
    { value: 'DEBITO' as PaymentMethod, label: 'Débito' },
    { value: 'CREDITO' as PaymentMethod, label: 'Crédito' },
    { value: 'PIX' as PaymentMethod, label: 'PIX' }
  ];

  selectedMethod: PaymentMethod | null = null;
  amountReceived: number = 0;
  change: number = 0;
  message = '';
  isError = false;
  processing = false;
  defaultCustomerId: string | null = null;

  constructor(
    private saleService: SaleService,
    private cartService: CartService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    // Load default consumer ID
    this.customerService.getDefaultConsumer().subscribe({
      next: (customer) => {
        this.defaultCustomerId = customer.id;
      },
      error: (err) => {
        console.error('Error loading default consumer:', err);
        this.showMessage('Erro ao carregar cliente padrão', true);
      }
    });

    // Listen for keyboard shortcuts
    document.addEventListener('keydown', this.handleKeyboard);
  }

  ngOnDestroy(): void {
    document.removeEventListener('keydown', this.handleKeyboard);
  }

  private handleKeyboard = (event: KeyboardEvent): void => {
    if (event.key === 'Escape') {
      this.onCancel();
    } else if (event.key === 'Enter' && this.canConfirm() && !this.processing) {
      this.confirmPayment();
    }
  };

  selectMethod(method: PaymentMethod): void {
    this.selectedMethod = method;
    if (method === 'DINHEIRO') {
      this.amountReceived = 0;
      this.change = 0;
    }
  }

  calculateChange(): void {
    this.change = this.amountReceived - this.total;
  }

  canConfirm(): boolean {
    if (!this.selectedMethod) return false;
    if (this.selectedMethod === 'DINHEIRO') {
      return this.amountReceived >= this.total;
    }
    return true;
  }

  confirmPayment(): void {
    if (!this.canConfirm() || this.processing || !this.defaultCustomerId) return;

    this.processing = true;
    this.message = '';

    const cart = this.cartService.getCart();
    const saleRequest: SaleRequest = {
      customer_id: this.defaultCustomerId,
      payment_method: this.selectedMethod!,
      items: cart.items.map(item => ({
        product_id: item.product_id,
        quantity: item.quantity,
        unit_price: item.unit_price
      })),
      discount: cart.discount || 0,
      amount_received: this.selectedMethod === 'DINHEIRO' ? this.amountReceived : this.total
    };

    this.saleService.createSale(saleRequest).subscribe({
      next: () => {
        this.showMessage('Venda finalizada com sucesso!');
        setTimeout(() => {
          this.paymentComplete.emit();
        }, 1500);
      },
      error: (err) => {
        console.error('Error creating sale:', err);
        this.showMessage('Erro ao processar venda. Tente novamente.', true);
        this.processing = false;
      }
    });
  }

  onCancel(): void {
    if (!this.processing) {
      this.cancel.emit();
    }
  }

  showMessage(msg: string, error = false): void {
    this.message = msg;
    this.isError = error;
  }
}
