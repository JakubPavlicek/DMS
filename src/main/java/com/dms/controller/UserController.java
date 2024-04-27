package com.dms.controller;

import com.dms.UsersApi;
import com.dms.dto.UserDTO;
import com.dms.dto.UserLoginDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.User;
import com.dms.mapper.dto.UserDTOMapper;
import com.dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling {@code /users} endpoints.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    /** Service responsible for user management operations. */
    private final UserService userService;

    @Override
    public ResponseEntity<UserDTO> createUser(UserRegisterDTO userRegister) {
        User user = UserDTOMapper.mapToUser(userRegister);
        User createdUser = userService.createUser(user);
        UserDTO createdUserDTO = UserDTOMapper.mapToUserDTO(createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDTO);
    }

    @Override
    public ResponseEntity<UserDTO> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        UserDTO currentUserDTO = UserDTOMapper.mapToUserDTO(currentUser);

        return ResponseEntity.ok(currentUserDTO);
    }

    @Override
    public ResponseEntity<Void> changePassword(UserLoginDTO userLogin) {
        userService.changePassword(userLogin.getEmail(), userLogin.getPassword());

        return ResponseEntity.noContent().build();
    }

}
