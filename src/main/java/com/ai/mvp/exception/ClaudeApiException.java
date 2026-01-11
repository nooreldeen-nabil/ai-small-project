package com.ai.mvp.exception;

/**
 * Exception thrown when Claude API calls fail
 *
 * This can happen for various reasons:
 * - Invalid API key
 * - Rate limiting
 * - Network errors
 * - API service issues
 * - Invalid requests
 */
public class ClaudeApiException extends RuntimeException {

    private final Integer statusCode;
    private final String errorType;

    public ClaudeApiException(String message) {
        super(message);
        this.statusCode = null;
        this.errorType = null;
    }

    public ClaudeApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.errorType = null;
    }

    public ClaudeApiException(String message, Integer statusCode, String errorType) {
        super(message);
        this.statusCode = statusCode;
        this.errorType = errorType;
    }

    public ClaudeApiException(String message, Integer statusCode, String errorType, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorType = errorType;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getErrorType() {
        return errorType;
    }
}
