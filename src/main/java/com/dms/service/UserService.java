package com.dms.service;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(User user) {
        String username = user.getUsername();
        String email = user.getEmail();

        return userRepository.findByUsernameAndEmail(username, email)
                             .orElse(userRepository.save(user));
    }
}
