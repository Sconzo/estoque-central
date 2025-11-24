import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chip';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { SalesOrderService, SalesOrderResponse, SalesOrderStatus } from '../services/sales-order.service';

/**
 * SalesOrderListComponent - List and manage B2B sales orders
 * Story 4.5: Sales Order B2B Interface
 */
@Component({
  selector: 'app-sales-order-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSelectModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule
  ],
  templateUrl: './sales-order-list.component.html',
  styleUrls: ['./sales-order-list.component.css']
})
export class SalesOrderListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private salesOrderService = inject(SalesOrderService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  filterForm!: FormGroup;
  salesOrders = signal<SalesOrderResponse[]>([]);
  loading = signal(false);
  totalElements = signal(0);
  pageSize = 20;
  pageIndex = 0;

  displayedColumns: string[] = [
    'orderNumber',
    'customer',
    'orderDate',
    'deliveryDate',
    'status',
    'totalAmount',
    'actions'
  ];

  statusOptions = [
    { value: '', label: 'Todos' },
    { value: 'DRAFT', label: 'Rascunho' },
    { value: 'CONFIRMED', label: 'Confirmado' },
    { value: 'INVOICED', label: 'Faturado' },
    { value: 'CANCELLED', label: 'Cancelado' }
  ];

  ngOnInit(): void {
    this.initFilterForm();
    this.loadSalesOrders();
  }

  initFilterForm(): void {
    this.filterForm = this.fb.group({
      orderNumber: [''],
      status: [''],
      orderDateFrom: [''],
      orderDateTo: ['']
    });
  }

  loadSalesOrders(): void {
    this.loading.set(true);
    const formValue = this.filterForm.value;

    this.salesOrderService.getSalesOrders({
      order_number: formValue.orderNumber || undefined,
      status: formValue.status || undefined,
      order_date_from: formValue.orderDateFrom ? this.formatDate(formValue.orderDateFrom) : undefined,
      order_date_to: formValue.orderDateTo ? this.formatDate(formValue.orderDateTo) : undefined,
      page: this.pageIndex,
      size: this.pageSize,
      sort: ['dataCriacao', 'desc']
    }).subscribe({
      next: (response) => {
        this.salesOrders.set(response.content);
        this.totalElements.set(response.totalElements);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading sales orders:', error);
        this.snackBar.open('Erro ao carregar pedidos de venda', 'Fechar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadSalesOrders();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.loadSalesOrders();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSalesOrders();
  }

  getStatusLabel(status: SalesOrderStatus): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'Rascunho',
      'CONFIRMED': 'Confirmado',
      'INVOICED': 'Faturado',
      'CANCELLED': 'Cancelado'
    };
    return statusMap[status] || status;
  }

  getStatusColor(status: SalesOrderStatus): string {
    const colorMap: { [key: string]: string } = {
      'DRAFT': 'gray',
      'CONFIRMED': 'primary',
      'INVOICED': 'accent',
      'CANCELLED': 'warn'
    };
    return colorMap[status] || '';
  }

  viewOrder(order: SalesOrderResponse): void {
    this.router.navigate(['/sales-orders', order.id]);
  }

  editOrder(order: SalesOrderResponse): void {
    if (order.status === SalesOrderStatus.DRAFT) {
      this.router.navigate(['/sales-orders', order.id, 'edit']);
    } else {
      this.snackBar.open('Apenas pedidos em rascunho podem ser editados', 'Fechar', { duration: 3000 });
    }
  }

  confirmOrder(order: SalesOrderResponse): void {
    if (order.status !== SalesOrderStatus.DRAFT) {
      this.snackBar.open('Apenas pedidos em rascunho podem ser confirmados', 'Fechar', { duration: 3000 });
      return;
    }

    if (confirm(`Confirmar pedido ${order.orderNumber}? Esta ação validará o estoque disponível.`)) {
      this.salesOrderService.confirmSalesOrder(order.id).subscribe({
        next: () => {
          this.snackBar.open('Pedido confirmado com sucesso!', 'Fechar', { duration: 3000 });
          this.loadSalesOrders();
        },
        error: (error) => {
          console.error('Error confirming order:', error);
          if (error.status === 409) {
            this.snackBar.open('Estoque insuficiente para confirmar o pedido', 'Fechar', { duration: 5000 });
          } else {
            this.snackBar.open('Erro ao confirmar pedido', 'Fechar', { duration: 3000 });
          }
        }
      });
    }
  }

  cancelOrder(order: SalesOrderResponse): void {
    if (order.status === SalesOrderStatus.INVOICED || order.status === SalesOrderStatus.CANCELLED) {
      this.snackBar.open('Este pedido não pode ser cancelado', 'Fechar', { duration: 3000 });
      return;
    }

    if (confirm(`Cancelar pedido ${order.orderNumber}?`)) {
      this.salesOrderService.cancelSalesOrder(order.id).subscribe({
        next: () => {
          this.snackBar.open('Pedido cancelado com sucesso!', 'Fechar', { duration: 3000 });
          this.loadSalesOrders();
        },
        error: (error) => {
          console.error('Error cancelling order:', error);
          this.snackBar.open('Erro ao cancelar pedido', 'Fechar', { duration: 3000 });
        }
      });
    }
  }

  createNewOrder(): void {
    this.router.navigate(['/sales-orders/new']);
  }

  canEdit(order: SalesOrderResponse): boolean {
    return order.status === SalesOrderStatus.DRAFT;
  }

  canConfirm(order: SalesOrderResponse): boolean {
    return order.status === SalesOrderStatus.DRAFT;
  }

  canCancel(order: SalesOrderResponse): boolean {
    return order.status === SalesOrderStatus.DRAFT || order.status === SalesOrderStatus.CONFIRMED;
  }
}
