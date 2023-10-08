package com.dms.exception;

import lombok.Getter;

@Getter
public class RevisionNotFoundException extends RuntimeException {

    public RevisionNotFoundException() {
    }

    public RevisionNotFoundException(String message) {
        super(message);
    }

    public RevisionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RevisionNotFoundException(Throwable cause) {
        super(cause);
    }

}
