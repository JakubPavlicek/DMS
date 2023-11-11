package com.dms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ActuatorAuthenticationProvider implements AuthenticationProvider {

    private final SecurityUserProperties securityUserProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String authName = authentication.getName();
        String authPassword = authentication.getCredentials().toString();
        List<SimpleGrantedAuthority> authorities = securityUserProperties.getRoles()
                                                                         .stream()
                                                                         .map(SimpleGrantedAuthority::new)
                                                                         .toList();

        if (authName.equals(securityUserProperties.getName()) && authPassword.equals(securityUserProperties.getPassword())) {
            return new UsernamePasswordAuthenticationToken(authName, authPassword, authorities);
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}