package com.unishut.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for uniShut notifications.
 *
 * Used by:
 *
 * - Student notification panel
 * - Driver notification panel
 * - Admin notifications
 * - Reservation notifications
 * - Trip notifications
 * - Bus/tracking notifications
 */
public class NotificationDTO {

    /**
     * Notification database ID.
     */
    private Long id;


    /**
     * ID of the user receiving the notification.
     */
    private Long userId;


    /**
     * Notification title.
     */
    @NotBlank(message = "Notification title is required")
    @Size(
            max = 150,
            message = "Notification title must not exceed 150 characters"
    )
    private String title;


    /**
     * Notification message.
     */
    @NotBlank(message = "Notification message is required")
    @Size(
            max = 500,
            message = "Notification message must not exceed 500 characters"
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
    @NotBlank(message = "Notification type is required")
    @Size(
            max = 30,
            message = "Notification type must not exceed 30 characters"
    )
    private String type;


    /**
     * Whether the notification has been read.
     */
    private boolean read;


    /**
     * Notification creation time.
     */
    private LocalDateTime createdAt;


    /**
     * Required by Jackson.
     */
    public NotificationDTO() {
    }


    /**
     * Constructor for creating a notification request.
     */
    public NotificationDTO(
            Long userId,
            String title,
            String message,
            String type
    ) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
    }


    /**
     * Complete constructor for API responses.
     */
    public NotificationDTO(
            Long id,
            Long userId,
            String title,
            String message,
            String type,
            boolean read,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getUserId() {
        return userId;
    }


    public void setUserId(Long userId) {
        this.userId = userId;
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


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}