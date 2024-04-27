package com.dms.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for tokens, defined in application.yaml.
 * Properties are prefixed with "token".
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@ConfigurationProperties(prefix = "token")
@Getter
public class TokenProperties {

    /**
     * The expiration time for tokens in hours.
     * Default value is 1 hour.
     * Minimum allowed value is 1 hour.
     */
    @Min(
        value = 1,
        message = "Minimal expiration time for token is 1 hour"
    )
    @Value("${token.expiration.time:1}")
    private int expirationTime;

}
