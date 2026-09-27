package com.unishut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User entity for the uniShut system.
 *
 * Supported roles:
 * STUDENT
 * DRIVER
 * ADMIN
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's full name.
     */
    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    /**
     * User's login email.
     */
    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    /**
     * BCrypt encoded password.
     *
     * Plain-text passwords must never be stored here.
     */
    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    private String password;

    /**
     * User role.
     *
     * Expected values:
     * STUDENT
     * DRIVER
     * ADMIN
     */
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private String role;

    /**
     * University ID.
     *
     * Mainly used for students.
     * It can remain null for ADMIN and DRIVER accounts.
     */
    @Column(
            name = "university_id",
            unique = true,
            length = 50
    )
    private String universityId;

    /**
     * Whether the account is active.
     */
    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled = true;

    /**
     * Account creation time.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    /**
     * Last account update time.
     */
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    /**
     * JPA requires a no-argument constructor.
     */
    public User() {
    }


    /**
     * Constructor for creating a new user.
     */
    public User(
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
        this.enabled = true;
    }


    /**
     * Set creation and update timestamps automatically
     * before the entity is inserted into the database.
     */
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }


    /**
     * Update the modification timestamp automatically
     * before the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
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


    public boolean isEnabled() {
        return enabled;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}