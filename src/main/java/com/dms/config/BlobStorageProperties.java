package com.dms.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Validated
@Getter
@ConfigurationProperties(prefix = "storage")
public class BlobStorageProperties implements Validator {

    @Value("${storage.path}")
    private String path;

    @Min(value = 1, message = "Minimalni delka prefixu pro adresare je 1")
    @Max(value = 10, message = "Maximalni delka prefixu pro adresare je 10")
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
                Files.createDirectory(storagePath);
            } catch (Exception e) {
                errors.rejectValue("path", "Blob Storage Error Occurred", "Blob storage couldn't be created");
            }
        }

        File file = new File(path);

        if(!file.isDirectory())
            errors.rejectValue("path", "Path Is Not A Directory", "Provided path: " + path + " is not a directory");
    }

}
