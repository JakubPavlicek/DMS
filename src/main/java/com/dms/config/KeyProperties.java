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

@ConfigurationProperties(prefix = "rsa")
@Getter
@Log4j2
public class KeyProperties implements Validator {

    @Value("${rsa.private-key}")
    private String privateKey;

    @Value("${rsa.public-key}")
    private String publicKey;

    @Override
    public boolean supports(Class<?> clazz) {
        return KeyProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateKeyPath(privateKey, "privateKey", "private", errors);
        validateKeyPath(publicKey, "publicKey", "public", errors);
    }

    private void validateKeyPath(String keyPath, String keyFieldName, String keyType, Errors errors) {
        Path path = Paths.get(keyPath).getParent();

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
