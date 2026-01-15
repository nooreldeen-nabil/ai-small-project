package com.ai.mvp.service.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool to query the Oracle database for statistics
 *
 * This tool allows the agent to get database statistics
 * like document counts, chunk counts, etc.
 *
 * Example use cases:
 * - "How many documents are in the database?"
 * - "Show me database statistics"
 * - "What's the total number of document chunks?"
 *
 * Phase 5: Agentic AI - Tool Use
 * Integrates with Oracle 23c database
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseQueryTool implements Tool {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String getName() {
        return "database_query";
    }

    @Override
    public String getDescription() {
        return "Query the database for statistics and information. " +
               "Can retrieve document counts, chunk counts, categories, and other database metadata. " +
               "Use this when the user asks about database statistics or wants to know what's stored in the system.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "queryType", Map.of(
                                "type", "string",
                                "description", "Type of query to execute",
                                "enum", new String[]{"document_count", "chunk_count", "categories", "all_stats"}
                        )
                ),
                "required", new String[]{"queryType"}
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> args) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Executing database_query tool with args: {}", args);

            // Get parameters
            String queryType = getParameter(args, "queryType", String.class, "all_stats");

            // Execute appropriate query
            Map<String, Object> result = new HashMap<>();

            switch (queryType) {
                case "document_count":
                    result.put("documentCount", getDocumentCount());
                    break;

                case "chunk_count":
                    result.put("chunkCount", getChunkCount());
                    break;

                case "categories":
                    result.put("categories", getCategories());
                    break;

                case "all_stats":
                default:
                    result.put("documentCount", getDocumentCount());
                    result.put("chunkCount", getChunkCount());
                    result.put("categories", getCategories());
                    result.put("averageChunksPerDocument", getAverageChunksPerDocument());
                    break;
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("database_query completed successfully in {}ms", executionTime);

            return ToolResult.success(result, executionTime);

        } catch (Exception e) {
            log.error("Error executing database_query: {}", e.getMessage(), e);
            return ToolResult.failure("Failed to query database: " + e.getMessage());
        }
    }

    /**
     * Get total number of documents
     */
    private Integer getDocumentCount() {
        String sql = "SELECT COUNT(*) FROM documents";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * Get total number of chunks
     */
    private Integer getChunkCount() {
        String sql = "SELECT COUNT(*) FROM document_chunks";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * Get list of unique categories
     */
    private Map<String, Integer> getCategories() {
        String sql = "SELECT category, COUNT(*) as count FROM documents " +
                    "WHERE category IS NOT NULL " +
                    "GROUP BY category " +
                    "ORDER BY count DESC";

        Map<String, Integer> categories = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            categories.put(rs.getString("category"), rs.getInt("count"));
        });

        return categories;
    }

    /**
     * Get average chunks per document
     */
    private Double getAverageChunksPerDocument() {
        String sql = "SELECT AVG(chunk_count) FROM " +
                    "(SELECT COUNT(*) as chunk_count FROM document_chunks GROUP BY document_id)";
        return jdbcTemplate.queryForObject(sql, Double.class);
    }
}
