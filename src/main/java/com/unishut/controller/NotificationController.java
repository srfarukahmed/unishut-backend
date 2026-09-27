package com.unishut.controller;

import com.unishut.dto.NotificationDTO;
import com.unishut.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/notifications")
public class NotificationController {


    private final NotificationService notificationService;


    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }



    /**
     * Get all notifications of a user
     *
     * GET /api/notifications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                notificationService.getUserNotifications(
                        userId
                )
        );
    }



    /**
     * Get unread notifications
     *
     * GET /api/notifications/user/{userId}/unread
     */
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(
                        userId
                )
        );
    }



    /**
     * Get notification by id
     *
     * GET /api/notifications/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<NotificationDTO> getNotificationById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificationService.getNotificationById(
                        id
                )
        );
    }



    /**
     * Mark notification as read
     *
     * PATCH /api/notifications/{userId}/{id}/read
     */
    @PatchMapping("/{userId}/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        id,
                        userId
                )
        );
    }



    /**
     * Delete notification
     *
     * DELETE /api/notifications/{userId}/{id}
     */
    @DeleteMapping("/{userId}/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long userId,
            @PathVariable Long id
    ) {

        notificationService.deleteNotification(
                id,
                userId
        );

        return ResponseEntity
                .noContent()
                .build();
    }



    /**
     * Delete all notifications of user
     *
     * DELETE /api/notifications/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<Void> deleteAllNotifications(
            @PathVariable Long userId
    ) {

        notificationService.deleteAllUserNotifications(
                userId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

}