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
                        "message", resolveMessage(ex.getMessage()),
                        "path", request.getRequestURI()
                )
        );
    }

    private String resolveMessage(String code) {
        return switch (code) {
            case "USERNAME_EXISTS" -> "Tên đăng nhập đã tồn tại";
            case "EMAIL_EXISTS"    -> "Email đã được sử dụng";
            case "USER_NOT_FOUND"  -> "Sai tài khoản hoặc mật khẩu";
            case "WRONG_PASSWORD"  -> "Sai tài khoản hoặc mật khẩu";
            case "EMAIL_NOT_FOUND" -> "Email không tồn tại trong hệ thống";
            case "INVALID_OTP"     -> "Mã OTP không hợp lệ hoặc đã hết hạn";
            default                -> "Đã có lỗi xảy ra";
        };
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