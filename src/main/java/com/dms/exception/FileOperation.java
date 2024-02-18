package com.dms.exception;

import lombok.Getter;

@Getter
public enum FileOperation {

    READ("File Read Error", "An error occurred while reading the file"),
    WRITE("File Write Error", "An error occurred while writing data from the file"),
    DELETE("File Deletion Error", "An error occurred while deleting the file"),
    LENGTH("File Length Error", "Content length could not be retrieved"),
    DEFAULT("File Operation Error", "An error occurred while working with the file");

    private final String title;
    private final String message;

    FileOperation(String title, String message) {
        this.title = title;
        this.message = message;
    }

}
