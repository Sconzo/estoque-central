import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chip';
import { Observable, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { SalesOrderService, PaymentTerms, SalesOrderStatus } from '../services/sales-order.service';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';

/**
 * SalesOrderFormComponent - Create/Edit B2B sales orders
 * Story 4.5: Sales Order B2B Interface
 *
 * Features:
 * - Customer autocomplete
 * - Location selection
 * - Dynamic items table with product autocomplete
 * - Real-time stock availability check
 * - Save as draft or confirm order
 */
@Component({
  selector: 'app-sales-order-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatCardModule,
    MatSnackBarModule,
    MatAutocompleteModule,
    MatChipsModule
  ],
  templateUrl: './sales-order-form.component.html',
  styleUrls: ['./sales-order-form.component.css']
})
export class SalesOrderFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private salesOrderService = inject(SalesOrderService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private confirmDialog = inject(ConfirmDialogService);

  orderForm!: FormGroup;
  loading = signal(false);
  isEditMode = signal(false);
  orderId: string | null = null;
  orderStatus = signal<SalesOrderStatus | null>(null);

  // Options
  paymentTermsOptions = [
    { value: PaymentTerms.A_VISTA, label: 'À Vista' },
    { value: PaymentTerms.DIAS_7, label: '7 Dias' },
    { value: PaymentTerms.DIAS_14, label: '14 Dias' },
    { value: PaymentTerms.DIAS_30, label: '30 Dias' },
    { value: PaymentTerms.DIAS_60, label: '60 Dias' },
    { value: PaymentTerms.DIAS_90, label: '90 Dias' }
  ];

  // Mock data for demo - in real app, these would come from APIs
  customers = signal([
    { id: '1', name: 'ABC Distribuidora LTDA', document: '12.345.678/0001-99' },
    { id: '2', name: 'XYZ Comércio ME', document: '98.765.432/0001-11' },
    { id: '3', name: 'João Silva', document: '123.456.789-00' }
  ]);

  locations = signal([
    { id: '1', name: 'Depósito Principal' },
    { id: '2', name: 'Loja Centro' },
    { id: '3', name: 'CD Norte' }
  ]);

  products = signal([
    { id: '1', name: 'Produto A', sku: 'PROD-001', price: 50.00 },
    { id: '2', name: 'Produto B', sku: 'PROD-002', price: 75.00 },
    { id: '3', name: 'Produto C', sku: 'PROD-003', price: 100.00 }
  ]);

  filteredCustomers = computed(() => this.customers());
  filteredProducts = computed(() => this.products());

  // Items table columns
  itemsColumns = ['product', 'quantity', 'unitPrice', 'total', 'stock', 'actions'];

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  initForm(): void {
    this.orderForm = this.fb.group({
      customerId: ['', Validators.required],
      stockLocationId: ['', Validators.required],
      orderDate: [new Date(), Validators.required],
      deliveryDateExpected: [''],
      paymentTerms: [''],
      notes: [''],
      items: this.fb.array([])
    });

    // Add first item row
    this.addItem();
  }

  checkEditMode(): void {
    this.orderId = this.route.snapshot.paramMap.get('id');
    if (this.orderId && this.orderId !== 'new') {
      this.isEditMode.set(true);
      this.loadOrder(this.orderId);
    }
  }

  loadOrder(id: string): void {
    this.loading.set(true);
    this.salesOrderService.getSalesOrderById(id).subscribe({
      next: (order) => {
        this.orderStatus.set(order.status);

        // Check if order can be edited
        if (order.status !== SalesOrderStatus.DRAFT) {
          this.snackBar.open('Apenas pedidos em rascunho podem ser editados', 'Fechar', { duration: 3000 });
          this.router.navigate(['/sales-orders']);
          return;
        }

        // Populate form
        this.orderForm.patchValue({
          customerId: order.customer?.id,
          stockLocationId: order.location?.id,
          orderDate: new Date(order.orderDate),
          deliveryDateExpected: order.deliveryDateExpected ? new Date(order.deliveryDateExpected) : null,
          paymentTerms: order.paymentTerms,
          notes: order.notes
        });

        // Clear and repopulate items
        this.items.clear();
        order.items?.forEach(item => {
          this.addItem({
            productId: item.productId,
            variantId: item.variantId,
            quantity: item.quantityOrdered,
            unitPrice: item.unitPrice
          });
        });

        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading order:', error);
        this.snackBar.open('Erro ao carregar pedido', 'Fechar', { duration: 3000 });
        this.router.navigate(['/sales-orders']);
      }
    });
  }

  get items(): FormArray {
    return this.orderForm.get('items') as FormArray;
  }

  addItem(data?: any): void {
    const itemGroup = this.fb.group({
      productId: [data?.productId || '', Validators.required],
      variantId: [data?.variantId || null],
      quantity: [data?.quantity || 1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [data?.unitPrice || 0, [Validators.required, Validators.min(0)]],
      stockAvailable: [0],
      stockReserved: [0],
      stockForSale: [0]
    });

    // Watch for product/location changes to check stock
    itemGroup.get('productId')?.valueChanges.subscribe(() => {
      this.checkItemStock(itemGroup);
    });

    this.items.push(itemGroup);
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    } else {
      this.snackBar.open('O pedido deve ter pelo menos um item', 'Fechar', { duration: 3000 });
    }
  }

  checkItemStock(itemGroup: FormGroup): void {
    const productId = itemGroup.get('productId')?.value;
    const variantId = itemGroup.get('variantId')?.value;
    const locationId = this.orderForm.get('stockLocationId')?.value;

    if (!productId || !locationId) {
      return;
    }

    this.salesOrderService.getStockAvailability(productId, variantId, locationId).subscribe({
      next: (stock) => {
        itemGroup.patchValue({
          stockAvailable: stock.available,
          stockReserved: stock.reserved,
          stockForSale: stock.forSale
        }, { emitEvent: false });
      },
      error: (error) => {
        console.error('Error checking stock:', error);
      }
    });
  }

  checkAllItemsStock(): void {
    const locationId = this.orderForm.get('stockLocationId')?.value;
    if (!locationId) {
      return;
    }

    this.items.controls.forEach(itemGroup => {
      this.checkItemStock(itemGroup as FormGroup);
    });
  }

  getItemTotal(item: FormGroup): number {
    const quantity = item.get('quantity')?.value || 0;
    const unitPrice = item.get('unitPrice')?.value || 0;
    return quantity * unitPrice;
  }

  getOrderTotal(): number {
    return this.items.controls.reduce((total, item) => {
      return total + this.getItemTotal(item as FormGroup);
    }, 0);
  }

  getProductName(productId: string): string {
    const product = this.products().find(p => p.id === productId);
    return product?.name || '';
  }

  hasStockIssues(item: FormGroup): boolean {
    const quantity = item.get('quantity')?.value || 0;
    const stockForSale = item.get('stockForSale')?.value || 0;
    return quantity > stockForSale;
  }

  saveDraft(): void {
    if (this.orderForm.invalid) {
      this.snackBar.open('Preencha todos os campos obrigatórios', 'Fechar', { duration: 3000 });
      return;
    }

    this.loading.set(true);
    const formValue = this.prepareFormData();

    const request$ = this.isEditMode() && this.orderId
      ? this.salesOrderService.updateSalesOrder(this.orderId, formValue)
      : this.salesOrderService.createSalesOrder(formValue);

    request$.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEditMode() ? 'Pedido atualizado com sucesso!' : 'Pedido criado com sucesso!',
          'Fechar',
          { duration: 3000 }
        );
        this.router.navigate(['/sales-orders']);
      },
      error: (error) => {
        console.error('Error saving order:', error);
        this.snackBar.open('Erro ao salvar pedido', 'Fechar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  confirmOrder(): void {
    if (this.orderForm.invalid) {
      this.snackBar.open('Preencha todos os campos obrigatórios', 'Fechar', { duration: 3000 });
      return;
    }

    // Check stock for all items
    const hasStockIssue = this.items.controls.some(item => this.hasStockIssues(item as FormGroup));
    if (hasStockIssue) {
      this.confirmDialog.confirm({
        title: 'Estoque Insuficiente',
        message: 'Alguns itens têm estoque insuficiente. Deseja continuar mesmo assim?',
        type: 'warning'
      }).subscribe(confirmed => {
        if (!confirmed) return;
        this.doConfirmOrder();
      });
    } else {
      this.doConfirmOrder();
    }
  }

  private doConfirmOrder(): void {
    this.loading.set(true);
    const formValue = this.prepareFormData();

    // First create/update, then confirm
    const saveRequest$ = this.isEditMode() && this.orderId
      ? this.salesOrderService.updateSalesOrder(this.orderId, formValue)
      : this.salesOrderService.createSalesOrder(formValue);

    saveRequest$.pipe(
      switchMap(order => this.salesOrderService.confirmSalesOrder(order.id))
    ).subscribe({
      next: () => {
        this.snackBar.open('Pedido confirmado com sucesso!', 'Fechar', { duration: 3000 });
        this.router.navigate(['/sales-orders']);
      },
      error: (error) => {
        console.error('Error confirming order:', error);
        if (error.status === 409) {
          this.snackBar.open('Estoque insuficiente para confirmar o pedido', 'Fechar', { duration: 5000 });
        } else {
          this.snackBar.open('Erro ao confirmar pedido', 'Fechar', { duration: 3000 });
        }
        this.loading.set(false);
      }
    });
  }

  prepareFormData(): any {
    const formValue = this.orderForm.value;

    return {
      customerId: formValue.customerId,
      stockLocationId: formValue.stockLocationId,
      orderDate: this.formatDate(formValue.orderDate),
      deliveryDateExpected: formValue.deliveryDateExpected ? this.formatDate(formValue.deliveryDateExpected) : undefined,
      paymentTerms: formValue.paymentTerms,
      notes: formValue.notes,
      items: formValue.items.map((item: any) => ({
        productId: item.productId || null,
        variantId: item.variantId || null,
        quantity: item.quantity,
        unitPrice: item.unitPrice
      }))
    };
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  cancel(): void {
    this.confirmDialog.confirm({
      title: 'Descartar Alterações',
      message: 'Descartar alterações?',
      type: 'warning'
    }).subscribe(confirmed => {
      if (!confirmed) return;
      this.router.navigate(['/sales-orders']);
    });
  }
}
