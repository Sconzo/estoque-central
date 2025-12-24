import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../auth/auth.service';
import { CompanyService, UserCompanyResponse } from '../services/company.service';
import { TenantService } from '../services/tenant.service';

/**
 * Unit tests for AuthGuard (Story 8.3)
 *
 * Tests the following acceptance criteria:
 * - AC2: Zero companies → redirect to /create-company
 * - AC3: One company → auto-select and redirect to /dashboard
 * - AC4: Multiple companies → redirect to /select-company
 * - AC5: Tenant context persistence
 */
describe('AuthGuard (Story 8.3)', () => {
  let guard: AuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let companyService: jasmine.SpyObj<CompanyService>;
  let tenantService: jasmine.SpyObj<TenantService>;
  let router: jasmine.SpyObj<Router>;

  const mockCompany1: UserCompanyResponse = {
    tenantId: 'tenant-uuid-1',
    nome: 'Empresa ABC',
    cnpj: '12345678000190',
    profileId: null,
    profileName: 'ADMIN'
  };

  const mockCompany2: UserCompanyResponse = {
    tenantId: 'tenant-uuid-2',
    nome: 'Empresa XYZ',
    cnpj: '98765432000111',
    profileId: null,
    profileName: 'USER'
  };

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    const companyServiceSpy = jasmine.createSpyObj('CompanyService', ['getUserCompanies']);
    const tenantServiceSpy = jasmine.createSpyObj('TenantService', ['getCurrentTenant', 'setCurrentTenant']);
    const routerSpy = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CompanyService, useValue: companyServiceSpy },
        { provide: TenantService, useValue: tenantServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    guard = TestBed.inject(AuthGuard);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    companyService = TestBed.inject(CompanyService) as jasmine.SpyObj<CompanyService>;
    tenantService = TestBed.inject(TenantService) as jasmine.SpyObj<TenantService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should redirect to /login if user is not authenticated', (done) => {
    authService.isAuthenticated.and.returnValue(false);
    router.createUrlTree.and.returnValue({} as any);

    const result = guard.canActivate();

    expect(authService.isAuthenticated).toHaveBeenCalled();
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
    done();
  });

  it('AC5: should allow access if tenant context is already set (persistence)', (done) => {
    authService.isAuthenticated.and.returnValue(true);
    tenantService.getCurrentTenant.and.returnValue('tenant-uuid-123');

    const result = guard.canActivate();

    expect(result).toBe(true);
    expect(companyService.getUserCompanies).not.toHaveBeenCalled();
    done();
  });

  it('AC2: should redirect to /create-company if user has zero companies', (done) => {
    authService.isAuthenticated.and.returnValue(true);
    tenantService.getCurrentTenant.and.returnValue(null);
    companyService.getUserCompanies.and.returnValue(of([]));
    router.createUrlTree.and.returnValue({} as any);

    const result$ = guard.canActivate();

    if (result$ instanceof Observable || typeof (result$ as any).subscribe === 'function') {
      (result$ as any).subscribe(() => {
        expect(router.createUrlTree).toHaveBeenCalledWith(['/create-company']);
        done();
      });
    }
  });

  it('AC3: should auto-select company and redirect to /dashboard if user has one company', (done) => {
    authService.isAuthenticated.and.returnValue(true);
    tenantService.getCurrentTenant.and.returnValue(null);
    companyService.getUserCompanies.and.returnValue(of([mockCompany1]));
    router.createUrlTree.and.returnValue({} as any);

    const result$ = guard.canActivate();

    if (result$ instanceof Observable || typeof (result$ as any).subscribe === 'function') {
      (result$ as any).subscribe(() => {
        expect(tenantService.setCurrentTenant).toHaveBeenCalledWith('tenant-uuid-1');
        expect(router.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
        done();
      });
    }
  });

  it('AC4: should redirect to /select-company if user has multiple companies', (done) => {
    authService.isAuthenticated.and.returnValue(true);
    tenantService.getCurrentTenant.and.returnValue(null);
    companyService.getUserCompanies.and.returnValue(of([mockCompany1, mockCompany2]));
    router.createUrlTree.and.returnValue({} as any);

    const result$ = guard.canActivate();

    if (result$ instanceof Observable || typeof (result$ as any).subscribe === 'function') {
      (result$ as any).subscribe(() => {
        expect(router.createUrlTree).toHaveBeenCalledWith(['/select-company']);
        expect(tenantService.setCurrentTenant).not.toHaveBeenCalled();
        done();
      });
    }
  });

  it('should redirect to /create-company on error fetching companies', (done) => {
    authService.isAuthenticated.and.returnValue(true);
    tenantService.getCurrentTenant.and.returnValue(null);
    companyService.getUserCompanies.and.returnValue(throwError(() => new Error('API Error')));
    router.createUrlTree.and.returnValue({} as any);

    const result$ = guard.canActivate();

    if (result$ instanceof Observable || typeof (result$ as any).subscribe === 'function') {
      (result$ as any).subscribe(() => {
        expect(router.createUrlTree).toHaveBeenCalledWith(['/create-company']);
        done();
      });
    }
  });
});
