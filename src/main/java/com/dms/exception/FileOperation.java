package com.dms.exception;

import lombok.Getter;

/**
 * Enumeration representing different file operations and their associated error messages.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Getter
public enum FileOperation {

    READ("File Read Error", "An error occurred while reading the file"),
    WRITE("File Write Error", "An error occurred while writing data from the file"),
    DELETE("File Deletion Error", "An error occurred while deleting the file"),
    FILE("File Resolve Error", "File could not be resolved"),
    DEFAULT("File Operation Error", "An error occurred while working with the file");

    /** The title of the error. */
    private final String title;
    /** The error message. */
    private final String message;

    /**
     * Constructs a new {@code FileOperation} with the specified title and message.
     *
     * @param title the title of the error
     * @param message the error message
     */
    FileOperation(String title, String message) {
        this.title = title;
        this.message = message;
    }

}
