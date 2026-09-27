package com.unishut.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for uniShut user information.
 *
 * IMPORTANT:
 * This DTO intentionally does NOT contain the user's password.
 *
 * It can be used for:
 *
 * - Student profile
 * - Driver profile
 * - Admin user management
 * - Authentication response
 * - User information displayed by the frontend
 */
public class UserDTO {

    /**
     * User database ID.
     */
    private Long id;


    /**
     * User's full name.
     */
    @NotBlank(message = "Name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Name must be between 2 and 100 characters"
    )
    private String name;


    /**
     * User's registered email.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters"
    )
    private String email;


    /**
     * User's system role.
     *
     * Expected values:
     *
     * STUDENT
     * DRIVER
     * ADMIN
     */
    @NotBlank(message = "Role is required")
    private String role;


    /**
     * University ID.
     *
     * Mainly applicable to students.
     */
    @Size(
            max = 50,
            message = "University ID must not exceed 50 characters"
    )
    private String universityId;


    /**
     * Indicates whether the account is active.
     */
    private boolean enabled;


    /**
     * Required by Jackson.
     */
    public UserDTO() {
    }


    /**
     * Constructor for creating a user response.
     */
    public UserDTO(
            Long id,
            String name,
            String email,
            String role,
            String universityId,
            boolean enabled
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.universityId = universityId;
        this.enabled = enabled;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getRole() {
        return role;
    }


    public void setRole(String role) {
        this.role = role;
    }


    public String getUniversityId() {
        return universityId;
    }


    public void setUniversityId(String universityId) {
        this.universityId = universityId;
    }


    public boolean isEnabled() {
        return enabled;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}