package com.ai.mvp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal DTO for Anthropic Claude API response
 *
 * This matches the exact format returned by Claude's API.
 * Documentation: https://docs.anthropic.com/claude/reference/messages_post
 *
 * We parse this response and convert it to our ChatResponse for users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeApiResponse {

    /**
     * Unique ID for this response
     */
    @JsonProperty("id")
    private String id;

    /**
     * Type of response (always "message" for chat)
     */
    @JsonProperty("type")
    private String type;

    /**
     * Role of the responder (always "assistant" for Claude)
     */
    @JsonProperty("role")
    private String role;

    /**
     * Content blocks in the response
     * Usually contains one text block with Claude's message
     */
    @JsonProperty("content")
    private List<ContentBlock> content;

    /**
     * Model that generated this response
     */
    @JsonProperty("model")
    private String model;

    /**
     * Why the response stopped
     * - "end_turn": Natural completion
     * - "max_tokens": Hit token limit
     * - "stop_sequence": Hit a stop sequence
     */
    @JsonProperty("stop_reason")
    private String stopReason;

    /**
     * Which stop sequence was hit (if applicable)
     */
    @JsonProperty("stop_sequence")
    private String stopSequence;

    /**
     * Token usage information
     */
    @JsonProperty("usage")
    private Usage usage;

    /**
     * Content block in the response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentBlock {
        /**
         * Type of content (usually "text")
         */
        @JsonProperty("type")
        private String type;

        /**
         * The actual text content
         */
        @JsonProperty("text")
        private String text;
    }

    /**
     * Token usage breakdown
     * IMPORTANT: Track this for cost management!
     *
     * Pricing (as of Jan 2026):
     * - Input tokens: ~$3 per million tokens
     * - Output tokens: ~$15 per million tokens
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * Number of input tokens (your prompt)
         */
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        /**
         * Number of output tokens (Claude's response)
         */
        @JsonProperty("output_tokens")
        private Integer outputTokens;
    }

    /**
     * Helper method to extract the text content from the response
     *
     * @return The text content from the first content block
     */
    public String getTextContent() {
        if (content != null && !content.isEmpty()) {
            ContentBlock firstBlock = content.get(0);
            if ("text".equals(firstBlock.getType())) {
                return firstBlock.getText();
            }
        }
        return "";
    }

    /**
     * Helper method to get total tokens used
     *
     * @return Total tokens (input + output)
     */
    public Integer getTotalTokens() {
        if (usage != null) {
            return usage.getInputTokens() + usage.getOutputTokens();
        }
        return 0;
    }
}
