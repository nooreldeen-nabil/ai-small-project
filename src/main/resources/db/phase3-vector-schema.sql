/*
 * ============================================================================
 * ORACLE 23C VECTOR DATABASE SCHEMA - PHASE 3
 * ============================================================================
 *
 * This schema implements native vector capabilities in Oracle Database 23c
 * to support semantic search and similarity operations on document chunks.
 *
 * KEY FEATURES:
 * - Native VECTOR data type (768 dimensions, FLOAT32 precision)
 * - HNSW (Hierarchical Navigable Small World) indexing for fast similarity search
 * - Support for cosine distance metric (standard for embeddings)
 * - Foreign key relationships for data integrity
 * - Comprehensive audit columns (created_at, updated_at)
 *
 * PRODUCTION NOTES:
 * - Ensure VECTOR support is enabled in Oracle 23c+
 * - Vector indexes require sufficient memory for nearest neighbor searches
 * - Cosine distance is ideal for normalized embeddings (common in ML/AI)
 * - HNSW index provides O(log n) approximate nearest neighbor search
 * ============================================================================
 */

-- ============================================================================
-- STEP 1: DROP EXISTING TABLES (if they exist)
-- ============================================================================
-- Important: Drop dependent tables first due to foreign key constraints
-- The correct order is: document_chunks FIRST, then documents

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE document_chunks';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE documents';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

-- ============================================================================
-- STEP 2: CREATE DOCUMENTS TABLE
-- ============================================================================
--
-- DOCUMENTS table serves as the parent table containing metadata about
-- the source documents being chunked and vectorized.
--
-- COLUMNS:
-- - id: Unique identifier (UUID format as VARCHAR2(36))
-- - title: Document title for human readability
-- - content: Full document content stored as CLOB (Character Large Object)
-- - category: Document classification/topic for filtering
-- - chunk_count: Denormalized count of chunks for quick statistics
-- - created_at: Audit column tracking when document was added
-- - updated_at: Audit column tracking last modification
--
-- INDEXES: Primary key automatically indexed for fast lookups

