package com.dms.exception;

/**
 * Exception indicating that a file with a specific path already exists.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class FileWithPathAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new {@code FileWithPathAlreadyExistsException} with the specified detail message.
     *
     * @param message the detail message
     */
    public FileWithPathAlreadyExistsException(String message) {
        super(message);
    }

}
