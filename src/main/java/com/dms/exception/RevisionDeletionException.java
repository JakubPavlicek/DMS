package com.dms.exception;

/**
 * Exception indicating an error occurred while deleting a document revision.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class RevisionDeletionException extends RuntimeException {

    /**
     * Constructs a new {@code RevisionDeletionException} with the specified detail message.
     *
     * @param message the detail message
     */
    public RevisionDeletionException(String message) {
        super(message);
    }

}
