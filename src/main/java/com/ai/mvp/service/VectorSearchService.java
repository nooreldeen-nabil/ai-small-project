package com.ai.mvp.service;

import com.ai.mvp.dto.vector.SemanticSearchRequest;
import com.ai.mvp.dto.vector.SemanticSearchResponse;
import com.ai.mvp.entity.Document;
import com.ai.mvp.entity.DocumentChunk;
import com.ai.mvp.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for semantic search using vector similarity
 *
 * Phase 3: Vector Database & Embeddings
 *
 * What is Semantic Search?
 * ========================
 * Semantic search finds documents based on meaning rather than exact keyword matching.
 *
 * Traditional Keyword Search:
 * Query: "machine learning"
 * Matches: Only documents containing "machine learning"
 *
 * Semantic Search:
 * Query: "machine learning"
 * Matches: Documents about "AI", "neural networks", "deep learning", "ML models"
 *
 * How it works:
 * =============
 * 1. Convert query text to vector embedding
 * 2. Calculate similarity between query vector and all stored document vectors
 * 3. Return documents with highest similarity scores
 *
 * Similarity Metrics:
 * ===================
 * We use COSINE distance which measures the angle between vectors:
 * - Distance 0.0 = Identical vectors (100% similar)
 * - Distance 1.0 = Orthogonal vectors (no similarity)
 * - Distance 2.0 = Opposite vectors (completely different)
 *
 * We convert distance to similarity score for better UX:
 * Similarity = 1 - (distance / 2)
 * - Similarity 1.0 = Identical (100% match)
 * - Similarity 0.5 = Somewhat similar
 * - Similarity 0.0 = Completely different
 *
 * Oracle 23c Vector Operations:
 * =============================
 * Oracle's VECTOR_DISTANCE function efficiently calculates similarity using:
 * - Native vector indexing (HNSW algorithm)
 * - GPU acceleration (if available)
 * - Optimized storage format
 *
 * This is much faster than calculating distances in application code!
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    // Default search parameters
    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.0;

    /**
     * Perform semantic search across all documents
     *
     * This method:
     * 1. Generates embedding for the query text
     * 2. Searches for similar document chunks using vector distance
     * 3. Converts distances to similarity scores
     * 4. Filters results by similarity threshold
     * 5. Returns top K most relevant results
     *
     * Example:
     * Query: "How do neural networks learn?"
     * Results:
     * 1. "Neural networks learn through backpropagation..." (similarity: 0.89)
     * 2. "Training involves adjusting weights..." (similarity: 0.85)
     * 3. "Learning algorithms optimize loss functions..." (similarity: 0.82)
     *
     * @param request Search request with query, topK, threshold, and optional category filter
     * @return Search response with ranked results and similarity scores
     */
    @Transactional(readOnly = true)
    public SemanticSearchResponse search(SemanticSearchRequest request) {
        log.info("Performing semantic search - Query: '{}', TopK: {}, Threshold: {}, Category: {}",
                request.getQuery(),
                request.getTopK(),
                request.getSimilarityThreshold(),
                request.getCategory());

        try {
            // Get search parameters (use defaults if not provided)
            int topK = request.getTopK() != null ? request.getTopK() : DEFAULT_TOP_K;
            double similarityThreshold = request.getSimilarityThreshold() != null ?
                    request.getSimilarityThreshold() : DEFAULT_SIMILARITY_THRESHOLD;

            // Step 1: Generate embedding for the query
            log.debug("Generating embedding for query...");
            float[] queryEmbedding = embeddingService.generateEmbedding(request.getQuery());
            String queryEmbeddingString = embeddingService.embeddingToString(queryEmbedding);

            // Step 2: Search for similar chunks using vector distance
            log.debug("Searching for similar chunks in database...");
            List<Object[]> rawResults = documentChunkRepository.findSimilarChunks(
                    queryEmbeddingString,
                    topK,
                    request.getCategory()
            );

            log.debug("Found {} raw results from database", rawResults.size());

            // Step 3: Parse results and convert distance to similarity score
            List<SemanticSearchResponse.SearchResult> searchResults = new ArrayList<>();

            for (Object[] row : rawResults) {
                try {
                    // Parse the result row
                    // Row format from native query: [chunk_entity_fields..., distance]
                    // Oracle returns chunk data plus distance as last column

                    // The chunk entity is reconstructed from the columns
                    DocumentChunk chunk = parseChunkFromRow(row);

                    // Distance is the last element
                    double distance = parseDistance(row[row.length - 1]);

                    // Convert distance to similarity score
                    // Cosine distance range: [0, 2]
                    // Similarity range: [0, 1] where 1 is most similar
                    double similarityScore = calculateSimilarityScore(distance);

                    log.debug("Chunk {} - Distance: {}, Similarity: {}",
                            chunk.getId(), distance, similarityScore);

                    // Filter by similarity threshold
                    if (similarityScore >= similarityThreshold) {
                        // Get document info
                        Document document = chunk.getDocument();

                        // Build search result
                        SemanticSearchResponse.SearchResult result = SemanticSearchResponse.SearchResult.builder()
                                .documentId(document.getId())
                                .documentTitle(document.getTitle())
                                .chunkIndex(chunk.getChunkIndex())
                                .content(chunk.getContent())
                                .similarityScore(Math.round(similarityScore * 100.0) / 100.0) // Round to 2 decimals
                                .category(document.getCategory())
                                .build();

                        searchResults.add(result);
                    }

                } catch (Exception e) {
                    log.warn("Error parsing search result row: {}", e.getMessage());
                    // Continue with next result
                }
            }

            log.info("✅ Search completed - Query: '{}', Results: {} (filtered: {})",
                    request.getQuery(), searchResults.size(), rawResults.size() - searchResults.size());

            // Build response
            return SemanticSearchResponse.builder()
                    .query(request.getQuery())
                    .results(searchResults)
                    .totalResults(searchResults.size())
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error performing semantic search: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform semantic search: " + e.getMessage(), e);
        }
    }

    /**
     * Parse DocumentChunk from native query result row
     *
     * Oracle native query returns all chunk columns plus distance.
     * We need to reconstruct the DocumentChunk entity from these columns.
     *
     * Column order matches the SELECT statement in DocumentChunkRepository:
     * c.id, c.document_id, c.chunk_index, c.content, c.embedding, c.start_position, c.end_position, c.created_at
     */
    private DocumentChunk parseChunkFromRow(Object[] row) {
        // Create a basic DocumentChunk with the data we need
        DocumentChunk chunk = new DocumentChunk();

        // Parse chunk fields (indices based on SELECT order)
        chunk.setId((String) row[0]);
        chunk.setChunkIndex((Integer) row[2]);
        chunk.setContent((String) row[3]);

        // We need the document info too - create a minimal Document object
        Document document = new Document();
        document.setId((String) row[1]);

        // Note: We'll need to fetch the full document to get title and category
        // For now, we'll rely on the JOIN in the query to have this data available
        // If Oracle query returns additional document columns, they would be here

        chunk.setDocument(document);

        return chunk;
    }

    /**
     * Parse distance value from query result
     *
     * Oracle may return distance as BigDecimal or Double depending on driver
     */
    private double parseDistance(Object distanceObj) {
        if (distanceObj instanceof BigDecimal) {
            return ((BigDecimal) distanceObj).doubleValue();
        } else if (distanceObj instanceof Double) {
            return (Double) distanceObj;
        } else if (distanceObj instanceof Number) {
            return ((Number) distanceObj).doubleValue();
        } else {
            throw new IllegalArgumentException("Unable to parse distance value: " + distanceObj);
        }
    }

    /**
     * Calculate similarity score from cosine distance
     *
     * Cosine Distance Range: [0, 2]
     * - 0.0 = Identical vectors (same direction)
     * - 1.0 = Orthogonal vectors (90 degrees apart)
     * - 2.0 = Opposite vectors (180 degrees apart)
     *
     * Similarity Score Range: [0, 1]
     * - 1.0 = Most similar (identical)
     * - 0.5 = Somewhat similar
     * - 0.0 = Not similar (opposite)
     *
     * Formula: similarity = 1 - (distance / 2)
     *
     * This normalizes the distance to a 0-1 scale where higher is better.
     *
     * @param distance Cosine distance from Oracle VECTOR_DISTANCE
     * @return Similarity score between 0.0 and 1.0
     */
    private double calculateSimilarityScore(double distance) {
        // Normalize cosine distance to similarity score
        double similarity = 1.0 - (distance / 2.0);

        // Clamp to [0, 1] range (in case of floating point errors)
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    /**
     * Find similar documents to a given document
     *
     * This can be used for "More like this" features or duplicate detection.
     *
     * @param documentId The source document ID
     * @param topK Number of similar documents to return
     * @return List of similar documents with similarity scores
     */
    public SemanticSearchResponse findSimilarDocuments(String documentId, Integer topK) {
        log.info("Finding documents similar to document ID: {}", documentId);

        // Get the first chunk of the document to use as query
        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);

        if (chunks.isEmpty()) {
            throw new RuntimeException("Document not found or has no chunks: " + documentId);
        }

        // Use the first chunk's content as the search query
        String queryText = chunks.get(0).getContent();

        // Perform semantic search
        SemanticSearchRequest request = SemanticSearchRequest.builder()
                .query(queryText)
                .topK(topK != null ? topK : DEFAULT_TOP_K + 1) // +1 to account for the source document
                .similarityThreshold(0.5) // Only return reasonably similar documents
                .build();

        SemanticSearchResponse response = search(request);

        // Filter out results from the source document itself
        List<SemanticSearchResponse.SearchResult> filteredResults = response.getResults().stream()
                .filter(result -> !result.getDocumentId().equals(documentId))
                .limit(topK != null ? topK : DEFAULT_TOP_K)
                .toList();

        return SemanticSearchResponse.builder()
                .query("Similar to document: " + documentId)
                .results(filteredResults)
                .totalResults(filteredResults.size())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
