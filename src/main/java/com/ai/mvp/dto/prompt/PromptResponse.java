package com.ai.mvp.dto.prompt;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for all prompt engineering techniques
 *
 * Phase 2: Prompt Engineering
 *
 * This response includes:
 * - The LLM's actual response
 * - The technique used
 * - The full prompt that was sent
 * - Metadata (tokens, model, etc.)
 *
 * This helps you understand how different prompting techniques affect the output.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response from prompt engineering technique")
public class PromptResponse {

    @Schema(description = "The LLM's response", example = "Positive")
    private String response;

    @Schema(description = "The prompting technique used", example = "FEW_SHOT")
    private String technique;

    @Schema(description = "The full prompt that was sent to the LLM (for learning purposes)")
    private String fullPrompt;

    @Schema(description = "Model used", example = "gemini-2.5-flash")
    private String model;

    @Schema(description = "Total tokens used", example = "245")
    private Integer tokensUsed;

    @Schema(description = "Input tokens", example = "195")
    private Integer inputTokens;

    @Schema(description = "Output tokens", example = "50")
    private Integer outputTokens;

    @Schema(description = "Why the LLM stopped", example = "STOP")
    private String stopReason;

    @Schema(description = "Response timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Estimated cost in USD", example = "0.000000")
    private Double estimatedCost;
}
