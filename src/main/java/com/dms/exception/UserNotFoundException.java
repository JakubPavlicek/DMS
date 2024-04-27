package com.dms.exception;

/**
 * Exception indicating that a user could not be found.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code UserNotFoundException} with the specified detail message.
     *
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }

}
