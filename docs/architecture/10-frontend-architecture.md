# 10. Frontend Architecture

## 10.1. Angular 17+ Standalone Components

```
src/app/
├── core/                           # Singleton services
│   ├── auth/
│   │   ├── auth.service.ts
│   │   ├── auth.guard.ts
│   │   └── auth.interceptor.ts
│   └── tenant/
│       └── tenant.interceptor.ts
├── shared/                         # Shared components
│   ├── components/
│   ├── models/
│   └── pipes/
├── features/                       # Feature modules
│   ├── produtos/
│   ├── pdv/
│   └── vendas/
└── layout/
```

## 10.2. State Management (Signals)

```typescript
// PDV Store usando Signals
@Injectable()
export class PdvStore {
  private _carrinho = signal<ItemCarrinho[]>([]);
  readonly carrinho = this._carrinho.asReadonly();

  readonly totalItens = computed(() =>
    this._carrinho().reduce((sum, item) => sum + item.quantidade, 0)
  );

  readonly totalValor = computed(() =>
    this._carrinho().reduce((sum, item) =>
      sum + (item.preco.valor * item.quantidade), 0
    )
  );

  adicionarItem(produto: Produto, quantidade: number) {
    this._carrinho.update(items => [...items, {
      produto,
      quantidade,
      preco: produto.preco
    }]);
  }

  removerItem(produtoId: string) {
    this._carrinho.update(items =>
      items.filter(i => i.produto.id !== produtoId)
    );
  }

  limpar() {
    this._carrinho.set([]);
  }
}
```

## 10.3. Routing com Lazy Loading

```typescript
// app.routes.ts
export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'produtos',
        loadComponent: () =>
          import('./features/produtos/produto-list/produto-list.component')
      },
      {
        path: 'pdv',
        loadComponent: () =>
          import('./features/pdv/pdv.component')
      },
      {
        path: 'vendas',
        loadChildren: () =>
          import('./features/vendas/vendas.routes')
      }
    ]
  },
  {
    path: 'login',
    component: LoginComponent
  }
];
```

## 10.4. HTTP Interceptors

```typescript
// Tenant Interceptor
export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const tenantService = inject(TenantService);
  const tenantId = tenantService.getTenantId();

  if (tenantId) {
    req = req.clone({
      setHeaders: {
        'X-Tenant-ID': tenantId
      }
    });
  }

  return next(req);
};

// Auth Interceptor
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
```

## 10.5. Material Theming + Tailwind

```scss
// styles.scss
@use '@angular/material' as mat;

$primary: mat.define-palette(mat.$indigo-palette);
$accent: mat.define-palette(mat.$pink-palette);

$theme: mat.define-light-theme((
  color: (
    primary: $primary,
    accent: $accent,
  )
));

@include mat.all-component-themes($theme);
```

```typescript
// Combina Material + Tailwind
<mat-card class="p-6 shadow-lg">
  <mat-card-header class="mb-4">
    <h2 class="text-2xl font-bold">Produtos</h2>
  </mat-card-header>
  <mat-card-content>
    <!-- Conteúdo -->
  </mat-card-content>
</mat-card>
```
