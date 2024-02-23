package com.dms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.security.user")
@Getter
public class SecurityUserProperties {

    @Value("${spring.security.user.name:admin}")
    private String name;

    @Value("${spring.security.user.password:admin123}")
    private String password;

    @Value("${spring.security.user.roles:ADMIN}")
    private List<String> roles;

}
