package com.dms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

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