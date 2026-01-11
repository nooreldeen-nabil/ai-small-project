package com.ai.mvp.exception;

/**
 * Exception thrown when LLM API calls fail
 *
 * This exception is used for all LLM providers (Claude, Gemini, Ollama, etc.)
 *
 * This can happen for various reasons:
 * - Invalid API key
 * - Rate limiting
 * - Network errors
 * - API service issues
 * - Invalid requests
 * - Model not found
 */
public class LlmApiException extends RuntimeException {

    private final Integer statusCode;
    private final String errorType;
    private final String provider;

    public LlmApiException(String message) {
        super(message);
        this.statusCode = null;
        this.errorType = null;
        this.provider = null;
    }

    public LlmApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.errorType = null;
        this.provider = null;
    }

    public LlmApiException(String message, Integer statusCode, String errorType) {
        super(message);
        this.statusCode = statusCode;
        this.errorType = errorType;
        this.provider = null;
    }

    public LlmApiException(String message, Integer statusCode, String errorType, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorType = errorType;
        this.provider = null;
    }

    public LlmApiException(String message, String provider, Integer statusCode, String errorType) {
        super(message);
        this.statusCode = statusCode;
        this.errorType = errorType;
        this.provider = provider;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getProvider() {
        return provider;
    }
}
