import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { ProfileService } from '../../services/profile.service';
import { Profile, ProfileCreateRequest, ProfileUpdateRequest } from '../../models/profile.model';
import { FeedbackService } from '../../../../../shared/services/feedback.service';

/**
 * ProfileFormComponent - Create/Edit profile form
 *
 * Features:
 * - Reactive form with validation
 * - Create new profile or edit existing
 * - Material Design 3 form components
 * - WCAG AA accessibility
 */
@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule
  ],
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.scss']
})
export class ProfileFormComponent implements OnInit {
  form: FormGroup;
  loading = false;
  isEditMode = false;
  profileId: string | null = null;
  profile: Profile | null = null;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private router: Router,
    private route: ActivatedRoute,
    private feedback: FeedbackService
  ) {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      descricao: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.profileId = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!this.profileId;

    if (this.isEditMode && this.profileId) {
      this.loadProfile(this.profileId);
    }
  }

  /**
   * Loads profile data for editing
   */
  loadProfile(id: string): void {
    this.loading = true;

    this.profileService.getById(id).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.form.patchValue({
          nome: profile.nome,
          descricao: profile.descricao
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        this.feedback.showError('Erro ao carregar perfil.', () => this.loadProfile(id));
        this.loading = false;
      }
    });
  }

  /**
   * Submits the form (create or update)
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    if (this.isEditMode && this.profileId) {
      this.updateProfile();
    } else {
      this.createProfile();
    }
  }

  /**
   * Creates a new profile
   */
  createProfile(): void {
    const request: ProfileCreateRequest = {
      nome: this.form.value.nome,
      descricao: this.form.value.descricao
    };

    this.profileService.create(request).subscribe({
      next: (profile) => {
        this.feedback.showSuccess('Perfil criado com sucesso!');
        this.router.navigate(['/usuarios/profiles']);
      },
      error: (err) => {
        console.error('Error creating profile:', err);
        this.feedback.showError('Erro ao criar perfil.', () => this.createProfile());
        this.loading = false;
      }
    });
  }

  /**
   * Updates an existing profile
   */
  updateProfile(): void {
    if (!this.profileId) return;

    const request: ProfileUpdateRequest = {
      nome: this.form.value.nome,
      descricao: this.form.value.descricao
    };

    this.profileService.update(this.profileId, request).subscribe({
      next: (profile) => {
        this.feedback.showSuccess('Perfil atualizado com sucesso!');
        this.router.navigate(['/usuarios/profiles']);
      },
      error: (err) => {
        console.error('Error updating profile:', err);
        this.feedback.showError('Erro ao atualizar perfil.', () => this.updateProfile());
        this.loading = false;
      }
    });
  }

  /**
   * Cancels and returns to list
   */
  cancel(): void {
    this.router.navigate(['/usuarios/profiles']);
  }

  /**
   * Returns form title based on mode
   */
  getTitle(): string {
    return this.isEditMode ? 'Editar Perfil' : 'Novo Perfil';
  }

  /**
   * Returns submit button text based on mode
   */
  getSubmitButtonText(): string {
    return this.isEditMode ? 'Salvar Alterações' : 'Criar Perfil';
  }
}
