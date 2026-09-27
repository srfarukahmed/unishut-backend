package com.unishut.service;

import com.unishut.dto.RegisterRequest;
import com.unishut.dto.UserDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.exception.ResourceNotFoundException;
import com.unishut.model.User;
import com.unishut.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Service responsible for user-related business logic.
 *
 * Responsibilities:
 *
 * - User registration
 * - User lookup
 * - Duplicate email validation
 * - University ID validation
 * - Password hashing
 * - User activation/deactivation
 * - User-to-DTO conversion
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    /**
     * Constructor injection.
     */
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Register a new STUDENT or DRIVER.
     *
     * ADMIN accounts are intentionally not created through
     * public registration.
     *
     * @param request registration request
     * @return created user DTO
     */
    public UserDTO registerUser(
            RegisterRequest request
    ) {

        String email = normalizeEmail(
                request.getEmail()
        );

        String role = normalizeRole(
                request.getRole()
        );


        /*
         * Public registration only allows STUDENT and DRIVER.
         *
         * RegisterRequest already validates this at the
         * controller level, but we check again here because
         * service methods should not blindly trust callers.
         */
        if (!role.equals("STUDENT")
                && !role.equals("DRIVER")) {

            throw new BadRequestException(
                    "Only STUDENT and DRIVER accounts can be registered"
            );
        }


        /*
         * Prevent duplicate email accounts.
         */
        if (userRepository.existsByEmail(email)) {

            throw new BadRequestException(
                    "Email is already registered"
            );
        }


        /*
         * University ID is required for students.
         */
        String universityId = normalizeNullable(
                request.getUniversityId()
        );

        if (role.equals("STUDENT")
                && universityId == null) {

            throw new BadRequestException(
                    "University ID is required for student registration"
            );
        }


        /*
         * University ID must be unique when supplied.
         */
        if (universityId != null
                && userRepository.existsByUniversityId(
                universityId
        )) {

            throw new BadRequestException(
                    "University ID is already registered"
            );
        }


        /*
         * Never store the raw password.
         */
        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );


        User user = new User();

        user.setName(
                request.getName().trim()
        );

        user.setEmail(email);

        user.setPassword(encodedPassword);

        user.setRole(role);

        user.setUniversityId(universityId);

        user.setEnabled(true);


        User savedUser = userRepository.save(user);

        return toDTO(savedUser);
    }


    /**
     * Find a user by database ID.
     *
     * @param id user ID
     * @return user DTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(
            Long id
    ) {

        User user = getUserEntityById(id);

        return toDTO(user);
    }


    /**
     * Find a user by email.
     *
     * This method is useful for authentication and
     * administrative operations.
     *
     * @param email user email
     * @return user DTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(
            String email
    ) {

        User user = getUserEntityByEmail(email);

        return toDTO(user);
    }


    /**
     * Find all users.
     *
     * @return list of user DTOs
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find users by role.
     *
     * Examples:
     *
     * STUDENT
     * DRIVER
     * ADMIN
     *
     * @param role user role
     * @return matching users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(
            String role
    ) {

        String normalizedRole = normalizeRole(role);

        return userRepository
                .findByRole(normalizedRole)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find all active users belonging to a role.
     *
     * @param role user role
     * @return active users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsersByRole(
            String role
    ) {

        String normalizedRole = normalizeRole(role);

        return userRepository
                .findByRoleAndEnabledTrue(normalizedRole)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Enable a user account.
     *
     * @param id user ID
     * @return updated user
     */
    public UserDTO enableUser(
            Long id
    ) {

        User user = getUserEntityById(id);

        user.setEnabled(true);

        return toDTO(
                userRepository.save(user)
        );
    }


    /**
     * Disable a user account.
     *
     * @param id user ID
     * @return updated user
     */
    public UserDTO disableUser(
            Long id
    ) {

        User user = getUserEntityById(id);

        user.setEnabled(false);

        return toDTO(
                userRepository.save(user)
        );
    }


    /**
     * Update a user's display name.
     *
     * Email, role, and password are intentionally not
     * changed through this method.
     *
     * @param id user ID
     * @param name new name
     * @return updated user
     */
    public UserDTO updateName(
            Long id,
            String name
    ) {

        if (name == null || name.isBlank()) {

            throw new BadRequestException(
                    "Name cannot be empty"
            );
        }


        String normalizedName = name.trim();

        if (normalizedName.length() < 2) {

            throw new BadRequestException(
                    "Name must contain at least 2 characters"
            );
        }


        if (normalizedName.length() > 100) {

            throw new BadRequestException(
                    "Name must not exceed 100 characters"
            );
        }


        User user = getUserEntityById(id);

        user.setName(normalizedName);

        return toDTO(
                userRepository.save(user)
        );
    }


    /**
     * Change a user's password.
     *
     * The new password is encoded using BCrypt before
     * being stored.
     *
     * @param id user ID
     * @param newPassword new raw password
     */
    public void changePassword(
            Long id,
            String newPassword
    ) {

        if (newPassword == null
                || newPassword.isBlank()) {

            throw new BadRequestException(
                    "Password cannot be empty"
            );
        }


        if (newPassword.length() < 6) {

            throw new BadRequestException(
                    "Password must contain at least 6 characters"
            );
        }


        if (newPassword.length() > 100) {

            throw new BadRequestException(
                    "Password must not exceed 100 characters"
            );
        }


        User user = getUserEntityById(id);

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }


    /**
     * Internal helper for retrieving the actual User entity.
     *
     * Services that need to create relationships with User
     * can use this method inside the service layer.
     *
     * @param id user ID
     * @return User entity
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(
            Long id
    ) {

        if (id == null) {

            throw new BadRequestException(
                    "User ID is required"
            );
        }


        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );
    }


    /**
     * Internal helper for retrieving a user by email.
     *
     * @param email email address
     * @return User entity
     */
    @Transactional(readOnly = true)
    public User getUserEntityByEmail(
            String email
    ) {

        String normalizedEmail =
                normalizeEmail(email);


        return userRepository.findByEmail(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: "
                                        + normalizedEmail
                        )
                );
    }


    /**
     * Check whether an email is already registered.
     *
     * @param email email address
     * @return true if registered
     */
    @Transactional(readOnly = true)
    public boolean emailExists(
            String email
    ) {

        return userRepository.existsByEmail(
                normalizeEmail(email)
        );
    }


    /**
     * Check whether a university ID is already registered.
     *
     * @param universityId university ID
     * @return true if registered
     */
    @Transactional(readOnly = true)
    public boolean universityIdExists(
            String universityId
    ) {

        String normalizedUniversityId =
                normalizeNullable(universityId);

        if (normalizedUniversityId == null) {
            return false;
        }

        return userRepository.existsByUniversityId(
                normalizedUniversityId
        );
    }


    /**
     * Convert a User entity into a safe UserDTO.
     *
     * Password is deliberately excluded.
     */
    public UserDTO toDTO(
            User user
    ) {

        if (user == null) {
            return null;
        }


        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getUniversityId(),
                user.isEnabled()
        );
    }


    /**
     * Normalize email addresses consistently.
     *
     * This prevents simple duplicate accounts such as:
     *
     * User@Example.com
     * user@example.com
     */
    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new BadRequestException(
                    "Email is required"
            );
        }


        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }


    /**
     * Normalize role values.
     */
    private String normalizeRole(
            String role
    ) {

        if (role == null || role.isBlank()) {

            throw new BadRequestException(
                    "Role is required"
            );
        }


        return role
                .trim()
                .toUpperCase(Locale.ROOT);
    }


    /**
     * Convert blank strings into null.
     */
    private String normalizeNullable(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}