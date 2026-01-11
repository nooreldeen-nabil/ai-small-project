package com.ai.mvp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Google Gemini API
 *
 * Get your free API key at: https://aistudio.google.com/app/apikey
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiConfig {

    /**
     * Google Gemini API key
     * Get yours at: https://aistudio.google.com/app/apikey
     * Example: "AIzaSy..."
     */
    private String apiKey;

    /**
     * Gemini API base URL
     * Default: "https://generativelanguage.googleapis.com/v1beta"
     */
    private String apiUrl;

    /**
     * Model to use
     * Options:
     * - gemini-1.5-flash (recommended for learning - fast and free)
     * - gemini-1.5-pro (more capable, still free tier available)
     * - gemini-1.0-pro (older model)
     */
    private String model;

    /**
     * Maximum output tokens
     * Range: 1 to 8192 for Flash, up to 32768 for Pro
     * Default: 2048
     */
    private Integer maxTokens;

    /**
     * Temperature for randomness
     * Range: 0.0 to 2.0 (Gemini allows up to 2.0)
     * Default: 0.7
     */
    private Double temperature;

    /**
     * Timeout for API requests (in seconds)
     * Default: 60 seconds
     */
    private Integer timeoutSeconds;

    /**
     * Validate configuration
     */
    public void validate() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is required. Please set GEMINI_API_KEY in .env file");
        }

        if (!apiKey.startsWith("AIza")) {
            throw new IllegalStateException("Invalid Gemini API key format. Key should start with 'AIza'");
        }

        if (maxTokens != null && (maxTokens < 1 || maxTokens > 32768)) {
            throw new IllegalStateException("maxTokens must be between 1 and 32,768");
        }

        if (temperature != null && (temperature < 0.0 || temperature > 2.0)) {
            throw new IllegalStateException("temperature must be between 0.0 and 2.0");
        }
    }
}
