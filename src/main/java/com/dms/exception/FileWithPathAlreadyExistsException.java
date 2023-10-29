package com.dms.exception;

public class FileWithPathAlreadyExistsException extends RuntimeException {

    public FileWithPathAlreadyExistsException(String message) {
        super(message);
    }

}
