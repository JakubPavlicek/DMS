package com.dms.exception;

/**
 * Exception indicating an error related to cryptographic keys.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class KeyException extends RuntimeException {

    /**
     * Constructs a new {@code KeyException} with the specified detail message.
     *
     * @param message the detail message
     */
    public KeyException(String message) {
        super(message);
    }

}
