package com.ai.mvp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a document chunk with vector embedding
 *
 * Phase 3: Vector Database & Embeddings
 *
 * This entity stores:
 * - A piece of text (chunk) from a larger document
 * - Its vector embedding (768-dimensional array)
 * - Metadata for tracking and search
 *
 * The embedding column is stored as Oracle VECTOR type for:
 * - Efficient storage
 * - Fast similarity search using vector indexes (HNSW)
 * - Native distance calculations (COSINE, EUCLIDEAN, DOT_PRODUCT)
 */
@Entity
@Table(name = "document_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "content", columnDefinition = "CLOB", nullable = false)
    private String content;

    /**
     * Vector embedding stored as Oracle VECTOR type
     * Oracle 23c supports native VECTOR columns with dimensions
     *
     * Format: VECTOR(768, FLOAT32)
     * - 768 dimensions (Gemini embedding model)
     * - FLOAT32 precision
     */
    @Column(name = "embedding", columnDefinition = "VECTOR(768, FLOAT32)")
    private String embedding; // Stored as JSON string, e.g., "[0.1, 0.2, ...]"

    @Column(name = "start_position")
    private Integer startPosition;

    @Column(name = "end_position")
    private Integer endPosition;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
