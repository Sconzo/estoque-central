import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { ProfileService } from '../../services/profile.service';
import { RoleService } from '../../services/role.service';
import { Profile, AssignRolesRequest } from '../../models/profile.model';
import { Role } from '../../models/role.model';
import { FeedbackService } from '../../../../../shared/services/feedback.service';

/**
 * RoleAssignmentComponent - Assign roles to a profile
 *
 * Features:
 * - List all available roles
 * - Checkbox selection for roles
 * - Grouped by category
 * - Material Design 3 components
 * - WCAG AA accessibility
 */
@Component({
  selector: 'app-role-assignment',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatChipsModule
  ],
  templateUrl: './role-assignment.component.html',
  styleUrls: ['./role-assignment.component.scss']
})
export class RoleAssignmentComponent implements OnInit {
  profileId: string | null = null;
  profile: Profile | null = null;
  availableRoles: Role[] = [];
  selectedRoleIds: Set<string> = new Set();

  loading = false;
  saving = false;

  constructor(
    private profileService: ProfileService,
    private roleService: RoleService,
    private router: Router,
    private route: ActivatedRoute,
    private feedback: FeedbackService
  ) {}

  ngOnInit(): void {
    this.profileId = this.route.snapshot.paramMap.get('id');

    if (this.profileId) {
      this.loadData();
    }
  }

  /**
   * Loads profile and available roles
   */
  loadData(): void {
    if (!this.profileId) return;

    this.loading = true;

    // Load profile with current roles
    this.profileService.getById(this.profileId).subscribe({
      next: (profile) => {
        this.profile = profile;

        // Pre-select current roles
        if (profile.roles) {
          profile.roles.forEach(role => this.selectedRoleIds.add(role.id));
        }

        // Load all available roles
        this.loadAvailableRoles();
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        this.feedback.showError('Erro ao carregar perfil.', () => this.loadData());
        this.loading = false;
      }
    });
  }

  /**
   * Loads all available roles
   */
  loadAvailableRoles(): void {
    this.roleService.listAll().subscribe({
      next: (roles) => {
        this.availableRoles = roles.filter(role => role.ativo);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading roles:', err);
        this.feedback.showError('Erro ao carregar permissões.', () => this.loadAvailableRoles());
        this.loading = false;
      }
    });
  }

  /**
   * Toggles role selection
   */
  toggleRole(roleId: string): void {
    if (this.selectedRoleIds.has(roleId)) {
      this.selectedRoleIds.delete(roleId);
    } else {
      this.selectedRoleIds.add(roleId);
    }
  }

  /**
   * Checks if a role is selected
   */
  isRoleSelected(roleId: string): boolean {
    return this.selectedRoleIds.has(roleId);
  }

  /**
   * Groups roles by category
   */
  getRolesByCategory(): Map<string, Role[]> {
    const grouped = new Map<string, Role[]>();

    this.availableRoles.forEach(role => {
      const category = role.categoria || 'Outras';
      if (!grouped.has(category)) {
        grouped.set(category, []);
      }
      grouped.get(category)!.push(role);
    });

    return grouped;
  }

  /**
   * Returns array of category names
   */
  getCategories(): string[] {
    return Array.from(this.getRolesByCategory().keys()).sort();
  }

  /**
   * Returns roles for a specific category
   */
  getRolesForCategory(category: string): Role[] {
    return this.getRolesByCategory().get(category) || [];
  }

  /**
   * Saves role assignments
   */
  save(): void {
    if (!this.profileId) return;

    this.saving = true;

    const request: AssignRolesRequest = {
      roleIds: Array.from(this.selectedRoleIds)
    };

    this.profileService.assignRoles(this.profileId, request).subscribe({
      next: () => {
        this.feedback.showSuccess('Permissões atualizadas com sucesso!');
        this.router.navigate(['/usuarios/profiles']);
      },
      error: (err) => {
        console.error('Error assigning roles:', err);
        this.feedback.showError('Erro ao atualizar permissões.', () => this.save());
        this.saving = false;
      }
    });
  }

  /**
   * Cancels and returns to list
   */
  cancel(): void {
    this.router.navigate(['/usuarios/profiles']);
  }
}
