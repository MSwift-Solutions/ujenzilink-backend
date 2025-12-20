package com.ujenzilink.ujenzilink_backend.configs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        // Records use the "Canonical Constructor" (pass all values at once)
        ApiCustomResponse<String> response = new ApiCustomResponse<>(
                null,
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        ApiCustomResponse<String> response = new ApiCustomResponse<>(
                null,
                ex.getMessage(),
                HttpStatus.METHOD_NOT_ALLOWED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleNullPointerException(NullPointerException ex) {
        ApiCustomResponse<String> response = new ApiCustomResponse<>(
                null,
                "A null pointer error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
