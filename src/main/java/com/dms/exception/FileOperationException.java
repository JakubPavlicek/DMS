package com.dms.exception;

import lombok.Getter;

@Getter
public class FileOperationException extends RuntimeException {

    private final FileOperation fileOperation;

    public FileOperationException(FileOperation fileOperation, String message) {
        super(message);
        this.fileOperation = fileOperation;
    }

}
