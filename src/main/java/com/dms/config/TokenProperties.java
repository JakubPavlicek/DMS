package com.dms.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token")
@Getter
public class TokenProperties {

    @Min(
        value = 1,
        message = "Minimal expiration time for token is 1 hour"
    )
    @Value("${token.expiration.time:1}")
    private int expirationTime;

}
