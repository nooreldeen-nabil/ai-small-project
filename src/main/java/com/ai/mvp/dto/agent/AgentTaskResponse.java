package com.ai.mvp.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for agent task execution
 *
 * Contains the final answer and a log of all steps
 * the agent took to complete the task.
 *
 * Phase 5: Agentic AI - Tool Use
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent task execution result with detailed execution log")
public class AgentTaskResponse {

    /**
     * Original task from user
     */
    @Schema(description = "Original task from the user")
    private String task;

    /**
     * Final answer from the agent
     */
    @Schema(description = "Agent's final answer to the task")
    private String answer;

    /**
     * Whether the task was completed successfully
     */
    @Schema(description = "Whether the task was completed successfully")
    private boolean success;

    /**
     * Error message if task failed
     */
    @Schema(description = "Error message if task failed")
    private String error;

    /**
     * Detailed execution log showing all steps
     */
    @Schema(description = "Detailed log of all steps the agent took")
    private List<AgentExecutionStep> executionLog;

    /**
     * Total number of LLM calls made
     */
    @Schema(description = "Total number of LLM calls made")
    private Integer llmCallCount;

    /**
     * Total number of tool calls made
     */
    @Schema(description = "Total number of tool calls made")
    private Integer toolCallCount;

    /**
     * Total tokens used
     */
    @Schema(description = "Total tokens used")
    private Integer totalTokens;

    /**
     * Total execution time in milliseconds
     */
    @Schema(description = "Total execution time in milliseconds")
    private Long totalExecutionTimeMs;

    /**
     * LLM provider used
     */
    @Schema(description = "LLM provider used (GEMINI/CLAUDE)")
    private String provider;

    /**
     * Timestamp when task started
     */
    @Schema(description = "When the task execution started")
    private LocalDateTime timestamp;
}
