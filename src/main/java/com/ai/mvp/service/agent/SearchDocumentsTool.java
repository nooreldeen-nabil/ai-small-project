package com.ai.mvp.service.agent;

import com.ai.mvp.dto.vector.SemanticSearchRequest;
import com.ai.mvp.dto.vector.SemanticSearchResponse;
import com.ai.mvp.service.VectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool to search documents using semantic search
 *
 * This tool allows the agent to search the vector database
 * for relevant documents and information.
 *
 * Example use cases:
 * - "Find information about neural networks"
 * - "Search for documents about machine learning"
 * - "What do we know about backpropagation?"
 *
 * Phase 5: Agentic AI - Tool Use
 * Integrates with Phase 3 (Vector Search)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchDocumentsTool implements Tool {

    private final VectorSearchService vectorSearchService;

    @Override
    public String getName() {
        return "search_documents";
    }

    @Override
    public String getDescription() {
        return "Search the document database using semantic search. " +
               "Returns relevant document chunks based on the meaning of the query, not just keywords. " +
               "Use this when the user asks questions about stored documents or requests information that might be in the database.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of(
                                "type", "string",
                                "description", "Search query - what to search for in the documents"
                        ),
                        "topK", Map.of(
                                "type", "integer",
                                "description", "Number of results to return (1-10). Default: 5"
                        ),
                        "similarityThreshold", Map.of(
                                "type", "number",
                                "description", "Minimum similarity score (0.0-1.0). Only return results above this threshold. Default: 0.5"
                        ),
                        "category", Map.of(
                                "type", "string",
                                "description", "Optional category filter (e.g., 'AI', 'Technology', 'Science')"
                        )
                ),
                "required", new String[]{"query"}
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> args) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Executing search_documents tool with args: {}", args);

            // Get parameters
            String query = getParameter(args, "query", String.class, null);
            if (query == null || query.isEmpty()) {
                return ToolResult.failure("Query is required");
            }

            Integer topK = getParameter(args, "topK", Integer.class, 5);
            Double similarityThreshold = getParameter(args, "similarityThreshold", Double.class, 0.5);
            String category = getParameter(args, "category", String.class, null);

            // Validate parameters
            if (topK < 1 || topK > 10) {
                topK = 5;
            }
            if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
                similarityThreshold = 0.5;
            }

            // Create search request
            SemanticSearchRequest searchRequest = SemanticSearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .category(category)
                    .build();

            // Execute search
            SemanticSearchResponse searchResponse = vectorSearchService.search(searchRequest);

            // Format results for agent
            List<Map<String, Object>> formattedResults = new ArrayList<>();
            if (searchResponse.getResults() != null) {
                for (SemanticSearchResponse.SearchResult result : searchResponse.getResults()) {
                    Map<String, Object> formattedResult = new HashMap<>();
                    formattedResult.put("document", result.getDocumentTitle());
                    formattedResult.put("content", result.getContent());
                    formattedResult.put("similarity", result.getSimilarityScore());
                    if (result.getCategory() != null) {
                        formattedResult.put("category", result.getCategory());
                    }
                    formattedResults.add(formattedResult);
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("search_documents completed successfully in {}ms - Found {} results",
                    executionTime, formattedResults.size());

            return ToolResult.success(Map.of(
                    "query", query,
                    "totalResults", searchResponse.getTotalResults(),
                    "results", formattedResults
            ), executionTime);

        } catch (Exception e) {
            log.error("Error executing search_documents: {}", e.getMessage(), e);
            return ToolResult.failure("Failed to search documents: " + e.getMessage());
        }
    }
}
