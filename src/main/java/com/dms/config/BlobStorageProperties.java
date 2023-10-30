package com.dms.config;

import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

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
                throw new FileOperationException(FileOperation.DEFAULT, "An error occurred while working with the file");
            }
        }
    }

}
