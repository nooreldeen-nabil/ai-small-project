package com.ai.mvp.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for agent task execution
 *
 * The user provides a task in natural language,
 * and the agent will use available tools to complete it.
 *
 * Phase 5: Agentic AI - Tool Use
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to execute a task using the AI agent")
public class AgentTaskRequest {

    /**
     * Task description in natural language
     * Example: "How many documents about AI are in the database?"
     */
    @NotBlank(message = "Task is required")
    @Size(max = 5000, message = "Task must be less than 5000 characters")
    @Schema(
            description = "Task for the agent to complete",
            example = "Search for information about neural networks and tell me what you find",
            required = true
    )
    private String task;

    /**
     * Maximum number of tool calls allowed (prevents infinite loops)
     * Default: 10
     */
    @Schema(
            description = "Maximum number of tool calls allowed (prevents infinite loops)",
            example = "10",
            defaultValue = "10"
    )
    private Integer maxToolCalls;

    /**
     * Temperature for LLM (0.0 - 1.0)
     * Lower = more focused, Higher = more creative
     * Default: 0.0 (most focused for tool use)
     */
    @Schema(
            description = "Temperature for LLM reasoning (0.0-1.0). Lower is more focused.",
            example = "0.0",
            defaultValue = "0.0"
    )
    private Double temperature;
}
