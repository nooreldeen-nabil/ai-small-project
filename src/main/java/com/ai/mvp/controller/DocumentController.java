package com.ai.mvp.controller;

import com.ai.mvp.dto.vector.DocumentUploadRequest;
import com.ai.mvp.dto.vector.DocumentUploadResponse;
import com.ai.mvp.dto.vector.SemanticSearchRequest;
import com.ai.mvp.dto.vector.SemanticSearchResponse;
import com.ai.mvp.entity.Document;
import com.ai.mvp.service.DocumentService;
import com.ai.mvp.service.VectorSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Vector Database operations
 *
 * Phase 3: Vector Database & Embeddings
 *
 * This controller provides endpoints for:
 * 1. Document upload and embedding generation
 * 2. Semantic search across documents
 * 3. Document management (get, delete)
 *
 * Learning Points:
 * - How to work with vector embeddings
 * - Semantic search vs keyword search
 * - Document chunking strategies
 * - Oracle 23c AI Vector Search capabilities
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Phase 3: Vector Database", description = "Document storage and semantic search with embeddings")
public class DocumentController {

    private final DocumentService documentService;
    private final VectorSearchService vectorSearchService;

    /**
     * POST /api/documents/upload - Upload and process a document
     *
     * This endpoint:
     * 1. Receives document text
     * 2. Splits it into chunks
     * 3. Generates vector embeddings for each chunk
     * 4. Stores in Oracle 23c with VECTOR columns
     */
    @PostMapping("/upload")
    @Operation(
            summary = "Upload document for vector search",
            description = "Upload a document which will be chunked, embedded, and stored for semantic search"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Document successfully uploaded and processed",
                    content = @Content(schema = @Schema(implementation = DocumentUploadResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            )
    })
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @Valid @RequestBody DocumentUploadRequest request) {
        log.info("Uploading document: {}", request.getTitle());
        DocumentUploadResponse response = documentService.uploadDocument(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/documents/search/semantic - Semantic search across documents
     *
     * This endpoint performs similarity search:
     * 1. Converts query to vector embedding
     * 2. Calculates cosine similarity with all chunks
     * 3. Returns top N most similar chunks
     */
    @PostMapping("/search/semantic")
    @Operation(
            summary = "Semantic search",
            description = "Find documents by meaning, not just keywords. Uses vector similarity."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results with similarity scores",
                    content = @Content(schema = @Schema(implementation = SemanticSearchResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            )
    })
    public ResponseEntity<SemanticSearchResponse> semanticSearch(
            @Valid @RequestBody SemanticSearchRequest request) {
        log.info("Semantic search: {}", request.getQuery());
        SemanticSearchResponse response = vectorSearchService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/documents/{id} - Get document by ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get document by ID",
            description = "Retrieve document details and metadata"
    )
    public ResponseEntity<Document> getDocument(@PathVariable String id) {
        log.info("Fetching document: {}", id);
        Document document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }

    /**
     * GET /api/documents - List all documents
     */
    @GetMapping
    @Operation(
            summary = "List all documents",
            description = "Get all documents in the system"
    )
    public ResponseEntity<List<Document>> getAllDocuments() {
        log.info("Listing all documents");
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * DELETE /api/documents/{id} - Delete document
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete document",
            description = "Delete document and all its chunks"
    )
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        log.info("Deleting document: {}", id);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/documents/health - Health check
     */
    @GetMapping("/health")
    @Operation(
            summary = "Vector service health check",
            description = "Verify vector database service is ready"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vector database service is ready! 🚀");
    }
}
