package com.dms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final String baseUrl = "http://localhost:8080/errors";

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ProblemDetail handleFileAlreadyExistsException(FileAlreadyExistsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("File Already Exists");
        problemDetail.setType(URI.create(baseUrl + "/file-already-exists"));

        return problemDetail;
    }

    @ExceptionHandler(FileOperationException.class)
    public ProblemDetail handleFileOperationEception(FileOperationException eception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, eception.getMessage());
        problemDetail.setTitle(eception.getFileOperation()
                                       .getTitle());
        problemDetail.setType(URI.create(baseUrl + "/file-operation"));

        return problemDetail;
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ProblemDetail handleDocumentNotFoundException(DocumentNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Document Not Found");
        problemDetail.setType(URI.create(baseUrl + "/document-not-found"));

        return problemDetail;
    }

    @ExceptionHandler(RevisionNotFoundException.class)
    public ProblemDetail handleRevisionNotFoundException(RevisionNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Revision Not Found");
        problemDetail.setType(URI.create(baseUrl + "/revision-not-found"));

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle("Unknown Problem");
        problemDetail.setType(URI.create(baseUrl + "/unknown"));

        return problemDetail;
    }

}
