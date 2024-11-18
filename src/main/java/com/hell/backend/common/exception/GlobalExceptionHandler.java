package com.hell.backend.common.exception;

import com.hell.backend.users.exception.DuplicateEmailException;
import com.hell.backend.users.exception.InvalidCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 중복된 이메일 예외 처리
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmailException(DuplicateEmailException ex) {
        logger.error("DuplicateEmailException 발생: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Duplicate Email");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
    }

    // 잘못된 자격 증명 예외 처리 (InvalidCredentialsException 사용)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        logger.error("InvalidCredentialsException 발생: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
    }

    // 데이터 무결성 위반 예외 처리 (예: 중복 키 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("DataIntegrityViolationException 발생: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Data Integrity Violation");
        response.put("message", "데이터 무결성 위반 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
    }

    // 잘못된 자격 증명 예외 처리 (BadCredentialsException 사용)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        logger.error("BadCredentialsException 발생: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Authentication Failed");
        response.put("message", "잘못된 이메일 또는 비밀번호입니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
    }

    // 유효성 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.error("MethodArgumentNotValidException 발생: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation Failed");
        response.put("details", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 Bad Request
    }

    // JSON 파싱 오류 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("HttpMessageNotReadableException 발생: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Malformed JSON");
        response.put("message", "요청한 JSON 형식이 잘못되었습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 Bad Request
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception ex) {
        logger.error("Unexpected exception 발생: ", ex);
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "예상치 못한 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 Internal Server Error
    }
}
