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

    /**
     * Initializes the validator.
     *
     * @param constraintAnnotation the annotation instance for this constraint
     */
    @Override
    public void initialize(ValidFile constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * Validates the uploaded file.
     *
     * @param file the uploaded file to be validated
     * @param context context in which the constraint is evaluated
     * @return true if the file is not null and its original filename is not blank, false otherwise
     */
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        return !Objects.requireNonNull(file.getOriginalFilename()).isBlank();
    }

}