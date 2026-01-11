package com.ai.mvp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for chat endpoint
 *
 * Contains Claude's response and metadata about the interaction.
 * This helps you understand:
 * - What Claude said (response)
 * - How many tokens were used (important for cost tracking)
 * - Which model was used
 * - When the response was generated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * Claude's response to the user's message
     */
    private String response;

    /**
     * The model that generated this response
     * Example: "claude-3-5-sonnet-20241022"
     */
    private String model;

    /**
     * Total tokens used in this interaction
     * Includes both input tokens (your message) and output tokens (Claude's response)
     * Important for cost tracking: ~$3 per million input tokens, ~$15 per million output tokens
     */
    private Integer tokensUsed;

    /**
     * Input tokens (your message + system prompt)
     */
    private Integer inputTokens;

    /**
     * Output tokens (Claude's response)
     */
    private Integer outputTokens;

    /**
     * Timestamp when the response was generated
     */
    private LocalDateTime timestamp;

    /**
     * Why did the response stop?
     * - "end_turn": Natural completion
     * - "max_tokens": Hit token limit
     * - "stop_sequence": Hit a stop sequence
     */
    private String stopReason;

    /**
     * Optional: Any warnings or messages from the API
     */
    private String warning;
}
