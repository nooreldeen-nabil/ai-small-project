package com.ai.mvp.service;

import com.ai.mvp.dto.ChatRequest;
import com.ai.mvp.dto.ChatResponse;
import com.ai.mvp.dto.rag.Citation;
import com.ai.mvp.dto.rag.DocumentQARequest;
import com.ai.mvp.dto.rag.DocumentQAResponse;
import com.ai.mvp.dto.vector.SemanticSearchRequest;
import com.ai.mvp.dto.vector.SemanticSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval Augmented Generation) Service
 *
 * Phase 4: RAG Implementation
 *
 * This service implements the RAG pattern:
 * 1. Retrieval: Search vector database for relevant document chunks
 * 2. Augmentation: Assemble context from retrieved chunks
 * 3. Generation: Use LLM to generate answer based on context
 *
 * RAG allows LLMs to answer questions about your specific documents,
 * going beyond their pre-trained knowledge.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorSearchService vectorSearchService;
    private final ChatService chatService;

    @Value("${llm.provider:GEMINI}")
    private String providerName;

    /**
     * Answer a question about documents using RAG
     *
     * Pipeline:
     * 1. Search vector DB for relevant chunks (semantic search)
     * 2. Check if any relevant chunks found
     * 3. Assemble context from top chunks (with token management)
     * 4. Build RAG prompt with context and question
     * 5. Get answer from LLM
     * 6. Build response with citations and confidence
     *
     * @param request Question and search parameters
     * @return Answer with source citations
     */
    public DocumentQAResponse answerQuestion(DocumentQARequest request) {
        log.info("Processing RAG question: '{}' (topK={}, threshold={})",
                request.getQuestion(), request.getTopK(), request.getSimilarityThreshold());

        // Step 1: Search for relevant document chunks
        SemanticSearchRequest searchRequest = SemanticSearchRequest.builder()
                .query(request.getQuestion())
                .topK(request.getTopK())
                .similarityThreshold(request.getSimilarityThreshold())
                .category(request.getCategory())
                .build();

        SemanticSearchResponse searchResults = vectorSearchService.search(searchRequest);

        log.info("Vector search found {} relevant chunks", searchResults.getTotalResults());

        // Step 2: Check if we found any relevant documents
        if (searchResults.getResults().isEmpty()) {
            return handleNoResults(request);
        }

        // Step 3: Assemble context from retrieved chunks (with token management)
        String context = assembleContext(searchResults.getResults(), request.getMaxTokens());

        // Step 4: Build RAG prompt
        String ragPrompt = buildRagPrompt(request.getQuestion(), context);

        // Step 5: Get answer from LLM
        ChatRequest chatRequest = ChatRequest.builder()
                .message(ragPrompt)
                .maxTokens(request.getMaxTokens())
                .temperature(request.getTemperature())
                .build();

        ChatResponse llmResponse = chatService.chat(chatRequest);

        log.info("LLM generated answer: {} tokens used", llmResponse.getTokensUsed());

        // Step 6: Build response with citations
        return buildResponse(request, llmResponse, searchResults);
    }

    /**
     * Assemble context from search results
     *
     * Combines multiple document chunks into a single context string.
     * Manages token limits to ensure context fits in LLM's context window.
     *
     * Token estimation: ~4 characters = 1 token (rough estimate)
     * We reserve tokens for: question + answer + prompt structure
     *
     * @param results Search results with document chunks
     * @param maxTokens Maximum tokens for entire LLM request
     * @return Assembled context string
     */
    private String assembleContext(List<SemanticSearchResponse.SearchResult> results, Integer maxTokens) {
        StringBuilder context = new StringBuilder();

        // Reserve tokens for question, answer, and prompt structure
        // maxTokens is for the entire request, we use ~60% for context
        int maxContextTokens = (int) (maxTokens * 0.6);
        int currentTokens = 0;

        log.debug("Assembling context (max tokens: {})", maxContextTokens);

        for (SemanticSearchResponse.SearchResult result : results) {
            // Estimate tokens: 1 token ≈ 4 characters
            int chunkTokens = result.getContent().length() / 4;

            // Check if adding this chunk would exceed limit
            if (currentTokens + chunkTokens > maxContextTokens) {
                log.debug("Reached token limit, stopping at {} chunks", context.toString().split("\n\n").length);
                break;
            }

            // Add chunk with metadata
            context.append(String.format(
                    "\n[Source: %s | Chunk %d | Relevance: %.0f%%]\n%s\n",
                    result.getDocumentTitle(),
                    result.getChunkIndex(),
                    result.getSimilarityScore() * 100,
                    result.getContent()
            ));

            currentTokens += chunkTokens;
        }

        log.debug("Context assembled: {} tokens (~{})", currentTokens, currentTokens * 4);
        return context.toString();
    }

    /**
     * Build RAG prompt
     *
     * Creates a structured prompt that:
     * - Provides clear instructions to the LLM
     * - Includes the retrieved context
     * - Contains the user's question
     * - Instructs the LLM to cite sources and stay grounded
     *
     * @param question User's question
     * @param context Assembled context from documents
     * @return Complete RAG prompt
     */
    private String buildRagPrompt(String question, String context) {
        return String.format("""
                You are a helpful AI assistant that answers questions based on provided documents.

                IMPORTANT INSTRUCTIONS:
                1. Answer the question using ONLY the information provided in the context below
                2. If the answer is not in the context, say "I don't have enough information to answer that question based on the provided documents."
                3. Be specific and cite which source you're using when possible
                4. If multiple sources provide different information, mention that
                5. Keep your answer clear, concise, and accurate

                CONTEXT FROM DOCUMENTS:
                %s

                QUESTION: %s

                ANSWER:""", context, question);
    }

    /**
     * Handle case when no relevant documents are found
     *
     * @param request Original request
     * @return Response indicating no information available
     */
    private DocumentQAResponse handleNoResults(DocumentQARequest request) {
        log.warn("No relevant documents found for question: '{}'", request.getQuestion());

        return DocumentQAResponse.builder()
                .question(request.getQuestion())
                .answer("I don't have any relevant documents to answer that question. " +
                        "Please try uploading documents on this topic or rephrasing your question.")
                .citations(List.of())
                .confidence(DocumentQAResponse.ConfidenceLevel.NONE)
                .documentsSearched(0)
                .relevantChunks(0)
                .tokensUsed(0)
                .inputTokens(0)
                .outputTokens(0)
                .provider(providerName)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Build final response with citations and metadata
     *
     * @param request Original request
     * @param llmResponse LLM's answer
     * @param searchResults Vector search results
     * @return Complete RAG response
     */
    private DocumentQAResponse buildResponse(
            DocumentQARequest request,
            ChatResponse llmResponse,
            SemanticSearchResponse searchResults) {

        // Convert search results to citations
        List<Citation> citations = searchResults.getResults().stream()
                .map(result -> Citation.builder()
                        .documentId(result.getDocumentId())
                        .documentTitle(result.getDocumentTitle())
                        .chunkIndex(result.getChunkIndex())
                        .similarityScore(result.getSimilarityScore())
                        .excerpt(truncateExcerpt(result.getContent(), 200))
                        .category(result.getCategory())
                        .build())
                .collect(Collectors.toList());

        // Calculate confidence based on average similarity scores
        DocumentQAResponse.ConfidenceLevel confidence = calculateConfidence(searchResults.getResults());

        log.info("RAG response built: {} citations, confidence={}", citations.size(), confidence);

        return DocumentQAResponse.builder()
                .question(request.getQuestion())
                .answer(llmResponse.getMessage())
                .citations(citations)
                .confidence(confidence)
                .documentsSearched(searchResults.getTotalResults())
                .relevantChunks(citations.size())
                .tokensUsed(llmResponse.getTokensUsed())
                .inputTokens(llmResponse.getInputTokens())
                .outputTokens(llmResponse.getOutputTokens())
                .provider(llmResponse.getProvider())
                .timestamp(llmResponse.getTimestamp())
                .build();
    }

    /**
     * Calculate confidence level based on similarity scores
     *
     * @param results Search results
     * @return Confidence level
     */
    private DocumentQAResponse.ConfidenceLevel calculateConfidence(
            List<SemanticSearchResponse.SearchResult> results) {

        if (results.isEmpty()) {
            return DocumentQAResponse.ConfidenceLevel.NONE;
        }

        // Calculate average similarity score
        double avgSimilarity = results.stream()
                .mapToDouble(SemanticSearchResponse.SearchResult::getSimilarityScore)
                .average()
                .orElse(0.0);

        // Classify confidence based on average similarity
        if (avgSimilarity >= 0.8) {
            return DocumentQAResponse.ConfidenceLevel.HIGH;
        } else if (avgSimilarity >= 0.6) {
            return DocumentQAResponse.ConfidenceLevel.MEDIUM;
        } else if (avgSimilarity >= 0.4) {
            return DocumentQAResponse.ConfidenceLevel.LOW;
        } else {
            return DocumentQAResponse.ConfidenceLevel.VERY_LOW;
        }
    }

    /**
     * Truncate excerpt to specified length
     *
     * @param text Full text
     * @param maxLength Maximum length
     * @return Truncated text
     */
    private String truncateExcerpt(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
