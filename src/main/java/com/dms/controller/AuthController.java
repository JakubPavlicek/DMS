package com.dms.controller;

import com.dms.AuthApi;
import com.dms.dto.TokenResponseDTO;
import com.dms.dto.UserLoginDTO;
import com.dms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling {@code /auth} endpoints.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    /** Service responsible for authentication operations. */
    private final AuthService authService;

    @Override
    public ResponseEntity<TokenResponseDTO> token(UserLoginDTO userLogin) {
        String token = authService.token(userLogin.getEmail(), userLogin.getPassword());
        TokenResponseDTO tokenResponse = new TokenResponseDTO(token);

        return ResponseEntity.ok(tokenResponse);
    }

}
