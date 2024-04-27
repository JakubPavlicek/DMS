package com.dms.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration properties for blob storage, defined in application.yaml.
 * Properties are prefixed with "storage".
 * <p>
 * Validates the storage properties and ensures the existence of the specified storage path.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@ConfigurationProperties(prefix = "storage")
@Validated
@Log4j2
@Getter
public class BlobStorageProperties implements Validator {

    /** The path to the blob storage directory. */
    @Value("${storage.path}")
    private String path;

    /**
     * The length of subdirectory prefixes within the storage directory.
     * Must be between 1 and 10 inclusive.
     * Default value is 2.
     */
    @Min(
        value = 1,
        message = "Minimal subdirectory prefix length is 1"
    )
    @Max(
        value = 10,
        message = "Maximum subdirectory prefix length is 10"
    )
    @Value("${storage.subdirectory-prefix-length:2}")
    private int subdirectoryPrefixLength;

    /**
     * Checks if the specified class is supported for validation.
     *
     * @param clazz the class to be checked
     * @return true if the class is assignable from {@code BlobStorageProperties}, otherwise false
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return BlobStorageProperties.class.isAssignableFrom(clazz);
    }

    /**
     * Validates the blob storage properties.
     *
     * @param target the object to be validated
     * @param errors stores and exposes information about data-binding and validation errors
     */
    @Override
    public void validate(Object target, Errors errors) {
        Path storagePath = Paths.get(path);

        // create directory if it doesn't exist
        if (Files.notExists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
                log.warn("The provided blob storage path {} did not exist. A new directory has been created at that path", storagePath);
            } catch (Exception exception) {
                String message = "Blob storage couldn't be created";
                log.error(message, exception);
                errors.rejectValue("path", "Blob Storage Error Occurred", message);
            }
        }

        // check if the provided path is a directory
        if (!Files.isDirectory(storagePath)) {
            String message = "Provided path: '" + path + "' is not a directory";
            log.error(message);
            errors.rejectValue("path", "Path Is Not A Directory", message);
        }
    }

}
