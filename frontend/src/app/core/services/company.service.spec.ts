import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CompanyService, CreateCompanyRequest, CreateCompanyResponse } from './company.service';

/**
 * Unit tests for CompanyService (Story 8.2)
 *
 * Tests AC4 and AC6:
 * - POST /api/public/companies
 * - JWT token storage in localStorage
 */
describe('CompanyService', () => {
  let service: CompanyService;
  let httpMock: HttpTestingController;

  const mockApiUrl = 'http://localhost:8080/api/public/companies';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CompanyService]
    });

    service = TestBed.inject(CompanyService);
    httpMock = TestBed.inject(HttpTestingController);

    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /**
   * AC4: POST request to /api/public/companies
   */
  describe('createCompany', () => {
    it('should send POST request to /api/public/companies with correct payload', () => {
      const request: CreateCompanyRequest = {
        nome: 'Test Company',
        cnpj: '12345678000190',
        email: 'test@test.com',
        telefone: '11987654321',
        userId: 123
      };

      const mockResponse: CreateCompanyResponse = {
        tenantId: 'uuid-123',
        nome: 'Test Company',
        schemaName: 'tenant_uuid123',
        token: 'jwt-token-123'
      };

      service.createCompany(request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(mockApiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);

      req.flush(mockResponse);
    });

    /**
     * AC6: Service returns JWT token (component stores it)
     */
    it('should return response with JWT token', () => {
      const request: CreateCompanyRequest = {
        nome: 'Test',
        email: 'test@test.com',
        userId: 123
      };

      const mockResponse: CreateCompanyResponse = {
        tenantId: 'uuid',
        nome: 'Test',
        schemaName: 'tenant_uuid',
        token: 'new-jwt-token'
      };

      service.createCompany(request).subscribe(response => {
        expect(response.token).toBe('new-jwt-token');
        expect(response.tenantId).toBe('uuid');
      });

      const req = httpMock.expectOne(mockApiUrl);
      req.flush(mockResponse);
    });

    it('should handle HTTP errors correctly', () => {
      const request: CreateCompanyRequest = {
        nome: 'Test',
        email: 'test@test.com',
        userId: 123
      };

      const mockError = {
        status: 500,
        statusText: 'Internal Server Error',
        error: { message: 'Schema creation failed' }
      };

      service.createCompany(request).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
          expect(error.error.message).toBe('Schema creation failed');
        }
      });

      const req = httpMock.expectOne(mockApiUrl);
      req.flush(mockError.error, mockError);
    });

    it('should return response even with empty token', () => {
      const request: CreateCompanyRequest = {
        nome: 'Test',
        email: 'test@test.com',
        userId: 123
      };

      const mockResponse = {
        tenantId: 'uuid',
        nome: 'Test',
        schemaName: 'tenant_uuid',
        token: ''
      };

      service.createCompany(request).subscribe(response => {
        expect(response.token).toBe('');
        expect(response.tenantId).toBe('uuid');
      });

      const req = httpMock.expectOne(mockApiUrl);
      req.flush(mockResponse);
    });
  });
});
