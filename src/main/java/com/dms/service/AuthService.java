package com.dms.service;

import com.dms.dto.TokenResponseDTO;
import com.dms.dto.UserLoginDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    public TokenResponseDTO token(UserLoginDTO userLogin) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));
        String token = generateToken(authentication);

        return TokenResponseDTO.builder()
                               .token(token)
                               .build();
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                                          .subject(authentication.getName())
                                          .issuedAt(now)
                                          .expiresAt(now.plus(1, ChronoUnit.HOURS))
                                          .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims))
                         .getTokenValue();
    }

}
