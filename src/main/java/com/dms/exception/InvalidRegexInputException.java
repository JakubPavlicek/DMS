package com.dms.exception;

/**
 * Exception indicating that an invalid regular expression input was provided.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class InvalidRegexInputException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidRegexInputException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidRegexInputException(String message) {
        super(message);
    }

}
