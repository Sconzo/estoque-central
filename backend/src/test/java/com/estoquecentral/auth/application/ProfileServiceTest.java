package com.estoquecentral.auth.application;

import com.estoquecentral.auth.adapter.out.ProfileRepository;
import com.estoquecentral.auth.adapter.out.ProfileRoleRepository;
import com.estoquecentral.auth.adapter.out.RoleRepository;
import com.estoquecentral.auth.domain.Profile;
import com.estoquecentral.auth.domain.ProfileRole;
import com.estoquecentral.auth.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileService
 *
 * <p>Tests profile management operations:
 * <ul>
 *   <li>List profiles by tenant</li>
 *   <li>Get profile by ID</li>
 *   <li>Create profile with roles</li>
 *   <li>Update profile</li>
 *   <li>Update profile roles</li>
 *   <li>Activate/deactivate profile</li>
 *   <li>Get roles by profile</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Unit Tests")
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileRoleRepository profileRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ProfileService profileService;

    private UUID tenantId;
    private UUID profileId;
    private UUID roleAdminId;
    private UUID roleGerenteId;
    private Profile adminProfile;
    private Role adminRole;
    private Role gerenteRole;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        roleAdminId = UUID.randomUUID();
        roleGerenteId = UUID.randomUUID();

        adminProfile = new Profile(
                profileId,
                tenantId,
                "Administrador",
                "Perfil com acesso administrativo"
        );

        adminRole = new Role(roleAdminId, "ADMIN", "Administrador", "SISTEMA");
        gerenteRole = new Role(roleGerenteId, "GERENTE", "Gerente", "GESTAO");
    }

    @Test
    @DisplayName("Should list profiles by tenant")
    void shouldListProfilesByTenant() {
        // Given
        Profile profile2 = new Profile(
                UUID.randomUUID(),
                tenantId,
                "Gerente",
                "Perfil gerente"
        );
        when(profileRepository.findByTenantIdAndAtivoTrue(tenantId))
                .thenReturn(List.of(adminProfile, profile2));

        // When
        List<Profile> profiles = profileService.listByTenant(tenantId);

        // Then
        assertThat(profiles).hasSize(2);
        assertThat(profiles).extracting(Profile::getNome)
                .containsExactlyInAnyOrder("Administrador", "Gerente");
        verify(profileRepository, times(1)).findByTenantIdAndAtivoTrue(tenantId);
    }

    @Test
    @DisplayName("Should get profile by ID")
    void shouldGetProfileById() {
        // Given
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(adminProfile));

        // When
        Profile profile = profileService.getById(profileId);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getNome()).isEqualTo("Administrador");
        verify(profileRepository, times(1)).findById(profileId);
    }

    @Test
    @DisplayName("Should throw exception when profile not found")
    void shouldThrowExceptionWhenProfileNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(profileRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> profileService.getById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Profile not found");

        verify(profileRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should create profile with roles")
    void shouldCreateProfileWithRoles() {
        // Given
        List<UUID> roleIds = List.of(roleAdminId, roleGerenteId);
        when(profileRepository.existsByTenantIdAndNome(tenantId, "Novo Perfil")).thenReturn(false);
        when(roleRepository.existsById(roleAdminId)).thenReturn(true);
        when(roleRepository.existsById(roleGerenteId)).thenReturn(true);
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRoleRepository.save(any(ProfileRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Profile newProfile = profileService.create(tenantId, "Novo Perfil", "Descrição", roleIds);

        // Then
        assertThat(newProfile).isNotNull();
        assertThat(newProfile.getNome()).isEqualTo("Novo Perfil");
        assertThat(newProfile.getTenantId()).isEqualTo(tenantId);
        assertThat(newProfile.getAtivo()).isTrue();

        verify(profileRepository, times(1)).existsByTenantIdAndNome(tenantId, "Novo Perfil");
        verify(profileRepository, times(1)).save(any(Profile.class));
        verify(profileRoleRepository, times(2)).save(any(ProfileRole.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate profile")
    void shouldThrowExceptionWhenCreatingDuplicateProfile() {
        // Given
        when(profileRepository.existsByTenantIdAndNome(tenantId, "Administrador")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> profileService.create(tenantId, "Administrador", "Desc", List.of(roleAdminId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Profile already exists");

        verify(profileRepository, times(1)).existsByTenantIdAndNome(tenantId, "Administrador");
        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating profile with non-existent role")
    void shouldThrowExceptionWhenCreatingProfileWithNonExistentRole() {
        // Given
        UUID nonExistentRoleId = UUID.randomUUID();
        when(profileRepository.existsByTenantIdAndNome(tenantId, "Novo Perfil")).thenReturn(false);
        when(roleRepository.existsById(nonExistentRoleId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> profileService.create(tenantId, "Novo Perfil", "Desc", List.of(nonExistentRoleId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");

        verify(roleRepository, times(1)).existsById(nonExistentRoleId);
        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update profile metadata")
    void shouldUpdateProfileMetadata() {
        // Given
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(adminProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Profile updatedProfile = profileService.update(profileId, "Novo Nome", "Nova Descrição");

        // Then
        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.getNome()).isEqualTo("Novo Nome");
        assertThat(updatedProfile.getDescricao()).isEqualTo("Nova Descrição");

        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Should update profile roles")
    void shouldUpdateProfileRoles() {
        // Given
        List<UUID> newRoleIds = List.of(roleAdminId, roleGerenteId);
        when(profileRepository.existsById(profileId)).thenReturn(true);
        when(roleRepository.existsById(roleAdminId)).thenReturn(true);
        when(roleRepository.existsById(roleGerenteId)).thenReturn(true);
        doNothing().when(profileRoleRepository).deleteByProfileId(profileId);
        when(profileRoleRepository.save(any(ProfileRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        profileService.updateRoles(profileId, newRoleIds);

        // Then
        verify(profileRepository, times(1)).existsById(profileId);
        verify(profileRoleRepository, times(1)).deleteByProfileId(profileId);
        verify(profileRoleRepository, times(2)).save(any(ProfileRole.class));
    }

    @Test
    @DisplayName("Should get roles by profile")
    void shouldGetRolesByProfile() {
        // Given
        ProfileRole pr1 = new ProfileRole(profileId, roleAdminId);
        ProfileRole pr2 = new ProfileRole(profileId, roleGerenteId);
        when(profileRoleRepository.findByProfileId(profileId)).thenReturn(List.of(pr1, pr2));
        when(roleRepository.findById(roleAdminId)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(roleGerenteId)).thenReturn(Optional.of(gerenteRole));

        // When
        List<Role> roles = profileService.getRolesByProfile(profileId);

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Role::getNome)
                .containsExactlyInAnyOrder("ADMIN", "GERENTE");

        verify(profileRoleRepository, times(1)).findByProfileId(profileId);
        verify(roleRepository, times(2)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should filter out inactive roles when getting roles by profile")
    void shouldFilterOutInactiveRolesWhenGettingRolesByProfile() {
        // Given
        Role inactiveRole = new Role(roleGerenteId, "GERENTE", "Gerente", "GESTAO");
        inactiveRole.deactivate();

        ProfileRole pr1 = new ProfileRole(profileId, roleAdminId);
        ProfileRole pr2 = new ProfileRole(profileId, roleGerenteId);
        when(profileRoleRepository.findByProfileId(profileId)).thenReturn(List.of(pr1, pr2));
        when(roleRepository.findById(roleAdminId)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(roleGerenteId)).thenReturn(Optional.of(inactiveRole));

        // When
        List<Role> roles = profileService.getRolesByProfile(profileId);

        // Then
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getNome()).isEqualTo("ADMIN");
        verify(profileRoleRepository, times(1)).findByProfileId(profileId);
    }

    @Test
    @DisplayName("Should deactivate profile")
    void shouldDeactivateProfile() {
        // Given
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(adminProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        profileService.deactivate(profileId);

        // Then
        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).save(argThat(profile -> !profile.getAtivo()));
    }

    @Test
    @DisplayName("Should activate profile")
    void shouldActivateProfile() {
        // Given
        adminProfile.setAtivo(false);
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(adminProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        profileService.activate(profileId);

        // Then
        verify(profileRepository, times(1)).findById(profileId);
        verify(profileRepository, times(1)).save(argThat(Profile::getAtivo));
    }

    @Test
    @DisplayName("Should return empty list when profile has no roles")
    void shouldReturnEmptyListWhenProfileHasNoRoles() {
        // Given
        when(profileRoleRepository.findByProfileId(profileId)).thenReturn(List.of());

        // When
        List<Role> roles = profileService.getRolesByProfile(profileId);

        // Then
        assertThat(roles).isEmpty();
        verify(profileRoleRepository, times(1)).findByProfileId(profileId);
    }
}
