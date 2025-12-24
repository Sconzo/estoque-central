import { Component, OnInit, ViewChild, signal } from '@angular/core';
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

  // Responsive detection
  isHandset$!: Observable<boolean>;

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
        { label: 'Locais', route: '/estoque/locais' }
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
        { label: 'PDV', route: '/pdv' }
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
      route: '/usuarios/profiles',
      children: [
        { label: 'Perfis de Acesso', route: '/usuarios/profiles' },
        { label: 'Novo Perfil', route: '/usuarios/profiles/new' }
      ]
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private breakpointObserver: BreakpointObserver
  ) {
    // Initialize responsive detection - Handset devices (phones/tablets)
    this.isHandset$ = this.breakpointObserver.observe(Breakpoints.Handset)
      .pipe(
        map(result => result.matches),
        shareReplay()
      );
  }

  ngOnInit(): void {
    // Load current user
    const user = this.authService.getCurrentUser();
    this.currentUser.set(user);
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
