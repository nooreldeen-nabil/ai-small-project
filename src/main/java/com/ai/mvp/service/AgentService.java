package com.ai.mvp.service;

import com.ai.mvp.config.LlmProvider;
import com.ai.mvp.dto.agent.AgentExecutionStep;
import com.ai.mvp.dto.agent.AgentTaskRequest;
import com.ai.mvp.dto.agent.AgentTaskResponse;
import com.ai.mvp.dto.gemini.GeminiApiRequest;
import com.ai.mvp.dto.gemini.GeminiApiResponse;
import com.ai.mvp.exception.LlmApiException;
import com.ai.mvp.service.agent.Tool;
import com.ai.mvp.service.agent.ToolResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent Service - Orchestrates AI agent with tool use
 *
 * This service implements the agentic AI pattern:
 * 1. User provides a task
 * 2. LLM reasons about the task and decides which tools to use
 * 3. Agent executes the tools
 * 4. Results are sent back to LLM
 * 5. LLM continues reasoning until task is complete
 *
 * Phase 5: Agentic AI - Tool Use
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final GeminiApiService geminiApiService;
    private final List<Tool> tools; // All tools auto-injected by Spring
    private final ObjectMapper objectMapper;

    @Value("${llm.provider:GEMINI}")
    private String providerName;

    private static final int DEFAULT_MAX_TOOL_CALLS = 10;
    private static final double DEFAULT_TEMPERATURE = 0.0;
    private static final int MAX_OUTPUT_TOKENS = 2048;

    /**
     * Execute a task using the AI agent
     *
     * The agent will:
     * 1. Understand the task
     * 2. Decide which tools to use
     * 3. Execute tools
     * 4. Synthesize results
     * 5. Provide final answer
     *
     * @param request Task request from user
     * @return Task response with answer and execution log
     */
    public AgentTaskResponse executeTask(AgentTaskRequest request) {
        long startTime = System.currentTimeMillis();
        LlmProvider provider = LlmProvider.valueOf(providerName.toUpperCase());

        log.info("Starting agent task execution - Task: '{}', Provider: {}",
                request.getTask(), provider);

        // Initialize execution tracking
        List<AgentExecutionStep> executionLog = new ArrayList<>();
        List<GeminiApiRequest.Content> conversationHistory = new ArrayList<>();
        int llmCallCount = 0;
        int toolCallCount = 0;
        int totalTokens = 0;

        // Configuration
        int maxToolCalls = request.getMaxToolCalls() != null ?
                request.getMaxToolCalls() : DEFAULT_MAX_TOOL_CALLS;
        double temperature = request.getTemperature() != null ?
                request.getTemperature() : DEFAULT_TEMPERATURE;

        try {
            // Add user's task to conversation
            conversationHistory.add(createUserMessage(request.getTask()));

            // Agent loop - continue until final answer or max iterations
            for (int iteration = 0; iteration < maxToolCalls; iteration++) {
                log.info("Agent iteration {} - LLM call count: {}, Tool call count: {}",
                        iteration + 1, llmCallCount, toolCallCount);

                // Call LLM with current conversation and available tools
                GeminiApiRequest llmRequest = buildAgentRequest(conversationHistory, temperature);
                llmCallCount++;

                GeminiApiResponse llmResponse = geminiApiService.sendMessage(llmRequest);
                totalTokens += llmResponse.getTotalTokens();

                // Check if LLM wants to call tools
                if (llmResponse.hasFunctionCalls()) {
                    List<GeminiApiResponse.FunctionCall> functionCalls = llmResponse.getFunctionCalls();
                    log.info("LLM requested {} tool call(s)", functionCalls.size());

                    // Log the tool call decision
                    executionLog.add(AgentExecutionStep.builder()
                            .type(AgentExecutionStep.StepType.THINKING)
                            .stepNumber(executionLog.size() + 1)
                            .thinking("Decided to use " + functionCalls.size() + " tool(s): " +
                                    functionCalls.stream()
                                            .map(GeminiApiResponse.FunctionCall::getName)
                                            .collect(Collectors.joining(", ")))
                            .build());

                    // Execute all requested tools
                    List<GeminiApiRequest.Part> toolResponseParts = new ArrayList<>();

                    for (GeminiApiResponse.FunctionCall functionCall : functionCalls) {
                        toolCallCount++;

                        // Log tool call
                        Map<String, Object> args = convertToMap(functionCall.getArgs());
                        executionLog.add(AgentExecutionStep.builder()
                                .type(AgentExecutionStep.StepType.TOOL_CALL)
                                .stepNumber(executionLog.size() + 1)
                                .toolName(functionCall.getName())
                                .toolArgs(args)
                                .build());

                        // Execute tool
                        ToolResult toolResult = executeTool(functionCall.getName(), args);

                        // Log tool result
                        executionLog.add(AgentExecutionStep.builder()
                                .type(AgentExecutionStep.StepType.TOOL_RESULT)
                                .stepNumber(executionLog.size() + 1)
                                .toolName(functionCall.getName())
                                .toolResult(toolResult.getResult())
                                .toolSuccess(toolResult.isSuccess())
                                .error(toolResult.getError())
                                .executionTimeMs(toolResult.getExecutionTimeMs())
                                .build());

                        // Create function response
                        Map<String, Object> responseContent = new HashMap<>();
                        if (toolResult.isSuccess()) {
                            responseContent.put("success", true);
                            responseContent.put("result", toolResult.getResult());
                        } else {
                            responseContent.put("success", false);
                            responseContent.put("error", toolResult.getError());
                        }

                        toolResponseParts.add(GeminiApiRequest.Part.builder()
                                .functionResponse(GeminiApiRequest.FunctionResponse.builder()
                                        .name(functionCall.getName())
                                        .response(responseContent)
                                        .build())
                                .build());
                    }

                    // Add model's function calls to conversation
                    conversationHistory.add(GeminiApiRequest.Content.builder()
                            .role("model")
                            .parts(functionCalls.stream()
                                    .map(fc -> GeminiApiRequest.Part.builder()
                                            .functionCall(GeminiApiRequest.FunctionCall.builder()
                                                    .name(fc.getName())
                                                    .args(fc.getArgs())
                                                    .build())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build());

                    // Add function responses to conversation
                    conversationHistory.add(GeminiApiRequest.Content.builder()
                            .role("user")
                            .parts(toolResponseParts)
                            .build());

                } else {
                    // LLM provided final answer (no more tool calls)
                    String finalAnswer = llmResponse.getTextContent();

                    if (finalAnswer == null || finalAnswer.isEmpty()) {
                        log.warn("LLM returned empty response");
                        finalAnswer = "I apologize, but I was unable to complete the task.";
                    }

                    log.info("Agent completed task with final answer (length: {} chars)", finalAnswer.length());

                    // Log final answer
                    executionLog.add(AgentExecutionStep.builder()
                            .type(AgentExecutionStep.StepType.FINAL_ANSWER)
                            .stepNumber(executionLog.size() + 1)
                            .thinking(finalAnswer)
                            .build());

                    // Build successful response
                    long totalTime = System.currentTimeMillis() - startTime;
                    return AgentTaskResponse.builder()
                            .task(request.getTask())
                            .answer(finalAnswer)
                            .success(true)
                            .executionLog(executionLog)
                            .llmCallCount(llmCallCount)
                            .toolCallCount(toolCallCount)
                            .totalTokens(totalTokens)
                            .totalExecutionTimeMs(totalTime)
                            .provider(provider.name())
                            .timestamp(LocalDateTime.now())
                            .build();
                }
            }

            // Max iterations reached without final answer
            log.warn("Agent reached max iterations ({}) without completing task", maxToolCalls);
            long totalTime = System.currentTimeMillis() - startTime;

            return AgentTaskResponse.builder()
                    .task(request.getTask())
                    .answer("I apologize, but I reached the maximum number of tool calls without completing the task. Please try rephrasing your request.")
                    .success(false)
                    .error("Max tool calls reached")
                    .executionLog(executionLog)
                    .llmCallCount(llmCallCount)
                    .toolCallCount(toolCallCount)
                    .totalTokens(totalTokens)
                    .totalExecutionTimeMs(totalTime)
                    .provider(provider.name())
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error executing agent task: {}", e.getMessage(), e);
            long totalTime = System.currentTimeMillis() - startTime;

            return AgentTaskResponse.builder()
                    .task(request.getTask())
                    .answer(null)
                    .success(false)
                    .error("Agent execution failed: " + e.getMessage())
                    .executionLog(executionLog)
                    .llmCallCount(llmCallCount)
                    .toolCallCount(toolCallCount)
                    .totalTokens(totalTokens)
                    .totalExecutionTimeMs(totalTime)
                    .provider(provider.name())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Build LLM request with conversation history and tool definitions
     */
    private GeminiApiRequest buildAgentRequest(List<GeminiApiRequest.Content> conversationHistory,
                                                double temperature) {
        // Convert tools to function declarations
        List<GeminiApiRequest.FunctionDeclaration> functionDeclarations = tools.stream()
                .map(Tool::toFunctionDeclaration)
                .collect(Collectors.toList());

        // Build tool definition
        GeminiApiRequest.Tool toolDefinition = GeminiApiRequest.Tool.builder()
                .functionDeclarations(functionDeclarations)
                .build();

        // Build generation config
        GeminiApiRequest.GenerationConfig generationConfig = GeminiApiRequest.GenerationConfig.builder()
                .temperature(temperature)
                .maxOutputTokens(MAX_OUTPUT_TOKENS)
                .build();

        // Build system instruction
        String systemPrompt = buildSystemPrompt();
        GeminiApiRequest.Content systemInstruction = GeminiApiRequest.Content.builder()
                .parts(List.of(GeminiApiRequest.Part.builder()
                        .text(systemPrompt)
                        .build()))
                .build();

        // Build request
        return GeminiApiRequest.builder()
                .contents(new ArrayList<>(conversationHistory))
                .tools(List.of(toolDefinition))
                .systemInstruction(systemInstruction)
                .generationConfig(generationConfig)
                .build();
    }

    /**
     * Build system prompt for the agent
     */
    private String buildSystemPrompt() {
        return """
                You are a helpful AI agent with access to tools.

                Your job is to:
                1. Understand the user's task
                2. Decide which tools to use to complete it
                3. Use the tools to gather information
                4. Provide a clear, helpful answer

                Guidelines:
                - Use tools when you need information from the database or need to perform calculations
                - You can use multiple tools if needed
                - After using tools, synthesize the results into a clear answer
                - If you can't complete the task, explain why
                - Be concise but thorough

                Available tools will be provided in the function definitions.
                """;
    }

    /**
     * Create a user message
     */
    private GeminiApiRequest.Content createUserMessage(String text) {
        return GeminiApiRequest.Content.builder()
                .role("user")
                .parts(List.of(GeminiApiRequest.Part.builder()
                        .text(text)
                        .build()))
                .build();
    }

    /**
     * Execute a tool by name
     */
    private ToolResult executeTool(String toolName, Map<String, Object> args) {
        log.info("Executing tool '{}' with args: {}", toolName, args);

        // Find tool by name
        Optional<Tool> toolOptional = tools.stream()
                .filter(t -> t.getName().equals(toolName))
                .findFirst();

        if (toolOptional.isEmpty()) {
            log.error("Tool not found: {}", toolName);
            return ToolResult.failure("Unknown tool: " + toolName);
        }

        // Execute tool
        try {
            Tool tool = toolOptional.get();
            return tool.execute(args);
        } catch (Exception e) {
            log.error("Error executing tool '{}': {}", toolName, e.getMessage(), e);
            return ToolResult.failure("Tool execution error: " + e.getMessage());
        }
    }

    /**
     * Convert function call args to Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object args) {
        if (args instanceof Map) {
            return (Map<String, Object>) args;
        }
        if (args == null) {
            return new HashMap<>();
        }
        // Convert to map via Jackson
        return objectMapper.convertValue(args, Map.class);
    }

    /**
     * Get list of available tools (for documentation/debugging)
     */
    public List<String> getAvailableTools() {
        return tools.stream()
                .map(Tool::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