CREATE TABLE documents (
  id VARCHAR2(36) PRIMARY KEY,
  title VARCHAR2(255) NOT NULL,
  content CLOB,
  category VARCHAR2(100),
  chunk_count NUMBER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create index on category for efficient filtering
CREATE INDEX idx_documents_category ON documents(category);

-- Create index on created_at for time-based queries
CREATE INDEX idx_documents_created_at ON documents(created_at);

COMMENT ON TABLE documents IS 'Parent table storing source documents for vector processing';
COMMENT ON COLUMN documents.id IS 'Unique identifier (UUID v4)';
COMMENT ON COLUMN documents.title IS 'Document title for human readability';
COMMENT ON COLUMN documents.content IS 'Full document content (CLOB)';
COMMENT ON COLUMN documents.category IS 'Document category/topic for filtering and organization';
COMMENT ON COLUMN documents.chunk_count IS 'Denormalized count of chunks for quick statistics';
COMMENT ON COLUMN documents.created_at IS 'Timestamp when document was created';
COMMENT ON COLUMN documents.updated_at IS 'Timestamp when document was last updated';


-- ============================================================================
-- STEP 3: CREATE DOCUMENT_CHUNKS TABLE
-- ============================================================================
--
-- DOCUMENT_CHUNKS table stores segmented portions of documents along with
-- their vectorized embeddings. Each chunk represents a semantic unit of text.
--
-- VECTOR DATA TYPE EXPLAINED:
-- Oracle 23c introduces native VECTOR support for storing embeddings directly
-- in the database without requiring separate vector stores. This enables:
-- 1. Transactional consistency (vector + metadata in same ACID transaction)
-- 2. Complex queries combining vector similarity + traditional SQL predicates
-- 3. Native vector indexing without middleware
--
-- VECTOR(768, FLOAT32):
-- - 768: Dimensionality (matches Google Gemini embeddings: 768 dimensions)
-- - FLOAT32: Precision type (industry standard for ML embeddings, 4 bytes/value)
-- - Total storage: ~3 KB per embedding (768 * 4 bytes)
--
-- COLUMNS:
-- - id: Unique chunk identifier (UUID format)
-- - document_id: Foreign key reference to parent document
-- - chunk_index: Sequential position within the document (0-based)
-- - content: Text content of this chunk (CLOB allows flexible sizes)
-- - embedding: Dense vector representation (768-dimensional, FLOAT32)
-- - start_position: Character offset in original document
-- - end_position: Character offset in original document
-- - created_at: Audit column for tracking chunk creation
--
-- CONSTRAINTS:
-- - Foreign key ensures referential integrity with documents table
-- - NOT NULL constraints ensure data quality
--
-- INDEXES:
-- - Primary key on id for direct chunk lookup
-- - Foreign key index on document_id for relationship queries
-- - VECTOR INDEX on embedding for similarity search (created separately)

CREATE TABLE document_chunks (
  id VARCHAR2(36) PRIMARY KEY,
  document_id VARCHAR2(36) NOT NULL,
  chunk_index NUMBER NOT NULL,
  content CLOB NOT NULL,
  embedding VECTOR(768, FLOAT32),
  start_position NUMBER,
  end_position NUMBER,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_document_chunks_document_id
    FOREIGN KEY (document_id)
    REFERENCES documents(id)
    ON DELETE CASCADE
);

-- Create index on document_id for efficient lookups of chunks by document
CREATE INDEX idx_document_chunks_document_id ON document_chunks(document_id);

-- Create index on chunk_index for ordering queries
CREATE INDEX idx_document_chunks_index ON document_chunks(document_index);

-- Create composite index for document + chunk_index lookups
CREATE INDEX idx_document_chunks_doc_idx ON document_chunks(document_id, chunk_index);

COMMENT ON TABLE document_chunks IS 'Document chunks with vector embeddings for semantic search';
COMMENT ON COLUMN document_chunks.id IS 'Unique chunk identifier (UUID v4)';
COMMENT ON COLUMN document_chunks.document_id IS 'Foreign key to parent document';
COMMENT ON COLUMN document_chunks.chunk_index IS 'Zero-based sequential position in document';
COMMENT ON COLUMN document_chunks.content IS 'Text content of the chunk (CLOB)';
COMMENT ON COLUMN document_chunks.embedding IS 'Dense vector (768-dim, FLOAT32) from embedding model';
COMMENT ON COLUMN document_chunks.start_position IS 'Character position in original document where chunk starts';
COMMENT ON COLUMN document_chunks.end_position IS 'Character position in original document where chunk ends';
COMMENT ON COLUMN document_chunks.created_at IS 'Timestamp when chunk was created';


-- ============================================================================
-- STEP 4: CREATE VECTOR INDEX (HNSW)
-- ============================================================================
--
-- HNSW (Hierarchical Navigable Small World) ALGORITHM EXPLAINED:
--
-- HNSW is a graph-based approximate nearest neighbor (ANN) algorithm that:
--
-- 1. HIERARCHICAL STRUCTURE:
--    - Organizes points in a multi-layer graph (like a skip list)
--    - Bottom layer: all points connected (high recall, slower)
--    - Upper layers: progressively fewer points (fast navigation)
--    - Search starts at top layer, descends through layers
--    - Result: O(log n) complexity instead of O(n) for exact search
--
-- 2. NAVIGABLE SMALL WORLD:
--    - Each node connects to local neighbors only (limited connections)
--    - Navigation through local shortcuts to reach distant points
--    - Maintains balance between search efficiency and memory usage
--
-- 3. ADVANTAGES:
--    - Fast approximate search (100-1000x faster than exact)
--    - Reasonable memory overhead (~4KB per point at 768 dims)
--    - Suitable for real-time applications
--    - Works well with cosine distance (normalized embeddings)
--
-- 4. TRADE-OFFS:
--    - Approximate results (not guaranteed exact nearest neighbors)
--    - Tuning required (M, ef_construction parameters)
--    - Index build time depends on dataset size
--
-- COSINE DISTANCE METRIC EXPLAINED:
--
-- Why Cosine Distance for embeddings?
-- - Cosine measures angle between vectors (range: 0 to 2)
--   - 0 = identical direction (perfect match)
--   - 1 = orthogonal (no similarity)
--   - 2 = opposite direction
--
-- - Ideal for normalized embeddings because:
--   - Focuses on direction/orientation (not magnitude)
--   - Text embeddings are typically normalized by ML models
--   - Language embeddings naturally cluster by semantic meaning
--   - Robust to document length variations
--
-- - Alternative metrics (for reference):
--   - EUCLIDEAN: Sensitive to magnitude, slower
--   - MANHATTAN: Less intuitive for high-dimensional spaces
--   - HAMMING: Only for binary vectors
--
-- INDEX PARAMETERS:
-- - type=HNSW: Algorithm selection
-- - distance=COSINE: Similarity metric
-- - M=8: Max connections per layer (balance: memory vs quality)
-- - EF_CONSTRUCTION=150: Search width during index building
-- - neighbors=32: Candidates evaluated during insertion

CREATE VECTOR INDEX idx_document_chunks_embedding
  ON document_chunks(embedding)
  DISTANCE COSINE
  WITH (type=HNSW);

-- Alternative HNSW index with tuned parameters for very large datasets:
-- CREATE VECTOR INDEX idx_document_chunks_embedding
--   ON document_chunks(embedding)
--   DISTANCE COSINE
--   WITH (type=HNSW, M=16, EF_CONSTRUCTION=300);

COMMENT ON INDEX idx_document_chunks_embedding IS 'HNSW vector index for approximate nearest neighbor search using cosine distance';


-- ============================================================================
-- STEP 5: GRANT PERMISSIONS (if needed)
-- ============================================================================
-- Uncomment and modify based on your Oracle security requirements
--
-- GRANT SELECT, INSERT, UPDATE, DELETE ON documents TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON document_chunks TO app_user;


-- ============================================================================
-- EXAMPLE INSERT STATEMENTS (commented out)
-- ============================================================================
--
-- These examples show how to insert documents and chunks with embeddings.
-- Note: Embeddings should be generated by your embedding model (Google Gemini)
-- and converted to Oracle VECTOR type before insertion.
--
-- Vector insertion example:
--
-- DECLARE
--   v_doc_id VARCHAR2(36) := '550e8400-e29b-41d4-a716-446655440000';
--   v_chunk_id VARCHAR2(36) := '550e8400-e29b-41d4-a716-446655440001';
--   v_embedding VECTOR := '[0.123, -0.456, 0.789, ... 765 more values]'::VECTOR;
-- BEGIN
--   -- Insert document
--   INSERT INTO documents (id, title, content, category, chunk_count)
--   VALUES (
--     v_doc_id,
--     'Machine Learning Fundamentals',
--     'Complete document content...',
--     'AI/ML',
--     5
--   );
--
--   -- Insert chunk with embedding
--   INSERT INTO document_chunks (
--     id,
--     document_id,
--     chunk_index,
--     content,
--     embedding,
--     start_position,
--     end_position
--   ) VALUES (
--     v_chunk_id,
--     v_doc_id,
--     0,
--     'First chunk of the document containing semantic content...',
--     v_embedding,
--     0,
--     250
--   );
--
--   COMMIT;
-- END;
-- /


-- ============================================================================
-- EXAMPLE VECTOR SEARCH QUERIES (commented out)
-- ============================================================================
--
-- These examples demonstrate semantic search using vector similarity.
-- Replace the embedding value with actual embeddings from your embedding model.
--
--
-- EXAMPLE 1: Find most similar chunks (exact k-nearest neighbors)
-- ============================================================================
-- Finds the 5 most semantically similar chunks to a query embedding.
-- The VECTOR_DISTANCE function computes cosine distance.
-- Lower distance = higher similarity (0 is perfect match).
--
-- DECLARE
--   v_query_embedding VECTOR := '[0.1, -0.2, 0.3, ... 765 more values]'::VECTOR;
-- BEGIN
--   SELECT
--     dc.id,
--     dc.chunk_index,
--     dc.content,
--     ROUND(VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE), 4) AS similarity_score,
--     d.title,
--     d.category
--   FROM document_chunks dc
--   JOIN documents d ON dc.document_id = d.id
--   ORDER BY VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE)
--   FETCH FIRST 5 ROWS ONLY;
-- END;
-- /
--
--
-- EXAMPLE 2: Find similar chunks with metadata filtering
-- ============================================================================
-- Combines vector search with SQL predicates for fine-grained filtering.
-- This shows the power of hybrid search (vector + traditional SQL).
--
-- DECLARE
--   v_query_embedding VECTOR := '[0.1, -0.2, 0.3, ... 765 more values]'::VECTOR;
--   v_similarity_threshold NUMBER := 0.7; -- Higher score = less similar
-- BEGIN
--   SELECT
--     d.title,
--     d.category,
--     dc.chunk_index,
--     dc.content,
--     ROUND(VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE), 4) AS distance_score
--   FROM document_chunks dc
--   JOIN documents d ON dc.document_id = d.id
--   WHERE d.category = 'AI/ML'
--     AND VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE) < v_similarity_threshold
--   ORDER BY VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE) ASC
--   FETCH FIRST 10 ROWS ONLY;
-- END;
-- /
--
--
-- EXAMPLE 3: Batch similarity search using APPROX_MATCH
-- ============================================================================
-- Uses approximate nearest neighbor search for faster results on large datasets.
-- trade-off: Speed vs absolute accuracy (acceptable for most NLP applications).
--
-- DECLARE
--   v_query_embedding VECTOR := '[0.1, -0.2, 0.3, ... 765 more values]'::VECTOR;
-- BEGIN
--   SELECT
--     dc.id,
--     dc.document_id,
--     dc.chunk_index,
--     ROUND(VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE), 4) AS distance
--   FROM document_chunks dc
--   WHERE VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE) < 1.5
--   ORDER BY VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE) ASC
--   FETCH FIRST 20 ROWS ONLY;
-- END;
-- /
--
--
-- EXAMPLE 4: Find related documents (document-level similarity)
-- ============================================================================
-- Aggregates chunk similarities to find semantically related documents.
-- Shows how to combine vector operations with standard SQL aggregation.
--
-- DECLARE
--   v_query_embedding VECTOR := '[0.1, -0.2, 0.3, ... 765 more values]'::VECTOR;
-- BEGIN
--   SELECT
--     d.id,
--     d.title,
--     d.category,
--     COUNT(*) AS matching_chunks,
--     ROUND(AVG(VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE)), 4) AS avg_distance
--   FROM documents d
--   JOIN document_chunks dc ON d.id = dc.document_id
--   WHERE VECTOR_DISTANCE(dc.embedding, v_query_embedding, COSINE) < 1.2
--   GROUP BY d.id, d.title, d.category
--   ORDER BY avg_distance ASC
--   FETCH FIRST 5 ROWS ONLY;
-- END;
-- /
--
--
-- EXAMPLE 5: Semantic deduplication (find duplicate/similar chunks)
-- ============================================================================
-- Identifies semantically similar chunks across documents.
-- Useful for deduplication and content consolidation.
--
-- SELECT
--   dc1.id AS chunk_1_id,
--   dc1.document_id AS doc_1_id,
--   dc2.id AS chunk_2_id,
--   dc2.document_id AS doc_2_id,
--   ROUND(VECTOR_DISTANCE(dc1.embedding, dc2.embedding, COSINE), 4) AS similarity
-- FROM document_chunks dc1
-- JOIN document_chunks dc2
--   ON dc1.id < dc2.id  -- Avoid duplicate pairs
-- WHERE VECTOR_DISTANCE(dc1.embedding, dc2.embedding, COSINE) < 0.3
-- ORDER BY similarity ASC
-- FETCH FIRST 100 ROWS ONLY;
--


