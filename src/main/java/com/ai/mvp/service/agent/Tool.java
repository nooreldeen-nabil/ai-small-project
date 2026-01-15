package com.ai.mvp.service.agent;

import com.ai.mvp.dto.gemini.GeminiApiRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Base interface for all agent tools
 *
 * A tool is a function that the AI agent can call to accomplish tasks.
 * Each tool:
 * 1. Has a unique name and description
 * 2. Defines its parameters (JSON Schema)
 * 3. Can execute and return results
 *
 * Phase 5: Agentic AI - Tool Use
 */
public interface Tool {

    /**
     * Get the tool name (must be unique)
     * Used by LLM to identify which tool to call
     */
    String getName();

    /**
     * Get the tool description
     * Used by LLM to understand when to use this tool
     */
    String getDescription();

    /**
     * Get the tool parameters as JSON Schema
     * Defines what arguments the tool accepts
     */
    Map<String, Object> getParametersSchema();

    /**
     * Execute the tool with given arguments
     *
     * @param args Arguments provided by the LLM
     * @return Tool execution result
     */
    ToolResult execute(Map<String, Object> args);

    /**
     * Convert this tool to Gemini function declaration
     */
    default GeminiApiRequest.FunctionDeclaration toFunctionDeclaration() {
        return GeminiApiRequest.FunctionDeclaration.builder()
                .name(getName())
                .description(getDescription())
                .parameters(getParametersSchema())
                .build();
    }

    /**
     * Helper method to safely get a parameter
     */
    default <T> T getParameter(Map<String, Object> args, String key, Class<T> type, T defaultValue) {
        Object value = args.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        // Try to convert if needed
        if (type == String.class) {
            return type.cast(value.toString());
        }
        if (type == Integer.class && value instanceof Number) {
            return type.cast(((Number) value).intValue());
        }
        if (type == Double.class && value instanceof Number) {
            return type.cast(((Number) value).doubleValue());
        }
        return defaultValue;
    }

    /**
     * Helper method to create parameter schema
     */
    static Map<String, Object> createParameterSchema(String type, String description, boolean required) {
        return Map.of(
                "type", type,
                "description", description
        );
    }
}
