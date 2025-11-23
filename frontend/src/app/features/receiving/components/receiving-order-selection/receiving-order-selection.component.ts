import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { ReceivingService, ReceivingOrderSummary } from '../../services/receiving.service';

@Component({
  selector: 'app-receiving-order-selection',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule
  ],
  templateUrl: './receiving-order-selection.component.html',
  styleUrls: ['./receiving-order-selection.component.scss']
})
export class ReceivingOrderSelectionComponent implements OnInit {
  orders: ReceivingOrderSummary[] = [];
  loading: boolean = false;
  error: string | null = null;

  // TODO: Get from auth service
  private tenantId = '00000000-0000-0000-0000-000000000001';

  constructor(
    private receivingService: ReceivingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPendingOrders();
  }

  loadPendingOrders(): void {
    this.loading = true;
    this.error = null;

    this.receivingService.getPendingOrders(this.tenantId).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading pending orders:', err);
        this.error = 'Erro ao carregar ordens de compra';
        this.loading = false;
      }
    });
  }

  onRefresh(): void {
    this.loadPendingOrders();
  }

  startReceiving(order: ReceivingOrderSummary): void {
    // Navigate to scanning screen
    this.router.navigate(['/receiving/scan', order.id]);
  }

  getProgressPercentage(order: ReceivingOrderSummary): number {
    if (order.itemsSummary.totalItems === 0) {
      return 0;
    }
    return (order.itemsSummary.totalReceived / order.itemsSummary.totalItems) * 100;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pt-BR').format(date);
  }
}
