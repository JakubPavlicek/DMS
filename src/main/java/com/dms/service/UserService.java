package com.dms.service;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getSavedUser(User user) {
        log.debug("Getting saved user: user={}", user);

        String username = user.getUsername();
        String email = user.getEmail();

        return userRepository.findByUsernameAndEmail(username, email)
                             .orElseGet(() -> userRepository.save(user));
    }

}
