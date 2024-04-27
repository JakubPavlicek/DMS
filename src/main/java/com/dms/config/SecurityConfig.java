package com.dms.config;

import com.dms.exception.KeyException;
import com.dms.exception.UserNotFoundException;
import com.dms.repository.UserRepository;
import com.dms.util.KeyManager;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static com.dms.entity.Role.ADMIN;
import static com.dms.entity.Role.USER;

/**
 * Configuration class for security settings using Spring Security.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** Manages cryptographic keys for token generation and validation. */
    private final KeyManager keyManager;
    /** Provides access to user data stored in the database. */
    private final UserRepository userRepository;

    /** Represents the RSA key used for JWT token encryption and decryption. */
    private RSAKey rsaKey;

    /**
     * Configures security filters and authorization rules for HTTP requests.
     *
     * @param http the {@link HttpSecurity} object to be configured
     * @return the {@link SecurityFilterChain} object
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.POST, "/users")
                                                         .permitAll()
                                                         .requestMatchers(HttpMethod.POST, "/auth/token")
                                                         .permitAll()
                                                         .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/openapi.yaml", "/example/**", "/parameter/**", "/path/**", "/requestBody/**", "/schema/**", "/response/**")
                                                         .permitAll()
                                                         .requestMatchers("/actuator/**", "/users/password")
                                                         .hasRole(ADMIN.name())
                                                         .anyRequest()
                                                         .hasRole(USER.name()))
            .authenticationProvider(authenticationProvider())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }

    /**
     * Configures converters for extracting authorities from JWT.
     *
     * @return the {@link JwtAuthenticationConverter} object
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * Configures JWT encoder for generating JWT tokens.
     *
     * @return the {@link JwtEncoder} object
     * @throws KeyException if an error occurs while retrieving the RSA key
     */
    @Bean
    JwtEncoder jwtEncoder() throws KeyException {
        rsaKey = keyManager.getRsaKey();
        JWKSet jwkSet = new JWKSet(rsaKey);
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Configures JWT decoder for validating JWT tokens.
     *
     * @return the {@link JwtDecoder} object
     * @throws JOSEException if an error occurs during JWT decoding
     */
    @Bean
    JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey())
                               .build();
    }

    /**
     * Provides the {@link UserDetailsService} implementation.
     *
     * @return an instance of {@link UserDetailsService}
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                                         .orElseThrow(() -> new UserNotFoundException("User with email: " + username + " not found"));
    }

    /**
     * Configures authentication provider for DAO authentication.
     *
     * @return the {@link AuthenticationProvider} object
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    /**
     * Configures authentication manager for managing authentication providers.
     *
     * @return the {@link AuthenticationManager} object
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    /**
     * Configures password encoder for encoding passwords.
     *
     * @return the {@link PasswordEncoder} object
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
