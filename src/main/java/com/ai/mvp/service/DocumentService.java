package com.ai.mvp.service;

import com.ai.mvp.dto.vector.DocumentUploadRequest;
import com.ai.mvp.dto.vector.DocumentUploadResponse;
import com.ai.mvp.entity.Document;
import com.ai.mvp.entity.DocumentChunk;
import com.ai.mvp.repository.DocumentChunkRepository;
import com.ai.mvp.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling document upload, chunking, and storage
 *
 * Phase 3: Vector Database & Embeddings
 *
 * What is Document Chunking?
 * ===========================
 * Chunking is the process of splitting large documents into smaller pieces (chunks).
 *
 * Why chunk documents?
 * ===================
 * 1. Embedding Model Limits: Models have max input length (e.g., 2048 tokens)
 * 2. Search Precision: Smaller chunks provide more accurate search results
 * 3. Context Matching: Better semantic matching for specific queries
 * 4. Performance: Faster vector similarity calculations
 *
 * Chunking Strategy:
 * ==================
 * - Default chunk size: 1000 characters (~200-250 words)
 * - Default overlap: 200 characters (~40-50 words)
 * - Overlap maintains context between chunks
 *
 * Example:
 * Chunk 1: [0-1000]
 * Chunk 2: [800-1800]  (200 char overlap with Chunk 1)
 * Chunk 3: [1600-2600] (200 char overlap with Chunk 2)
 *
 * This ensures sentences that span chunk boundaries aren't lost.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    // Default chunking parameters
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_CHUNK_OVERLAP = 200;

    /**
     * Upload and process a document for vector search
     *
     * This method:
     * 1. Creates a Document entity
     * 2. Splits content into chunks with overlap
     * 3. Generates embeddings for each chunk
     * 4. Stores document and chunks in database
     *
     * @param request Document upload request with title, content, and chunking parameters
     * @return Response with document ID and processing details
     */
    @Transactional
    public DocumentUploadResponse uploadDocument(DocumentUploadRequest request) {
        log.info("Processing document upload - Title: '{}', Content length: {} chars",
                request.getTitle(), request.getContent().length());

        try {
            // Get chunking parameters (use defaults if not provided)
            int chunkSize = request.getChunkSize() != null ? request.getChunkSize() : DEFAULT_CHUNK_SIZE;
            int chunkOverlap = request.getChunkOverlap() != null ? request.getChunkOverlap() : DEFAULT_CHUNK_OVERLAP;

            // Validate parameters
            if (chunkSize < 100) {
                throw new IllegalArgumentException("Chunk size must be at least 100 characters");
            }
            if (chunkOverlap >= chunkSize) {
                throw new IllegalArgumentException("Chunk overlap must be less than chunk size");
            }

            // Create Document entity
            Document document = Document.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .category(request.getCategory() != null ? request.getCategory() : "general")
                    .chunkCount(0) // Will be updated after chunking
                    .build();

            // Split content into chunks
            List<String> chunks = splitIntoChunks(request.getContent(), chunkSize, chunkOverlap);
            log.info("Document split into {} chunks (size: {}, overlap: {})",
                    chunks.size(), chunkSize, chunkOverlap);

            // Generate embeddings and create DocumentChunk entities
            List<DocumentChunk> documentChunks = new ArrayList<>();
            int position = 0;

            for (int i = 0; i < chunks.size(); i++) {
                String chunkContent = chunks.get(i);

                log.debug("Generating embedding for chunk {}/{}", i + 1, chunks.size());

                // Generate embedding for this chunk
                float[] embedding = embeddingService.generateEmbedding(chunkContent);
                String embeddingString = embeddingService.embeddingToString(embedding);

                // Create DocumentChunk entity
                DocumentChunk chunk = DocumentChunk.builder()
                        .chunkIndex(i)
                        .content(chunkContent)
                        .embedding(embeddingString)
                        .startPosition(position)
                        .endPosition(position + chunkContent.length())
                        .build();

                // Add chunk to document
                document.addChunk(chunk);
                documentChunks.add(chunk);

                // Update position (accounting for overlap)
                position += (chunkSize - chunkOverlap);
            }

            // Update chunk count
            document.setChunkCount(chunks.size());

            // Save document (cascades to chunks)
            Document savedDocument = documentRepository.save(document);
            log.info("✅ Document saved successfully - ID: {}, Chunks: {}",
                    savedDocument.getId(), savedDocument.getChunkCount());

            // Build response
            return DocumentUploadResponse.builder()
                    .documentId(savedDocument.getId())
                    .title(savedDocument.getTitle())
                    .chunksCreated(savedDocument.getChunkCount())
                    .vectorDimension(embeddingService.getEmbeddingDimensions())
                    .status("SUCCESS")
                    .message("Document successfully processed and stored")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing document upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    /**
     * Get a document by ID
     *
     * @param id Document ID
     * @return Document entity
     * @throws RuntimeException if document not found
     */
    public Document getDocument(String id) {
        log.debug("Fetching document with ID: {}", id);

        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
    }

    /**
     * Delete a document by ID
     *
     * This will cascade delete all associated chunks due to CascadeType.ALL
     *
     * @param id Document ID
     * @throws RuntimeException if document not found
     */
    @Transactional
    public void deleteDocument(String id) {
        log.info("Deleting document with ID: {}", id);

        // Verify document exists
        Document document = getDocument(id);

        // Delete document (cascades to chunks)
        documentRepository.delete(document);

        log.info("✅ Document deleted successfully - ID: {}, Title: '{}'",
                id, document.getTitle());
    }

    /**
     * Get all documents
     *
     * @return List of all documents
     */
    public List<Document> getAllDocuments() {
        log.debug("Fetching all documents");
        return documentRepository.findAll();
    }

    /**
     * Get documents by category
     *
     * @param category Category name
     * @return List of documents in that category
     */
    public List<Document> getDocumentsByCategory(String category) {
        log.debug("Fetching documents in category: {}", category);
        return documentRepository.findByCategory(category);
    }

    /**
     * Get chunks for a specific document
     *
     * @param documentId Document ID
     * @return List of document chunks ordered by chunk index
     */
    public List<DocumentChunk> getDocumentChunks(String documentId) {
        log.debug("Fetching chunks for document ID: {}", documentId);
        return documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);
    }

    /**
     * Split text into chunks with overlap
     *
     * This method implements sliding window chunking:
     * - Each chunk is 'chunkSize' characters
     * - Adjacent chunks overlap by 'chunkOverlap' characters
     * - Last chunk may be smaller if remaining text < chunkSize
     *
     * Example (chunkSize=10, overlap=3):
     * Text: "Hello world, how are you?"
     * Chunk 1: "Hello worl" [0-10]
     * Chunk 2: "orld, how " [7-17] (3 char overlap with Chunk 1)
     * Chunk 3: "w are you?" [14-24] (3 char overlap with Chunk 2)
     *
     * @param text The text to split
     * @param chunkSize Size of each chunk in characters
     * @param chunkOverlap Number of overlapping characters between chunks
     * @return List of text chunks
     */
    private List<String> splitIntoChunks(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();

        // Handle empty or short text
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        if (text.length() <= chunkSize) {
            // Text fits in one chunk
            chunks.add(text);
            return chunks;
        }

        // Calculate step size (how far to move forward after each chunk)
        int step = chunkSize - chunkOverlap;
        if (step <= 0) {
            throw new IllegalArgumentException("Chunk overlap must be less than chunk size");
        }

        // Create chunks using sliding window
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end);

            // Only add non-empty chunks
            if (!chunk.trim().isEmpty()) {
                chunks.add(chunk);
            }

            // Move to next chunk position
            start += step;

            // If remaining text is smaller than overlap, we're done
            if (end == text.length()) {
                break;
            }
        }

        log.debug("Split text of {} chars into {} chunks", text.length(), chunks.size());
        return chunks;
    }
}