-- ============================================================================
-- STEP 6: PERFORMANCE MONITORING VIEWS (optional)
-- ============================================================================
--
-- These queries help monitor vector index performance and statistics:
--
-- View index statistics:
-- SELECT index_name, table_name, status, num_rows
-- FROM user_indexes
-- WHERE table_name IN ('DOCUMENTS', 'DOCUMENT_CHUNKS');
--
-- Check index space usage:
-- SELECT index_name, leaf_blocks, ROUND(leaf_blocks * 8 / 1024, 2) AS size_mb
-- FROM user_indexes
-- WHERE table_name = 'DOCUMENT_CHUNKS';
--
-- Monitor vector index:
-- SELECT * FROM user_vector_indexes
-- WHERE table_name = 'DOCUMENT_CHUNKS';


-- ============================================================================
-- STEP 7: SCHEMA COMPLETION
-- ============================================================================

COMMIT;

-- Display confirmation message
PROMPT
PROMPT ============================================================================
PROMPT Oracle 23c Vector Database Schema for Phase 3 - CREATED SUCCESSFULLY
PROMPT ============================================================================
PROMPT
PROMPT Tables created:
PROMPT   - documents: Source documents with metadata
PROMPT   - document_chunks: Document chunks with 768-dimensional embeddings
PROMPT
PROMPT Indexes created:
PROMPT   - idx_documents_category: For category-based filtering
PROMPT   - idx_documents_created_at: For time-based queries
PROMPT   - idx_document_chunks_document_id: For document relationship queries
PROMPT   - idx_document_chunks_doc_idx: For document + index composite queries
PROMPT   - idx_document_chunks_embedding: HNSW vector index for semantic search
PROMPT
PROMPT Vector Index Details:
PROMPT   - Algorithm: HNSW (Hierarchical Navigable Small World)
PROMPT   - Distance Metric: COSINE (ideal for normalized embeddings)
PROMPT   - Dimensionality: 768 (matches Google Gemini embeddings)
PROMPT   - Data Type: VECTOR(768, FLOAT32) - native Oracle 23c type
PROMPT
PROMPT Ready for Phase 3 Vector Database operations!
PROMPT ============================================================================
PROMPT
