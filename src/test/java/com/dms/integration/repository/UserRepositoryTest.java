package com.dms.integration.repository;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                   .userId(UUID.randomUUID().toString())
                   .name("james")
                   .email("james@gmail.com")
                   .password("secret123!")
                   .build();

        userRepository.save(user);
    }

    @Test
    void shouldReturnUserWhenUserFoundByEmail() {
        User userByEmail = userRepository.findByEmail(user.getEmail()).get();
        assertThat(userByEmail).isEqualTo(user);
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFoundByEmail() {
        Optional<User> userByEmail = userRepository.findByEmail("john@gmail.com");
        assertThat(userByEmail).isEmpty();
    }

    @Test
    void shouldReturnUserWhenUserFoundByUserId() {
        User userByUserId = userRepository.findByUserId(user.getUserId()).get();
        assertThat(userByUserId).isEqualTo(user);
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFoundByUserId() {
        Optional<User> userByUserId = userRepository.findByUserId(UUID.randomUUID().toString());
        assertThat(userByUserId).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUserExistsByEmail() {
        assertThat(userRepository.existsByEmail(user.getEmail())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistByEmail() {
        assertThat(userRepository.existsByEmail("john@gmail.com")).isFalse();
    }

}