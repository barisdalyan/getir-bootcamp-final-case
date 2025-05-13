package com.barisdalyanemre.librarymanagement.exception;

import com.barisdalyanemre.librarymanagement.dto.response.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private ObjectMapper objectMapper;
    private WebRequest webRequest;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/test");
        
        ServletWebRequest servletWebRequest = mock(ServletWebRequest.class);
        when(servletWebRequest.getRequest()).thenReturn(httpServletRequest);
        webRequest = servletWebRequest;
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Book not found with id: 999");
        
        ResponseEntity<Object> response = globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found with id: 999", ((ApiError) response.getBody()).getMessage());
    }

    @Test
    void handleMethodArgumentTypeMismatch() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getRequiredType()).thenReturn((Class) Long.class);
        
        ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("'id' should be a valid 'Long'", ((ApiError) response.getBody()).getMessage());
    }
    
    @Test
    void handleMethodArgumentNotValid() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        HttpHeaders headers = new HttpHeaders();
        
        ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentNotValid(
            exception, headers, HttpStatus.BAD_REQUEST, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation error", ((ApiError) response.getBody()).getMessage());
    }
    
    @Test
    void handleHttpMessageNotReadable() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        HttpHeaders headers = new HttpHeaders();
        
        ResponseEntity<Object> response = globalExceptionHandler.handleHttpMessageNotReadable(
            exception, headers, HttpStatus.BAD_REQUEST, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request body is missing or malformed", ((ApiError) response.getBody()).getMessage());
    }
    
    @Test
    void handleConflictException() {
        ConflictException exception = new ConflictException("Book is already borrowed");
        
        ResponseEntity<Object> response = globalExceptionHandler.handleConflictException(exception, webRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book is already borrowed", ((ApiError) response.getBody()).getMessage());
    }
    
    @Test
    void handleBadRequestException() {
        BadRequestException exception = new BadRequestException("Invalid request parameters");
        
        ResponseEntity<Object> response = globalExceptionHandler.handleBadRequestException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request parameters", ((ApiError) response.getBody()).getMessage());
    }
}
