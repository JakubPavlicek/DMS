package com.dms.controller;

import com.dms.UsersApi;
import com.dms.dto.UserDTO;
import com.dms.dto.UserLoginDTO;
import com.dms.dto.UserRegisterDTO;
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
    public ResponseEntity<UserDTO> createUser(UserRegisterDTO userRegister) {
        UserDTO userDTO = userService.createUser(userRegister);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @Override
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO userDTO = userService.getCurrentUser();
        return ResponseEntity.ok(userDTO);
    }

    @Override
    public ResponseEntity<Void> changePassword(UserLoginDTO userLogin) {
        userService.changePassword(userLogin);
        return ResponseEntity.noContent().build();
    }

}
