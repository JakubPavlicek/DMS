package com.dms.exception;

public class FileAlreadyExistsException extends RuntimeException {

    public FileAlreadyExistsException(String message) {
        super(message);
    }

}
