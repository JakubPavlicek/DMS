package com.dms.service;

import com.dms.dto.UserDTO;
import com.dms.dto.UserLoginDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.User;
import com.dms.exception.EmailAlreadyExistsException;
import com.dms.exception.UserNotFoundException;
import com.dms.mapper.dto.UserDTOMapper;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                             .orElseThrow(() -> new UserNotFoundException("User with email: " + username + " not found"));
    }

    public User getAuthenticatedUser() {
        String authUserEmail = SecurityContextHolder.getContext()
                                                    .getAuthentication()
                                                    .getName();
        return getUserByEmail(authUserEmail);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email: " + email + " is already taken");
        }
    }

    public UserDTO createUser(UserRegisterDTO userRegister) {
        log.debug("Request - Creating user: userRegister={}", userRegister);
        validateUniqueEmail(userRegister.getEmail());

        User user = UserDTOMapper.mapToUser(userRegister);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("Successfully created user {}", savedUser.getUserId());
        return UserDTOMapper.mapToUserDTO(savedUser);
    }

    public UserDTO getCurrentUser() {
        log.debug("Request - Getting current user");
        User authUser = getAuthenticatedUser();
        log.info("Successfully retrieved current user");
        return UserDTOMapper.mapToUserDTO(authUser);
    }

    public void changePassword(UserLoginDTO userLogin) {
        log.debug("Request - Changing users password");
        User user = getUserByEmail(userLogin.getEmail());
        user.setPassword(passwordEncoder.encode(userLogin.getPassword()));
        userRepository.save(user);
        log.info("Successfully changed users {} password", user.getUserId());
    }

}
