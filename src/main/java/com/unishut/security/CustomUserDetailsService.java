package com.unishut.security;

import com.unishut.model.User;
import com.unishut.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads UniShut users for Spring Security authentication.
 *
 * The application uses email addresses as usernames.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }


    /**
     * Load a user by email.
     */
    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException(
                    "Email is required"
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        User user =
                userRepository.findByEmail(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );


        String role =
                user.getRole() == null
                        ? ""
                        : user.getRole()
                                .trim()
                                .toUpperCase();


        List<GrantedAuthority> authorities =
                role.isBlank()
                        ? List.of()
                        : List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        );


        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }
}