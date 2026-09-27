package com.unishut.controller;

import com.unishut.dto.UserDTO;
import com.unishut.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for authenticated user operations.
 *
 * Base URL:
 *
 * /api/users
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }


    /**
     * Return the currently authenticated user's profile.
     *
     * GET /api/users/me
     *
     * Requires:
     * Authorization: Bearer <JWT>
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity.status(401).build();
        }


        String email = authentication.getName();


        UserDTO user =
                userService.getUserByEmail(email);


        return ResponseEntity.ok(user);
    }



    /**
     * Get all users.
     *
     * ADMIN only.
     *
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

}