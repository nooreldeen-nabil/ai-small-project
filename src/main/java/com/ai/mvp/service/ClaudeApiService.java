package com.ai.mvp.service;

import com.ai.mvp.config.AnthropicConfig;
import com.ai.mvp.dto.ClaudeApiRequest;
import com.ai.mvp.dto.ClaudeApiResponse;
import com.ai.mvp.exception.ClaudeApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service for communicating with Anthropic Claude API
 *
 * This is the core service that:
 * 1. Builds requests in Claude's expected format
 * 2. Sends HTTP requests to Anthropic API
 * 3. Handles responses and errors
 * 4. Manages API keys and headers
 *
 * Learning Points:
 * - How to call external REST APIs from Spring Boot
 * - How to handle authentication (API keys)
 * - How to parse JSON responses
 * - How to handle errors gracefully
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeApiService {

    private final AnthropicConfig config;
    private final ObjectMapper objectMapper;

    private OkHttpClient httpClient;

    /**
     * Initialize HTTP client after bean construction
     * PostConstruct runs after dependency injection is complete
     */
    @PostConstruct
    public void init() {
        // Validate configuration
        config.validate();

        // Create HTTP client with timeout settings
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                .build();

        log.info("✅ Claude API Service initialized with model: {}", config.getModel());
    }

    /**
     * Send a message to Claude and get a response
     *
     * This is the main method that:
     * 1. Takes a ClaudeApiRequest
     * 2. Converts it to JSON
     * 3. Sends HTTP POST to Anthropic API
     * 4. Parses the response
     * 5. Returns ClaudeApiResponse
     *
     * @param request The request to send to Claude
     * @return Claude's response
     * @throws ClaudeApiException if the API call fails
     */
    public ClaudeApiResponse sendMessage(ClaudeApiRequest request) {
        try {
            // Log the request (sanitize API key)
            log.debug("Sending request to Claude API - Model: {}, Max Tokens: {}",
                    request.getModel(), request.getMaxTokens());

            // Convert request to JSON
            String requestJson = objectMapper.writeValueAsString(request);
            log.trace("Request JSON: {}", requestJson);

            // Build HTTP request
            Request httpRequest = new Request.Builder()
                    .url(config.getApiUrl())
                    .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                    .addHeader("x-api-key", config.getApiKey())
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                // Check if request was successful
                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody);
                }

                // Parse successful response
                ClaudeApiResponse claudeResponse = objectMapper.readValue(responseBody, ClaudeApiResponse.class);

                // Log response metadata
                log.debug("Received response from Claude - ID: {}, Tokens: {} (in: {}, out: {}), Stop Reason: {}",
                        claudeResponse.getId(),
                        claudeResponse.getTotalTokens(),
                        claudeResponse.getUsage().getInputTokens(),
                        claudeResponse.getUsage().getOutputTokens(),
                        claudeResponse.getStopReason());

                return claudeResponse;
            }

        } catch (IOException e) {
            log.error("Network error calling Claude API: {}", e.getMessage(), e);
            throw new ClaudeApiException("Network error calling Claude API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Claude API: {}", e.getMessage(), e);
            throw new ClaudeApiException("Unexpected error calling Claude API: " + e.getMessage(), e);
        }
    }

    /**
     * Handle error responses from Claude API
     *
     * Different error codes mean different things:
     * - 400: Bad request (invalid parameters)
     * - 401: Authentication error (invalid API key)
     * - 429: Rate limit exceeded
     * - 500: Anthropic server error
     */
    private void handleErrorResponse(int statusCode, String responseBody) {
        log.error("Claude API error - Status: {}, Response: {}", statusCode, responseBody);

        String errorMessage;
        String errorType;

        try {
            // Try to parse error response
            var errorNode = objectMapper.readTree(responseBody);
            errorType = errorNode.path("error").path("type").asText("unknown");
            errorMessage = errorNode.path("error").path("message").asText("Unknown error");
        } catch (Exception e) {
            errorType = "unknown";
            errorMessage = "Failed to parse error response: " + responseBody;
        }

        // Map status codes to user-friendly messages
        switch (statusCode) {
            case 400:
                throw new ClaudeApiException("Invalid request: " + errorMessage, statusCode, errorType);
            case 401:
                throw new ClaudeApiException("Authentication failed. Check your API key.", statusCode, errorType);
            case 429:
                throw new ClaudeApiException("Rate limit exceeded. Please try again later.", statusCode, errorType);
            case 500:
            case 502:
            case 503:
                throw new ClaudeApiException("Anthropic service is temporarily unavailable. Please try again later.", statusCode, errorType);
            default:
                throw new ClaudeApiException("API error: " + errorMessage, statusCode, errorType);
        }
    }

    /**
     * Create a simple text message request
     * Helper method for Phase 1 - keeps things simple
     */
    public ClaudeApiRequest createSimpleRequest(String userMessage, String systemPrompt,
                                                 Double temperature, Integer maxTokens) {
        return ClaudeApiRequest.builder()
                .model(config.getModel())
                .maxTokens(maxTokens != null ? maxTokens : config.getMaxTokens())
                .temperature(temperature != null ? temperature : config.getTemperature())
                .system(systemPrompt)
                .messages(java.util.List.of(
                        ClaudeApiRequest.Message.builder()
                                .role("user")
                                .content(userMessage)
                                .build()
                ))
                .build();
    }
}
