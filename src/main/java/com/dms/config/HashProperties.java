package com.dms.config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.security.MessageDigest;

@ConfigurationProperties(prefix = "hash")
@Validated
@Log4j2
@Getter
public class HashProperties implements Validator {

    @Value("${hash.algorithm:SHA-256}")
    private String algorithm;

    @Override
    public boolean supports(Class<?> clazz) {
        return HashProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        try {
            MessageDigest.getInstance(algorithm);
        } catch (Exception exception) {
            String message = "Hash algorithm does not exist";
            log.error(message, exception);
            errors.rejectValue("algorithm", "Hash Algorithm Not Found", message);
        }
    }

}
