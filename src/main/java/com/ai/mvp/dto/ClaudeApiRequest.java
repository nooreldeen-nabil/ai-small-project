package com.ai.mvp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal DTO for Anthropic Claude API request
 *
 * This matches the exact format expected by Claude's API.
 * Documentation: https://docs.anthropic.com/claude/reference/messages_post
 *
 * Important: We send requests in this format but users don't see it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeApiRequest {

    /**
     * The model to use
     * Example: "claude-3-5-sonnet-20241022"
     */
    @JsonProperty("model")
    private String model;

    /**
     * Maximum tokens to generate in response
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * List of messages in the conversation
     * Each message has a "role" (user/assistant) and "content"
     */
    @JsonProperty("messages")
    private List<Message> messages;

    /**
     * Optional: System prompt to guide Claude's behavior
     */
    @JsonProperty("system")
    private String system;

    /**
     * Temperature for randomness (0.0 to 1.0)
     */
    @JsonProperty("temperature")
    private Double temperature;

    /**
     * Optional: Metadata for request tracking
     */
    @JsonProperty("metadata")
    private Metadata metadata;

    /**
     * Message in the conversation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * Role: "user" or "assistant"
         */
        @JsonProperty("role")
        private String role;

        /**
         * Content of the message
         */
        @JsonProperty("content")
        private String content;
    }

    /**
     * Metadata for request tracking
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        /**
         * Unique user ID (for tracking/analytics)
         */
        @JsonProperty("user_id")
        private String userId;
    }
}
