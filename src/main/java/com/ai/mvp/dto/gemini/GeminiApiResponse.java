package com.ai.mvp.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Google Gemini API
 *
 * Documentation: https://ai.google.dev/api/rest/v1/models/generateContent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiApiResponse {

    /**
     * List of candidates (response options)
     * Usually contains one candidate
     */
    @JsonProperty("candidates")
    private List<Candidate> candidates;

    /**
     * Token usage metadata
     */
    @JsonProperty("usageMetadata")
    private UsageMetadata usageMetadata;

    /**
     * Model version used
     */
    @JsonProperty("modelVersion")
    private String modelVersion;

    /**
     * Candidate response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        /**
         * Content of the response
         */
        @JsonProperty("content")
        private Content content;

        /**
         * Finish reason (STOP, MAX_TOKENS, SAFETY, etc.)
         */
        @JsonProperty("finishReason")
        private String finishReason;

        /**
         * Safety ratings
         */
        @JsonProperty("safetyRatings")
        private List<SafetyRating> safetyRatings;

        /**
         * Index of this candidate
         */
        @JsonProperty("index")
        private Integer index;
    }

    /**
     * Content object
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        /**
         * List of parts
         */
        @JsonProperty("parts")
        private List<Part> parts;

        /**
         * Role (usually "model")
         */
        @JsonProperty("role")
        private String role;
    }

    /**
     * Part of content
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
     * Safety rating for content
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyRating {
        /**
         * Category
         */
        @JsonProperty("category")
        private String category;

        /**
         * Probability (NEGLIGIBLE, LOW, MEDIUM, HIGH)
         */
        @JsonProperty("probability")
        private String probability;
    }

    /**
     * Token usage metadata
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageMetadata {
        /**
         * Number of prompt tokens (input)
         */
        @JsonProperty("promptTokenCount")
        private Integer promptTokenCount;

        /**
         * Number of candidates tokens (output)
         */
        @JsonProperty("candidatesTokenCount")
        private Integer candidatesTokenCount;

        /**
         * Total token count
         */
        @JsonProperty("totalTokenCount")
        private Integer totalTokenCount;
    }

    /**
     * Helper method to extract text from the first candidate
     */
    public String getTextContent() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.getContent() != null &&
                firstCandidate.getContent().getParts() != null &&
                !firstCandidate.getContent().getParts().isEmpty()) {
                return firstCandidate.getContent().getParts().get(0).getText();
            }
        }
        return "";
    }

    /**
     * Helper method to get finish reason
     */
    public String getFinishReason() {
        if (candidates != null && !candidates.isEmpty()) {
            return candidates.get(0).getFinishReason();
        }
        return "UNKNOWN";
    }

    /**
     * Helper method to get total tokens
     */
    public Integer getTotalTokens() {
        if (usageMetadata != null) {
            return usageMetadata.getTotalTokenCount();
        }
        return 0;
    }

    /**
     * Helper method to get input tokens
     */
    public Integer getInputTokens() {
        if (usageMetadata != null) {
            return usageMetadata.getPromptTokenCount();
        }
        return 0;
    }

    /**
     * Helper method to get output tokens
     */
    public Integer getOutputTokens() {
        if (usageMetadata != null) {
            return usageMetadata.getCandidatesTokenCount();
        }
        return 0;
    }
}
