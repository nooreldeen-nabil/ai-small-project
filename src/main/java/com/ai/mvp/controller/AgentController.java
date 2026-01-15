package com.ai.mvp.controller;

import com.ai.mvp.dto.agent.AgentTaskRequest;
import com.ai.mvp.dto.agent.AgentTaskResponse;
import com.ai.mvp.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI Agent operations
 *
 * Phase 5: Agentic AI - Tool Use
 *
 * This controller provides endpoints for:
 * - Executing tasks using the AI agent with tool use
 * - Listing available tools
 *
 * The agent can:
 * - Search documents (semantic search)
 * - Get current date/time
 * - Perform calculations
 * - Query database statistics
 *
 * Example tasks:
 * - "How many documents about AI are in the database?"
 * - "Search for information about neural networks and summarize what you find"
 * - "What day is it today and how many days until December 31st?"
 * - "Calculate 15% of 2500"
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Tag(name = "Agent", description = "AI Agent with Tool Use (Phase 5)")
public class AgentController {

    private final AgentService agentService;

    /**
     * Execute a task using the AI agent
     *
     * The agent will:
     * 1. Understand your task
     * 2. Decide which tools to use
     * 3. Execute the tools
     * 4. Provide a final answer
     *
     * You can see all the steps the agent took in the executionLog.
     *
     * @param request Task to execute
     * @return Agent response with answer and execution log
     */
    @PostMapping("/task")
    @Operation(
            summary = "Execute a task using the AI agent",
            description = """
                    Execute a task using the AI agent with tool use capabilities.

                    The agent has access to these tools:
                    - search_documents: Search the vector database for relevant information
                    - get_current_date: Get current date and time
                    - calculate: Perform mathematical calculations
                    - database_query: Query database statistics

                    The agent will autonomously decide which tools to use based on your task.
                    The response includes a detailed execution log showing all steps taken.

                    Example tasks:
                    - "How many documents about AI are in the database?"
                    - "Search for information about neural networks and tell me what you find"
                    - "What is today's date?"
                    - "Calculate 15% of 2500"
                    - "Find documents about machine learning and count how many there are"
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task executed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AgentTaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<AgentTaskResponse> executeTask(
            @Valid @RequestBody AgentTaskRequest request) {

        log.info("Received agent task request - Task: '{}'", request.getTask());

        AgentTaskResponse response = agentService.executeTask(request);

        if (response.isSuccess()) {
            log.info("Agent task completed successfully - Tool calls: {}, LLM calls: {}, Total time: {}ms",
                    response.getToolCallCount(),
                    response.getLlmCallCount(),
                    response.getTotalExecutionTimeMs());
        } else {
            log.warn("Agent task failed - Error: {}", response.getError());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get list of available tools
     *
     * Returns the names of all tools available to the agent.
     *
     * @return List of tool names
     */
    @GetMapping("/tools")
    @Operation(
            summary = "List available tools",
            description = "Get a list of all tools available to the AI agent. " +
                         "Each tool has specific capabilities that the agent can use to complete tasks."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved tool list",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Map<String, Object>> getAvailableTools() {
        log.info("Retrieving available tools");

        List<String> tools = agentService.getAvailableTools();

        return ResponseEntity.ok(Map.of(
                "totalTools", tools.size(),
                "tools", tools,
                "description", "Tools available to the AI agent for task execution"
        ));
    }

    /**
     * Health check endpoint for agent service
     *
     * @return Health status
     */
    @GetMapping("/health")
    @Operation(
            summary = "Agent service health check",
            description = "Check if the agent service is running and ready to accept tasks"
    )
    public ResponseEntity<Map<String, Object>> health() {
        List<String> tools = agentService.getAvailableTools();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Agent Service (Phase 5)",
                "toolsAvailable", tools.size(),
                "capabilities", List.of(
                        "Semantic document search",
                        "Date/time queries",
                        "Mathematical calculations",
                        "Database statistics"
                )
        ));
    }
}
