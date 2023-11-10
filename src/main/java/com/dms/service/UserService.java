package com.dms.service;

import com.dms.dto.UserDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.Document;
import com.dms.entity.User;
import com.dms.exception.EmailAlreadyExistsException;
import com.dms.exception.UserNotFoundException;
import com.dms.mapper.dto.UserDTOMapper;
import com.dms.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final DocumentCommonService documentCommonService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                             .orElseThrow(() -> new UserNotFoundException("User with email: " + username + " not found"));
    }

    public String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName();
    }

    public User getAuthenticatedUser() {
        String authUserEmail = getAuthenticatedUserEmail();
        return userRepository.findByEmail(authUserEmail)
                             .orElseThrow(() -> new UserNotFoundException("User with email: " + authUserEmail + " not found"));
    }

    private User getUserById(String userId) {
        return userRepository.findByUserId(userId)
                             .orElseThrow(() -> new UserNotFoundException("User with ID: " + userId + " not found"));
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

    @Transactional
    public void deleteUser(String userId) {
        log.debug("Request - Deleting user: userId={}", userId);
        User user = getUserById(userId);

        List<Document> documents = documentCommonService.getDocumentsByAuthor(user);
        documents.forEach(documentCommonService::deleteDocumentWithRevisions);

        userRepository.delete(user);
        log.info("Successfully deleted user {} and all his documents", userId);
    }

    public UserDTO getCurrentUser() {
        log.debug("Request - Getting current user");
        User authenticatedUser = getAuthenticatedUser();
        log.info("Successfully retrieved current user");
        return UserDTOMapper.mapToUserDTO(authenticatedUser);
    }

    @Transactional
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        log.debug("Request - Updating user: userId={}, userDTO={}", userId, userDTO);
        validateUniqueEmail(userDTO.getEmail());

        User userById = getUserById(userId);
        User userFromDTO = UserDTOMapper.mapToUser(userDTO);

        userById.setName(userFromDTO.getName());
        userById.setEmail(userFromDTO.getEmail());
        userById.setPassword(passwordEncoder.encode(userFromDTO.getPassword()));

        User savedUser = userRepository.save(userById);

        log.info("Successfully updated user {}", userId);
        return UserDTOMapper.mapToUserDTO(savedUser);
    }

}
