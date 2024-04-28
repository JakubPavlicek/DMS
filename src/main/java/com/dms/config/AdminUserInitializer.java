package com.dms.config;

import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This class is responsible for initializing the admin user.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Component
public class AdminUserInitializer {

    /** Private constructor to prevent instantiation of this utility class. */
    private AdminUserInitializer() {
    }

    /**
     * Initializes the admin user if it does not already exist in the system.
     * If an admin user with the specified email already exists, this method does nothing.
     *
     * @param userRepository the repository for managing user data
     * @param adminProperties the properties containing admin user details such as email, name, and password
     * @param passwordEncoder the encoder for encoding passwords before storing them
     * @return a {@link CommandLineRunner} bean to execute the initialization process
     */
    @Bean
    public CommandLineRunner commandLineRunner(UserRepository userRepository, AdminUserProperties adminProperties, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> adminByEmail = userRepository.findByEmail(adminProperties.getEmail());

            // admin already exists so we dont want to create another admin
            if (adminByEmail.isPresent()) {
                return;
            }

            User admin = User.builder()
                             .name(adminProperties.getName())
                             .email(adminProperties.getEmail())
                             .password(passwordEncoder.encode(adminProperties.getPassword()))
                             .role(Role.ADMIN)
                             .build();

            userRepository.save(admin);
        };
    }

}
