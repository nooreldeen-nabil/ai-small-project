package com.ai.mvp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Anthropic Claude API
 *
 * This class reads all "anthropic.*" properties from application.yml
 * and makes them available as a Spring bean.
 *
 * Benefits:
 * - Type-safe configuration
 * - IDE autocomplete support
 * - Validation support
 * - Easy to test (can create instances programmatically)
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "anthropic")
public class AnthropicConfig {

    /**
     * Anthropic API key
     * Get yours at: https://console.anthropic.com/settings/keys
     * Example: "sk-ant-api03-..."
     */
    private String apiKey;

    /**
     * Anthropic API URL
     * Default: "https://api.anthropic.com/v1/messages"
     */
    private String apiUrl;

    /**
     * Model to use for completions
     * Options:
     * - claude-3-5-sonnet-20241022 (best balance, recommended)
     * - claude-3-opus-20240229 (most capable, expensive)
     * - claude-3-haiku-20240307 (fastest, cheapest)
     */
    private String model;

    /**
     * Maximum tokens to generate in responses
     * Range: 1 to 200,000 (depends on model)
     * Default: 4096
     *
     * Note: Higher values = longer responses but higher cost
     */
    private Integer maxTokens;

    /**
     * Temperature for response randomness
     * Range: 0.0 to 1.0
     * - 0.0: Deterministic, focused
     * - 1.0: Creative, random
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
     * Called automatically by Spring after properties are loaded
     * Note: Validation is optional if not using Claude as the provider
     */
    public void validate() {
        // Skip validation if API key is not set (may be using another provider)
        if (apiKey == null || apiKey.isEmpty()) {
            return;
        }

        if (!apiKey.startsWith("sk-ant-")) {
            throw new IllegalStateException("Invalid Anthropic API key format. Key should start with 'sk-ant-'");
        }

        if (maxTokens != null && (maxTokens < 1 || maxTokens > 200000)) {
            throw new IllegalStateException("maxTokens must be between 1 and 200,000");
        }

        if (temperature != null && (temperature < 0.0 || temperature > 1.0)) {
            throw new IllegalStateException("temperature must be between 0.0 and 1.0");
        }
    }
}
