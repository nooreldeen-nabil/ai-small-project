package com.ai.mvp.dto.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Few-Shot Prompting
 *
 * Phase 2: Prompt Engineering
 *
 * Few-shot prompting provides examples to guide the LLM's response format and style.
 * This is useful when you want consistent formatting or when the task is ambiguous.
 *
 * Example:
 * Classify sentiment:
 *
 * Review: "Great product!" -> Sentiment: Positive
 * Review: "Terrible quality" -> Sentiment: Negative
 * Review: "It's okay" -> Sentiment: ?
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Few-shot prompting with examples to guide the LLM")
public class FewShotRequest {

    @NotBlank(message = "Task description is required")
    @Schema(
            description = "Description of the task",
            example = "Classify the sentiment of product reviews"
    )
    private String task;

    @NotEmpty(message = "At least one example is required")
    @Schema(
            description = "List of input-output example pairs",
            example = "[{\"input\": \"Great product!\", \"output\": \"Positive\"}, {\"input\": \"Terrible\", \"output\": \"Negative\"}]"
    )
    private List<Example> examples;

    @NotBlank(message = "Input to process is required")
    @Schema(
            description = "The actual input to process",
            example = "The delivery was slow but product is okay."
    )
    private String input;

    @Schema(
            description = "Maximum tokens in response",
            example = "100",
            defaultValue = "500"
    )
    private Integer maxTokens;

    @Schema(
            description = "Temperature for randomness (0.0-1.0)",
            example = "0.3",
            defaultValue = "0.7"
    )
    private Double temperature;

    /**
     * Example pair: input and expected output
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Example input-output pair")
    public static class Example {
        @NotBlank
        @Schema(description = "Example input", example = "Great product!")
        private String input;

        @NotBlank
        @Schema(description = "Example output", example = "Positive")
        private String output;
    }
}
