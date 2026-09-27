package com.unishut.service;

import com.unishut.dto.NotificationDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.exception.ResourceNotFoundException;
import com.unishut.model.Notification;
import com.unishut.model.User;
import com.unishut.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for notification-related business logic.
 *
 * Responsibilities:
 *
 * - Create notifications
 * - Retrieve user notifications
 * - Retrieve unread notifications
 * - Count unread notifications
 * - Mark notifications as read
 * - Mark all notifications as read
 * - Delete notifications
 * - Create predefined notification types
 * - Convert Notification entities to DTOs
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserService userService;


    /**
     * Constructor injection.
     */
    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService
    ) {
        this.notificationRepository =
                notificationRepository;

        this.userService =
                userService;
    }


    /**
     * Create a notification for a user.
     *
     * The Notification entity automatically creates
     * createdAt through its @PrePersist callback.
     *
     * @param userId recipient user ID
     * @param title notification title
     * @param message notification message
     * @param type notification type
     * @return created notification
     */
    public NotificationDTO createNotification(
            Long userId,
            String title,
            String message,
            String type
    ) {

        validateUserId(userId);


        validateText(
                title,
                "Notification title",
                150
        );


        validateText(
                message,
                "Notification message",
                500
        );


        validateText(
                type,
                "Notification type",
                30
        );


        User user =
                userService.getUserEntityById(
                        userId
                );


        Notification notification =
                new Notification();


        notification.setUser(
                user
        );


        notification.setTitle(
                title.trim()
        );


        notification.setMessage(
                message.trim()
        );


        notification.setType(
                type.trim().toUpperCase()
        );


        notification.setRead(
                false
        );


        /*
         * Do NOT manually set createdAt here.
         *
         * Notification.@PrePersist automatically sets:
         *
         * createdAt = LocalDateTime.now()
         */
        Notification savedNotification =
                notificationRepository.save(
                        notification
                );


        return toDTO(
                savedNotification
        );
    }


    /**
     * Get a notification by ID.
     *
     * @param id notification ID
     * @return notification DTO
     */
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(
            Long id
    ) {

        validatePositiveId(
                id,
                "Notification ID"
        );


        Notification notification =
                getNotificationEntity(
                        id
                );


        return toDTO(
                notification
        );
    }


    /**
     * Get all notifications for a user.
     *
     * Newest notifications are returned first.
     *
     * @param userId user ID
     * @return user's notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(
            Long userId
    ) {

        validateUserId(userId);


        User user =
                userService.getUserEntityById(
                        userId
                );


        return notificationRepository
                .findByUserOrderByCreatedAtDesc(
                        user
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Get unread notifications for a user.
     *
     * @param userId user ID
     * @return unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(
            Long userId
    ) {

        validateUserId(userId);


        User user =
                userService.getUserEntityById(
                        userId
                );


        return notificationRepository
                .findByUserAndReadFalseOrderByCreatedAtDesc(
                        user
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Count unread notifications for a user.
     *
     * @param userId user ID
     * @return unread notification count
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(
            Long userId
    ) {

        validateUserId(userId);


        User user =
                userService.getUserEntityById(
                        userId
                );


        return notificationRepository
                .countByUserAndReadFalse(
                        user
                );
    }


    /**
     * Mark one notification as read.
     *
     * @param notificationId notification ID
     * @param userId requesting user's ID
     * @return updated notification
     */
    public NotificationDTO markAsRead(
            Long notificationId,
            Long userId
    ) {

        validatePositiveId(
                notificationId,
                "Notification ID"
        );


        validateUserId(userId);


        Notification notification =
                getNotificationEntity(
                        notificationId
                );


        validateNotificationOwner(
                notification,
                userId
        );


        notification.setRead(
                true
        );


        Notification savedNotification =
                notificationRepository.save(
                        notification
                );


        return toDTO(
                savedNotification
        );
    }


    /**
     * Mark all notifications of a user as read.
     *
     * @param userId user ID
     */
    public void markAllAsRead(
            Long userId
    ) {

        validateUserId(userId);


        User user =
                userService.getUserEntityById(
                        userId
                );


        List<Notification> notifications =
                notificationRepository
                        .findByUserAndReadFalseOrderByCreatedAtDesc(
                                user
                        );


        if (notifications.isEmpty()) {

            return;
        }


        for (Notification notification :
                notifications) {

            notification.setRead(
                    true
            );
        }


        notificationRepository.saveAll(
                notifications
        );
    }


    /**
     * Delete one notification.
     *
     * @param notificationId notification ID
     * @param userId requesting user's ID
     */
    public void deleteNotification(
            Long notificationId,
            Long userId
    ) {

        validatePositiveId(
                notificationId,
                "Notification ID"
        );


        validateUserId(userId);


        Notification notification =
                getNotificationEntity(
                        notificationId
                );


        validateNotificationOwner(
                notification,
                userId
        );


        notificationRepository.delete(
                notification
        );
    }


    /**
     * Delete all notifications belonging to a user.
     *
     * @param userId user ID
     */
    public void deleteAllUserNotifications(
            Long userId
    ) {

        validateUserId(userId);


        User user =
                userService.getUserEntityById(
                        userId
                );


        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(
                                user
                        );


        if (!notifications.isEmpty()) {

            notificationRepository.deleteAll(
                    notifications
            );
        }
    }


    /**
     * Create a reservation-related notification.
     *
     * @param userId recipient user
     * @param message notification message
     * @return created notification
     */
    public NotificationDTO createReservationNotification(
            Long userId,
            String message
    ) {

        return createNotification(
                userId,
                "Reservation Update",
                message,
                "RESERVATION"
        );
    }


    /**
     * Create a trip-related notification.
     *
     * @param userId recipient user
     * @param message notification message
     * @return created notification
     */
    public NotificationDTO createTripNotification(
            Long userId,
            String message
    ) {

        return createNotification(
                userId,
                "Trip Update",
                message,
                "TRIP"
        );
    }


    /**
     * Create a bus-related notification.
     *
     * @param userId recipient user
     * @param message notification message
     * @return created notification
     */
    public NotificationDTO createBusNotification(
            Long userId,
            String message
    ) {

        return createNotification(
                userId,
                "Bus Update",
                message,
                "BUS"
        );
    }


    /**
     * Create a system notification.
     *
     * @param userId recipient user
     * @param message notification message
     * @return created notification
     */
    public NotificationDTO createSystemNotification(
            Long userId,
            String message
    ) {

        return createNotification(
                userId,
                "System Notification",
                message,
                "SYSTEM"
        );
    }


    /**
     * Retrieve the actual Notification entity.
     *
     * @param id notification ID
     * @return notification entity
     */
    @Transactional(readOnly = true)
    public Notification getNotificationEntity(
            Long id
    ) {

        validatePositiveId(
                id,
                "Notification ID"
        );


        return notificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with ID: "
                                        + id
                        )
                );
    }


    /**
     * Convert Notification entity to NotificationDTO.
     *
     * @param notification notification entity
     * @return notification DTO
     */
    public NotificationDTO toDTO(
            Notification notification
    ) {

        if (notification == null) {

            return null;
        }


        Long userId = null;


        if (notification.getUser() != null) {

            userId =
                    notification.getUser().getId();
        }


        return new NotificationDTO(
                notification.getId(),
                userId,
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }


    /**
     * Verify that a notification belongs to
     * the requesting user.
     */
    private void validateNotificationOwner(
            Notification notification,
            Long userId
    ) {

        if (notification.getUser() == null
                || notification.getUser().getId() == null
                || !notification.getUser()
                .getId()
                .equals(userId)) {

            throw new BadRequestException(
                    "You are not allowed to modify this notification"
            );
        }
    }


    /**
     * Validate user ID.
     */
    private void validateUserId(
            Long userId
    ) {

        validatePositiveId(
                userId,
                "User ID"
        );
    }


    /**
     * Validate a positive ID.
     */
    private void validatePositiveId(
            Long id,
            String fieldName
    ) {

        if (id == null || id <= 0) {

            throw new BadRequestException(
                    fieldName
                            + " must be a positive number"
            );
        }
    }


    /**
     * Validate notification text.
     */
    private void validateText(
            String value,
            String fieldName,
            int maxLength
    ) {

        if (value == null
                || value.isBlank()) {

            throw new BadRequestException(
                    fieldName
                            + " is required"
            );
        }


        if (value.trim().length() > maxLength) {

            throw new BadRequestException(
                    fieldName
                            + " must not exceed "
                            + maxLength
                            + " characters"
            );
        }
    }
}