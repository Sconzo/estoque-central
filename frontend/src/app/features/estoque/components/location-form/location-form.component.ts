import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LocationService } from '../../services/location.service';
import { LocationType, LOCATION_TYPE_LABELS } from '../../models/location.model';
import { TenantService } from '../../../../core/services/tenant.service';

/**
 * LocationFormComponent - Create/Edit stock location
 * Refactored with Material Design (UX-4)
 */
@Component({
  selector: 'app-location-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './location-form.component.html',
  styleUrls: ['./location-form.component.scss']
})
export class LocationFormComponent implements OnInit {
  locationForm!: FormGroup;
  isEditMode = false;
  locationId: string | null = null;
  isSubmitting = false;
  errorMessage: string | null = null;

  // Enums for template
  locationTypes = Object.values(LocationType);
  locationTypeLabels = LOCATION_TYPE_LABELS;

  constructor(
    private fb: FormBuilder,
    private locationService: LocationService,
    private tenantService: TenantService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();

    // Check if edit mode
    this.locationId = this.route.snapshot.paramMap.get('id');
    if (this.locationId) {
      this.isEditMode = true;
      this.loadLocation(this.locationId);
    }
  }

  private initForm(): void {
    this.locationForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      code: [
        '',
        [
          Validators.required,
          Validators.maxLength(20),
          Validators.pattern(/^[A-Z0-9-]+$/)
        ]
      ],
      type: [LocationType.WAREHOUSE, Validators.required],
      description: [''],
      address: [''],
      city: [''],
      state: [''],
      postalCode: [''],
      country: [''],
      phone: [''],
      email: ['', Validators.email],
      managerId: ['']
    });

    // Disable code field in edit mode
    if (this.isEditMode) {
      this.locationForm.get('code')?.disable();
    }
  }

  private loadLocation(id: string): void {
    this.locationService.getById(id).subscribe({
      next: (location) => {
        this.locationForm.patchValue({
          name: location.name,
          code: location.code,
          type: location.type,
          description: location.description,
          address: location.address,
          city: location.city,
          state: location.state,
          postalCode: location.postalCode,
          country: location.country,
          phone: location.phone,
          email: location.email,
          managerId: location.managerId
        });
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar local de estoque';
        console.error('Error loading location:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.locationForm.invalid) {
      Object.keys(this.locationForm.controls).forEach(key => {
        this.locationForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const formValue = this.locationForm.getRawValue();
    const tenantId = this.tenantService.getCurrentTenant();
    if (!tenantId) {
      this.errorMessage = 'Nenhuma empresa selecionada';
      this.isSubmitting = false;
      return;
    }

    if (this.isEditMode && this.locationId) {
      // Update
      this.locationService.update(this.locationId, formValue).subscribe({
        next: () => {
          this.router.navigate(['/estoque/locais']);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Erro ao atualizar local de estoque';
          this.isSubmitting = false;
        }
      });
    } else {
      // Create
      this.locationService.create(formValue, tenantId).subscribe({
        next: () => {
          this.router.navigate(['/estoque/locais']);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Erro ao criar local de estoque';
          this.isSubmitting = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/estoque/locais']);
  }

  // Helper methods for validation
  isFieldInvalid(fieldName: string): boolean {
    const field = this.locationForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.locationForm.get(fieldName);
    if (!field || !field.errors) return '';

    if (field.errors['required']) return 'Campo obrigatório';
    if (field.errors['maxlength']) return `Máximo ${field.errors['maxlength'].requiredLength} caracteres`;
    if (field.errors['pattern']) return 'Use apenas letras maiúsculas, números e hífens';
    if (field.errors['email']) return 'Email inválido';

    return 'Campo inválido';
  }
}
