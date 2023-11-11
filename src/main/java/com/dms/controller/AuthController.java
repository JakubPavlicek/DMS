package com.dms.controller;

import com.dms.Oauth2Api;
import com.dms.dto.TokenResponseDTO;
import com.dms.dto.UserLoginDTO;
import com.dms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements Oauth2Api {

    private final AuthService authService;

    @Override
    public ResponseEntity<TokenResponseDTO> token(UserLoginDTO userLogin) {
        return ResponseEntity.ok(authService.token(userLogin));
    }

}
