package com.example.assistant_rh.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication
    .BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.
    MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.
    ExceptionHandler;
import org.springframework.web.bind.annotation.
    RestControllerAdvice;
import org.springframework.web.multipart.
    MaxUploadSizeExceededException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
            handleNotFound(ResourceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND,
            ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>>
            handleEmailExists(
                EmailAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT,
            ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>>
            handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST,
            ex.getMessage());
    }

    @ExceptionHandler(
        InsufficientLeaveBalanceException.class)
    public ResponseEntity<Map<String, Object>>
            handleBalance(
                InsufficientLeaveBalanceException ex) {
        return build(HttpStatus.BAD_REQUEST,
            ex.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Map<String, Object>>
            handleFileUpload(FileUploadException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>>
            handleBadCredentials(
                BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED,
            "Incorrect email or password");
    }

    @ExceptionHandler(
        MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
            handleValidation(
                MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
            .forEach(error -> {
                String field =
                    ((FieldError) error).getField();
                errors.put(field,
                    error.getDefaultMessage());
            });
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp",
            LocalDateTime.now().toString());
        body.put("status", 400);
        body.put("error", "Validation error");
        body.put("details", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(
        MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>>
            handleMaxSize(
                MaxUploadSizeExceededException ex) {
        return build(HttpStatus.BAD_REQUEST,
            "File too large. Maximum size: 10MB");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
            handleGeneric(Exception ex) {
        log.error("An unexpected error occurred : {}",
            ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> build(
            HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp",
            LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
    // Ajouter dans GlobalExceptionHandler.java
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Map<String, Object>>
            handlePasswordMismatch(
                PasswordMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
            ex.getMessage());
    }
}
