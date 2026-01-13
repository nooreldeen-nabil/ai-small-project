package com.ai.mvp.controller;

import com.ai.mvp.dto.rag.DocumentQARequest;
import com.ai.mvp.dto.rag.DocumentQAResponse;
import com.ai.mvp.service.RagService;
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

/**
 * REST Controller for RAG (Retrieval Augmented Generation)
 *
 * Phase 4: RAG Implementation
 *
 * This controller enables intelligent document Q&A by combining:
 * 1. Vector search (Phase 3) - Find relevant document chunks
 * 2. LLM generation (Phase 1) - Generate answer based on context
 *
 * RAG allows the LLM to answer questions about YOUR specific documents,
 * going beyond its pre-trained knowledge.
 *
 * Learning Points:
 * - How retrieval augmented generation works
 * - How to provide context to LLMs
 * - How to track citations and sources
 * - How to manage context windows
 */
@Slf4j
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
@Tag(name = "Phase 4: RAG", description = "Intelligent document Q&A using retrieval augmented generation")
public class RagController {

    private final RagService ragService;

    /**
     * POST /api/qa/document - Ask questions about uploaded documents
     *
     * This is the main RAG endpoint for Phase 4.
     *
     * How it works:
     * 1. Searches vector database for relevant document chunks (semantic search)
     * 2. Assembles context from top matching chunks
     * 3. Sends context + question to LLM
     * 4. Returns answer with source citations
     *
     * Example Request:
     * {
     *   "question": "How do neural networks learn?",
     *   "topK": 5,
     *   "similarityThreshold": 0.5,
     *   "category": "AI/ML",
     *   "maxTokens": 2000,
     *   "temperature": 0.3
     * }
     *
     * Example Response:
     * {
     *   "question": "How do neural networks learn?",
     *   "answer": "Neural networks learn through backpropagation...",
     *   "citations": [
     *     {
     *       "documentTitle": "Intro to Neural Networks",
     *       "chunkIndex": 2,
     *       "similarityScore": 0.89,
     *       "excerpt": "Neural networks learn through..."
     *     }
     *   ],
     *   "confidence": "HIGH",
     *   "tokensUsed": 450
     * }
     *
     * @param request Question and search parameters
     * @return Answer with source citations
     */
    @PostMapping("/document")
    @Operation(
            summary = "Ask questions about documents (RAG)",
            description = "Uses retrieval augmented generation to answer questions based on uploaded documents. " +
                    "Retrieves relevant chunks from vector database and uses them as context for LLM generation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Question answered successfully",
                    content = @Content(schema = @Schema(implementation = DocumentQAResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request (e.g., empty question, invalid parameters)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error (e.g., LLM API error, database error)"
            )
    })
    public ResponseEntity<DocumentQAResponse> askQuestion(
            @Valid @RequestBody DocumentQARequest request) {

        log.info("Received RAG question: '{}'", request.getQuestion());

        DocumentQAResponse response = ragService.answerQuestion(request);

        log.info("RAG question answered successfully - Confidence: {}, Citations: {}",
                response.getConfidence(), response.getCitations().size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/qa/health - Health check
     *
     * @return Simple health status
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if the RAG service is available"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RAG service is healthy");
    }
}
