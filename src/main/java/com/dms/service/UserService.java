package com.dms.service;

import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.exception.EmailAlreadyExistsException;
import com.dms.exception.UserNotFoundException;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user-related operations.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class UserService {

    /** Repository for user data access. */
    private final UserRepository userRepository;
    /** Encoder for password hashing. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves the authenticated user from the security context.
     *
     * @return the authenticated user
     */
    public User getAuthenticatedUser() {
        String authUserEmail = SecurityContextHolder.getContext()
                                                    .getAuthentication()
                                                    .getName();
        return getUserByEmail(authUserEmail);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user
     * @return the user
     * @throws UserNotFoundException if no user with the given email is found
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
    }

    /**
     * Validates that the provided email is unique.
     *
     * @param email the email address to validate
     * @throws EmailAlreadyExistsException if the email is already taken
     */
    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email: " + email + " is already taken");
        }
    }

    /**
     * Creates a new user in the system.
     *
     * @param user the user to be created
     * @return the created user
     */
    public User createUser(User user) {
        log.debug("Request - Creating user");

        // ensure that email is unique
        validateUniqueEmail(user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        log.info("Successfully created user {}", savedUser.getUserId());

        return savedUser;
    }

    /**
     * Retrieves the current authenticated user.
     *
     * @return the current authenticated user
     */
    public User getCurrentUser() {
        log.debug("Request - Retrieving current user");

        User authUser = getAuthenticatedUser();

        log.info("Successfully retrieved current user");

        return authUser;
    }

    /**
     * Changes the password of a user.
     *
     * @param email the email address of the user
     * @param password the new password
     */
    public void changePassword(String email, String password) {
        log.debug("Request - Changing users password");

        User user = getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        log.info("Successfully changed user's {} password", user.getUserId());
    }

}
