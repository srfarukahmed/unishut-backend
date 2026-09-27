package com.unishut.config;

import com.unishut.model.User;
import com.unishut.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Development-only bootstrap configuration.
 *
 * Creates the initial ADMIN account when no account with
 * the configured email exists.
 *
 * This initializer should be removed or replaced with a
 * secure environment-based provisioning mechanism before
 * production deployment.
 */
@Configuration
public class AdminDataInitializer {

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            String email = "admin@unishut.com";

            if (userRepository.existsByEmail(email)) {
                return;
            }

            User admin = new User();

            admin.setName("UniShut Administrator");
            admin.setEmail(email);

            admin.setPassword(
                    passwordEncoder.encode("Admin1234")
            );

            admin.setRole("ADMIN");

            admin.setUniversityId(null);

            admin.setEnabled(true);

            userRepository.save(admin);

            System.out.println(
                    "--------------------------------------"
            );

            System.out.println(
                    " Initial UniShut ADMIN account created"
            );

            System.out.println(
                    " Email: " + email
            );

            System.out.println(
                    " Password: Admin1234"
            );

            System.out.println(
                    "--------------------------------------"
            );
        };
    }
}