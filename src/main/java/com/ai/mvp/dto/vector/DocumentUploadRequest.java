package com.ai.mvp.dto.vector;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for uploading documents for vector storage
 *
 * Phase 3: Vector Database & Embeddings
 *
 * This request allows users to upload text documents which will be:
 * 1. Chunked into smaller pieces (for better search granularity)
 * 2. Converted to vector embeddings
 * 3. Stored in Oracle 23c with vector search capabilities
 *
 * Use cases:
 * - Building a knowledge base
 * - Document Q&A systems
 * - Semantic search applications
 * - RAG (Retrieval Augmented Generation) systems
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to upload and process a document for vector search")
public class DocumentUploadRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    @Schema(
            description = "Document title",
            example = "Introduction to Large Language Models",
            required = true
    )
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    @Schema(
            description = "Document content (will be chunked and embedded)",
            example = "Large Language Models (LLMs) are AI models trained on vast amounts of text data...",
            required = true
    )
    private String content;

    @Schema(
            description = "Document category/type",
            example = "AI/ML",
            defaultValue = "general"
    )
    private String category;

    @Schema(
            description = "Chunk size for splitting document (in characters)",
            example = "500",
            defaultValue = "1000"
    )
    private Integer chunkSize;

    @Schema(
            description = "Overlap between chunks (in characters) - helps maintain context",
            example = "100",
            defaultValue = "200"
    )
    private Integer chunkOverlap;
}
