package com.unishut.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password hashing configuration for uniShut.
 *
 * Kept separate from SecurityConfig so services can use
 * PasswordEncoder without creating a circular dependency
 * with the JWT security filter.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt password encoder.
     *
     * Passwords must never be stored as plain text.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}