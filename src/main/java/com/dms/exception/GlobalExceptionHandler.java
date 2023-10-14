package com.dms.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final String baseUrl = "http://localhost:8080/errors";

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

        List<String> errorMessages = getMethodArgumentErrorMessages(ex);

        problemDetail.setTitle("Invalid Data Provided");
        problemDetail.setType(URI.create(baseUrl + "/method-argument-not-valid"));
        problemDetail.setProperty("errors", errorMessages);

        return ResponseEntity.of(problemDetail)
                             .build();
    }

    private List<String> getMethodArgumentErrorMessages(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();

        BindingResult bindingResult = ex.getBindingResult();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> errorMessages.add(error.getDefaultMessage()));

        return errorMessages;
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Missing Request Part");
        problemDetail.setType(URI.create(baseUrl + "/missing-request-part"));

        return ResponseEntity.of(problemDetail)
                             .build();
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Missing Request Parameter");
        problemDetail.setType(URI.create(baseUrl + "/missing-request-parameter"));

        return ResponseEntity.of(problemDetail)
                             .build();
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create(baseUrl + "/resource-not-found"));

        return ResponseEntity.of(problemDetail)
                             .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Invalid Data Provided");
        problemDetail.setType(URI.create(baseUrl + "/invalid-data-provided"));

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        List<String> errors = getConstraintViolationErrorMessages(violations);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    private List<String> getConstraintViolationErrorMessages(Set<ConstraintViolation<?>> violations) {
        List<String> errorMessages = new ArrayList<>();

        violations.forEach(violation -> {
            String value = String.valueOf(violation.getInvalidValue());
            String message = violation.getMessage();

            String errorMessage = String.format("Value '%s' was rejected for the following reason: %s", value, message);

            errorMessages.add(errorMessage);
        });

        return errorMessages;
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropertyReferenceException(PropertyReferenceException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Property Doesn't Exist");
        problemDetail.setType(URI.create(baseUrl + "/property-doesnt-exist"));

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create(baseUrl + "/unknown"));

        return problemDetail;
    }

}
