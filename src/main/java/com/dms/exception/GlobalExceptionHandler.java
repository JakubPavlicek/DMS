package com.dms.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final String baseUrl = "http://localhost:8080/errors";

    @ExceptionHandler(HashAlgorithmNotFoundException.class)
    public ProblemDetail handleHashAlgorithmNotFoundException(HashAlgorithmNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Hash Algorithm Not Found");
        problemDetail.setType(URI.create(baseUrl + "/hash-algorithm-not-found"));

        return problemDetail;
    }

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

    @ExceptionHandler(UserParsingException.class)
    public ProblemDetail handleUserParsingException(UserParsingException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("User Parsing Failed");
        problemDetail.setType(URI.create(baseUrl + "/user-parsing"));

        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        List<String> errorMessages = getErrorMessages(ex);

        problemDetail.setTitle("Invalid Data Provided");
        problemDetail.setType(URI.create(baseUrl + "/method-argument-not-valid"));
        problemDetail.setProperty("errors", errorMessages);

        return ResponseEntity.of(problemDetail)
                             .build();
    }

    private List<String> getErrorMessages(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();

        BindingResult bindingResult = ex.getBindingResult();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> errorMessages.add(error.getDefaultMessage()));

        return errorMessages;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle("Unknown Problem");
        problemDetail.setType(URI.create(baseUrl + "/unknown"));

        return problemDetail;
    }

}
