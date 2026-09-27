package com.unishut.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body used when creating a new uniShut account.
 *
 * Supported roles:
 *
 * STUDENT
 * DRIVER
 *
 * ADMIN accounts should normally be created by the
 * system administrator rather than through public registration.
 */
public class RegisterRequest {

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
     * User's email address.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters"
    )
    private String email;


    /**
     * Password for the new account.
     */
    @NotBlank(message = "Password is required")
    @Size(
            min = 6,
            max = 100,
            message = "Password must be between 6 and 100 characters"
    )
    private String password;


    /**
     * Account role.
     *
     * Allowed values:
     *
     * STUDENT
     * DRIVER
     *
     * ADMIN registration is intentionally not accepted
     * through this request.
     */
    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "STUDENT|DRIVER",
            message = "Role must be either STUDENT or DRIVER"
    )
    private String role;


    /**
     * University ID.
     *
     * Required for students.
     *
     * For drivers this field may be null.
     */
    @Size(
            max = 50,
            message = "University ID must not exceed 50 characters"
    )
    private String universityId;


    /**
     * Required by Jackson.
     */
    public RegisterRequest() {
    }


    /**
     * Constructor for convenient programmatic use.
     */
    public RegisterRequest(
            String name,
            String email,
            String password,
            String role,
            String universityId
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.universityId = universityId;
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


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
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
}