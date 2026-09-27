package com.unishut.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a notification sent to a uniShut user.
 *
 * Notifications can be used for:
 *
 * - Trip reminders
 * - Reservation confirmation
 * - Waitlist updates
 * - Bus arrival updates
 * - Trip cancellation
 * - Administrative announcements
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notification_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_notification_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_notification_read",
                        columnList = "is_read"
                )
        }
)
public class Notification {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * User who receives the notification.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_notification_user"
            )
    )
    private User user;


    /**
     * Notification title.
     *
     * Example:
     * "Reservation Confirmed"
     */
    @Column(
            name = "title",
            nullable = false,
            length = 150
    )
    private String title;


    /**
     * Notification message.
     */
    @Column(
            name = "message",
            nullable = false,
            length = 500
    )
    private String message;


    /**
     * Notification type.
     *
     * Examples:
     *
     * RESERVATION
     * TRIP
     * BUS
     * SYSTEM
     * ADMIN
     */
    @Column(
            name = "type",
            nullable = false,
            length = 30
    )
    private String type;


    /**
     * Indicates whether the user has read the notification.
     */
    @Column(
            name = "is_read",
            nullable = false
    )
    private boolean read = false;


    /**
     * Notification creation time.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    /**
     * Required by JPA.
     */
    public Notification() {
    }


    /**
     * Constructor for creating a notification.
     */
    public Notification(
            User user,
            String title,
            String message,
            String type
    ) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
    }


    /**
     * Automatically set the creation time.
     */
    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (type == null || type.isBlank()) {
            type = "SYSTEM";
        }
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public boolean isRead() {
        return read;
    }


    public void setRead(boolean read) {
        this.read = read;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}