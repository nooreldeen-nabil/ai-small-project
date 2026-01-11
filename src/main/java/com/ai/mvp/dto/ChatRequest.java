package com.ai.mvp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for chat endpoint
 *
 * This represents a user's message to the AI assistant.
 * In Phase 1, we keep it simple with just a message.
 * In later phases, we'll add context, conversation history, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * The user's message/question to the AI
     * Must be between 1 and 10,000 characters
     */
    @NotBlank(message = "Message cannot be empty")
    @Size(min = 1, max = 10000, message = "Message must be between 1 and 10,000 characters")
    private String message;

    /**
     * Optional: System prompt to guide Claude's behavior
     * Example: "You are a helpful Java programming tutor"
     */
    private String systemPrompt;

    /**
     * Optional: Temperature for response randomness (0.0 to 1.0)
     * Lower = more focused and deterministic
     * Higher = more creative and random
     * Default: 0.7 (from application.yml)
     */
    private Double temperature;

    /**
     * Optional: Maximum tokens in response
     * Default: 4096 (from application.yml)
     */
    private Integer maxTokens;
}
