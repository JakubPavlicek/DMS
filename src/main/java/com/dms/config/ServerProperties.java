package com.dms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Getter
@ConfigurationProperties(prefix = "server")
public class ServerProperties {

    @Value("${server.error.path:#{'/errors'}}")
    private String errorPath;

    public String getErrorUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .path(errorPath)
                                          .toUriString();
    }

}
