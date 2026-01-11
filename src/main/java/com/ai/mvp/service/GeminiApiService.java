package com.ai.mvp.service;

import com.ai.mvp.config.GeminiConfig;
import com.ai.mvp.dto.gemini.GeminiApiRequest;
import com.ai.mvp.dto.gemini.GeminiApiResponse;
import com.ai.mvp.exception.ClaudeApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for communicating with Google Gemini API
 *
 * This service handles:
 * 1. Building requests in Gemini's expected format
 * 2. Sending HTTP requests to Google's Gemini API
 * 3. Handling responses and errors
 * 4. Managing API keys and headers
 *
 * Free Tier Limits (as of Jan 2026):
 * - 60 requests per minute
 * - 1500 requests per day
 * - Perfect for learning and development!
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiApiService {

    private final GeminiConfig config;
    private final ObjectMapper objectMapper;

    private OkHttpClient httpClient;

    /**
     * Initialize HTTP client after bean construction
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

        log.info("✅ Gemini API Service initialized with model: {}", config.getModel());
    }

    /**
     * Send a message to Gemini and get a response
     *
     * @param request The request to send to Gemini
     * @return Gemini's response
     * @throws ClaudeApiException if the API call fails (reusing for simplicity)
     */
    public GeminiApiResponse sendMessage(GeminiApiRequest request) {
        try {
            // Log the request
            log.debug("Sending request to Gemini API - Model: {}, Max Tokens: {}",
                    config.getModel(), request.getGenerationConfig().getMaxOutputTokens());

            // Convert request to JSON
            String requestJson = objectMapper.writeValueAsString(request);
            log.trace("Request JSON: {}", requestJson);

            // Build the URL with API key as query parameter
            // Gemini uses: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    config.getApiUrl(),
                    config.getModel(),
                    config.getApiKey());

            // Build HTTP request
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                // Check if request was successful
                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody);
                }

                // Parse successful response
                GeminiApiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiApiResponse.class);

                // Log response metadata
                log.debug("Received response from Gemini - Tokens: {} (in: {}, out: {}), Finish Reason: {}",
                        geminiResponse.getTotalTokens(),
                        geminiResponse.getInputTokens(),
                        geminiResponse.getOutputTokens(),
                        geminiResponse.getFinishReason());

                return geminiResponse;
            }

        } catch (IOException e) {
            log.error("Network error calling Gemini API: {}", e.getMessage(), e);
            throw new ClaudeApiException("Network error calling Gemini API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
            throw new ClaudeApiException("Unexpected error calling Gemini API: " + e.getMessage(), e);
        }
    }

    /**
     * Handle error responses from Gemini API
     */
    private void handleErrorResponse(int statusCode, String responseBody) {
        log.error("Gemini API error - Status: {}, Response: {}", statusCode, responseBody);

        String errorMessage;
        String errorType;

        try {
            // Try to parse error response
            var errorNode = objectMapper.readTree(responseBody);
            errorType = errorNode.path("error").path("code").asText("unknown");
            errorMessage = errorNode.path("error").path("message").asText("Unknown error");
        } catch (Exception e) {
            errorType = "unknown";
            errorMessage = "Failed to parse error response: " + responseBody;
        }

        // Map status codes to user-friendly messages
        switch (statusCode) {
            case 400:
                throw new ClaudeApiException("Invalid request to Gemini: " + errorMessage, statusCode, errorType);
            case 403:
                throw new ClaudeApiException("API key invalid or quota exceeded. Check your Gemini API key.", statusCode, errorType);
            case 429:
                throw new ClaudeApiException("Rate limit exceeded. Please try again later.", statusCode, errorType);
            case 500:
            case 502:
            case 503:
                throw new ClaudeApiException("Gemini service is temporarily unavailable. Please try again later.", statusCode, errorType);
            default:
                throw new ClaudeApiException("Gemini API error: " + errorMessage, statusCode, errorType);
        }
    }

    /**
     * Create a simple text message request
     * Helper method for Phase 1 - keeps things simple
     */
    public GeminiApiRequest createSimpleRequest(String userMessage, String systemPrompt,
                                                 Double temperature, Integer maxTokens) {
        // Build the user content
        GeminiApiRequest.Content userContent = GeminiApiRequest.Content.builder()
                .role("user")
                .parts(List.of(
                        GeminiApiRequest.Part.builder()
                                .text(userMessage)
                                .build()
                ))
                .build();

        // Build generation config
        GeminiApiRequest.GenerationConfig generationConfig = GeminiApiRequest.GenerationConfig.builder()
                .temperature(temperature != null ? temperature : config.getTemperature())
                .maxOutputTokens(maxTokens != null ? maxTokens : config.getMaxTokens())
                .build();

        // Build the request
        GeminiApiRequest.GeminiApiRequestBuilder requestBuilder = GeminiApiRequest.builder()
                .contents(List.of(userContent))
                .generationConfig(generationConfig);

        // Add system instruction if provided
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            GeminiApiRequest.Content systemContent = GeminiApiRequest.Content.builder()
                    .parts(List.of(
                            GeminiApiRequest.Part.builder()
                                    .text(systemPrompt)
                                    .build()
                    ))
                    .build();
            requestBuilder.systemInstruction(systemContent);
        }

        return requestBuilder.build();
    }
}
