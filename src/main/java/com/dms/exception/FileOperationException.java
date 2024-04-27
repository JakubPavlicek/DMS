package com.dms.exception;

import lombok.Getter;

/**
 * Exception indicating an error occurred during a file operation.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Getter
public class FileOperationException extends RuntimeException {

    /** The file operation associated with the exception. */
    private final FileOperation fileOperation;

    /**
     * Constructs a new {@code FileOperationException} with the specified file operation.
     *
     * @param fileOperation the file operation that caused the exception
     */
    public FileOperationException(FileOperation fileOperation) {
        super(fileOperation.getMessage());
        this.fileOperation = fileOperation;
    }

}
