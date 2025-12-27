package com.teambind.co.kr.chatdding.adapter.in.web;

import com.teambind.co.kr.chatdding.adapter.in.web.dto.ApiResponse;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ApiResponse<Void>> handleChatException(ChatException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("ChatException: {} - {}", errorCode.getCode(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("유효성 검증 실패");

        log.warn("Validation failed: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeaderException(MissingRequestHeaderException e) {
        log.warn("Missing header: {}", e.getHeaderName());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("MISSING_HEADER", "필수 헤더가 누락되었습니다: " + e.getHeaderName()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"));
    }
}
