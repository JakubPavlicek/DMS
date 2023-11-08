package com.dms.controller;

import com.dms.Oauth2Api;
import com.dms.dto.UserDTO;
import com.dms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements Oauth2Api {

    private final AuthService authService;

    @Override
    public ResponseEntity<String> token(UserDTO user) {
        return ResponseEntity.ok(authService.token(user));
    }

}
