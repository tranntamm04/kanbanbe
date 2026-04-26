package com.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "code", ex.getMessage(),
                        "message", "Business error",
                        "path", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 500,
                        "code", "INTERNAL_ERROR",
                        "message", ex.getMessage(),
                        "path", request.getRequestURI()
                )
        );
    }
}