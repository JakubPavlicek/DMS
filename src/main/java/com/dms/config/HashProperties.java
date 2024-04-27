package com.dms.config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.security.MessageDigest;

/**
 * Configuration properties for hashing algorithm, defined in application.yaml.
 * Properties are prefixed with "hash".
 * <p>
 * Validates the specified hashing algorithm and ensures its existence.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@ConfigurationProperties(prefix = "hash")
@Validated
@Log4j2
@Getter
public class HashProperties implements Validator {

    /**
     * The hashing algorithm to be used.
     * Default value is "SHA-256".
     */
    @Value("${hash.algorithm:SHA-256}")
    private String algorithm;

    /**
     * Checks if the specified class is supported for validation.
     *
     * @param clazz the class to be checked
     * @return true if the class is assignable from {@code HashProperties}, otherwise false
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return HashProperties.class.isAssignableFrom(clazz);
    }

    /**
     * Validates the specified hashing algorithm by attempting to retrieve its instance.
     *
     * @param target the object to be validated
     * @param errors stores and exposes information about data-binding and validation errors
     */
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
