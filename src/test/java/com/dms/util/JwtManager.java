package com.dms.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.function.Consumer;

public class JwtManager {

    public static Consumer<Jwt.Builder> createJwt(String email) {
        return jwt -> jwt.header("alg", "RS256")
                         .claim("sub", email);
    }

}
