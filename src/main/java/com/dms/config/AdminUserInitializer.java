package com.dms.config;

import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminUserInitializer {

    @Bean
    public CommandLineRunner commandLineRunner(UserRepository userRepository, AdminUserProperties adminProperties, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> adminByEmail = userRepository.findByEmail(adminProperties.getEmail());

            // admin already exists so we dont want to create another admin
            if (adminByEmail.isPresent())
                return;

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
