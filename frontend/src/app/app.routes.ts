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
    path: 'estoque/locais',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/estoque/components/location-list/location-list.component').then(m => m.LocationListComponent)
  },
  {
    path: 'estoque/locais/novo',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/estoque/components/location-form/location-form.component').then(m => m.LocationFormComponent)
  },
  {
    path: 'estoque/locais/:id/editar',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/estoque/components/location-form/location-form.component').then(m => m.LocationFormComponent)
  },
  {
    path: 'clientes',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/vendas/components/customer-list/customer-list.component').then(m => m.CustomerListComponent)
  },
  {
    path: 'clientes/novo',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/vendas/components/customer-form/customer-form.component').then(m => m.CustomerFormComponent)
  },
  {
    path: 'clientes/:id/editar',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/vendas/components/customer-form/customer-form.component').then(m => m.CustomerFormComponent)
  },
  {
    path: 'pdv',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/pdv/components/pdv-layout/pdv-layout.component').then(m => m.PdvLayoutComponent)
  },
  {
    path: 'integracoes',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/integrations/mercadolivre-integration/mercadolivre-integration.component').then(m => m.MercadoLivreIntegrationComponent)
  },
  {
    path: 'integracoes/mercadolivre/importar',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/integrations/mercadolivre-import/mercadolivre-import.component').then(m => m.MercadoLivreImportComponent)
  },
  {
    path: 'integracoes/mercadolivre/publicar',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/integrations/mercadolivre-publish/mercadolivre-publish-wizard.component').then(m => m.MercadoLivrePublishWizardComponent)
  },
  {
    path: 'integracoes/mercadolivre/historico',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/integrations/mercadolivre-sync-history/mercadolivre-sync-history.component').then(m => m.MercadoLivreSyncHistoryComponent)
  },
  {
    path: 'integracoes/mercadolivre/pedidos',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/integrations/mercadolivre-orders/mercadolivre-orders.component').then(m => m.MercadoLivreOrdersComponent)
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
