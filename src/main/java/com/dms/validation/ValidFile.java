package com.dms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code ValidFile} annotation is used to validate files in request parameters.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FileValidator.class})
public @interface ValidFile {

    String message() default "File is mandatory";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
