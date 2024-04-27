package com.dms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * The {@code FileValidator} class implements the validation logic for the {@link ValidFile} annotation.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        return !Objects.requireNonNull(file.getOriginalFilename()).isBlank();
    }

}