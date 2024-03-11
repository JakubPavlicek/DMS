package com.dms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.user")
@Getter
public class AdminUserProperties {

    @Value("${admin.name:admin}")
    private String name;

    @Value("${admin.email:#{'admin@mail.com'}}")
    private String email;

    @Value("${admin.password:admin123}")
    private String password;

}
