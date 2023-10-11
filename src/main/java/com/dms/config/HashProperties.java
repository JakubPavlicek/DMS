package com.dms.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Validated
@ConfigurationProperties(prefix = "hash")
public class HashProperties implements Validator {

    @NotBlank(message = "Hashovaci algoritmus musi byt uveden")
    private final String algorithm;

    @ConstructorBinding
    public HashProperties(String algorithm) {
        this.algorithm = algorithm;
    }

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
