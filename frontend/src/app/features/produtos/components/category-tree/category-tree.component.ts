import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../services/category.service';
import { Category, CategoryTreeNode, CategoryCreateRequest } from '../../models/category.model';

/**
 * CategoryTreeComponent - Hierarchical category tree management
 *
 * Features:
 * - Hierarchical tree display with unlimited depth
 * - Expand/collapse nodes
 * - Add child category
 * - Edit category inline
 * - Delete category (soft delete)
 * - Breadcrumb path display
 */
@Component({
  selector: 'app-category-tree',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-tree.component.html',
  styleUrls: ['./category-tree.component.scss']
})
export class CategoryTreeComponent implements OnInit {
  tree: CategoryTreeNode[] = [];
  loading = false;
  error: string | null = null;

  // Add/Edit modal state
  showModal = false;
  modalMode: 'create' | 'edit' | 'create-child' = 'create';
  modalTitle = '';
  currentCategory: Category | null = null;
  parentCategory: Category | null = null;

  // Form data
  formData: CategoryCreateRequest = {
    name: '',
    description: '',
    parentId: undefined
  };

  // Breadcrumb
  breadcrumb: Category[] = [];
  selectedCategoryId: string | null = null;

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadTree();
  }

  /**
   * Loads category tree from backend
   */
  loadTree(): void {
    this.loading = true;
    this.error = null;

    this.categoryService.getTree().subscribe({
      next: (tree) => {
        this.tree = tree;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar categorias: ' + (err.message || 'Erro desconhecido');
        this.loading = false;
        console.error('Error loading category tree:', err);
      }
    });
  }

  /**
   * Toggles node expansion
   */
  toggleNode(node: CategoryTreeNode): void {
    node.expanded = !node.expanded;
  }

  /**
   * Opens modal to create root category
   */
  openCreateRootModal(): void {
    this.modalMode = 'create';
    this.modalTitle = 'Nova Categoria Raiz';
    this.currentCategory = null;
    this.parentCategory = null;
    this.formData = {
      name: '',
      description: '',
      parentId: undefined
    };
    this.showModal = true;
  }

  /**
   * Opens modal to create child category
   */
  openCreateChildModal(parent: Category): void {
    this.modalMode = 'create-child';
    this.modalTitle = `Nova Subcategoria de "${parent.name}"`;
    this.currentCategory = null;
    this.parentCategory = parent;
    this.formData = {
      name: '',
      description: '',
      parentId: parent.id
    };
    this.showModal = true;
  }

  /**
   * Opens modal to edit category
   */
  openEditModal(category: Category): void {
    this.modalMode = 'edit';
    this.modalTitle = `Editar Categoria "${category.name}"`;
    this.currentCategory = category;
    this.parentCategory = null;
    this.formData = {
      name: category.name,
      description: category.description || '',
      parentId: category.parentId
    };
    this.showModal = true;
  }

  /**
   * Closes modal
   */
  closeModal(): void {
    this.showModal = false;
    this.currentCategory = null;
    this.parentCategory = null;
    this.formData = {
      name: '',
      description: '',
      parentId: undefined
    };
  }

  /**
   * Saves category (create or update)
   */
  saveCategory(): void {
    if (!this.formData.name || this.formData.name.trim() === '') {
      alert('Nome da categoria é obrigatório');
      return;
    }

    this.loading = true;

    if (this.modalMode === 'edit' && this.currentCategory) {
      // Update existing category
      this.categoryService.update(this.currentCategory.id, this.formData).subscribe({
        next: () => {
          this.closeModal();
          this.loadTree();
        },
        error: (err) => {
          alert('Erro ao atualizar categoria: ' + (err.error?.message || err.message || 'Erro desconhecido'));
          this.loading = false;
          console.error('Error updating category:', err);
        }
      });
    } else {
      // Create new category
      this.categoryService.create(this.formData).subscribe({
        next: () => {
          this.closeModal();
          this.loadTree();
        },
        error: (err) => {
          alert('Erro ao criar categoria: ' + (err.error?.message || err.message || 'Erro desconhecido'));
          this.loading = false;
          console.error('Error creating category:', err);
        }
      });
    }
  }

  /**
   * Deletes category with confirmation
   */
  deleteCategory(category: Category, event: Event): void {
    event.stopPropagation(); // Prevent node toggle

    const confirmMessage = `Tem certeza que deseja excluir a categoria "${category.name}"?\n\nEsta ação marcará a categoria como inativa (soft delete).`;

    if (!confirm(confirmMessage)) {
      return;
    }

    this.loading = true;

    this.categoryService.delete(category.id).subscribe({
      next: () => {
        this.loadTree();
      },
      error: (err) => {
        alert('Erro ao deletar categoria: ' + (err.error?.message || err.message || 'Erro desconhecido'));
        this.loading = false;
        console.error('Error deleting category:', err);
      }
    });
  }

  /**
   * Loads breadcrumb path for selected category
   */
  loadBreadcrumb(categoryId: string): void {
    this.selectedCategoryId = categoryId;

    this.categoryService.getPath(categoryId).subscribe({
      next: (path) => {
        this.breadcrumb = path;
      },
      error: (err) => {
        console.error('Error loading breadcrumb:', err);
        this.breadcrumb = [];
      }
    });
  }

  /**
   * Clears breadcrumb selection
   */
  clearBreadcrumb(): void {
    this.selectedCategoryId = null;
    this.breadcrumb = [];
  }

  /**
   * Navigates to category in breadcrumb
   */
  navigateToBreadcrumb(category: Category): void {
    this.selectedCategoryId = category.id;
    this.loadBreadcrumb(category.id);
  }
}
