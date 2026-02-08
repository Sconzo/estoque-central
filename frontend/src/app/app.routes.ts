import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Rotas públicas (sem layout)
  {
    path: 'component-showcase',
    loadComponent: () => import('./component-showcase/component-showcase.component').then(m => m.ComponentShowcaseComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'create-company',
    loadComponent: () => import('./features/company/create-company/create-company.component').then(m => m.CreateCompanyComponent)
  },
  {
    path: 'select-company',
    loadComponent: () => import('./features/company/select-company/select-company.component').then(m => m.SelectCompanyComponent)
  },
  {
    path: '403',
    loadComponent: () => import('./features/error/forbidden/forbidden.component').then(m => m.ForbiddenComponent)
  },

  // Rotas autenticadas (com MainLayout)
  {
    path: '',
    canActivate: [AuthGuard],
    loadComponent: () => import('./shared/layouts/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'categorias',
        loadComponent: () => import('./features/produtos/components/category-tree/category-tree.component').then(m => m.CategoryTreeComponent)
      },
      {
        path: 'produtos',
        loadComponent: () => import('./features/produtos/components/product-list/product-list.component').then(m => m.ProductListComponent)
      },
      {
        path: 'produtos/novo',
        loadComponent: () => import('./features/produtos/components/product-form/product-form.component').then(m => m.ProductFormComponent)
      },
      {
        path: 'produtos/importar',
        loadComponent: () => import('./features/produtos/components/product-import/product-import.component').then(m => m.ProductImportComponent)
      },
      {
        path: 'produtos/:id',
        loadComponent: () => import('./features/produtos/components/product-detail/product-detail.component').then(m => m.ProductDetailComponent)
      },
      {
        path: 'produtos/:id/editar',
        loadComponent: () => import('./features/produtos/components/product-form/product-form.component').then(m => m.ProductFormComponent)
      },
      {
        path: 'estoque/locais',
        loadComponent: () => import('./features/estoque/components/location-list/location-list.component').then(m => m.LocationListComponent)
      },
      {
        path: 'estoque/locais/novo',
        loadComponent: () => import('./features/estoque/components/location-form/location-form.component').then(m => m.LocationFormComponent)
      },
      {
        path: 'estoque/locais/:id/editar',
        loadComponent: () => import('./features/estoque/components/location-form/location-form.component').then(m => m.LocationFormComponent)
      },
      {
        path: 'clientes',
        loadComponent: () => import('./features/vendas/components/customer-list/customer-list.component').then(m => m.CustomerListComponent)
      },
      {
        path: 'clientes/novo',
        loadComponent: () => import('./features/vendas/components/customer-form/customer-form.component').then(m => m.CustomerFormComponent)
      },
      {
        path: 'clientes/:id/editar',
        loadComponent: () => import('./features/vendas/components/customer-form/customer-form.component').then(m => m.CustomerFormComponent)
      },
      // Usuários/Perfis
      {
        path: 'usuarios/profiles',
        loadComponent: () => import('./features/auth/usuarios/components/profile-list/profile-list.component').then(m => m.ProfileListComponent)
      },
      {
        path: 'usuarios/profiles/new',
        loadComponent: () => import('./features/auth/usuarios/components/profile-form/profile-form.component').then(m => m.ProfileFormComponent)
      },
      {
        path: 'usuarios/profiles/edit/:id',
        loadComponent: () => import('./features/auth/usuarios/components/profile-form/profile-form.component').then(m => m.ProfileFormComponent)
      },
      {
        path: 'usuarios/profiles/:id/roles',
        loadComponent: () => import('./features/auth/usuarios/components/role-assignment/role-assignment.component').then(m => m.RoleAssignmentComponent)
      },
      {
        path: 'pdv',
        loadComponent: () => import('./features/pdv/components/pdv-layout/pdv-layout.component').then(m => m.PdvLayoutComponent)
      },
      {
        path: 'integracoes',
        loadComponent: () => import('./features/integrations/mercadolivre-integration/mercadolivre-integration.component').then(m => m.MercadoLivreIntegrationComponent)
      },
      {
        path: 'integracoes/mercadolivre/importar',
        loadComponent: () => import('./features/integrations/mercadolivre-import/mercadolivre-import.component').then(m => m.MercadoLivreImportComponent)
      },
      {
        path: 'integracoes/mercadolivre/publicar',
        loadComponent: () => import('./features/integrations/mercadolivre-publish/mercadolivre-publish-wizard.component').then(m => m.MercadoLivrePublishWizardComponent)
      },
      {
        path: 'integracoes/mercadolivre/historico',
        loadComponent: () => import('./features/integrations/mercadolivre-sync-history/mercadolivre-sync-history.component').then(m => m.MercadoLivreSyncHistoryComponent)
      },
      {
        path: 'integracoes/mercadolivre/pedidos',
        loadComponent: () => import('./features/integrations/mercadolivre-orders/mercadolivre-orders.component').then(m => m.MercadoLivreOrdersComponent)
      },
      {
        path: 'integracoes/mercadolivre/margem-seguranca',
        loadComponent: () => import('./features/integrations/safety-margin-config/safety-margin-config.component').then(m => m.SafetyMarginConfigComponent)
      },
      // Settings routes (Epic 10 - RBAC)
      {
        path: 'settings/collaborators',
        canActivate: [RoleGuard],
        data: { requiredRole: 'ADMIN' },
        loadComponent: () => import('./features/collaborators/collaborators.component').then(m => m.CollaboratorsComponent)
      },
      {
        path: 'settings/company',
        canActivate: [RoleGuard],
        data: { requiredRole: 'ADMIN' },
        loadComponent: () => import('./features/company/company-settings.component').then(m => m.CompanySettingsComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  }
];
