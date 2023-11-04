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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "storage")
@Validated
@Log4j2
@Getter
public class BlobStorageProperties implements Validator {

    @Value("${storage.path}")
    private String path;

    @Min(value = 1, message = "Minimal directory prefix length is 1")
    @Max(value = 10, message = "Maximum directory prefix length is 10")
    @Value("${storage.directory-prefix-length:2}")
    private int directoryPrefixLength;

    @Override
    public boolean supports(Class<?> clazz) {
        return BlobStorageProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Path storagePath = Paths.get(path);

        if(Files.notExists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
                log.warn("The provided blob storage path: '{}' did not exist. A new directory has been created at that path", storagePath);
            } catch (Exception e) {
                String message = "Blob storage couldn't be created";
                log.error(message, e);
                errors.rejectValue("path", "Blob Storage Error Occurred", message);
            }
        }

        File file = new File(path);

        if(!file.isDirectory()) {
            String message = "Provided path: " + path + " is not a directory";
            log.error(message);
            errors.rejectValue("path", "Path Is Not A Directory", message);
        }
    }

}
