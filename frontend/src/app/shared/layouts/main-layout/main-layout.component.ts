import { Component, OnInit, ViewChild, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { MatSidenavModule, MatSidenav } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { Observable } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';
import { UserAvatarMenuComponent } from '../../components/user-avatar-menu/user-avatar-menu.component';
import { TenantService, Company } from '../../../core/services/tenant.service';

/**
 * MainLayoutComponent - Main application shell with responsive navigation
 * Story UX-6: Navigation Responsiva
 *
 * Features:
 * - Desktop: Permanent sidebar (mode="side" opened) - Full width content
 * - Mobile (Handset): Hamburger menu (mode="over") + Bottom nav
 * - Content uses 100% available width
 * - Bottom navigation for mobile (5 main items)
 * - BreakpointObserver detects real mobile devices
 * - Skip links for accessibility
 */
@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatDividerModule,
    UserAvatarMenuComponent
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent implements OnInit {
  @ViewChild('drawer') drawer!: MatSidenav;

  title = 'Estoque Central';
  currentUser = signal<any>(null);
  currentCompany = signal<Company | null>(null);

  // Responsive detection
  isHandset$!: Observable<boolean>;

  // Tracks which parent menus are expanded
  expandedMenus = new Set<string>();

  // Navigation menu items
  menuItems = [
    {
      icon: 'dashboard',
      label: 'Dashboard',
      route: '/dashboard'
    },
    {
      icon: 'inventory',
      label: 'Produtos',
      route: '/produtos',
      children: [
        { label: 'Lista de Produtos', route: '/produtos' },
        { label: 'Categorias', route: '/categorias' },
        { label: 'Importar Produtos', route: '/produtos/importar' }
      ]
    },
    {
      icon: 'warehouse',
      label: 'Estoque',
      route: '/estoque',
      children: [
        { label: 'Locais', route: '/estoque/locais' },
        { label: 'Transferências', route: '/estoque/transferencias' }
      ]
    },
    {
      icon: 'shopping_bag',
      label: 'Compras',
      route: '/compras',
      children: [
        { label: 'Fornecedores', route: '/compras/fornecedores' },
        { label: 'Ordens de Compra', route: '/compras/ordens' },
        { label: 'Recebimento', route: '/compras/recebimento' }
      ]
    },
    {
      icon: 'people',
      label: 'Clientes',
      route: '/clientes'
    },
    {
      icon: 'shopping_cart',
      label: 'Vendas',
      route: '/vendas',
      children: [
        { label: 'PDV', route: '/pdv' },
        { label: 'Ordens de Venda', route: '/vendas/ordens' },
        { label: 'NFCe Pendentes', route: '/vendas/pendentes' }
      ]
    },
    {
      icon: 'settings_input_component',
      label: 'Integrações',
      route: '/integracoes',
      children: [
        { label: 'Mercado Livre', route: '/integracoes' },
        { label: 'Pedidos ML', route: '/integracoes/mercadolivre/pedidos' },
        { label: 'Margem de Segurança', route: '/integracoes/mercadolivre/margem-seguranca' }
      ]
    },
    {
      icon: 'admin_panel_settings',
      label: 'Gestão de Usuários',
      route: '/settings/collaborators',
      children: [
        { label: 'Usuários', route: '/settings/collaborators' },
        { label: 'Perfis de Acesso', route: '/usuarios/profiles' }
      ]
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private tenantService: TenantService
  ) {
    // Initialize responsive detection - Handset devices (phones/tablets)
    this.isHandset$ = this.breakpointObserver.observe(Breakpoints.Handset)
      .pipe(
        map(result => result.matches),
        shareReplay()
      );

    // Subscribe to tenant changes to update company name
    effect(() => {
      const tenantId = this.tenantService.currentTenant$();
      if (tenantId) {
        this.loadCurrentCompany();
      }
    });
  }

  ngOnInit(): void {
    // Load current user from JWT token
    const user = this.authService.getUserFromToken();
    if (user) {
      this.currentUser.set(user);
    }

    // Load current company
    this.loadCurrentCompany();

    // Auto-expand the menu that contains the current active route
    this.expandActiveMenu();
  }

  /**
   * Loads the current company information
   */
  private loadCurrentCompany(): void {
    const tenantId = this.tenantService.getCurrentTenant();
    console.log('🏢 Loading company for tenantId:', tenantId);

    if (tenantId) {
      this.tenantService.getUserCompanies().subscribe({
        next: (companies) => {
          console.log('🏢 Companies received:', companies);
          const company = companies.find(c => c.tenantId === tenantId);
          console.log('🏢 Matched company:', company);
          if (company) {
            this.currentCompany.set(company);
          }
        },
        error: (err) => {
          console.error('Error loading companies:', err);
        }
      });
    } else {
      console.log('🏢 No tenantId found in localStorage');
    }
  }

  /**
   * Toggle expand/collapse for parent menus with children
   */
  toggleMenu(route: string): void {
    if (this.expandedMenus.has(route)) {
      this.expandedMenus.delete(route);
    } else {
      this.expandedMenus.clear();
      this.expandedMenus.add(route);
    }
  }

  /**
   * Check if a parent menu is expanded
   */
  isMenuExpanded(route: string): boolean {
    return this.expandedMenus.has(route);
  }

  /**
   * Auto-expand the parent menu whose child matches the current route
   */
  private expandActiveMenu(): void {
    for (const item of this.menuItems) {
      if (item.children && this.isRouteActive(item.route)) {
        this.expandedMenus.add(item.route);
      }
    }
  }

  /**
   * Navigate to route and close drawer on mobile
   */
  navigate(route: string, isHandset: boolean): void {
    this.router.navigate([route]);
    if (isHandset && this.drawer) {
      this.drawer.close();
    }
  }

  /**
   * Toggle sidebar (mobile only)
   */
  toggleSidenav(): void {
    if (this.drawer) {
      this.drawer.toggle();
    }
  }

  /**
   * User logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  /**
   * Check if route is active
   */
  isRouteActive(route: string): boolean {
    return this.router.url.startsWith(route);
  }
}
