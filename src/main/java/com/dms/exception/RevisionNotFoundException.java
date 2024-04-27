package com.dms.exception;

/**
 * Exception indicating that a document revision could not be found.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class RevisionNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code RevisionNotFoundException} with the specified detail message.
     *
     * @param message the detail message
     */
    public RevisionNotFoundException(String message) {
        super(message);
    }

}
