package com.unishut.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body used when a user logs into the uniShut system.
 *
 * Supported users:
 *
 * STUDENT
 * DRIVER
 * ADMIN
 */
public class LoginRequest {

    /**
     * User's registered email address.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;


    /**
     * User's account password.
     */
    @NotBlank(message = "Password is required")
    @Size(
            min = 6,
            max = 100,
            message = "Password must be between 6 and 100 characters"
    )
    private String password;


    /**
     * Required by Spring/Jackson when converting
     * the incoming JSON request into this object.
     */
    public LoginRequest() {
    }


    /**
     * Constructor for convenient programmatic use.
     */
    public LoginRequest(
            String email,
            String password
    ) {
        this.email = email;
        this.password = password;
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
}