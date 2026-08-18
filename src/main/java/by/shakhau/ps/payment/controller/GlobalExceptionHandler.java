package by.shakhau.ps.payment.controller;

import by.shakhau.ps.payment.controller.dto.ErrorResponse;
import by.shakhau.ps.payment.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<Class<? extends Exception>, HttpStatus> RESPONSE_STATUSES = new HashMap<>();

    private final boolean localProfile;

    public GlobalExceptionHandler(@Value("${spring.profiles.active}") String activeProfile) {
        this.localProfile = "local".equals(activeProfile);
    }

    static {
        RESPONSE_STATUSES.put(ResourceNotFoundException.class, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        HttpStatus status = findHttpStatus(exception.getClass());

        if (status.is5xxServerError()) {
            log.error("Status: {}, request: {}, exception message: {}",
                    status.value(), request.getRequestURI(), exception.getMessage(), exception);
            if (localProfile) {
                return buildErrorResponse(status, exception, request);
            }

            return buildErrorResponse(status, "Server error", request);
        }

        log.warn("Status: {}, request: {}, exception message: {}",
                status.value(), request.getRequestURI(), exception.getMessage());

        return buildErrorResponse(status, exception, request);
    }

    private HttpStatus findHttpStatus(Class<?> exceptionType) {
        while (exceptionType != null && exceptionType != Exception.class) {
            HttpStatus status = RESPONSE_STATUSES.get(exceptionType);
            if (status != null) {
                return status;
            }
            exceptionType = exceptionType.getSuperclass();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String message, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, Exception exception, HttpServletRequest request) {
        return buildErrorResponse(status, exception.getMessage(), request);
    }
}
