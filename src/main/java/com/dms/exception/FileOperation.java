package com.dms.exception;

import lombok.Getter;

@Getter
public enum FileOperation {

    READ("File Read Error"),
    WRITE("File Write Error"),
    DELETE("File Deletion Error");

    private final String title;

    FileOperation(String title) {
        this.title = title;
    }

}
