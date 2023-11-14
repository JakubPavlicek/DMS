package com.dms.integration.repository;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                   .userId("24af2142-0183-432a-89a7-78ea84a663f9")
                   .name("james")
                   .email("james@gmail.com")
                   .password("secret123!")
                   .build();

        userRepository.save(user);
    }

    @Test
    void whenValidEmail_thenUserShouldBeFound() {
        assertThat(userRepository.findByEmail(user.getEmail())).isPresent();
    }

    @Test
    void whenInvalidEmail_thenNoUserShouldBeFound() {
        assertThat(userRepository.findByEmail("john@gmail.com")).isEmpty();
    }

    @Test
    void whenValidUserId_thenUserShouldBeFound() {
        assertThat(userRepository.findByUserId(user.getUserId())).isPresent();
    }

    @Test
    void whenInvalidUserId_thenNoUserShouldBeFound() {
        assertThat(userRepository.findByUserId("b3a4f897-cfd3-4f1b-a68c-8c175963f0a0")).isEmpty();
    }

    @Test
    void whenValidEmail_thenUserShouldExist() {
        assertThat(userRepository.existsByEmail(user.getEmail())).isTrue();
    }

    @Test
    void whenInvalidEmail_thenNoUserShouldExist() {
        assertThat(userRepository.existsByEmail("john@gmail.com")).isFalse();
    }

}