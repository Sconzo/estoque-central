# Story 10.10: Frontend - Edição de Dados da Empresa

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.10
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

---

## User Story

As a **Admin**,
I want **to edit my company's information via UI**,
So that **I can keep data up-to-date without technical help**.

---

## Acceptance Criteria

### AC1: Company Data Form
**Given** admin navigates to `/settings/company`
**When** page loads
**Then** form displays current company data from `/api/companies/current` (GET)
**And** fields: Nome, CNPJ, Email, Telefone, Endereço

### AC2: Real-Time Validation
**Given** form editing
**When** admin modifies fields
**Then** validators run in real-time
**And** save button disabled if invalid

### AC3: Form Submission
**Given** form submission
**When** admin clicks "Salvar Alterações"
**Then** PUT to `/api/companies/current`
**And** button shows loading spinner

### AC4: Success Feedback
**Given** successful save
**When** backend returns success
**Then** success MatSnackBar: "Dados atualizados" (UX14)

### AC5: Delete Company Option
**Given** delete company option
**When** admin scrolls to danger zone
**Then** "Deletar Empresa" button visible (red)
**And** on click, strong confirmation dialog

---

## Definition of Done
- [x] Form implementado
- [x] Validation em tempo real
- [x] Save com loading spinner
- [x] Delete option com confirmação

## Implementation Plan

### Arquivos a Criar

1. **company-settings.component.ts** - Componente principal
   - Rota: `/settings/company`
   - GET `/api/companies/current` para carregar dados (AC1)
   - FormGroup com validators (AC2)
   - PUT `/api/companies/current` para salvar (AC3)
   - DELETE `/api/companies/current` com confirmação (AC5)

2. **company-settings.component.html** - Template
   ```html
   <mat-card>
     <mat-card-header>
       <mat-card-title>Dados da Empresa</mat-card-title>
     </mat-card-header>

     <mat-card-content>
       <form [formGroup]="companyForm">
         <!-- Nome -->
         <mat-form-field appearance="outline">
           <mat-label>Nome da Empresa</mat-label>
           <input matInput formControlName="name" required>
           <mat-error *ngIf="companyForm.get('name').hasError('required')">
             Nome é obrigatório
           </mat-error>
         </mat-form-field>

         <!-- CNPJ (readonly) -->
         <mat-form-field appearance="outline">
           <mat-label>CNPJ</mat-label>
           <input matInput formControlName="cnpj" readonly>
         </mat-form-field>

         <!-- Email -->
         <mat-form-field appearance="outline">
           <mat-label>Email</mat-label>
           <input matInput formControlName="email" type="email">
           <mat-error *ngIf="companyForm.get('email').hasError('email')">
             Email inválido
           </mat-error>
         </mat-form-field>

         <!-- Telefone -->
         <mat-form-field appearance="outline">
           <mat-label>Telefone</mat-label>
           <input matInput formControlName="phone">
         </mat-form-field>
       </form>
     </mat-card-content>

     <mat-card-actions>
       <button mat-raised-button
               color="primary"
               [disabled]="companyForm.invalid || loading"
               (click)="saveCompany()">
         <mat-spinner *ngIf="loading" diameter="20"></mat-spinner>
         <span *ngIf="!loading">Salvar Alterações</span>
       </button>
     </mat-card-actions>
   </mat-card>

   <!-- Danger Zone -->
   <mat-card class="danger-zone">
     <mat-card-header>
       <mat-card-title>Zona de Perigo</mat-card-title>
     </mat-card-header>
     <mat-card-content>
       <p>A exclusão da empresa é irreversível.</p>
       <button mat-raised-button
               color="warn"
               (click)="deleteCompany()">
         Deletar Empresa
       </button>
     </mat-card-content>
   </mat-card>
   ```

3. **company.service.ts** - Service Angular
   ```typescript
   @Injectable()
   export class CompanyService {
     getCurrentCompany(): Observable<Company> {
       return this.http.get<Company>('/api/companies/current');
     }

     updateCompany(data: UpdateCompanyRequest): Observable<Company> {
       return this.http.put<Company>('/api/companies/current', data);
     }

     deleteCompany(): Observable<void> {
       return this.http.delete<void>('/api/companies/current');
     }
   }
   ```

4. **company-settings.component.ts** - Lógica
   ```typescript
   export class CompanySettingsComponent implements OnInit {
     companyForm: FormGroup;
     loading = false;

     ngOnInit() {
       this.buildForm();
       this.loadCompanyData(); // AC1
     }

     buildForm() {
       this.companyForm = this.fb.group({
         name: ['', [Validators.required, Validators.maxLength(255)]],
         cnpj: [{value: '', disabled: true}], // readonly
         email: ['', [Validators.email]],
         phone: ['', [Validators.maxLength(20)]]
       });

       // AC2: Real-time validation (já funciona com reactive forms)
     }

     loadCompanyData() {
       this.companyService.getCurrentCompany().subscribe({
         next: (company) => {
           this.companyForm.patchValue(company);
         },
         error: (err) => {
           this.snackBar.open('Erro ao carregar dados', 'OK');
         }
       });
     }

     saveCompany() {
       if (this.companyForm.invalid) return;

       this.loading = true; // AC3: loading spinner
       const data = this.companyForm.getRawValue();

       this.companyService.updateCompany(data).subscribe({
         next: () => {
           this.loading = false;
           this.snackBar.open('Dados atualizados com sucesso!', 'OK'); // AC4
         },
         error: (err) => {
           this.loading = false;
           this.snackBar.open('Erro ao salvar: ' + err.message, 'OK');
         }
       });
     }

     deleteCompany() {
       // AC5: Strong confirmation
       const dialogRef = this.dialog.open(ConfirmDialogComponent, {
         data: {
           title: 'Deletar Empresa?',
           message: 'Esta ação é IRREVERSÍVEL. Todos os dados serão perdidos. Digite "DELETAR" para confirmar.',
           confirmText: 'DELETAR',
           requiresTextConfirmation: true
         }
       });

       dialogRef.afterClosed().subscribe(confirmed => {
         if (confirmed) {
           this.companyService.deleteCompany().subscribe({
             next: () => {
               this.snackBar.open('Empresa deletada', 'OK');
               this.authService.logout();
               this.router.navigate(['/login']);
             },
             error: (err) => {
               this.snackBar.open('Erro: ' + err.error.message, 'OK');
             }
           });
         }
       });
     }
   }
   ```

### Validações (AC2)

- **Real-time**: Reactive Forms já fornece validação em tempo real
- **Save button disabled**: `[disabled]="companyForm.invalid || loading"`
- **Error messages**: `<mat-error>` exibe mensagens contextuais

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
