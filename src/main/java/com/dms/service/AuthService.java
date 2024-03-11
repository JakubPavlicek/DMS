package com.dms.service;

import com.dms.config.TokenProperties;
import com.dms.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final UserDetailsService userDetailsService;

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    private final TokenProperties tokenProperties;

    public String token(String email, String password) {
        User user = (User) userDetailsService.loadUserByUsername(email);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password, user.getAuthorities()));
        log.info("User authenticated successfully");

        return generateToken(authentication);
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                                          .subject(authentication.getName())
                                          .issuedAt(now)
                                          .expiresAt(now.plus(tokenProperties.getExpirationTime(), ChronoUnit.HOURS))
                                          .claim("role", authentication.getAuthorities().stream().findFirst().get().getAuthority())
                                          .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        log.info("Successfully generated token");
        return token;
    }

}
