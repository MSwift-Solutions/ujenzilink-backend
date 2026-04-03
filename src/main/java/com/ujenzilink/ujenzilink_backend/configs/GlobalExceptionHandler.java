package com.ujenzilink.ujenzilink_backend.configs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {



        /**
         * CATCH-ALL EXCEPTION HANDLER
         * Any unhandled RuntimeException in the Service or Controller will end up here.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiCustomResponse<String>> handleAllUncaughtExceptions(Exception ex) {
                // Log the actual error for debugging (in production, use a proper logger)
                System.err.println("Unhandled exception: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());

                return ResponseEntity.internalServerError().body(new ApiCustomResponse<>(
                                null,
                                "An unexpected error occurred. Please try again later.",
                                HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        // Handles @Valid failures
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiCustomResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                                errors,
                                "Input validation failed",
                                HttpStatus.BAD_REQUEST.value()));
        }

        // Handles JSON parsing errors
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiCustomResponse<String>> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex) {
                return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                                null,
                                "Malformed JSON request: " + ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value()));
        }

        // Handles wrong HTTP Methods (e.g. GET instead of POST)
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiCustomResponse<String>> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException ex) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ApiCustomResponse<>(
                                null,
                                ex.getMessage(),
                                HttpStatus.METHOD_NOT_ALLOWED.value()));
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleUsernameNotFound(UsernameNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiCustomResponse<>(
                                null,
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value()));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiCustomResponse<>(
                                null,
                                "Invalid email or password",
                                HttpStatus.UNAUTHORIZED.value()));
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleDisabledAccount(DisabledException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiCustomResponse<>(
                                null,
                                "Account unverified. Please check your email for verification instructions.",
                                HttpStatus.FORBIDDEN.value()));
        }

        @ExceptionHandler(LockedException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleLockedAccount(LockedException ex) {
                String message = ex.getMessage();

                // If it's a suspension, return the suspension message
                if (message != null && (message.toLowerCase().contains("suspended") || message.contains("Reason"))) {
                        return ResponseEntity.status(HttpStatus.LOCKED).body(new ApiCustomResponse<>(
                                        null,
                                        message,
                                        HttpStatus.LOCKED.value()));
                }

                // If it's a generic LockedException (often thrown by Spring Security on wrong password for an already locked/suspended user),
                // we should return "Invalid email or password" to avoid leaking account status or showing "locked" prematurely.
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiCustomResponse<>(
                                null,
                                "Invalid email or password",
                                HttpStatus.UNAUTHORIZED.value()));
        }

        @ExceptionHandler(org.springframework.security.authentication.InternalAuthenticationServiceException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleInternalAuthException(
                        org.springframework.security.authentication.InternalAuthenticationServiceException ex) {
                if (ex.getCause() instanceof LockedException) {
                        return handleLockedAccount((LockedException) ex.getCause());
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiCustomResponse<>(
                                null,
                                ex.getMessage() != null ? ex.getMessage() : "Authentication failed.",
                                HttpStatus.UNAUTHORIZED.value()));
        }

        @ExceptionHandler(ExpiredJwtException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleExpiredJwt(ExpiredJwtException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiCustomResponse<>(
                                null, "Your session has expired. Please log in again.", 401));
        }

        @ExceptionHandler(MalformedJwtException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleMalformedJwt(MalformedJwtException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiCustomResponse<>(
                                null, "Invalid authentication token.", 401));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex) {
                // Check if it's a duplicate email constraint violation
                String message = ex.getMessage();
                if (message != null && message.toLowerCase().contains("email")) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiCustomResponse<>(
                                        null,
                                        "Email already registered. Please log in.",
                                        HttpStatus.CONFLICT.value()));
                }

                // TODO: Add proper logging here
                System.err.println("Database constraint violation: " + ex.getMessage());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiCustomResponse<>(
                                null,
                                "An error occurred while processing your request. Please try again.",
                                HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        // Handles file size limit exceeded
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleMaxUploadSizeExceededException(
                        org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
                return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(new ApiCustomResponse<>(
                                null,
                                "File size exceeds the maximum limit. Please upload a smaller file.",
                                HttpStatus.CONTENT_TOO_LARGE.value()));
        }

        // Handles generic multipart file upload errors
        @ExceptionHandler(MultipartException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleMultipartException(
                        org.springframework.web.multipart.MultipartException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiCustomResponse<>(
                                null,
                                "File upload failed. Please ensure you are uploading a valid file.",
                                HttpStatus.BAD_REQUEST.value()));
        }

        /**
         * Handles validation errors thrown by ImageValidationService and other logic
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiCustomResponse<>(
                                null,
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value()));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiCustomResponse<Void>> handleMethodArgumentTypeMismatch(
                        MethodArgumentTypeMismatchException ex) {
                String error = String.format("Invalid parameter value for '%s'. Expected type %s.",
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiCustomResponse<>(
                                null,
                                error,
                                HttpStatus.BAD_REQUEST.value()));
        }

}
