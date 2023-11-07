package com.dms.service;

import com.dms.dto.UserDTO;
import com.dms.entity.User;
import com.dms.mapper.dto.UserDTOMapper;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                             .orElseThrow(() -> new RuntimeException("User with email: " + username + " not found"));
    }

    public UserDTO register(UserDTO userDTO) {
        User user = UserDTOMapper.mapToUser(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return UserDTOMapper.mapToUserDTO(savedUser);
    }

    public String token(UserDTO user) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        return tokenService.generateToken(authentication);
    }

    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName();
    }

    public User getAuthenticatedUser() {
        String authUserEmail = getAuthenticatedUserEmail();
        return userRepository.findByEmail(authUserEmail)
                             .orElseThrow(() -> new RuntimeException("User with email: " + authUserEmail + " not found"));
    }

}
