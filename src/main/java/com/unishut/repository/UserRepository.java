package com.unishut.repository;

import com.unishut.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email.
     *
     * Used during login and authentication.
     */
    Optional<User> findByEmail(String email);


    /**
     * Check whether an email is already registered.
     */
    boolean existsByEmail(String email);


    /**
     * Check whether a university ID is already registered.
     *
     * Mainly useful for student registration.
     */
    boolean existsByUniversityId(String universityId);


    /**
     * Find all users belonging to a specific role.
     *
     * Examples:
     * STUDENT
     * DRIVER
     * ADMIN
     */
    java.util.List<User> findByRole(String role);


    /**
     * Find all active users with a specific role.
     */
    java.util.List<User> findByRoleAndEnabledTrue(String role);
}