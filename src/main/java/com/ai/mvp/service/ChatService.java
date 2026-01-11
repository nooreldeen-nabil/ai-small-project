package com.ai.mvp.service;

import com.ai.mvp.config.LlmProvider;
import com.ai.mvp.dto.ChatRequest;
import com.ai.mvp.dto.ChatResponse;
import com.ai.mvp.dto.ClaudeApiRequest;
import com.ai.mvp.dto.ClaudeApiResponse;
import com.ai.mvp.dto.gemini.GeminiApiRequest;
import com.ai.mvp.dto.gemini.GeminiApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * High-level chat service
 *
 * This service:
 * 1. Takes simple ChatRequest from controllers
 * 2. Delegates to the appropriate LLM provider (Claude, Gemini, etc.)
 * 3. Converts provider responses to user-friendly ChatResponse
 *
 * Supports multiple LLM providers:
 * - Claude (Anthropic) - High quality, paid
 * - Gemini (Google) - Free tier available
 * - Ollama - Local, completely free
 *
 * Provider is configured via application.yml: llm.provider
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ClaudeApiService claudeApiService;
    private final GeminiApiService geminiApiService;

    @Value("${llm.provider:GEMINI}")
    private String providerName;

    /**
     * Send a chat message and get a response
     *
     * This is the main entry point for Phase 1.
     * Takes a user's message, sends it to the configured LLM provider, and returns the response.
     *
     * @param chatRequest The user's message and parameters
     * @return LLM's response with metadata
     */
    public ChatResponse chat(ChatRequest chatRequest) {
        LlmProvider provider = LlmProvider.valueOf(providerName.toUpperCase());
        log.info("Processing chat request using {} - Message length: {} chars",
                provider, chatRequest.getMessage().length());

        try {
            ChatResponse response;

            switch (provider) {
                case CLAUDE:
                    response = chatWithClaude(chatRequest);
                    break;
                case GEMINI:
                    response = chatWithGemini(chatRequest);
                    break;
                default:
                    throw new IllegalStateException("Unsupported LLM provider: " + provider);
            }

            log.info("Chat completed successfully using {} - Tokens used: {} (in: {}, out: {})",
                    provider,
                    response.getTokensUsed(),
                    response.getInputTokens(),
                    response.getOutputTokens());

            return response;

        } catch (Exception e) {
            log.error("Error processing chat request with {}: {}", provider, e.getMessage(), e);
            throw e; // Will be caught by global exception handler
        }
    }

    /**
     * Chat using Claude (Anthropic)
     */
    private ChatResponse chatWithClaude(ChatRequest chatRequest) {
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
        return convertClaudeResponse(apiResponse);
    }

    /**
     * Chat using Gemini (Google)
     */
    private ChatResponse chatWithGemini(ChatRequest chatRequest) {
        // Create Gemini API request
        GeminiApiRequest apiRequest = geminiApiService.createSimpleRequest(
                chatRequest.getMessage(),
                chatRequest.getSystemPrompt(),
                chatRequest.getTemperature(),
                chatRequest.getMaxTokens()
        );

        // Call Gemini API
        GeminiApiResponse apiResponse = geminiApiService.sendMessage(apiRequest);

        // Convert to user-friendly response
        return convertGeminiResponse(apiResponse);
    }

    /**
     * Convert ClaudeApiResponse to ChatResponse
     */
    private ChatResponse convertClaudeResponse(ClaudeApiResponse apiResponse) {
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
     * Convert GeminiApiResponse to ChatResponse
     */
    private ChatResponse convertGeminiResponse(GeminiApiResponse apiResponse) {
        return ChatResponse.builder()
                .response(apiResponse.getTextContent())
                .model(apiResponse.getModelVersion() != null ? apiResponse.getModelVersion() : "gemini-pro")
                .tokensUsed(apiResponse.getTotalTokens())
                .inputTokens(apiResponse.getInputTokens())
                .outputTokens(apiResponse.getOutputTokens())
                .stopReason(apiResponse.getFinishReason())
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
