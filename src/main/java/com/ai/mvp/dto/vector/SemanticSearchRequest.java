package com.ai.mvp.dto.vector;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for semantic search
 *
 * Phase 3: Vector Database & Embeddings
 *
 * Semantic search finds documents by meaning, not just keyword matching.
 *
 * Example:
 * Query: "machine learning algorithms"
 * Will find documents about: "AI models", "neural networks", "deep learning"
 * Even if they don't contain the exact phrase "machine learning algorithms"
 *
 * How it works:
 * 1. Convert query to vector embedding
 * 2. Calculate cosine similarity with all document chunks
 * 3. Return top N most similar chunks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for semantic search across documents")
public class SemanticSearchRequest {

    @NotBlank(message = "Query is required")
    @Schema(
            description = "Search query - will find documents with similar meaning",
            example = "How do neural networks learn?",
            required = true
    )
    private String query;

    @Min(value = 1, message = "Top K must be at least 1")
    @Max(value = 100, message = "Top K must not exceed 100")
    @Schema(
            description = "Number of top results to return",
            example = "5",
            defaultValue = "5"
    )
    private Integer topK;

    @Min(value = 0, message = "Similarity threshold must be between 0 and 1")
    @Max(value = 1, message = "Similarity threshold must be between 0 and 1")
    @Schema(
            description = "Minimum similarity threshold (0.0 to 1.0) - higher means more similar",
            example = "0.7",
            defaultValue = "0.0"
    )
    private Double similarityThreshold;

    @Schema(
            description = "Filter by document category",
            example = "AI/ML"
    )
    private String category;
}
