import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MetricCardComponent } from '../../shared/components/feedback/metric-card/metric-card.component';

/**
 * DashboardComponent - Página inicial com resumo e métricas
 *
 * Exibe:
 * - Cards com estatísticas principais
 * - Alertas de estoque baixo
 * - Produtos mais vendidos
 * - Resumo de vendas
 */
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
  loading = signal(true);

  // Estatísticas principais
  stats = signal({
    totalProdutos: 0,
    produtosAtivos: 0,
    estoqueTotal: 0,
    alertasEstoque: 0,
    vendasHoje: 0,
    vendasMes: 0,
    clientesAtivos: 0,
    pedidosPendentes: 0
  });

  // Produtos com estoque baixo
  produtosEstoqueBaixo = signal<any[]>([]);

  // Produtos mais vendidos
  produtosMaisVendidos = signal<any[]>([]);

  // Atividades recentes
  atividadesRecentes = signal<any[]>([]);

  ngOnInit() {
    this.carregarDados();
  }

  async carregarDados() {
    try {
      this.loading.set(true);

      // TODO: Substituir por chamadas reais de API
      // Simulação de dados para demonstração
      await this.simularCarregamento();

      this.stats.set({
        totalProdutos: 156,
        produtosAtivos: 142,
        estoqueTotal: 3420,
        alertasEstoque: 12,
        vendasHoje: 8,
        vendasMes: 234,
        clientesAtivos: 89,
        pedidosPendentes: 5
      });

      this.produtosEstoqueBaixo.set([
        { id: 1, nome: 'Produto A', estoque: 3, minimo: 10, codigo: 'PROD-001' },
        { id: 2, nome: 'Produto B', estoque: 1, minimo: 5, codigo: 'PROD-002' },
        { id: 3, nome: 'Produto C', estoque: 5, minimo: 15, codigo: 'PROD-003' }
      ]);

      this.produtosMaisVendidos.set([
        { id: 1, nome: 'Produto X', vendas: 45, codigo: 'PROD-X' },
        { id: 2, nome: 'Produto Y', vendas: 38, codigo: 'PROD-Y' },
        { id: 3, nome: 'Produto Z', vendas: 32, codigo: 'PROD-Z' }
      ]);

      this.atividadesRecentes.set([
        { tipo: 'venda', descricao: 'Venda #1234 finalizada', tempo: '5 min atrás' },
        { tipo: 'estoque', descricao: 'Estoque atualizado: Produto A', tempo: '15 min atrás' },
        { tipo: 'cliente', descricao: 'Novo cliente cadastrado', tempo: '1 hora atrás' }
      ]);

    } catch (error) {
      console.error('Erro ao carregar dados do dashboard:', error);
    } finally {
      this.loading.set(false);
    }
  }

  private simularCarregamento(): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, 800));
  }

  getAlertClass(estoque: number, minimo: number): string {
    const percentual = (estoque / minimo) * 100;
    if (percentual < 30) return 'alert-critical';
    if (percentual < 60) return 'alert-warning';
    return 'alert-ok';
  }
}
