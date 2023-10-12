package com.dms.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Validated
@ConfigurationProperties(prefix = "storage")
public class BlobStoragePropertiesValidator implements Validator {

    @NotBlank(message = "Cesta k Blob storage musi byt zadana")
    private final String path;

    @Min(value = 1, message = "Minimalni delka prefixu pro adresare je 1")
    @Max(value = 10, message = "Maximalni delka prefixu pro adresare je 10")
    private final int directoryPrefixLength;

    @ConstructorBinding
    public BlobStoragePropertiesValidator(String path, int directoryPrefixLength)
    {
        this.path = path;
        this.directoryPrefixLength = directoryPrefixLength;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return BlobStoragePropertiesValidator.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Path storagePath = Paths.get(path);

        if(Files.notExists(storagePath))
            errors.rejectValue("path", "Blob Storage Path Not Found", "Blob storage se zadanou cestou neexistuje");
    }

}
