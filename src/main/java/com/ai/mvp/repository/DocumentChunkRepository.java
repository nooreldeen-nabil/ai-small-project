package com.ai.mvp.repository;

import com.ai.mvp.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DocumentChunk entity with vector search capabilities
 *
 * Phase 3: Vector Database & Embeddings
 *
 * This repository provides semantic search using Oracle 23c's native VECTOR functions:
 * - VECTOR_DISTANCE(): Calculate similarity between vectors
 * - COSINE distance metric: Measures angle between vectors (0 = identical, 2 = opposite)
 *
 * Smaller distance = more similar
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, String> {

    /**
     * Find chunks by document ID
     */
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(String documentId);

    /**
     * Semantic search using vector similarity
     *
     * This query:
     * 1. Calculates COSINE distance between query vector and all chunk embeddings
     * 2. Orders by distance (smallest = most similar)
     * 3. Returns top N results
     *
     * Oracle VECTOR_DISTANCE syntax:
     * VECTOR_DISTANCE(vector1, vector2, COSINE)
     *
     * Note: We convert the embedding string to VECTOR type using TO_VECTOR()
     */
    @Query(value = """
            SELECT c.*,
                   VECTOR_DISTANCE(TO_VECTOR(c.embedding), TO_VECTOR(:queryEmbedding), COSINE) as distance
            FROM document_chunks c
            JOIN documents d ON c.document_id = d.id
            WHERE (:category IS NULL OR d.category = :category)
            ORDER BY distance
            FETCH FIRST :topK ROWS ONLY
            """, nativeQuery = true)
    List<Object[]> findSimilarChunks(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topK") int topK,
            @Param("category") String category
    );

    /**
     * Count chunks for a document
     */
    long countByDocumentId(String documentId);

    /**
     * Delete all chunks for a document
     */
    void deleteByDocumentId(String documentId);
}
