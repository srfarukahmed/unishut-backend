package com.unishut.repository;

import com.unishut.model.Notification;
import com.unishut.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user.
     *
     * Newest notifications are returned first.
     */
    List<Notification> findByUserOrderByCreatedAtDesc(
            User user
    );


    /**
     * Find unread notifications for a specific user.
     *
     * Newest unread notifications are returned first.
     */
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(
            User user
    );


    /**
     * Count unread notifications for a user.
     *
     * Useful for displaying the notification badge
     * in the student, driver, and admin dashboards.
     */
    long countByUserAndReadFalse(
            User user
    );


    /**
     * Find notifications by user and notification type.
     *
     * Examples:
     *
     * RESERVATION
     * TRIP
     * BUS
     * SYSTEM
     * ADMIN
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(
            User user,
            String type
    );


    /**
     * Delete all notifications belonging to a user.
     *
     * This can be used when an account is permanently
     * removed from the system.
     */
    void deleteByUser(User user);
}