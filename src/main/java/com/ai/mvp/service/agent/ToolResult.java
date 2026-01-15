package com.ai.mvp.service.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of tool execution
 *
 * Contains:
 * - success: Whether the tool executed successfully
 * - result: The result data (can be any type)
 * - error: Error message if failed
 *
 * Phase 5: Agentic AI - Tool Use
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    /**
     * Whether the tool executed successfully
     */
    private boolean success;

    /**
     * Tool execution result (can be string, object, array, etc.)
     */
    private Object result;

    /**
     * Error message if execution failed
     */
    private String error;

    /**
     * Tool execution time in milliseconds
     */
    private Long executionTimeMs;

    /**
     * Create a successful result
     */
    public static ToolResult success(Object result, Long executionTimeMs) {
        return ToolResult.builder()
                .success(true)
                .result(result)
                .executionTimeMs(executionTimeMs)
                .build();
    }

    /**
     * Create a failed result
     */
    public static ToolResult failure(String error) {
        return ToolResult.builder()
                .success(false)
                .error(error)
                .build();
    }
}
