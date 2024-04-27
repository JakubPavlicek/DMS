package com.dms.config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration properties for keys, defined in application.yaml.
 * Properties are prefixed with "rsa".
 * <p>
 * Provides properties for specifying RSA private and public keys.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@ConfigurationProperties(prefix = "rsa")
@Getter
@Log4j2
public class KeyProperties implements Validator {

    /** The path to the RSA private key file. */
    @Value("${rsa.private-key}")
    private String privateKey;

    /** The path to the RSA public key file. */
    @Value("${rsa.public-key}")
    private String publicKey;

    /**
     * Checks if the specified class is supported for validation.
     *
     * @param clazz the class to be checked
     * @return true if the class is assignable from {@code KeyProperties}, otherwise false
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return KeyProperties.class.isAssignableFrom(clazz);
    }

    /**
     * Validates the specified key paths by ensuring their parent directories exist.
     *
     * @param target the object to be validated
     * @param errors stores and exposes information about data-binding and validation errors
     */
    @Override
    public void validate(Object target, Errors errors) {
        validateKeyPath(privateKey, "privateKey", "private", errors);
        validateKeyPath(publicKey, "publicKey", "public", errors);
    }

    /**
     * Validates the existence of the directory containing the key file.
     *
     * @param keyPath the path to the key file
     * @param keyFieldName the name of the key field
     * @param keyType the type of the key (private/public)
     * @param errors stores and exposes information about data-binding and validation errors
     */
    private void validateKeyPath(String keyPath, String keyFieldName, String keyType, Errors errors) {
        Path path = Paths.get(keyPath).getParent();

        // create directories of the key file if they don't exist
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
                log.warn("The provided {} key path directory {} did not exist. A new directory has been created at that path", keyType, path);
            } catch (Exception exception) {
                keyType = StringUtils.capitalize(keyType);
                String message = keyType + " key path couldn't be created";
                log.error(message, exception);
                errors.rejectValue(keyFieldName, keyType + " Key Path Error Occurred", message);
            }
        }
    }

}
