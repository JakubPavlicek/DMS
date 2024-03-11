package com.dms.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.context.request.WebRequest;

@Getter
@ConfigurationProperties(prefix = "server")
public class ServerProperties {

    @Value("${server.error.path:#{'errors'}}")
    private String errorPath;

    public String getErrorUrl(HttpServletRequest request) {
        return request.getContextPath() + "/" + errorPath;
    }

    public String getErrorUrl(WebRequest request) {
        return request.getContextPath() + "/" + errorPath;
    }

}
