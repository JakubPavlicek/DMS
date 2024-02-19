package com.dms.unit.service;

import com.dms.dto.UserDTO;
import com.dms.dto.UserLoginDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.User;
import com.dms.exception.EmailAlreadyExistsException;
import com.dms.exception.UserNotFoundException;
import com.dms.repository.UserRepository;
import com.dms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                   .userId("0e60c305-f63c-4a69-9d93-e73cebd2c070")
                   .name("james")
                   .email("james@gmail.com")
                   .password("secret123!")
                   .build();
    }

    @Test
    void shouldReturnUserByEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(user.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());

        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUsernameIsInvalid() {
        String email = user.getEmail();

        assertThatThrownBy(() -> userService.loadUserByUsername(email)).isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void shouldReturnAuthenticatedUser() {
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User authenticatedUser = userService.getAuthenticatedUser();

        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getEmail()).isEqualTo(user.getEmail());

        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserIsNotAuthenticated() {
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        assertThatThrownBy(() -> userService.getAuthenticatedUser()).isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    void shouldCreateUser() {
        String hashedPassword = "$2a$10$j53AK./k2ElWhOSsSB757.8WpiGa3naFdVYW.GRWx5kTL77TRCCpG";

        UserRegisterDTO userRegisterDTO = UserRegisterDTO.builder()
                                                         .name("james")
                                                         .email("james@gmail.com")
                                                         .password("secret123!")
                                                         .build();

        UserDTO userDTO = UserDTO.builder()
                                 .userId("0e60c305-f63c-4a69-9d93-e73cebd2c070")
                                 .name("james")
                                 .email("james@gmail.com")
                                 .build();

        User savedUser = User.builder()
                             .id(1L)
                             .userId("0e60c305-f63c-4a69-9d93-e73cebd2c070")
                             .name("james")
                             .email("james@gmail.com")
                             .password(hashedPassword)
                             .build();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(passwordEncoder.encode(any())).thenReturn(hashedPassword);

        UserDTO actualUser = userService.createUser(userRegisterDTO);

        assertThat(actualUser).isEqualTo(userDTO);

        verify(userRepository, times(1)).existsByEmail(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
        UserRegisterDTO userRegisterDTO = UserRegisterDTO.builder()
                                                         .name("james")
                                                         .email("james@gmail.com")
                                                         .password("secret123!")
                                                         .build();

        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRegisterDTO)).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, times(1)).existsByEmail(any());
    }

    @Test
    void shouldReturnCurrentUser() {
        UserDTO userDTO = UserDTO.builder()
                                 .userId("0e60c305-f63c-4a69-9d93-e73cebd2c070")
                                 .name("james")
                                 .email("james@gmail.com")
                                 .build();

        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDTO currentUser = userService.getCurrentUser();

        assertThat(currentUser).isEqualTo(userDTO);

        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    void shouldChangePassword() {
        String hashedPassword = "$2a$10$tulEZFULNzQ5.uzak/TR9OOIIDA57K7DRijI2BruMmMty5IKSyDwO";

        UserLoginDTO userLoginDTO = UserLoginDTO.builder()
                                                .email("james@gmail.com")
                                                .password("password123!")
                                                .build();

        User changedPasswordUser = User.builder()
                                       .name("james")
                                       .email("james@gmail.com")
                                       .password(hashedPassword)
                                       .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn(hashedPassword);
        when(userRepository.save(any())).thenReturn(changedPasswordUser);

        userService.changePassword(userLoginDTO);

        assertThat(user.getPassword()).isEqualTo(hashedPassword);

        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserLoginIsInvalid() {
        UserLoginDTO userLoginDTO = UserLoginDTO.builder()
                                                .email("james@gmail.com")
                                                .password("password123!")
                                                .build();

        when(userRepository.findByEmail(any())).thenThrow(UserNotFoundException.class);

        assertThatThrownBy(() -> userService.changePassword(userLoginDTO)).isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

}