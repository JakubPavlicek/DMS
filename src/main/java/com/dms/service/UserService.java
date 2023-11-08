package com.dms.service;

import com.dms.dto.UserDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.Document;
import com.dms.entity.User;
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

    private final DeletionService deletionService;
    private final ManagementService managementService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                             .orElseThrow(() -> new RuntimeException("User with email: " + username + " not found"));
    }

    public String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName();
    }

    public User getAuthenticatedUser() {
        String authUserEmail = getAuthenticatedUserEmail();
        return userRepository.findByEmail(authUserEmail)
                             .orElseThrow(() -> new RuntimeException("User with email: " + authUserEmail + " not found"));
    }

    private User getUserById(String userId) {
        return userRepository.findByUserId(userId)
                             .orElseThrow(() -> new RuntimeException("User with ID: " + userId + " not found"));
    }

    public UserDTO createUser(UserRegisterDTO userRegister) {
        User user = UserDTOMapper.mapToUser(userRegister);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return UserDTOMapper.mapToUserDTO(savedUser);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = getUserById(userId);

        List<Document> documents = managementService.getDocumentsByUser(user);
        documents.forEach(document -> deletionService.deleteDocumentWithRevisions(document.getDocumentId()));

        userRepository.delete(user);
    }

    public UserDTO getCurrentUser() {
        User authenticatedUser = getAuthenticatedUser();
        return UserDTOMapper.mapToUserDTO(authenticatedUser);
    }

    @Transactional
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        User userById = getUserById(userId);
        User userFromDTO = UserDTOMapper.mapToUser(userDTO);

        userById.setName(userFromDTO.getName());
        userById.setEmail(userFromDTO.getEmail());
        userById.setPassword(passwordEncoder.encode(userFromDTO.getPassword()));

        User savedUser = userRepository.save(userById);
        return UserDTOMapper.mapToUserDTO(savedUser);
    }

}
