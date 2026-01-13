package com.ai.mvp.dto.rag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for document Q&A (RAG)
 *
 * Phase 4: RAG Implementation
 * Ask questions about uploaded documents using retrieval augmented generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Document Q&A request using RAG")
public class DocumentQARequest {

    @NotBlank(message = "Question is required")
    @Size(min = 3, max = 1000, message = "Question must be between 3 and 1000 characters")
    @Schema(description = "The question to ask about the documents",
            example = "How do neural networks learn?",
            required = true)
    private String question;

    @Min(value = 1, message = "topK must be at least 1")
    @Max(value = 20, message = "topK cannot exceed 20")
    @Schema(description = "Number of relevant chunks to retrieve",
            example = "5",
            defaultValue = "5")
    @Builder.Default
    private Integer topK = 5;

    @Min(value = 0, message = "Similarity threshold must be between 0.0 and 1.0")
    @Max(value = 1, message = "Similarity threshold must be between 0.0 and 1.0")
    @Schema(description = "Minimum similarity score threshold (0.0 to 1.0)",
            example = "0.5",
            defaultValue = "0.5")
    @Builder.Default
    private Double similarityThreshold = 0.5;

    @Schema(description = "Filter by document category (optional)",
            example = "AI/ML")
    private String category;

    @Min(value = 100, message = "maxTokens must be at least 100")
    @Max(value = 4096, message = "maxTokens cannot exceed 4096")
    @Schema(description = "Maximum tokens for LLM response",
            example = "2000",
            defaultValue = "2000")
    @Builder.Default
    private Integer maxTokens = 2000;

    @Schema(description = "Temperature for LLM response (0.0 = deterministic, 1.0 = creative)",
            example = "0.3",
            defaultValue = "0.3")
    @Builder.Default
    private Double temperature = 0.3;
}
