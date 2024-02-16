package com.dms.exception;

import lombok.Getter;

@Getter
public class FileOperationException extends RuntimeException {

    private final FileOperation fileOperation;

    public FileOperationException(FileOperation fileOperation) {
        super(fileOperation.getMessage());
        this.fileOperation = fileOperation;
    }

}
