package com.ai.mvp.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Google Gemini API
 *
 * Documentation: https://ai.google.dev/api/rest/v1/models/generateContent
 *
 * Gemini API structure is different from Claude's, but we'll adapt it
 * to work with our existing ChatService interface.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiApiRequest {

    /**
     * List of content parts (text, images, etc.)
     * For simple text chat, we use one text part
     */
    @JsonProperty("contents")
    private List<Content> contents;

    /**
     * Optional: Safety settings to control harmful content filtering
     */
    @JsonProperty("safetySettings")
    private List<SafetySetting> safetySettings;

    /**
     * Optional: Generation configuration (temperature, maxTokens, etc.)
     */
    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;

    /**
     * Optional: System instruction (similar to Claude's system prompt)
     */
    @JsonProperty("systemInstruction")
    private Content systemInstruction;

    /**
     * Content object containing parts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        /**
         * Role: "user" or "model" (Gemini uses "model" instead of "assistant")
         */
        @JsonProperty("role")
        private String role;

        /**
         * List of parts (usually just one text part)
         */
        @JsonProperty("parts")
        private List<Part> parts;
    }

    /**
     * Part of content (text, image, etc.)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        /**
         * Text content
         */
        @JsonProperty("text")
        private String text;
    }

    /**
     * Safety settings to control content filtering
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetySetting {
        /**
         * Category to filter (HARM_CATEGORY_HARASSMENT, etc.)
         */
        @JsonProperty("category")
        private String category;

        /**
         * Threshold (BLOCK_NONE, BLOCK_ONLY_HIGH, etc.)
         */
        @JsonProperty("threshold")
        private String threshold;
    }

    /**
     * Generation configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        /**
         * Temperature (0.0 to 1.0)
         */
        @JsonProperty("temperature")
        private Double temperature;

        /**
         * Maximum output tokens
         */
        @JsonProperty("maxOutputTokens")
        private Integer maxOutputTokens;

        /**
         * Top-p sampling (alternative to temperature)
         */
        @JsonProperty("topP")
        private Double topP;

        /**
         * Top-k sampling
         */
        @JsonProperty("topK")
        private Integer topK;
    }
}
