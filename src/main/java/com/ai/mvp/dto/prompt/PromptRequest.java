package com.ai.mvp.dto.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base request DTO for prompt engineering techniques
 *
 * Phase 2: Prompt Engineering
 * This base class is extended by specific prompt technique DTOs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base request for prompt engineering techniques")
public class PromptRequest {

    @NotBlank(message = "Message is required")
    @Schema(
            description = "The main question or task to send to the LLM",
            example = "Classify this product review: 'Amazing product! Highly recommend.'"
    )
    private String message;

    @Schema(
            description = "Maximum tokens in response",
            example = "500",
            defaultValue = "1000"
    )
    private Integer maxTokens;

    @Schema(
            description = "Temperature for randomness (0.0-1.0)",
            example = "0.7",
            defaultValue = "0.7"
    )
    private Double temperature;
}
