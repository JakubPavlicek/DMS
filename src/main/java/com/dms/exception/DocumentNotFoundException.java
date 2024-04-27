package com.dms.exception;

/**
 * Exception indicating that a document could not be found.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class DocumentNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code DocumentNotFoundException} with the specified detail message.
     *
     * @param message the detail message
     */
    public DocumentNotFoundException(String message) {
        super(message);
    }

}
