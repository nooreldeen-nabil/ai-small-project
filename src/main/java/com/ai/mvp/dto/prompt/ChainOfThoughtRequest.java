package com.ai.mvp.dto.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Chain-of-Thought Prompting
 *
 * Phase 2: Prompt Engineering
 *
 * Chain-of-thought prompting asks the LLM to show its reasoning process step-by-step.
 * This is particularly effective for:
 * - Math problems
 * - Logic puzzles
 * - Complex analysis
 * - Multi-step reasoning
 *
 * Example:
 * "Solve this problem step by step:
 * A store has 15 apples. They sell 40% in the morning.
 * How many apples are left?
 *
 * Let's think through this step by step:
 * 1. ..."
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chain-of-thought prompting for step-by-step reasoning")
public class ChainOfThoughtRequest {

    @NotBlank(message = "Problem is required")
    @Schema(
            description = "The problem or question to solve",
            example = "A store has 15 apples. They sell 40% in the morning and 1/3 of the remainder in the afternoon. How many are left?"
    )
    private String problem;

    @Schema(
            description = "Whether to explicitly request step-by-step reasoning",
            example = "true",
            defaultValue = "true"
    )
    private Boolean requestSteps;

    @Schema(
            description = "Custom reasoning instruction",
            example = "Let's solve this step by step:",
            defaultValue = "Let's think through this step by step:"
    )
    private String reasoningPrompt;

    @Schema(
            description = "Maximum tokens in response",
            example = "1000",
            defaultValue = "1500"
    )
    private Integer maxTokens;

    @Schema(
            description = "Temperature for randomness (0.0-1.0)",
            example = "0.2",
            defaultValue = "0.3"
    )
    private Double temperature;
}
