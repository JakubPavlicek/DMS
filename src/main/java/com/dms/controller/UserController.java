package com.dms.controller;

import com.dms.UsersApi;
import com.dms.dto.UserDTO;
import com.dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserDTO> createUser(UserDTO user) {
        UserDTO userDTO = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @Override
    public ResponseEntity<Void> deleteUser(String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO userDTO = userService.getCurrentUser();
        return ResponseEntity.ok(userDTO);
    }

    @Override
    public ResponseEntity<UserDTO> updateUser(String userId, UserDTO user) {
        UserDTO userDTO = userService.updateUser(userId, user);
        return ResponseEntity.ok(userDTO);
    }

}
