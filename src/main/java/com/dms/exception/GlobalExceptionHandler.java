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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

/**
 * Global exception handler to handle exceptions across the application.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@RestControllerAdvice
@Log4j2
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** The properties related to the server configuration. */
    private final ServerProperties serverProperties;

    /** Context info key for {@link ProblemDetail}. */
    private static final String CONTEXT_INFO = "context_info";
    /** Messages key for {@link ProblemDetail}. */
    private static final String MESSAGES = "messages";

    /** Log message format for exceptions. */
    private static final String LOG_MESSAGE = "Request {} raised: ";

    /**
     * Retrieves the request URI from the given {@link WebRequest}.
     *
     * @param request the {@link WebRequest} from which to retrieve the URI
     * @return the URI of the request
     */
    private String getRequestURI(WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        return servletWebRequest.getRequest().getRequestURI();
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(LOG_MESSAGE, getRequestURI(request), ex);

        String detail = "Unsupported media type: " + ex.getContentType() + ", supported media types are: " + MediaType.toString(ex.getSupportedMediaTypes());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, detail);
        problemDetail.setTitle("Unsupported Media Type");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/media-type-not-supported"));

        Map<String, List<String>> message = new HashMap<>();
        message.put(MESSAGES, List.of("Request must contain data"));

        problemDetail.setProperty(CONTEXT_INFO, message);

        return ResponseEntity.of(problemDetail).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(LOG_MESSAGE, getRequestURI(request), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Provided data are not valid");
        problemDetail.setTitle("Invalid Data Provided");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/invalid-data-provided"));

        Map<String, List<String>> message = new HashMap<>();
        message.put(MESSAGES, getMethodArgumentErrorMessages(ex));

        problemDetail.setProperty(CONTEXT_INFO, message);

        return ResponseEntity.of(problemDetail).build();
    }

    /**
     * Extracts error messages from the {@link MethodArgumentNotValidException}.
     *
     * @param ex the {@link MethodArgumentNotValidException}
     * @return a list of error messages
     */
    private List<String> getMethodArgumentErrorMessages(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();

        BindingResult bindingResult = ex.getBindingResult();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> errorMessages.add(error.getField() + ": " + error.getDefaultMessage()));

        return errorMessages;
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(LOG_MESSAGE, getRequestURI(request), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Missing Request Part");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/missing-request-part"));

        return ResponseEntity.of(problemDetail).build();
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(LOG_MESSAGE, getRequestURI(request), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/resource-not-found"));

        return ResponseEntity.of(problemDetail).build();
    }

    /**
     * Handles the {@link BadCredentialsException}.
     *
     * @param exception the {@link BadCredentialsException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/bad-credentials"));

        return problemDetail;
    }

    /**
     * Handles the {@link FileOperationException}.
     *
     * @param exception the {@link FileOperationException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(FileOperationException.class)
    public ProblemDetail handleFileOperationEception(FileOperationException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle(exception.getFileOperation().getTitle());
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/file-operation"));

        return problemDetail;
    }

    /**
     * Handles the {@link FileWithPathAlreadyExistsException}.
     *
     * @param exception the {@link FileWithPathAlreadyExistsException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(FileWithPathAlreadyExistsException.class)
    public ProblemDetail handleFileWithPathAlreadyExistsException(FileWithPathAlreadyExistsException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("File With Path Already Exists");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/document-with-path-already-exists"));

        return problemDetail;
    }

    /**
     * Handles the {@link UserNotFoundException}.
     *
     * @param exception the {@link UserNotFoundException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("User Not Found");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/user-not-found"));

        return problemDetail;
    }

    /**
     * Handles the {@link EmailAlreadyExistsException}.
     *
     * @param exception the {@link EmailAlreadyExistsException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Email Already Exists");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/email-already-exists"));

        return problemDetail;
    }

    /**
     * Handles the {@link DocumentNotFoundException}.
     *
     * @param exception the {@link DocumentNotFoundException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ProblemDetail handleDocumentNotFoundException(DocumentNotFoundException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Document Not Found");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/document-not-found"));

        return problemDetail;
    }

    /**
     * Handles the {@link RevisionNotFoundException}.
     *
     * @param exception the {@link RevisionNotFoundException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(RevisionNotFoundException.class)
    public ProblemDetail handleRevisionNotFoundException(RevisionNotFoundException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Revision Not Found");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/revision-not-found"));

        return problemDetail;
    }

    /**
     * Handles the {@link RevisionDeletionException}.
     *
     * @param exception the {@link RevisionDeletionException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(RevisionDeletionException.class)
    public ProblemDetail handleRevisionDeletionException(RevisionDeletionException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Revision Deletion Error");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/revision-deletion-error"));

        return problemDetail;
    }

    /**
     * Handles the {@link ConstraintViolationException}.
     *
     * @param exception the {@link ConstraintViolationException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Provided data are not valid");
        problemDetail.setTitle("Invalid Data Provided");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/invalid-data-provided"));

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        Map<String, List<String>> message = new HashMap<>();
        message.put(MESSAGES, getConstraintViolationErrorMessages(violations));

        problemDetail.setProperty(CONTEXT_INFO, message);

        return problemDetail;
    }

    /**
     * Extracts error messages from the given set of constraint violations.
     *
     * @param violations the set of {@link ConstraintViolation}
     * @return a list of error messages
     */
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

    /**
     * Handles the {@link PropertyReferenceException}.
     *
     * @param exception the {@link PropertyReferenceException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropertyReferenceException(PropertyReferenceException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Property Doesn't Exist");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/property-doesnt-exist"));

        return problemDetail;
    }

    /**
     * Handles the {@link InvalidRegexInputException}.
     *
     * @param exception the {@link InvalidRegexInputException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(InvalidRegexInputException.class)
    public ProblemDetail handleInvalidRegexInputException(InvalidRegexInputException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Pattern Doesn't Match");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/pattern-doesnt-match"));

        return problemDetail;
    }

    /**
     * Handles the {@link MultipartException}.
     *
     * @param exception the {@link MultipartException}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(MultipartException.class)
    public ProblemDetail handleMultipartException(MultipartException exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, exception.getMessage());
        problemDetail.setTitle("Payload Too Large");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/payload-too-large"));

        return problemDetail;
    }

    /**
     * Handles generic exceptions.
     *
     * @param exception the {@link Exception}
     * @param request the {@link HttpServletRequest}
     * @return a {@link ProblemDetail} representing the error
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception exception, HttpServletRequest request) {
        log.error(LOG_MESSAGE, request.getRequestURI(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        problemDetail.setTitle("Unexpected Error Occurred");
        problemDetail.setType(URI.create(serverProperties.getErrorUrl(request) + "/unexpected"));

        return problemDetail;
    }

}
