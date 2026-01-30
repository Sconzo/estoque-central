package com.estoquecentral.company.application;

import com.estoquecentral.auth.adapter.out.PublicUserRepository;
import com.estoquecentral.auth.domain.User;
import com.estoquecentral.company.adapter.out.CompanyUserRepository;
import com.estoquecentral.company.domain.CompanyUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    private static final Logger logger = LoggerFactory.getLogger(CollaboratorService.class);

    private final CompanyUserRepository companyUserRepository;
    private final PublicUserRepository publicUserRepository;

    public CollaboratorService(
            CompanyUserRepository companyUserRepository,
            PublicUserRepository publicUserRepository) {
        this.companyUserRepository = companyUserRepository;
        this.publicUserRepository = publicUserRepository;
    }

    /**
     * Invites a user to join a company by email (Story 10.1).
     *
     * <p>This method supports both existing users and new users:
     * <ul>
     *   <li>If user with email exists: creates company_user association</li>
     *   <li>If user doesn't exist: creates placeholder user record with google_id=NULL</li>
     * </ul>
     *
     * @param companyId Company ID
     * @param email User email to invite
     * @param role Role to assign
     * @return Created invitation
     * @throws IllegalArgumentException if user is already invited
     */
    @Transactional
    public CompanyUser inviteCollaboratorByEmail(UUID companyId, String email, String role) {
        logger.info("Inviting collaborator: companyId={}, email={}, role={}", companyId, email, role);

        // Find or create user
        User user = publicUserRepository.findByEmail(email)
                .orElseGet(() -> {
                    logger.info("User not found with email: {}. Creating placeholder user.", email);
                    User newUser = new User(email, email, null); // google_id=NULL for placeholder
                    User savedUser = publicUserRepository.save(newUser);
                    logger.info("Placeholder user created: id={}, email={}", savedUser.getId(), savedUser.getEmail());
                    return savedUser;
                });

        // Check if user is already associated with this company
        if (companyUserRepository.existsByCompanyIdAndUserId(companyId, user.getId())) {
            logger.warn("User already associated with company: companyId={}, userId={}", companyId, user.getId());
            throw new IllegalArgumentException("User is already associated with this company");
        }

        // Create company-user association
        CompanyUser invitation = CompanyUser.invite(companyId, user.getId(), role);
        CompanyUser accepted = invitation.accept(); // Auto-accept for now (MVP simplification)
        CompanyUser saved = companyUserRepository.save(accepted);

        logger.info("Collaborator invited successfully: id={}, userId={}, companyId={}, role={}",
                saved.id(), saved.userId(), saved.companyId(), saved.role());

        return saved;
    }

    /**
     * Invites a user to join a company (Epic 10 - Collaborator management).
     *
     * @param companyId Company ID
     * @param userId User ID to invite
     * @param role Role to assign
     * @return Created invitation
     * @throws IllegalArgumentException if user is already invited
     * @deprecated Use {@link #inviteCollaboratorByEmail(Long, String, String)} instead
     */
    @Deprecated
    @Transactional
    public CompanyUser inviteCollaborator(UUID companyId, UUID userId, String role) {
        if (companyUserRepository.existsByCompanyIdAndUserId(companyId, userId)) {
            throw new IllegalArgumentException("User is already associated with this company");
        }

        CompanyUser invitation = CompanyUser.invite(companyId, userId, role);
        CompanyUser accepted = invitation.accept(); // Auto-accept for now (can be changed to pending invitations)
        return companyUserRepository.save(accepted);
    }

    /**
     * Lists all active collaborators for a company (Story 10.2).
     *
     * <p>Returns CompanyUser entities without user details.
     * Use {@link #listCollaboratorsWithDetails(Long)} for detailed information.
     *
     * @param companyId Company ID
     * @return List of active collaborators
     */
    public List<CompanyUser> listCollaborators(UUID companyId) {
        return companyUserRepository.findAllActiveByCompanyId(companyId);
    }

    /**
     * Lists all active collaborators for a company with user details (Story 10.2 - AC2).
     *
     * <p>This method performs joins between company_users and users tables
     * to provide complete collaborator information including name and email.
     *
     * @param companyId Company ID
     * @return Map of CompanyUser to User entities
     */
    public java.util.Map<CompanyUser, User> listCollaboratorsWithDetails(UUID companyId) {
        logger.debug("Listing collaborators with details for company: {}", companyId);

        List<CompanyUser> collaborators = companyUserRepository.findAllActiveByCompanyId(companyId);

        // Fetch user details for each collaborator
        java.util.Map<CompanyUser, User> result = new java.util.LinkedHashMap<>();
        for (CompanyUser collaborator : collaborators) {
            User user = publicUserRepository.findById(collaborator.userId())
                    .orElseThrow(() -> new IllegalStateException(
                            "User not found for collaborator: userId=" + collaborator.userId()));
            result.put(collaborator, user);
        }

        logger.debug("Found {} collaborators for company {}", result.size(), companyId);
        return result;
    }

    /**
     * Removes a collaborator from a company (Story 10.3 - AC1, AC2, AC3, AC4).
     *
     * <p><strong>Validations:</strong>
     * <ul>
     *   <li>AC3: Prevents self-removal (user cannot remove themselves)</li>
     *   <li>AC4: Prevents removing last admin (company must have at least one admin)</li>
     * </ul>
     *
     * @param companyId Company ID
     * @param userId User ID to remove
     * @param currentUserId ID of the user performing the removal (for AC3)
     * @throws IllegalArgumentException if association not found
     * @throws IllegalStateException if self-removal attempt (AC3) or last admin (AC4)
     */
    @Transactional
    public void removeCollaborator(UUID companyId, UUID userId, UUID currentUserId) {
        logger.info("Removing collaborator: companyId={}, userId={}, requestedBy={}", companyId, userId, currentUserId);

        // AC3: Self-removal protection
        if (userId.equals(currentUserId)) {
            logger.warn("Self-removal attempt blocked: userId={}, companyId={}", userId, companyId);
            throw new IllegalStateException("You cannot remove yourself from the company");
        }

        CompanyUser association = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Association not found"));

        // AC4: Last admin protection
        if ("ADMIN".equalsIgnoreCase(association.role())) {
            long adminCount = countActiveAdmins(companyId);
            if (adminCount <= 1) {
                logger.warn("Last admin removal attempt blocked: userId={}, companyId={}", userId, companyId);
                throw new IllegalStateException("Cannot remove the last admin from the company");
            }
        }

        // AC2: Soft delete (deactivate)
        CompanyUser deactivated = association.deactivate();
        companyUserRepository.save(deactivated);

        logger.info("Collaborator removed successfully: userId={}, companyId={}", userId, companyId);
    }

    /**
     * Counts active admin users in a company.
     *
     * @param companyId Company ID
     * @return Number of active admins
     */
    private long countActiveAdmins(UUID companyId) {
        List<CompanyUser> collaborators = companyUserRepository.findAllActiveByCompanyId(companyId);
        return collaborators.stream()
                .filter(cu -> "ADMIN".equalsIgnoreCase(cu.role()))
                .count();
    }

    /**
     * Promotes a collaborator to ADMIN role (Story 10.4 - AC1, AC2, AC3).
     *
     * <p><strong>Validations:</strong>
     * <ul>
     *   <li>AC2: Prevents promoting user who is already admin</li>
     * </ul>
     *
     * <p><strong>Multi-admin support:</strong>
     * <ul>
     *   <li>AC3: Company can have multiple admins with equal permissions</li>
     * </ul>
     *
     * @param companyId Company ID
     * @param userId User ID to promote
     * @throws IllegalArgumentException if association not found
     * @throws IllegalStateException if user is already admin (AC2)
     */
    @Transactional
    public void promoteToAdmin(UUID companyId, UUID userId) {
        logger.info("Promoting collaborator to ADMIN: companyId={}, userId={}", companyId, userId);

        CompanyUser association = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Association not found"));

        // AC2: Already admin check
        if ("ADMIN".equalsIgnoreCase(association.role())) {
            logger.warn("User is already admin: userId={}, companyId={}", userId, companyId);
            throw new IllegalStateException("User is already an admin");
        }

        // AC1, AC3: Promote to ADMIN (supports multiple admins)
        CompanyUser promoted = association.updateRole("ADMIN");
        companyUserRepository.save(promoted);

        logger.info("Collaborator promoted to ADMIN successfully: userId={}, companyId={}", userId, companyId);
    }

    /**
     * Finds a user by ID.
     *
     * @param userId User ID
     * @return User entity
     * @throws IllegalArgumentException if user not found
     */
    public User findUserById(UUID userId) {
        return publicUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }
}
