package com.dms.exception;

import lombok.Getter;

@Getter
public class RevisionNotFoundException extends RuntimeException {

    public RevisionNotFoundException(String message) {
        super(message);
    }

}
