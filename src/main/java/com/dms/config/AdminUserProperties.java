package com.dms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the admin user, defined in application.yaml.
 * Properties are prefixed with "spring.security.user".
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@ConfigurationProperties(prefix = "spring.security.user")
@Getter
public class AdminUserProperties {

    /**
     * The name of the admin user.
     * Default value is "admin".
     */
    @Value("${admin.name:admin}")
    private String name;

    /**
     * The email of the admin user.
     * Default value is "admin@mail.com".
     */
    @Value("${admin.email:#{'admin@mail.com'}}")
    private String email;

    /**
     * The password of the admin user.
     * Default value is "admin123".
     */
    @Value("${admin.password:admin123}")
    private String password;

}
