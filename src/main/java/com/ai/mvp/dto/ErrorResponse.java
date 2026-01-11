package com.ai.mvp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response DTO
 *
 * Provides consistent error format across the entire API.
 * Makes it easier for clients to handle errors predictably.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * HTTP status code
     * Example: 400, 401, 500
     */
    private Integer status;

    /**
     * Short error code for programmatic handling
     * Examples: "INVALID_REQUEST", "API_ERROR", "NOT_FOUND"
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Detailed error description (for debugging)
     */
    private String details;

    /**
     * List of validation errors (if applicable)
     */
    private List<String> validationErrors;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;

    /**
     * Unique request ID (for tracking in logs)
     */
    private String requestId;
}
