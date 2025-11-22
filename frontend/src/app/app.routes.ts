import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '403',
    loadComponent: () => import('./features/error/forbidden/forbidden.component').then(m => m.ForbiddenComponent)
  },
  {
    path: 'categorias',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/produtos/components/category-tree/category-tree.component').then(m => m.CategoryTreeComponent)
  },
  {
    path: 'produtos',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/produtos/components/product-list/product-list.component').then(m => m.ProductListComponent)
  },
  {
    path: 'produtos/novo',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/produtos/components/product-form/product-form.component').then(m => m.ProductFormComponent)
  },
  {
    path: 'produtos/importar',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/produtos/components/product-import/product-import.component').then(m => m.ProductImportComponent)
  },
  {
    path: 'produtos/:id/editar',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/produtos/components/product-form/product-form.component').then(m => m.ProductFormComponent)
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () => import('./app.component').then(m => m.AppComponent)
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  }
];
