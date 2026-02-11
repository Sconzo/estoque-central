import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SafetyMarginService, SafetyMarginRule } from '../services/safety-margin.service';
import { SafetyMarginModalComponent } from '../safety-margin-modal/safety-margin-modal.component';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';

/**
 * SafetyMarginConfigComponent - Configure safety stock margins
 * Story 5.7: Configurable Safety Stock Margin - AC7
 */
@Component({
  selector: 'app-safety-margin-config',
  standalone: true,
  imports: [CommonModule, FormsModule, SafetyMarginModalComponent],
  templateUrl: './safety-margin-config.component.html',
  styleUrls: ['./safety-margin-config.component.scss']
})
export class SafetyMarginConfigComponent implements OnInit {
  private safetyMarginService = inject(SafetyMarginService);
  private confirmDialog = inject(ConfirmDialogService);

  rules: SafetyMarginRule[] = [];
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  // Filters
  selectedMarketplace = '';
  marketplaces = ['', 'MERCADO_LIVRE'];

  // Modal state
  showModal = false;
  editingRule: SafetyMarginRule | null = null;

  ngOnInit() {
    this.loadRules();
  }

  loadRules() {
    this.loading = true;
    this.error = null;
    this.successMessage = null;

    const marketplaceFilter = this.selectedMarketplace || undefined;

    this.safetyMarginService.listRules(marketplaceFilter).subscribe({
      next: (rules) => {
        this.rules = rules;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar regras de margem de segurança';
        console.error('Error loading safety margin rules:', err);
        this.loading = false;
      }
    });
  }

  onMarketplaceFilterChange() {
    this.loadRules();
  }

  openCreateModal() {
    this.editingRule = null;
    this.showModal = true;
  }

  openEditModal(rule: SafetyMarginRule) {
    this.editingRule = rule;
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.editingRule = null;
  }

  onRuleSaved() {
    this.closeModal();
    this.loadRules();
    this.successMessage = 'Regra salva com sucesso!';
    setTimeout(() => {
      this.successMessage = null;
    }, 3000);
  }

  deleteRule(rule: SafetyMarginRule) {
    this.confirmDialog.confirmDanger({
      title: 'Deletar Regra',
      message: 'Tem certeza que deseja deletar esta regra de margem de segurança?'
    }).subscribe(confirmed => {
      if (!confirmed) return;

      this.loading = true;
      this.error = null;

      this.safetyMarginService.deleteRule(rule.id).subscribe({
        next: () => {
          this.successMessage = 'Regra deletada com sucesso!';
          this.loadRules();
          setTimeout(() => {
            this.successMessage = null;
          }, 3000);
        },
        error: (err) => {
          this.error = 'Erro ao deletar regra';
          console.error('Error deleting rule:', err);
          this.loading = false;
        }
      });
    });
  }

  getPriorityLabel(priority: string): string {
    switch (priority) {
      case 'PRODUCT':
        return '🎯 Produto Específico';
      case 'CATEGORY':
        return '📁 Categoria';
      case 'GLOBAL':
        return '🌐 Global (Marketplace)';
      default:
        return priority;
    }
  }

  getPriorityBadgeClass(priority: string): string {
    switch (priority) {
      case 'PRODUCT':
        return 'badge-product';
      case 'CATEGORY':
        return 'badge-category';
      case 'GLOBAL':
        return 'badge-global';
      default:
        return 'badge-default';
    }
  }

  getScopeName(rule: SafetyMarginRule): string {
    if (rule.priority === 'PRODUCT') {
      return rule.productName || rule.productId || '-';
    } else if (rule.priority === 'CATEGORY') {
      return rule.categoryName || rule.categoryId || '-';
    } else {
      return 'Todos os produtos';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('pt-BR');
  }

  getMarginBadgeClass(margin: number): string {
    if (margin >= 90) {
      return 'margin-high';
    } else if (margin >= 70) {
      return 'margin-medium';
    } else {
      return 'margin-low';
    }
  }
}
