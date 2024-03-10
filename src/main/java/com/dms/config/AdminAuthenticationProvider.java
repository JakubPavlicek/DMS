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
public class AdminAuthenticationProvider implements AuthenticationProvider {

    private final SecurityUserProperties securityUserProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        List<SimpleGrantedAuthority> authorities = securityUserProperties.getRoles()
                                                                         .stream()
                                                                         .map(SimpleGrantedAuthority::new)
                                                                         .toList();

        if (name.equals(securityUserProperties.getName()) && password.equals(securityUserProperties.getPassword())) {
            return new UsernamePasswordAuthenticationToken(name, password, authorities);
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}