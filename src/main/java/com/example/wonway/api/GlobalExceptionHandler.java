package com.example.wonway.api;

import com.example.wonway.agent.exception.AgentUnavailableException;
import com.example.wonway.api.dto.response.ErrorResponse;
import com.example.wonway.session.exception.SessionClosedException;
import com.example.wonway.session.exception.SessionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청값이 올바르지 않습니다.");
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(
            SessionNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(SessionClosedException.class)
    public ResponseEntity<ErrorResponse> handleSessionClosed(
            SessionClosedException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, "SESSION_CLOSED", exception.getMessage(), request);
    }

    @ExceptionHandler(AgentUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAgentUnavailable(
            AgentUnavailableException exception,
            HttpServletRequest request
    ) {
        Throwable cause = exception.getCause();
        log.warn(
                "AI 상담 처리 실패: causeType={}",
                cause == null ? exception.getClass().getSimpleName() : cause.getClass().getSimpleName()
        );
        return response(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AGENT_UNAVAILABLE",
                "현재 상담 답변을 생성할 수 없습니다. 잠시 후 다시 시도해 주세요.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("예상하지 못한 서버 오류: causeType={}", exception.getClass().getSimpleName());
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다.",
                request
        );
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                code,
                message,
                request.getRequestURI()
        ));
    }
}
