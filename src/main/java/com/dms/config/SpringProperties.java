package com.dms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties
public class SpringProperties implements Validator {

    // TODO: check if activeProfile is one of "postgresql", "oracle", "mssql"

    @Value("${spring.profiles.active:postgresql}")
    private String activeProfile;

    @Value("${spring.servlet.multipart.enabled:true}")
    private boolean multipartEnabled;

    @Value("${spring.servlet.multipart.max-file-size:1GB}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:1GB}")
    private String maxRequestSize;

    @Value("${spring.jpa.open-in-view:false}")
    private boolean openInView;

    @Value("${spring.mvc.problemdetails.enabled:true}")
    private boolean problemDetailsEnabled;

    @Value("${spring.mvc.throw-exception-if-no-handler-found:true}")
    private boolean throwExceptionIfNoHandlerFound;

    @Value("${spring.liquibase.enabled:true}")
    private boolean liquibaseEnabled;

    @Value("${spring.liquibase.change-log:classpath:db/changelog/changelog-master.yaml}")
    private String liquibaseChangeLog;

    @Override
    public boolean supports(Class<?> clazz) {
        return SpringProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

    }

}
