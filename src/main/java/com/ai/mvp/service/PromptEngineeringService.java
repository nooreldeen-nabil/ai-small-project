package com.ai.mvp.service;

import com.ai.mvp.dto.ChatRequest;
import com.ai.mvp.dto.ChatResponse;
import com.ai.mvp.dto.prompt.*;
import com.ai.mvp.util.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for demonstrating prompt engineering techniques
 *
 * Phase 2: Prompt Engineering
 *
 * This service implements 4 key prompting techniques:
 * 1. Zero-shot: Direct questions without examples
 * 2. Few-shot: Learning from examples
 * 3. Chain-of-thought: Step-by-step reasoning
 * 4. Structured output: JSON/formatted responses
 *
 * Each technique uses the ChatService to actually call the LLM,
 * but wraps it with specialized prompt construction.
 *
 * Learning Points:
 * - How different prompting techniques affect LLM output
 * - When to use each technique
 * - How to construct effective prompts
 * - The importance of prompt engineering for quality results
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptEngineeringService {

    private final ChatService chatService;

    /**
     * Zero-Shot Prompting
     *
     * The simplest approach - just ask directly without any examples or special formatting.
     *
     * Best for:
     * - Straightforward questions
     * - Tasks the LLM has seen many times during training
     * - When you want creative/diverse outputs
     *
     * Example: "Translate 'Hello' to French"
     */
    public PromptResponse zeroShot(PromptRequest request) {
        log.info("Processing zero-shot prompt");

        // Build the prompt (zero-shot is just the direct message)
        String prompt = PromptTemplate.buildZeroShotPrompt(request.getMessage());

        // Call the LLM
        ChatRequest chatRequest = ChatRequest.builder()
                .message(prompt)
                .maxTokens(request.getMaxTokens())
                .temperature(request.getTemperature())
                .build();

        ChatResponse chatResponse = chatService.chat(chatRequest);

        // Convert to PromptResponse
        return buildPromptResponse(chatResponse, "ZERO_SHOT", prompt);
    }

    /**
     * Few-Shot Prompting
     *
     * Provides examples to guide the LLM's response format and style.
     *
     * Best for:
     * - Consistent output formatting
     * - Ambiguous tasks that need clarification
     * - Teaching the LLM a specific pattern
     *
     * Example:
     * Review: "Great!" → Positive
     * Review: "Terrible" → Negative
     * Review: "It's okay" → ?
     */
    public PromptResponse fewShot(FewShotRequest request) {
        log.info("Processing few-shot prompt with {} examples", request.getExamples().size());

        // Build the prompt with examples
        String prompt = PromptTemplate.buildFewShotPrompt(request);

        // Use low temperature for consistent formatting
        Double temperature = request.getTemperature() != null ? request.getTemperature() : 0.3;

        // Call the LLM
        ChatRequest chatRequest = ChatRequest.builder()
                .message(prompt)
                .maxTokens(request.getMaxTokens())
                .temperature(temperature)
                .build();

        ChatResponse chatResponse = chatService.chat(chatRequest);

        // Convert to PromptResponse
        return buildPromptResponse(chatResponse, "FEW_SHOT", prompt);
    }

    /**
     * Chain-of-Thought Prompting
     *
     * Asks the LLM to show its reasoning process step-by-step.
     *
     * Best for:
     * - Math problems
     * - Logic puzzles
     * - Complex analysis requiring multiple steps
     * - When you need to understand the reasoning
     *
     * Example:
     * "Solve step by step: A store has 15 apples. They sell 40%. How many left?
     * Let's think through this:
     * 1. Calculate 40% of 15..."
     */
    public PromptResponse chainOfThought(ChainOfThoughtRequest request) {
        log.info("Processing chain-of-thought prompt");

        // Build the prompt requesting step-by-step reasoning
        String prompt = PromptTemplate.buildChainOfThoughtPrompt(request);

        // Use low temperature for logical consistency
        Double temperature = request.getTemperature() != null ? request.getTemperature() : 0.3;

        // Call the LLM
        ChatRequest chatRequest = ChatRequest.builder()
                .message(prompt)
                .maxTokens(request.getMaxTokens())
                .temperature(temperature)
                .build();

        ChatResponse chatResponse = chatService.chat(chatRequest);

        // Convert to PromptResponse
        return buildPromptResponse(chatResponse, "CHAIN_OF_THOUGHT", prompt);
    }

    /**
     * Structured Output Prompting
     *
     * Requests responses in specific formats like JSON, XML, CSV, etc.
     *
     * Best for:
     * - API integrations
     * - Database storage
     * - Programmatic parsing
     * - Data pipelines
     *
     * Example:
     * "Extract info and return as JSON:
     * 'John, 30, Engineer at Tech Corp'
     *
     * Format: {\"name\": \"...\", \"age\": ..., \"job\": \"...\"}"
     */
    public PromptResponse structuredOutput(StructuredOutputRequest request) {
        log.info("Processing structured output prompt for format: {}", request.getFormatType());

        // Build the prompt requesting structured output
        String prompt = PromptTemplate.buildStructuredOutputPrompt(request);

        // Use very low temperature for deterministic, structured output
        Double temperature = request.getTemperature() != null ? request.getTemperature() : 0.2;

        // Add system prompt to enforce structure
        String systemPrompt = PromptTemplate.buildSystemPrompt(
                "a precise data extraction assistant",
                "You always return data in the exact format requested, with no additional text or explanations."
        );

        // Call the LLM
        ChatRequest chatRequest = ChatRequest.builder()
                .message(prompt)
                .systemPrompt(systemPrompt)
                .maxTokens(request.getMaxTokens())
                .temperature(temperature)
                .build();

        ChatResponse chatResponse = chatService.chat(chatRequest);

        // Convert to PromptResponse
        return buildPromptResponse(chatResponse, "STRUCTURED_OUTPUT", prompt);
    }

    /**
     * Helper method to convert ChatResponse to PromptResponse
     *
     * This adds the technique type and full prompt to the response,
     * which helps users understand how the prompt was constructed.
     */
    private PromptResponse buildPromptResponse(ChatResponse chatResponse, String technique, String fullPrompt) {
        // Calculate estimated cost
        double cost = chatService.calculateCost(chatResponse.getInputTokens(), chatResponse.getOutputTokens());

        return PromptResponse.builder()
                .response(chatResponse.getResponse())
                .technique(technique)
                .fullPrompt(fullPrompt)
                .model(chatResponse.getModel())
                .tokensUsed(chatResponse.getTokensUsed())
                .inputTokens(chatResponse.getInputTokens())
                .outputTokens(chatResponse.getOutputTokens())
                .stopReason(chatResponse.getStopReason())
                .timestamp(LocalDateTime.now())
                .estimatedCost(cost)
                .build();
    }
}
