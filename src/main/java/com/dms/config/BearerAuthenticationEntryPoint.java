package com.dms.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.net.URI;

import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@RequiredArgsConstructor
public class BearerAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ServerProperties serverProperties;
    private final JsonFactory jsonFactory;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String bearer = BEARER.getValue() + " ";

        if (authorizationHeader == null || !authorizationHeader.startsWith(bearer)) {
            handleMissingBearerToken(request, response);
        }
    }

    private void handleMissingBearerToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/problem+json");

        ProblemDetail problemDetail = createProblemDetail(request);
        writeProblemDetailToResponse(problemDetail, response);
    }

    private ProblemDetail createProblemDetail(HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Bearer token not found in request headers");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/missing-access-token"));
        problemDetail.setTitle("Missing Access Token");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    private void writeProblemDetailToResponse(ProblemDetail problemDetail, HttpServletResponse response) throws IOException {
        try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(response.getWriter())) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("type", String.valueOf(problemDetail.getType()));
            jsonGenerator.writeStringField("title", problemDetail.getTitle());
            jsonGenerator.writeNumberField("status", problemDetail.getStatus());
            jsonGenerator.writeStringField("detail", problemDetail.getDetail());
            jsonGenerator.writeStringField("instance", String.valueOf(problemDetail.getInstance()));
            jsonGenerator.writeEndObject();
        }
    }

}
