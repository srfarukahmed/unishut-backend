package com.unishut.service;

import com.unishut.dto.AuthResponse;
import com.unishut.dto.LoginRequest;
import com.unishut.dto.RegisterRequest;
import com.unishut.dto.UserDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.model.User;
import com.unishut.repository.UserRepository;
import com.unishut.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service for UniShut.
 *
 * Responsibilities:
 *
 * - User registration
 * - Credential verification
 * - JWT generation
 * - Authentication response creation
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserService userService;


    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }


    /**
     * Register a new student or driver and return
     * an authenticated JWT response.
     */
    public AuthResponse register(
            RegisterRequest request
    ) {

        UserDTO user =
                userService.registerUser(request);

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole()
                );

        return new AuthResponse(
                token,
                user
        );
    }


    /**
     * Authenticate an existing user.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(
            LoginRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Login request is required"
            );
        }


        if (request.getEmail() == null
                || request.getEmail().isBlank()) {

            throw new BadRequestException(
                    "Email is required"
            );
        }


        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new BadRequestException(
                    "Password is required"
            );
        }


        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid email or password"
                                )
                        );


        if (!user.isEnabled()) {

            throw new BadRequestException(
                    "User account is disabled"
            );
        }


        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );


        if (!passwordMatches) {

            throw new BadRequestException(
                    "Invalid email or password"
            );
        }


        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole()
                );


        UserDTO userDTO =
                userService.toDTO(user);


        return new AuthResponse(
                token,
                userDTO
        );
    }
}