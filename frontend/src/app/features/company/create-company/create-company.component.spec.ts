import { ComponentFixture, TestBed, fakeAsync, tick, flush } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { CreateCompanyComponent } from './create-company.component';
import { CompanyService, CreateCompanyResponse } from '../../../core/services/company.service';
import { AuthService } from '../../../core/auth/auth.service';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * Unit tests for CreateCompanyComponent (Story 8.2)
 *
 * Tests all Acceptance Criteria:
 * - AC2: Form initialization with validators
 * - AC3: Form validation and error messages
 * - AC4: Form submission with userId from JWT
 * - AC5: Loading state during provisioning
 * - AC6: Success handling with redirect
 * - AC7: Error handling with retry
 */
describe('CreateCompanyComponent', () => {
  let component: CreateCompanyComponent;
  let fixture: ComponentFixture<CreateCompanyComponent>;
  let mockCompanyService: jasmine.SpyObj<CompanyService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockTenantService: jasmine.SpyObj<TenantService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJlbWFpbCI6InRlc3RAdGVzdC5jb20iLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6OTk5OTk5OTk5OX0.test';

  beforeEach(async () => {
    mockCompanyService = jasmine.createSpyObj('CompanyService', ['createCompany']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getToken']);
    mockTenantService = jasmine.createSpyObj('TenantService', ['setCurrentTenant']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // Create MatSnackBar mock with proper return value
    const snackBarRefMock = {
      onAction: () => of(void 0)
    };
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockSnackBar.open.and.returnValue(snackBarRefMock as any);

    await TestBed.configureTestingModule({
      imports: [
        CreateCompanyComponent,
        ReactiveFormsModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: CompanyService, useValue: mockCompanyService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: TenantService, useValue: mockTenantService },
        { provide: Router, useValue: mockRouter },
        { provide: MatSnackBar, useValue: mockSnackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateCompanyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  /**
   * AC2: Form initialization with reactive forms and validators
   */
  describe('AC2: Form Initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize form with all required fields', () => {
      expect(component.companyForm).toBeDefined();
      expect(component.companyForm.get('nome')).toBeDefined();
      expect(component.companyForm.get('cnpj')).toBeDefined();
      expect(component.companyForm.get('email')).toBeDefined();
      expect(component.companyForm.get('telefone')).toBeDefined();
    });

    it('should have nome field as required with max 255 chars', () => {
      const nomeControl = component.companyForm.get('nome');
      expect(nomeControl?.hasError('required')).toBe(true);

      nomeControl?.setValue('A'.repeat(256));
      expect(nomeControl?.hasError('maxlength')).toBe(true);
    });

    it('should have email field as required with email validation', () => {
      const emailControl = component.companyForm.get('email');
      expect(emailControl?.hasError('required')).toBe(true);

      emailControl?.setValue('invalid-email');
      expect(emailControl?.hasError('email')).toBe(true);

      emailControl?.setValue('valid@email.com');
      expect(emailControl?.hasError('email')).toBe(false);
    });

    it('should validate CNPJ pattern (14 digits)', () => {
      const cnpjControl = component.companyForm.get('cnpj');

      cnpjControl?.setValue('123');
      expect(cnpjControl?.hasError('pattern')).toBe(true);

      cnpjControl?.setValue('12345678000190');
      expect(cnpjControl?.hasError('pattern')).toBe(false);
    });
  });

  /**
   * AC3: Form validation with inline error messages
   */
  describe('AC3: Form Validation', () => {
    it('should return error message for required fields', () => {
      const nomeControl = component.companyForm.get('nome');
      nomeControl?.markAsTouched();
      nomeControl?.setValue('');

      const errorMessage = component.getErrorMessage('nome');
      expect(errorMessage).toBe('Este campo é obrigatório');
    });

    it('should return error message for invalid email', () => {
      const emailControl = component.companyForm.get('email');
      emailControl?.markAsTouched();
      emailControl?.setValue('invalid-email');

      const errorMessage = component.getErrorMessage('email');
      expect(errorMessage).toBe('Email inválido');
    });

    it('should return error message for invalid CNPJ pattern', () => {
      const cnpjControl = component.companyForm.get('cnpj');
      cnpjControl?.markAsTouched();
      cnpjControl?.setValue('123');

      const errorMessage = component.getErrorMessage('cnpj');
      expect(errorMessage).toBe('CNPJ deve ter 14 dígitos (apenas números)');
    });

    it('should disable submit button when form is invalid', () => {
      component.companyForm.patchValue({
        nome: '',
        email: 'invalid'
      });

      expect(component.companyForm.invalid).toBe(true);
    });
  });

  /**
   * AC4: Form submission with POST to /api/public/companies
   */
  describe('AC4: Form Submission', () => {
    it('should not submit if form is invalid', () => {
      component.companyForm.patchValue({
        nome: '',
        email: ''
      });

      component.onSubmit();

      expect(mockCompanyService.createCompany).not.toHaveBeenCalled();
    });

    it('should extract userId from JWT token and include in request', fakeAsync(() => {
      mockAuthService.getToken.and.returnValue(mockToken);

      const mockResponse: CreateCompanyResponse = {
        tenantId: 'uuid-123',
        nome: 'Test Company',
        schemaName: 'tenant_uuid123',
        token: 'new-jwt-token'
      };

      mockCompanyService.createCompany.and.returnValue(of(mockResponse));

      component.companyForm.patchValue({
        nome: 'Test Company',
        cnpj: '12345678000190',
        email: 'test@test.com',
        telefone: '11987654321'
      });

      component.onSubmit();
      tick();

      expect(mockCompanyService.createCompany).toHaveBeenCalledWith({
        nome: 'Test Company',
        cnpj: '12345678000190',
        email: 'test@test.com',
        telefone: '11987654321',
        userId: 123
      });
    }));

    it('should redirect to login if token is not found', () => {
      mockAuthService.getToken.and.returnValue(null);

      component.companyForm.patchValue({
        nome: 'Test',
        email: 'test@test.com'
      });

      component.onSubmit();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  /**
   * AC5: Loading state during provisioning
   */
  describe('AC5: Loading State', () => {
    it('should show loading state and disable form during submission', fakeAsync(() => {
      mockAuthService.getToken.and.returnValue(mockToken);
      mockCompanyService.createCompany.and.returnValue(of({
        tenantId: 'uuid',
        nome: 'Test',
        schemaName: 'tenant_uuid',
        token: 'token'
      }));

      component.companyForm.patchValue({
        nome: 'Test',
        email: 'test@test.com'
      });

      expect(component.isLoading).toBe(false);
      expect(component.companyForm.enabled).toBe(true);

      component.onSubmit();

      expect(component.isLoading).toBe(true);
      expect(component.companyForm.disabled).toBe(true);

      flush();

      expect(component.isLoading).toBe(false);
    }));
  });

  /**
   * AC6: Success handling with JWT storage and redirect
   */
  describe('AC6: Success Handling', () => {
    it('should store tenantId, show success message, and redirect to dashboard', fakeAsync(() => {
      mockAuthService.getToken.and.returnValue(mockToken);

      const mockResponse: CreateCompanyResponse = {
        tenantId: 'uuid-123',
        nome: 'Test Company',
        schemaName: 'tenant_uuid123',
        token: 'new-jwt-token'
      };

      mockCompanyService.createCompany.and.returnValue(of(mockResponse));

      component.companyForm.patchValue({
        nome: 'Test Company',
        email: 'test@test.com'
      });

      component.onSubmit();
      flush();

      // AC6: TenantService updates current tenant context
      expect(mockTenantService.setCurrentTenant).toHaveBeenCalledWith('uuid-123');

      // AC6: Success message displayed
      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Empresa "Test Company" criada com sucesso!',
        'Fechar',
        jasmine.objectContaining({ duration: 5000 })
      );

      // AC6: Redirect to dashboard
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    }));
  });

  /**
   * AC7: Error handling with retry button
   */
  describe('AC7: Error Handling', () => {
    it('should show error message and re-enable form on error', fakeAsync(() => {
      mockAuthService.getToken.and.returnValue(mockToken);
      mockCompanyService.createCompany.and.returnValue(
        throwError(() => ({ error: { message: 'CNPJ já cadastrado' } }))
      );

      component.companyForm.patchValue({
        nome: 'Test',
        email: 'test@test.com'
      });

      component.onSubmit();
      flush();

      // AC7: Error message displayed
      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'CNPJ já cadastrado',
        'Tentar Novamente',
        jasmine.objectContaining({ duration: 8000 })
      );

      // AC7: Form re-enabled for editing
      expect(component.isLoading).toBe(false);
      expect(component.companyForm.enabled).toBe(true);
    }));

    it('should show generic error message when error details not available', fakeAsync(() => {
      mockAuthService.getToken.and.returnValue(mockToken);
      mockCompanyService.createCompany.and.returnValue(
        throwError(() => ({}))
      );

      component.companyForm.patchValue({
        nome: 'Test',
        email: 'test@test.com'
      });

      component.onSubmit();
      flush();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Erro ao criar empresa. Tente novamente.',
        'Tentar Novamente',
        jasmine.objectContaining({ duration: 8000 })
      );
    }));
  });
});
