package com.unishut.security;

import com.unishut.model.User;
import com.unishut.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter for UniShut.
 *
 * Every HTTP request passes through this filter once.
 *
 * Expected header:
 *
 * Authorization: Bearer <JWT>
 *
 * Flow:
 *
 * Request
 *   |
 *   v
 * JwtAuthenticationFilter
 *   |
 *   +--> No token --------> continue request
 *   |
 *   +--> Invalid token ----> continue without authentication
 *   |
 *   +--> Valid token
 *          |
 *          v
 *      Load user
 *          |
 *          v
 *      Create Authentication
 *          |
 *          v
 *      SecurityContext
 */
@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    private static final String AUTHORIZATION_HEADER =
            "Authorization";


    private static final String BEARER_PREFIX =
            "Bearer ";


    private final JwtService jwtService;

    private final UserService userService;


    /**
     * Constructor injection.
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserService userService
    ) {

        this.jwtService =
                jwtService;

        this.userService =
                userService;
    }


    /**
     * Process one HTTP request.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {


        String authorizationHeader =
                request.getHeader(
                        AUTHORIZATION_HEADER
                );


        /*
         * No Authorization header.
         *
         * The request can still continue. Public endpoints
         * do not require authentication.
         */
        if (authorizationHeader == null
                || authorizationHeader.isBlank()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        /*
         * Ignore malformed authorization headers.
         */
        if (!authorizationHeader.startsWith(
                BEARER_PREFIX
        )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        String token =
                authorizationHeader.substring(
                        BEARER_PREFIX.length()
                ).trim();


        /*
         * Do not attempt authentication with an empty token.
         */
        if (token.isBlank()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        try {

            /*
             * Extract the email stored in the JWT subject.
             */
            String email =
                    jwtService.extractUsername(
                            token
                    );


            /*
             * Only authenticate if Spring Security does not
             * already have an authenticated principal.
             */
            if (email != null
                    && !email.isBlank()
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {


                /*
                 * Validate the token cryptographically and
                 * verify its expiration.
                 */
                User user =
                        userService.getUserEntityByEmail(
                                email
                        );


                boolean validToken =
                        jwtService.isTokenValid(
                                token,
                                user.getEmail()
                        );


                /*
                 * Disabled users must not be authenticated
                 * even if they possess an otherwise valid JWT.
                 */
                if (validToken
                        && user.isEnabled()) {


                    String role =
                            user.getRole();


                    /*
                     * Spring Security expects authorities
                     * in the form ROLE_STUDENT,
                     * ROLE_DRIVER, ROLE_ADMIN.
                     */
                    List<SimpleGrantedAuthority>
                            authorities =
                            buildAuthorities(
                                    role
                            );


                    UsernamePasswordAuthenticationToken
                            authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    null,
                                    authorities
                            );


                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(
                                            request
                                    )
                    );


                    /*
                     * Store the authenticated user in the
                     * current SecurityContext.
                     */
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );
                }
            }


        } catch (Exception exception) {

            /*
             * Invalid JWTs are deliberately not turned into
             * an exception here.
             *
             * Spring Security will decide whether the requested
             * endpoint requires authentication.
             */
            SecurityContextHolder
                    .clearContext();
        }


        /*
         * Always continue the filter chain.
         */
        filterChain.doFilter(
                request,
                response
        );
    }


    /**
     * Convert the application's role into a Spring Security
     * authority.
     *
     * Examples:
     *
     * STUDENT -> ROLE_STUDENT
     * DRIVER  -> ROLE_DRIVER
     * ADMIN   -> ROLE_ADMIN
     */
    private List<SimpleGrantedAuthority>
    buildAuthorities(
            String role
    ) {

        if (role == null
                || role.isBlank()) {

            return List.of();
        }


        String normalizedRole =
                role.trim()
                        .toUpperCase();


        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_"
                                + normalizedRole
                )
        );
    }
}