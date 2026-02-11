import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ProfileService } from '../../services/profile.service';
import { Profile } from '../../models/profile.model';
import { FeedbackService } from '../../../../../shared/services/feedback.service';
import { ConfirmDialogService } from '../../../../../shared/services/confirm-dialog.service';

/**
 * ProfileListComponent - Profile listing with actions
 *
 * Features:
 * - Material table with profile list
 * - Quick search by name
 * - Actions: View roles, Edit, Assign Roles, Delete
 * - Material Design 3 components
 * - WCAG AA accessibility
 */
@Component({
  selector: 'app-profile-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDialogModule
  ],
  templateUrl: './profile-list.component.html',
  styleUrls: ['./profile-list.component.scss']
})
export class ProfileListComponent implements OnInit {
  // Data
  profiles: Profile[] = [];
  filteredProfiles: Profile[] = [];

  // Loading states
  loading = false;
  error: string | null = null;

  // Filters
  searchQuery = '';

  // Mat-table columns
  displayedColumns: string[] = ['nome', 'descricao', 'roles', 'status', 'actions'];

  constructor(
    private profileService: ProfileService,
    private router: Router,
    private feedback: FeedbackService,
    private dialog: MatDialog,
    private confirmDialog: ConfirmDialogService
  ) {}

  ngOnInit(): void {
    this.loadProfiles();
  }

  /**
   * Loads all profiles
   */
  loadProfiles(): void {
    this.loading = true;
    this.error = null;

    this.profileService.listAll().subscribe({
      next: (data) => {
        this.profiles = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading profiles:', err);
        this.error = 'Erro ao carregar perfis. Tente novamente.';
        this.loading = false;
      }
    });
  }

  /**
   * Applies search filter
   */
  applyFilters(): void {
    const query = this.searchQuery.toLowerCase().trim();

    if (!query) {
      this.filteredProfiles = [...this.profiles];
      return;
    }

    this.filteredProfiles = this.profiles.filter(profile =>
      profile.nome.toLowerCase().includes(query) ||
      profile.descricao?.toLowerCase().includes(query)
    );
  }

  /**
   * Handles search input
   */
  onSearchChange(query: string): void {
    this.searchQuery = query;
    this.applyFilters();
  }

  /**
   * Navigates to create profile page
   */
  createProfile(): void {
    this.router.navigate(['/usuarios/profiles/new']);
  }

  /**
   * Navigates to edit profile page
   */
  editProfile(id: string): void {
    this.router.navigate(['/usuarios/profiles/edit', id]);
  }

  /**
   * Opens role assignment dialog
   */
  assignRoles(profile: Profile): void {
    this.router.navigate(['/usuarios/profiles', profile.id, 'roles']);
  }

  /**
   * Soft deletes a profile (marks as inactive)
   */
  deleteProfile(profile: Profile): void {
    this.confirmDialog.confirmDanger({
      title: 'Desativar Perfil',
      message: `Tem certeza que deseja desativar o perfil "${profile.nome}"?`
    }).subscribe(confirmed => {
      if (!confirmed) return;

      this.profileService.delete(profile.id).subscribe({
        next: () => {
          this.feedback.showSuccess('Perfil desativado com sucesso!');
          this.loadProfiles(); // Reload list
        },
        error: (err) => {
          console.error('Error deleting profile:', err);
          this.feedback.showError('Erro ao desativar perfil.', () => this.deleteProfile(profile));
        }
      });
    });
  }

  /**
   * Returns badge class for status
   */
  getStatusBadgeClass(ativo: boolean): string {
    return ativo ? 'badge-active' : 'badge-inactive';
  }

  /**
   * Returns badge label for status
   */
  getStatusLabel(ativo: boolean): string {
    return ativo ? 'Ativo' : 'Inativo';
  }

  /**
   * Returns number of roles for a profile
   */
  getRoleCount(profile: Profile): number {
    return profile.roles?.length || 0;
  }
}
