package com.dms.exception;

import com.dms.config.ServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
@Log4j2
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ServerProperties serverProperties;

    private final static String CONTEXT_INFO = "context_info";
    private final static String MESSAGES = "messages";

    private String getRequestURI(WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        return servletWebRequest.getRequest().getRequestURI();
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("Request {} raised:", getRequestURI(request), ex);

        String detail = "Unsupported media type: " + ex.getContentType() + ", supported media types are: " + MediaType.toString(ex.getSupportedMediaTypes());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, detail);
        problemDetail.setTitle("Unsupported Media Type");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/media-type-not-supported"));

        Map<String, List<String>> message = new HashMap<>();
        message.put(MESSAGES, List.of("Request must contain data"));

        problemDetail.setProperty(CONTEXT_INFO, message);

        return ResponseEntity.of(problemDetail).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("Request {} raised: ", getRequestURI(request), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Provided data are not valid");
        problemDetail.setTitle("Invalid Data Provided");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/invalid-data-provided"));

        Map<String, List<String>> message = new HashMap<>();
        message.put(MESSAGES, getMethodArgumentErrorMessages(ex));

        problemDetail.setProperty(CONTEXT_INFO, message);

        return ResponseEntity.of(problemDetail).build();
    }

    private List<String> getMethodArgumentErrorMessages(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();

        BindingResult bindingResult = ex.getBindingResult();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> errorMessages.add(error.getField() + ": " + error.getDefaultMessage()));

        return errorMessages;
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("Request {} raised:", getRequestURI(request), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Missing Request Part");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/missing-request-part"));

        return ResponseEntity.of(problemDetail).build();
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("Request {} raised:", getRequestURI(request), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Missing Request Parameter");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/missing-request-parameter"));

        return ResponseEntity.of(problemDetail).build();
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("Request {} raised:", getRequestURI(request), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/resource-not-found"));

        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ProblemDetail handleUnauthorizedAccessException(UnauthorizedAccessException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/unauthorized-access"));

        return problemDetail;
    }

    @ExceptionHandler(FileOperationException.class)
    public ProblemDetail handleFileOperationEception(FileOperationException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle(exception.getFileOperation().getTitle());
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/file-operation"));

        return problemDetail;
    }

    @ExceptionHandler(FileWithPathAlreadyExistsException.class)
    public ProblemDetail handleFileWithPathAlreadyExistsException(FileWithPathAlreadyExistsException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("File With Path Already Exists");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/document-with-path-already-exists"));

        return problemDetail;
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ProblemDetail handleDocumentNotFoundException(DocumentNotFoundException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Document Not Found");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/document-not-found"));

        return problemDetail;
    }

    @ExceptionHandler(RevisionNotFoundException.class)
    public ProblemDetail handleRevisionNotFoundException(RevisionNotFoundException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Revision Not Found");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/revision-not-found"));

        return problemDetail;
    }

    @ExceptionHandler(RevisionDeletionException.class)
    public ProblemDetail handleRevisionDeletionException(RevisionDeletionException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Revision Deletion Error");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/revision-deletion-error"));

        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Provided data are not valid");
        problemDetail.setTitle("Invalid Data Provided");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/invalid-data-provided"));

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        Map<String, List<String>> message = new HashMap<>();
        message.put(MESSAGES, getConstraintViolationErrorMessages(violations));

        problemDetail.setProperty(CONTEXT_INFO, message);

        return problemDetail;
    }

    private List<String> getConstraintViolationErrorMessages(Set<ConstraintViolation<?>> violations) {
        List<String> errorMessages = new ArrayList<>();

        violations.forEach(violation -> {
            String message = violation.getMessage();
            String propertyPath = violation.getPropertyPath().toString();
            String property = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);

            String errorMessage = String.format("%s: %s", property, message);

            errorMessages.add(errorMessage);
        });

        return errorMessages;
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropertyReferenceException(PropertyReferenceException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Property Doesn't Exist");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/property-doesnt-exist"));

        return problemDetail;
    }

    @ExceptionHandler(InvalidRegexInputException.class)
    public ProblemDetail handleInvalidRegexInputException(InvalidRegexInputException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Pattern Doesn't Match");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/pattern-doesnt-match"));

        return problemDetail;
    }

    @ExceptionHandler(MultipartException.class)
    public ProblemDetail handleMultipartException(MultipartException exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, exception.getMessage());
        problemDetail.setTitle("Payload Too Large");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/payload-too-large"));

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception exception, HttpServletRequest request) {
        log.error("Request {} raised:", request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle("Unexpected Error Occurred");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl() + "/unexpected"));

        return problemDetail;
    }

}
