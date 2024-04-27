package com.dms.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.context.request.WebRequest;

/**
 * Configuration properties for server-related settings, defined in application.yaml.
 * Properties are prefixed with "server".
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Getter
@ConfigurationProperties(prefix = "server")
public class ServerProperties {

    /** The path for server errors. Default: "errors". */
    @Value("${server.error.path:#{'errors'}}")
    private String errorPath;

    /**
     * Constructs the error URL based on the provided {@link HttpServletRequest}.
     *
     * @param request the {@link HttpServletRequest} object
     * @return the constructed error URL
     */
    public String getErrorUrl(HttpServletRequest request) {
        return request.getContextPath() + "/" + errorPath;
    }

    /**
     * Constructs the error URL based on the provided {@link WebRequest}.
     *
     * @param request the {@link WebRequest} object
     * @return the constructed error URL
     */
    public String getErrorUrl(WebRequest request) {
        return request.getContextPath() + "/" + errorPath;
    }

}
