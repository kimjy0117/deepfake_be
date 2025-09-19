package com.example.deepfake.common.exception;

import com.example.deepfake.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.add(fieldName + ": " + errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("VALIDATION_ERROR")
                .message("입력값이 올바르지 않습니다")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        
        log.warn("유효성 검증 오류: {}", details);
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("UNAUTHORIZED")
                .message("이메일 또는 비밀번호가 올바르지 않습니다")
                .timestamp(LocalDateTime.now())
                .build();
        
        log.warn("인증 실패: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("UNAUTHORIZED")
                .message("인증이 필요합니다")
                .timestamp(LocalDateTime.now())
                .build();
        
        log.warn("인증 오류: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("FORBIDDEN")
                .message("접근 권한이 없습니다")
                .timestamp(LocalDateTime.now())
                .build();
        
        log.warn("접근 거부: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        log.warn("리소스 없음: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflictException(ResourceConflictException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("CONFLICT")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        log.warn("리소스 충돌: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("FILE_UPLOAD_ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        log.error("파일 업로드 오류: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다")
                .timestamp(LocalDateTime.now())
                .build();
        
        log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
