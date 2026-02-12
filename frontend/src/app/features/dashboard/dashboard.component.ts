import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin } from 'rxjs';
import { MetricCardComponent } from '../../shared/components/feedback/metric-card/metric-card.component';
import { DashboardService } from './services/dashboard.service';
import { CriticalStockProduct, TopProduct, RecentActivity } from '../../shared/models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MetricCardComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);

  loading = signal(true);
  error = signal<string | null>(null);

  stats = signal({
    totalProdutos: 0,
    estoqueTotal: 0,
    alertasEstoque: 0,
    vendasHoje: 0,
    vendasMes: 0,
    clientesAtivos: 0,
    pedidosPendentes: 0
  });

  produtosEstoqueBaixo = signal<CriticalStockProduct[]>([]);
  produtosMaisVendidos = signal<TopProduct[]>([]);
  atividadesRecentes = signal<RecentActivity[]>([]);

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      summary: this.dashboardService.getSummary(),
      totalProducts: this.dashboardService.getTotalActiveProducts(),
      criticalStock: this.dashboardService.getCriticalStock(10),
      topProducts: this.dashboardService.getTopProducts(10),
      monthlySales: this.dashboardService.getMonthlySales(),
      activeCustomers: this.dashboardService.getActiveCustomersCount(),
      recentActivities: this.dashboardService.getRecentActivities(10)
    }).subscribe({
      next: (data) => {
        const s = data.summary;
        this.stats.set({
          totalProdutos: data.totalProducts.count,
          estoqueTotal: s.totalInventoryQuantity,
          alertasEstoque: s.outOfStockCount + s.criticalStockCount + s.lowStockCount,
          vendasHoje: s.dailyOrderCount,
          vendasMes: data.monthlySales.orderCount,
          clientesAtivos: data.activeCustomers.count,
          pedidosPendentes: s.pendingOrdersCount
        });

        this.produtosEstoqueBaixo.set(data.criticalStock);
        this.produtosMaisVendidos.set(data.topProducts);
        this.atividadesRecentes.set(data.recentActivities);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar dados do dashboard:', err);
        this.error.set('Erro ao carregar dados do dashboard. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  getAlertClass(alertLevel: string): string {
    switch (alertLevel) {
      case 'OUT_OF_STOCK': return 'alert-critical';
      case 'CRITICAL': return 'alert-warning';
      case 'LOW': return 'alert-ok';
      default: return 'alert-ok';
    }
  }

  getAlertLabel(alertLevel: string): string {
    switch (alertLevel) {
      case 'OUT_OF_STOCK': return 'Esgotado';
      case 'CRITICAL': return 'Crítico';
      case 'LOW': return 'Baixo';
      default: return 'OK';
    }
  }

  formatTimestamp(timestamp: string): string {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMin = Math.floor(diffMs / 60000);

    if (diffMin < 1) return 'Agora';
    if (diffMin < 60) return `${diffMin} min atrás`;

    const diffHours = Math.floor(diffMin / 60);
    if (diffHours < 24) return `${diffHours}h atrás`;

    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays}d atrás`;
  }
}
