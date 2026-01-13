package com.ai.mvp.dto.rag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Citation DTO for RAG responses
 *
 * Phase 4: RAG Implementation
 * Represents a source document/chunk used to generate the answer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Citation reference for answer source")
public class Citation {

    @Schema(description = "Document ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String documentId;

    @Schema(description = "Document title", example = "Introduction to Neural Networks")
    private String documentTitle;

    @Schema(description = "Chunk index within document", example = "2")
    private Integer chunkIndex;

    @Schema(description = "Similarity score (0.0 to 1.0) - how relevant this source was", example = "0.89")
    private Double similarityScore;

    @Schema(description = "Excerpt from the chunk", example = "Neural networks learn through backpropagation...")
    private String excerpt;

    @Schema(description = "Document category", example = "AI/ML")
    private String category;
}
