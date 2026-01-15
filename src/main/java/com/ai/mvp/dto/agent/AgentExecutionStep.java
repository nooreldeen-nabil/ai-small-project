package com.ai.mvp.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Single step in agent execution
 *
 * Represents one decision/action the agent took:
 * - Thinking (LLM reasoning)
 * - Tool call (using a tool)
 * - Tool result (result from tool)
 * - Final answer (completing the task)
 *
 * Phase 5: Agentic AI - Tool Use
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single step in agent execution showing decisions and actions")
public class AgentExecutionStep {

    /**
     * Step type: THINKING, TOOL_CALL, TOOL_RESULT, FINAL_ANSWER
     */
    @Schema(description = "Type of step", example = "TOOL_CALL")
    private StepType type;

    /**
     * Step number (starts at 1)
     */
    @Schema(description = "Step number in execution sequence", example = "1")
    private Integer stepNumber;

    /**
     * Thinking/reasoning from LLM
     */
    @Schema(description = "Agent's thinking or reasoning (if applicable)")
    private String thinking;

    /**
     * Tool name (if tool call)
     */
    @Schema(description = "Name of tool being called (if applicable)", example = "search_documents")
    private String toolName;

    /**
     * Tool arguments (if tool call)
     */
    @Schema(description = "Arguments passed to the tool (if applicable)")
    private Map<String, Object> toolArgs;

    /**
     * Tool result (if tool result)
     */
    @Schema(description = "Result from tool execution (if applicable)")
    private Object toolResult;

    /**
     * Whether tool execution was successful
     */
    @Schema(description = "Whether tool execution succeeded (if applicable)")
    private Boolean toolSuccess;

    /**
     * Error message (if tool failed)
     */
    @Schema(description = "Error message if tool failed (if applicable)")
    private String error;

    /**
     * Execution time for this step in milliseconds
     */
    @Schema(description = "Execution time for this step in milliseconds", example = "245")
    private Long executionTimeMs;

    /**
     * Step types
     */
    public enum StepType {
        /**
         * LLM is thinking/reasoning
         */
        THINKING,

        /**
         * LLM decided to call a tool
         */
        TOOL_CALL,

        /**
         * Tool execution result
         */
        TOOL_RESULT,

        /**
         * Agent providing final answer
         */
        FINAL_ANSWER
    }
}
