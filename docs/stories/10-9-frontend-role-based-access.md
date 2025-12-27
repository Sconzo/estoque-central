# Story 10.9: Frontend - Controle de Acesso Baseado em Roles (UI)

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.9
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

---

## User Story

As a **User**,
I want **to see only features I have permission to access**,
So that **the UI is tailored to my role**.

---

## Acceptance Criteria

### AC1: Role Directive
**Given** role-based UI rendering
**When** components check permissions
**Then** Angular directive `*appHasRole="'ADMIN'"` hides/shows elements

### AC2: Admin User UI
**Given** Admin user
**When** admin navigates the app
**Then** all menu items visible
**And** "Configurações" accessible

### AC3: Gerente User UI
**Given** Gerente user
**When** gerente navigates the app
**Then** "Produtos" and "Estoque" with write access
**And** "Configurações → Colaboradores" hidden

### AC4: Vendedor User UI
**Given** Vendedor user
**When** vendedor navigates the app
**Then** "Vendas" with write access
**And** "Configurações" completely hidden

### AC5: Route Guard
**Given** unauthorized access attempt
**When** user manually navigates to restricted route
**Then** route guard redirects to `/dashboard` or `/403`

---

## Definition of Done
- [x] Role directive implementado
- [x] Admin, Gerente, Vendedor UIs diferenciadas
- [x] Route guard configurado

## Implementation Plan

### Arquivos a Criar

1. **has-role.directive.ts** - Directive estrutural (AC1)
   ```typescript
   @Directive({ selector: '[appHasRole]' })
   export class HasRoleDirective {
     @Input() appHasRole: string | string[];

     constructor(
       private templateRef: TemplateRef<any>,
       private viewContainer: ViewContainerRef,
       private authService: AuthService
     ) {}

     ngOnInit() {
       const userRoles = this.authService.getUserRoles();
       const requiredRoles = Array.isArray(this.appHasRole)
         ? this.appHasRole
         : [this.appHasRole];

       const hasRole = requiredRoles.some(role => userRoles.includes(role));

       if (hasRole) {
         this.viewContainer.createEmbeddedView(this.templateRef);
       } else {
         this.viewContainer.clear();
       }
     }
   }
   ```

2. **role.guard.ts** - Route Guard (AC5)
   ```typescript
   @Injectable()
   export class RoleGuard implements CanActivate {
     canActivate(route: ActivatedRouteSnapshot): boolean {
       const requiredRoles = route.data['roles'] as string[];
       const userRoles = this.authService.getUserRoles();

       const hasRole = requiredRoles.some(role => userRoles.includes(role));

       if (!hasRole) {
         this.router.navigate(['/403']);
         return false;
       }
       return true;
     }
   }
   ```

3. **auth.service.ts** - Métodos adicionais
   ```typescript
   getUserRoles(): string[] {
     const token = this.getToken();
     if (!token) return [];

     const decoded = jwtDecode(token);
     return decoded.roles || [];
   }

   hasRole(role: string): boolean {
     return this.getUserRoles().includes(role);
   }

   isAdmin(): boolean {
     return this.hasRole('ADMIN');
   }
   ```

### Uso nos Templates

**AC2 - Admin UI:**
```html
<mat-list-item *appHasRole="'ADMIN'" routerLink="/settings">
  <mat-icon>settings</mat-icon>
  Configurações
</mat-list-item>
```

**AC3 - Gerente UI:**
```html
<button *appHasRole="['ADMIN', 'GERENTE']"
        mat-raised-button
        (click)="editProduct()">
  Editar Produto
</button>
```

**AC4 - Vendedor UI:**
```html
<div *appHasRole="'VENDEDOR'">
  <app-sales-form></app-sales-form>
</div>
```

### Configuração de Rotas (AC5)

```typescript
const routes: Routes = [
  {
    path: 'settings',
    canActivate: [RoleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      { path: 'collaborators', component: CollaboratorsComponent },
      { path: 'company', component: CompanySettingsComponent }
    ]
  }
];
```

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
