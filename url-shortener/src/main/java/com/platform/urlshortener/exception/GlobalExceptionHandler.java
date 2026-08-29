package com.platform.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.platform.urlshortener.exception.DuplicateAliasException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid request");

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "error", "BAD_REQUEST",
                        "message", message
                ));
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleShortUrlNotFound(
            ShortUrlNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "NOT_FOUND",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(ShortUrlExpiredException.class)
    public ResponseEntity<Map<String, String>> handleShortUrlExpired(
            ShortUrlExpiredException exception) {

        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(Map.of(
                        "error", "GONE",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(DuplicateAliasException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateAlias(
            DuplicateAliasException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "CONFLICT",
                        "message", exception.getMessage()
                ));
    }    
}