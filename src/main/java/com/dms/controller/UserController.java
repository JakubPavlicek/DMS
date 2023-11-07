package com.dms.controller;

import com.dms.UsersApi;
import com.dms.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    @Override
    public ResponseEntity<UserDTO> createUser(UserDTO userDTO) {
        return UsersApi.super.createUser(userDTO);
    }

    @Override
    public ResponseEntity<Void> deleteUser(String userId) {
        return UsersApi.super.deleteUser(userId);
    }

    @Override
    public ResponseEntity<UserDTO> getCurrentUser() {
        return UsersApi.super.getCurrentUser();
    }

    @Override
    public ResponseEntity<UserDTO> updateUser(String userId, UserDTO userDTO) {
        return UsersApi.super.updateUser(userId, userDTO);
    }

}
