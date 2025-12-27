package com.estoquecentral.auth.adapter.out;

import com.estoquecentral.auth.domain.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity (public.users table).
 *
 * <p>This repository operates on the PUBLIC schema (not tenant schemas).
 * It manages global user authentication records.
 *
 * @see User
 * @since 1.0
 */
@Repository
public interface PublicUserRepository extends CrudRepository<User, Long> {

    /**
     * Finds a user by Google OAuth ID.
     *
     * @param googleId Google user ID (sub claim from Google ID token)
     * @return Optional containing user if found
     */
    @Query("SELECT * FROM public.users WHERE google_id = :googleId")
    Optional<User> findByGoogleId(@Param("googleId") String googleId);

    /**
     * Finds a user by email.
     *
     * @param email User email
     * @return Optional containing user if found
     */
    @Query("SELECT * FROM public.users WHERE email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Checks if a user with the given Google ID exists.
     *
     * @param googleId Google user ID
     * @return true if user exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM public.users WHERE google_id = :googleId")
    boolean existsByGoogleId(@Param("googleId") String googleId);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email User email
     * @return true if user exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM public.users WHERE email = :email")
    boolean existsByEmail(@Param("email") String email);
}
