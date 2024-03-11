package com.dms.integration.repository;

import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

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
                   .role(Role.USER)
                   .build();

        userRepository.save(user);
    }

    @Test
    void shouldFindUserByEmail() {
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        assertThat(foundUser).isPresent();
    }

    @Test
    void shouldNotFindUserByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("john@gmail.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUserExists() {
        boolean userExists = userRepository.existsByEmail(user.getEmail());

        assertThat(userExists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        boolean userExists = userRepository.existsByEmail("john@gmail.com");

        assertThat(userExists).isFalse();
    }

}