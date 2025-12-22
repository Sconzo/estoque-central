package com.estoquecentral.company.application;

import com.estoquecentral.company.adapter.out.CompanyUserRepository;
import com.estoquecentral.company.domain.CompanyUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for collaborator management.
 *
 * <p>Handles invitations, role management, and collaborator removal following
 * the use cases defined in Epic 10.</p>
 *
 * @since 1.0
 */
@Service
public class CollaboratorService {

    private final CompanyUserRepository companyUserRepository;

    public CollaboratorService(CompanyUserRepository companyUserRepository) {
        this.companyUserRepository = companyUserRepository;
    }

    /**
     * Invites a user to join a company (Epic 10 - Collaborator management).
     *
     * @param companyId Company ID
     * @param userId User ID to invite
     * @param role Role to assign
     * @return Created invitation
     * @throws IllegalArgumentException if user is already invited
     */
    @Transactional
    public CompanyUser inviteCollaborator(Long companyId, Long userId, String role) {
        if (companyUserRepository.existsByCompanyIdAndUserId(companyId, userId)) {
            throw new IllegalArgumentException("User is already associated with this company");
        }

        CompanyUser invitation = CompanyUser.invite(companyId, userId, role);
        CompanyUser accepted = invitation.accept(); // Auto-accept for now (can be changed to pending invitations)
        return companyUserRepository.save(accepted);
    }

    /**
     * Lists all active collaborators for a company (Epic 10).
     *
     * @param companyId Company ID
     * @return List of active collaborators
     */
    public List<CompanyUser> listCollaborators(Long companyId) {
        return companyUserRepository.findAllActiveByCompanyId(companyId);
    }

    /**
     * Removes a collaborator from a company (Epic 10).
     *
     * @param companyId Company ID
     * @param userId User ID to remove
     */
    @Transactional
    public void removeCollaborator(Long companyId, Long userId) {
        CompanyUser association = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Association not found"));

        CompanyUser deactivated = association.deactivate();
        companyUserRepository.save(deactivated);
    }

    /**
     * Promotes a collaborator to ADMIN role (Epic 10).
     *
     * @param companyId Company ID
     * @param userId User ID to promote
     */
    @Transactional
    public void promoteToAdmin(Long companyId, Long userId) {
        CompanyUser association = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Association not found"));

        CompanyUser promoted = association.updateRole("ADMIN");
        companyUserRepository.save(promoted);
    }
}
