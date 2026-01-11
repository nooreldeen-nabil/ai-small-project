package com.ai.mvp.dto.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Structured Output Prompting
 *
 * Phase 2: Prompt Engineering
 *
 * Structured output prompting requests responses in specific formats like JSON, XML, CSV, etc.
 * This is crucial when:
 * - Integrating with other systems/APIs
 * - Parsing LLM responses programmatically
 * - Storing data in databases
 * - Building data pipelines
 *
 * Example:
 * "Extract information from this text and return as JSON:
 * 'John Doe, age 30, works as a Software Engineer at Tech Corp in San Francisco.'
 *
 * Return format:
 * {
 *   \"name\": \"...\",
 *   \"age\": ...,
 *   \"occupation\": \"...\",
 *   \"company\": \"...\",
 *   \"location\": \"...\"
 * }"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Structured output prompting for JSON/formatted responses")
public class StructuredOutputRequest {

    @NotBlank(message = "Task description is required")
    @Schema(
            description = "What to extract or analyze",
            example = "Extract key information from this job posting"
    )
    private String task;

    @NotBlank(message = "Input text is required")
    @Schema(
            description = "The text to process",
            example = "Senior Java Developer needed at Tech Corp. 5+ years experience required. Salary: $120k-150k. Location: San Francisco, CA."
    )
    private String inputText;

    @NotBlank(message = "Output format is required")
    @Schema(
            description = "The desired output format (usually JSON schema)",
            example = "{\"position\": \"...\", \"experience\": \"...\", \"salary\": \"...\", \"location\": \"...\"}"
    )
    private String outputFormat;

    @Schema(
            description = "Output format type",
            example = "JSON",
            allowableValues = {"JSON", "XML", "CSV", "YAML"},
            defaultValue = "JSON"
    )
    private String formatType;

    @Schema(
            description = "Maximum tokens in response",
            example = "500",
            defaultValue = "1000"
    )
    private Integer maxTokens;

    @Schema(
            description = "Temperature for randomness (0.0-1.0) - lower for more deterministic output",
            example = "0.1",
            defaultValue = "0.2"
    )
    private Double temperature;
}
