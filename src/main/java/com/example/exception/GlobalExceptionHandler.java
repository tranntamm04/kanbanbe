package com.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "code", "VALIDATION_ERROR",
                        "message", "Dữ liệu gửi lên chưa hợp lệ",
                        "fields", fields,
                        "path", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception at {}", request.getRequestURI(), ex);

        return ResponseEntity.internalServerError().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 500,
                        "code", "INTERNAL_ERROR",
                        "message", "Đã có lỗi xảy ra",
                        "path", request.getRequestURI()
                )
        );
    }

    private String resolveMessage(String code) {
        return switch (code) {
            case "USERNAME_EXISTS" -> "Tên đăng nhập đã tồn tại";
            case "EMAIL_EXISTS" -> "Email đã được sử dụng";
            case "USER_NOT_FOUND", "WRONG_PASSWORD" -> "Sai tài khoản hoặc mật khẩu";
            case "EMAIL_NOT_FOUND" -> "Email không tồn tại trong hệ thống";
            case "INVALID_OTP" -> "Mã OTP không hợp lệ hoặc đã hết hạn";
            case "Access denied", "Permission denied", "Not member", "Not a member of this workspace" ->
                    "Bạn không có quyền thực hiện thao tác này";
            case "Workspace not found" -> "Không tìm thấy workspace";
            case "Only owner or admin can invite users" -> "Chỉ chủ sở hữu hoặc quản trị viên được mời thành viên";
            case "User is already a member" -> "Người dùng này đã là thành viên của workspace";
            case "Only owner can delete workspace" -> "Chỉ chủ sở hữu được xóa workspace";
            case "Invalid invite token" -> "Lời mời không hợp lệ";
            case "Invite already accepted" -> "Lời mời này đã được chấp nhận";
            case "Invite expired" -> "Lời mời đã hết hạn";
            case "Email mismatch" -> "Email tài khoản không khớp với email được mời";
            case "Already a member" -> "Bạn đã là thành viên của workspace này";
            default -> "Đã có lỗi xảy ra";
        };
    }
}