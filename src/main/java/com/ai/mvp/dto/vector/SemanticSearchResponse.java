package com.ai.mvp.dto.vector;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for semantic search results
 *
 * Phase 3: Vector Database & Embeddings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Semantic search results with similarity scores")
public class SemanticSearchResponse {

    @Schema(description = "Search query", example = "How do neural networks learn?")
    private String query;

    @Schema(description = "List of matching document chunks")
    private List<SearchResult> results;

    @Schema(description = "Total results found", example = "5")
    private Integer totalResults;

    @Schema(description = "Search timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Individual search result
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Single search result")
    public static class SearchResult {

        @Schema(description = "Document ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String documentId;

        @Schema(description = "Document title", example = "Introduction to LLMs")
        private String documentTitle;

        @Schema(description = "Chunk ID", example = "1")
        private Integer chunkIndex;

        @Schema(description = "Chunk text content", example = "Neural networks learn through backpropagation...")
        private String content;

        @Schema(description = "Similarity score (0.0 to 1.0) - higher is more similar", example = "0.89")
        private Double similarityScore;

        @Schema(description = "Document category", example = "AI/ML")
        private String category;
    }
}
