package com.dms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Validated
@Getter
@ConfigurationProperties(prefix = "hash")
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
        } catch (NoSuchAlgorithmException e) {
            errors.rejectValue("algorithm", "Hash Algorithm Not Found", "Hashovaci algoritmus neexistuje");
        }
    }

}
