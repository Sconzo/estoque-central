package com.estoquecentral.auth.application;

import com.estoquecentral.auth.adapter.out.PublicUserRepository;
import com.estoquecentral.auth.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * PublicUserService - Application service for public.users management
 *
 * <p>Handles user lifecycle operations for global authentication (public.users table):
 * <ul>
 *   <li>Find or create user from Google OAuth data</li>
 *   <li>Update existing user information</li>
 *   <li>Retrieve user by ID, Google ID, or email</li>
 * </ul>
 *
 * <p><strong>Important:</strong> This service operates on the PUBLIC schema (not tenant schemas).
 * For tenant-specific users, use {@link UserService}.
 *
 * @see User
 * @see PublicUserRepository
 * @see GoogleAuthService
 * @since 1.0
 */
@Service
public class PublicUserService {

    private static final Logger logger = LoggerFactory.getLogger(PublicUserService.class);

    private final PublicUserRepository publicUserRepository;

    public PublicUserService(PublicUserRepository publicUserRepository) {
        this.publicUserRepository = publicUserRepository;
    }

    /**
     * Finds or creates a user from Google OAuth data in public.users table.
     *
     * <p>If user with googleId exists:
     * <ul>
     *   <li>Update nome (may have changed in Google)</li>
     *   <li>Update ultimo_login timestamp</li>
     *   <li>Return existing user</li>
     * </ul>
     *
     * <p>If user doesn't exist:
     * <ul>
     *   <li>Create new user in public.users</li>
     *   <li>Set ultimo_login to current timestamp</li>
     *   <li>Return new user</li>
     * </ul>
     *
     * @param googleId   Google user ID (sub claim from Google ID token)
     * @param email      User email from Google
     * @param nome       User name from Google
     * @return the created or updated User
     */
    @Transactional
    public User findOrCreateUser(String googleId, String email, String nome) {
        logger.debug("Finding or creating user in public.users: googleId={}, email={}", googleId, email);

        // Check if user already exists by googleId
        Optional<User> existingUser = publicUserRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            // User exists - update information
            User user = existingUser.get();
            logger.info("User found in public.users by googleId: {} - updating information", googleId);

            user.updateFromGoogle(nome);
            user.updateLastLogin();
            user = publicUserRepository.save(user);

            logger.info("User updated successfully in public.users: id={}, email={}", user.getId(), user.getEmail());
            return user;
        }

        // User doesn't exist - create new
        logger.info("User not found in public.users by googleId: {} - creating new user", googleId);

        User newUser = new User(nome, email, googleId);
        newUser.updateLastLogin();
        newUser = publicUserRepository.save(newUser);

        logger.info("New user created successfully in public.users: id={}, email={}", newUser.getId(), newUser.getEmail());

        return newUser;
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId User ID
     * @return the User
     * @throws IllegalArgumentException if user not found
     */
    public User getUserById(UUID userId) {
        logger.debug("Retrieving user from public.users by ID: {}", userId);

        return publicUserRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found in public.users: {}", userId);
                    return new IllegalArgumentException("User not found: " + userId);
                });
    }

    /**
     * Retrieves a user by Google ID.
     *
     * @param googleId Google user ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByGoogleId(String googleId) {
        logger.debug("Retrieving user from public.users by googleId: {}", googleId);
        return publicUserRepository.findByGoogleId(googleId);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email User email
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByEmail(String email) {
        logger.debug("Retrieving user from public.users by email: {}", email);
        return publicUserRepository.findByEmail(email);
    }

    /**
     * Checks if a user is active.
     *
     * @param userId User ID
     * @return true if user exists and is active, false otherwise
     */
    public boolean isUserActive(UUID userId) {
        return publicUserRepository.findById(userId)
                .map(User::getAtivo)
                .orElse(false);
    }

    /**
     * Deactivates a user (soft delete).
     *
     * @param userId User ID
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public void deactivateUser(UUID userId) {
        logger.info("Deactivating user in public.users: {}", userId);

        User user = getUserById(userId);
        user.setAtivo(false);
        publicUserRepository.save(user);

        logger.info("User deactivated successfully in public.users: {}", userId);
    }

    /**
     * Activates a user.
     *
     * @param userId User ID
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public void activateUser(UUID userId) {
        logger.info("Activating user in public.users: {}", userId);

        User user = getUserById(userId);
        user.setAtivo(true);
        publicUserRepository.save(user);

        logger.info("User activated successfully in public.users: {}", userId);
    }
}
