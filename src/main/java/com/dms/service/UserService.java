package com.dms.service;

import com.dms.entity.User;
import com.dms.exception.UserParsingException;
import com.dms.repository.UserRepository;
import com.dms.request.DocumentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                             .orElseGet(() -> userRepository.save(user));
    }

    public User getUserFromRequest(DocumentRequest documentRequest) {
        try {
            return new ObjectMapper().readValue(documentRequest.getUser(), User.class);
        } catch (JsonProcessingException e) {
            throw new UserParsingException("Nepodarilo se ziskat uzivatele z JSONu: " + documentRequest.getUser());
        }
    }

}
