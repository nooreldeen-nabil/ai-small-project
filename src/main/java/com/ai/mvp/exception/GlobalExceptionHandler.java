package com.ai.mvp.exception;

import com.ai.mvp.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Global Exception Handler
 *
 * This class catches ALL exceptions thrown by controllers and converts them
 * to standardized ErrorResponse objects.
 *
 * Benefits:
 * - Consistent error format across the entire API
 * - Centralized error handling logic
 * - Better error logging
 * - Hide internal errors from users (security)
 *
 * How it works:
 * 1. Exception is thrown somewhere in the application
 * 2. Spring catches it and looks for a matching @ExceptionHandler
 * 3. Handler converts exception to ErrorResponse
 * 4. Spring returns error response to client
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Works across ALL controllers
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     *
     * Triggered when @Valid fails on request DTOs
     * Example: Empty message, message too long, etc.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Validation error on {}: {}",
                requestId, request.getRequestURI(), ex.getMessage());

        // Extract all validation error messages
        List<String> validationErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Invalid request parameters")
                .details("Please check the validation errors and try again")
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle LLM API errors
     *
     * These are errors from any LLM provider (Claude, Gemini, etc.):
     * - Invalid API key
     * - Rate limiting
     * - Service unavailable
     * - Model not found
     * etc.
     */
    @ExceptionHandler(LlmApiException.class)
    public ResponseEntity<ErrorResponse> handleLlmApiException(
            LlmApiException ex,
            HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        String provider = ex.getProvider() != null ? ex.getProvider() : "LLM";
        log.error("[{}] {} API error on {}: {}",
                requestId, provider, request.getRequestURI(), ex.getMessage(), ex);

        // Map API status codes to HTTP status codes
        HttpStatus httpStatus;
        if (ex.getStatusCode() != null) {
            switch (ex.getStatusCode()) {
                case 400:
                    httpStatus = HttpStatus.BAD_REQUEST;
                    break;
                case 401:
                case 403:
                    httpStatus = HttpStatus.UNAUTHORIZED;
                    break;
                case 404:
                    httpStatus = HttpStatus.NOT_FOUND;
                    break;
                case 429:
                    httpStatus = HttpStatus.TOO_MANY_REQUESTS;
                    break;
                case 500:
                case 502:
                case 503:
                    httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
                    break;
                default:
                    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String errorCode = provider.toUpperCase() + "_API_ERROR";
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(httpStatus.value())
                .error(errorCode)
                .message(ex.getMessage())
                .details(ex.getErrorType() != null ? "Error Type: " + ex.getErrorType() : null)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions
     *
     * Usually programming errors or invalid configurations
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Illegal argument on {}: {}",
                requestId, request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle illegal state exceptions
     *
     * Usually configuration errors (e.g., missing API key)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("[{}] Illegal state on {}: {}",
                requestId, request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("CONFIGURATION_ERROR")
                .message("Service configuration error")
                .details(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle all other uncaught exceptions
     *
     * This is the catch-all handler for any exception not handled above.
     * Important: Don't expose internal error details to users (security risk)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.error("[{}] Unexpected error on {}: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .details("Please contact support if the problem persists. Request ID: " + requestId)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
