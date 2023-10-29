package com.dms.service;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getSavedUser(User user) {
        String username = user.getUsername();
        String email = user.getEmail();

        return userRepository.findByUsernameAndEmail(username, email)
                             .orElseGet(() -> userRepository.save(user));
    }

}
