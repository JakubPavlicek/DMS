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

/**
 * Service class for handling user authentication and JWT token generation.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    /** Service for loading user details by username. */
    private final UserDetailsService userDetailsService;

    /** Authentication manager for authenticating users. */
    private final AuthenticationManager authenticationManager;
    /** Encoder for generating JWT tokens. */
    private final JwtEncoder jwtEncoder;

    /** Configuration properties for JWT token generation. */
    private final TokenProperties tokenProperties;

    /**
     * Generates a JWT token for the provided user credentials.
     *
     * @param email the email address of the user
     * @param password the user's password
     * @return the JWT token
     */
    public String token(String email, String password) {
        User user = (User) userDetailsService.loadUserByUsername(email);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password, user.getAuthorities()));
        log.info("User authenticated successfully");

        return generateToken(authentication);
    }

    /**
     * Generates a JWT token based on the authentication details.
     *
     * @param authentication the authentication object
     * @return the generated JWT token
     */
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
