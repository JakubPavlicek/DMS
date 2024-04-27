package com.dms.exception;

/**
 * Exception indicating that an email already exists in the system.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new {@code EmailAlreadyExistsException} with the specified detail message.
     *
     * @param message the detail message
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

}
