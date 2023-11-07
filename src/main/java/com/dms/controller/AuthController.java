package com.dms.controller;

import com.dms.Oauth2Api;
import com.dms.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements Oauth2Api {

    @Override
    public ResponseEntity<Void> authorize(UserDTO userDTO) {
        return Oauth2Api.super.authorize(userDTO);
    }

    @Override
    public ResponseEntity<Void> revoke(UserDTO userDTO) {
        return Oauth2Api.super.revoke(userDTO);
    }

    @Override
    public ResponseEntity<String> token(UserDTO userDTO) {
        return Oauth2Api.super.token(userDTO);
    }

}
