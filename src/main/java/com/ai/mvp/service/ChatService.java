package com.ai.mvp.service;

import com.ai.mvp.dto.ChatRequest;
import com.ai.mvp.dto.ChatResponse;
import com.ai.mvp.dto.ClaudeApiRequest;
import com.ai.mvp.dto.ClaudeApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * High-level chat service
 *
 * This service:
 * 1. Takes simple ChatRequest from controllers
 * 2. Calls ClaudeApiService to communicate with Anthropic
 * 3. Converts ClaudeApiResponse to user-friendly ChatResponse
 *
 * Why separate ClaudeApiService and ChatService?
 * - Separation of concerns: API communication vs business logic
 * - Easier testing: Can mock ClaudeApiService
 * - Flexibility: Can add caching, rate limiting, etc. here
 * - Cleaner code: Controllers don't need to know about Claude API details
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ClaudeApiService claudeApiService;

    /**
     * Send a chat message and get a response
     *
     * This is the main entry point for Phase 1.
     * Takes a user's message, sends it to Claude, and returns the response.
     *
     * @param chatRequest The user's message and parameters
     * @return Claude's response with metadata
     */
    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("Processing chat request - Message length: {} chars", chatRequest.getMessage().length());

        try {
            // Create Claude API request
            ClaudeApiRequest apiRequest = claudeApiService.createSimpleRequest(
                    chatRequest.getMessage(),
                    chatRequest.getSystemPrompt(),
                    chatRequest.getTemperature(),
                    chatRequest.getMaxTokens()
            );

            // Call Claude API
            ClaudeApiResponse apiResponse = claudeApiService.sendMessage(apiRequest);

            // Convert to user-friendly response
            ChatResponse response = convertToResponse(apiResponse);

            log.info("Chat completed successfully - Tokens used: {} (in: {}, out: {})",
                    response.getTokensUsed(),
                    response.getInputTokens(),
                    response.getOutputTokens());

            return response;

        } catch (Exception e) {
            log.error("Error processing chat request: {}", e.getMessage(), e);
            throw e; // Will be caught by global exception handler
        }
    }

    /**
     * Convert ClaudeApiResponse to ChatResponse
     *
     * This method:
     * - Extracts the text content from Claude's response
     * - Copies over metadata (tokens, model, etc.)
     * - Adds timestamp
     * - Handles any warnings or issues
     *
     * @param apiResponse Raw response from Claude API
     * @return User-friendly chat response
     */
    private ChatResponse convertToResponse(ClaudeApiResponse apiResponse) {
        return ChatResponse.builder()
                .response(apiResponse.getTextContent())
                .model(apiResponse.getModel())
                .tokensUsed(apiResponse.getTotalTokens())
                .inputTokens(apiResponse.getUsage().getInputTokens())
                .outputTokens(apiResponse.getUsage().getOutputTokens())
                .stopReason(apiResponse.getStopReason())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Calculate approximate cost for a request
     * Useful for understanding API costs
     *
     * Pricing (Claude 3.5 Sonnet as of Jan 2026):
     * - Input: $3 per million tokens
     * - Output: $15 per million tokens
     *
     * @param inputTokens Number of input tokens
     * @param outputTokens Number of output tokens
     * @return Approximate cost in USD
     */
    public double calculateCost(int inputTokens, int outputTokens) {
        double inputCost = (inputTokens / 1_000_000.0) * 3.0;
        double outputCost = (outputTokens / 1_000_000.0) * 15.0;
        return inputCost + outputCost;
    }
}
