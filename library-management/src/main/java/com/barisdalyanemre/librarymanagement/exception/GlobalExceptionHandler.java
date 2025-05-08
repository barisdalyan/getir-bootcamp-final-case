package com.barisdalyanemre.librarymanagement.exception;

import com.barisdalyanemre.librarymanagement.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle custom exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException ex) {
        log.error("Forbidden: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Handle Spring Security exceptions
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "Invalid username or password", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Access denied", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "Authentication failed", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Handle constraint violations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error", ex);
        
        ex.getConstraintViolations().forEach(violation -> {
            apiError.addValidationError(
                String.format("%s: %s", violation.getPropertyPath(), violation.getMessage())
            );
        });
        
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Handle Database exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        String message = "Database constraint violated";
        
        if (ex.getMessage().contains("duplicate key")) {
            message = "Duplicate key violation";
        }
        
        ApiError apiError = new ApiError(HttpStatus.CONFLICT, message, ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Handle type mismatch exceptions
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());
        String message = String.format("'%s' should be a valid '%s'", 
            ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "type");
            
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, message, ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error", ex);
        
        List<String> errors = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        
        errors.forEach(apiError::addValidationError);
        
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
            
        log.error("Missing parameter: {}", ex.getMessage());
        String message = ex.getParameterName() + " parameter is missing";
        
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, message, ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
