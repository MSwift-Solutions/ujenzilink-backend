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

    // Handles @Valid failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiCustomResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                errors,
                "Validation failed",
                HttpStatus.BAD_REQUEST.value()
        ));
    }

    // Handles JSON parsing errors
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                null,
                "Malformed JSON request: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        ));
    }

    // Handles wrong HTTP Methods (e.g. GET instead of POST)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiCustomResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ApiCustomResponse<>(
                null,
                ex.getMessage(),
                HttpStatus.METHOD_NOT_ALLOWED.value()
        ));
    }

    /**
     * CATCH-ALL EXCEPTION HANDLER
     * Any unhandled RuntimeException in the Service or Controller will end up here.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiCustomResponse<String>> handleAllUncaughtExceptions(Exception ex) {
        return ResponseEntity.internalServerError().body(new ApiCustomResponse<>(
                null,
                "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }
}
