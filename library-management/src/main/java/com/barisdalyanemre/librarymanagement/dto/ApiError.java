package com.barisdalyanemre.librarymanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"error", "status", "message", "path", "timestamp", "errors", "validationErrors"})
public class ApiError {
    private String error;
    private int status;
    private String message;
    private String path;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private List<String> errors;
    private List<ValidationError> validationErrors;

    public ApiError() {
        timestamp = LocalDateTime.now();
    }

    public ApiError(HttpStatus status, String message, Throwable ex) {
        this();
        this.error = status.getReasonPhrase();
        this.status = status.value();
        this.message = message;
        
        if (ex != null && ex.getMessage() != null) {
            this.errors = new ArrayList<>();
            this.errors.add(ex.getMessage());
        }
    }
    
    public void addValidationErrors(MethodArgumentNotValidException ex) {
        this.validationErrors = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new ValidationError(error.getField(), error.getDefaultMessage()));
        }
        
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            validationErrors.add(new ValidationError(error.getObjectName(), error.getDefaultMessage()));
        }
    }
    
    @Data
    private static class ValidationError {
        private String field;
        private String message;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
