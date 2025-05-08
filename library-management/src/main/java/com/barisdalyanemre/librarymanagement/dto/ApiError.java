package com.barisdalyanemre.librarymanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private HttpStatus status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    
    private String message;
    private String debugMessage;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    public ApiError(HttpStatus status, String message, Throwable ex) {
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
        this.timestamp = LocalDateTime.now();
    }
    
    public void addValidationError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }
}
