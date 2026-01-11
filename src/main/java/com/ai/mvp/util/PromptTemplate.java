package com.ai.mvp.util;

import com.ai.mvp.dto.prompt.ChainOfThoughtRequest;
import com.ai.mvp.dto.prompt.FewShotRequest;
import com.ai.mvp.dto.prompt.StructuredOutputRequest;

import java.util.List;

/**
 * Utility class for building prompts using different techniques
 *
 * Phase 2: Prompt Engineering
 *
 * This class demonstrates how to construct effective prompts for different use cases.
 * Each method builds a prompt following best practices for that technique.
 *
 * Learning Points:
 * - How to structure prompts for different techniques
 * - How to provide context and examples
 * - How to request specific output formats
 * - How to guide the LLM's reasoning process
 */
public class PromptTemplate {

    /**
     * Build a zero-shot prompt
     *
     * Zero-shot prompting is the simplest approach - just ask directly without examples.
     * Works well when:
     * - The task is straightforward
     * - The LLM has been trained on similar tasks
     * - You want creative/diverse outputs
     *
     * @param message The question or task
     * @return The formatted prompt
     */
    public static String buildZeroShotPrompt(String message) {
        return message; // Zero-shot is just the direct question!
    }

    /**
     * Build a few-shot prompt with examples
     *
     * Few-shot prompting provides examples to guide the LLM's output format and style.
     * Works well when:
     * - You need consistent formatting
     * - The task is ambiguous
     * - You want to establish a pattern
     *
     * @param request The few-shot request with examples
     * @return The formatted prompt with examples
     */
    public static String buildFewShotPrompt(FewShotRequest request) {
        StringBuilder prompt = new StringBuilder();

        // Add task description
        prompt.append(request.getTask()).append("\n\n");

        // Add examples
        for (FewShotRequest.Example example : request.getExamples()) {
            prompt.append("Input: \"").append(example.getInput()).append("\"\n");
            prompt.append("Output: ").append(example.getOutput()).append("\n\n");
        }

        // Add the actual input to process
        prompt.append("Input: \"").append(request.getInput()).append("\"\n");
        prompt.append("Output:");

        return prompt.toString();
    }

    /**
     * Build a chain-of-thought prompt
     *
     * Chain-of-thought prompting asks the LLM to show its reasoning step-by-step.
     * Works well when:
     * - Solving math problems
     * - Complex logical reasoning
     * - Multi-step analysis
     * - You need explainability
     *
     * @param request The chain-of-thought request
     * @return The formatted prompt requesting step-by-step reasoning
     */
    public static String buildChainOfThoughtPrompt(ChainOfThoughtRequest request) {
        StringBuilder prompt = new StringBuilder();

        // Add the problem
        prompt.append(request.getProblem()).append("\n\n");

        // Add reasoning instruction if requested
        if (request.getRequestSteps() == null || request.getRequestSteps()) {
            String reasoningPrompt = request.getReasoningPrompt() != null
                    ? request.getReasoningPrompt()
                    : "Let's think through this step by step:";
            prompt.append(reasoningPrompt).append("\n");
        }

        return prompt.toString();
    }

    /**
     * Build a structured output prompt
     *
     * Structured output prompting requests responses in specific formats (JSON, XML, etc.).
     * Works well when:
     * - Integrating with other systems/APIs
     * - Need to parse responses programmatically
     * - Storing extracted data in databases
     * - Building data pipelines
     *
     * @param request The structured output request
     * @return The formatted prompt requesting structured output
     */
    public static String buildStructuredOutputPrompt(StructuredOutputRequest request) {
        StringBuilder prompt = new StringBuilder();

        // Add task description
        prompt.append(request.getTask()).append("\n\n");

        // Add input text
        prompt.append("Text to process:\n");
        prompt.append("\"").append(request.getInputText()).append("\"\n\n");

        // Add format instructions
        String formatType = request.getFormatType() != null ? request.getFormatType() : "JSON";
        prompt.append("Return the result in ").append(formatType).append(" format:\n");
        prompt.append(request.getOutputFormat()).append("\n\n");

        // Add strict formatting instruction
        prompt.append("IMPORTANT: Return ONLY the ").append(formatType);
        prompt.append(" - no additional text, explanations, or markdown formatting.");

        return prompt.toString();
    }

    /**
     * Build a system prompt for consistent behavior
     *
     * System prompts set the overall behavior and personality of the LLM.
     * They apply to the entire conversation, not just one message.
     *
     * @param role The role the LLM should play
     * @param instructions Additional instructions
     * @return The formatted system prompt
     */
    public static String buildSystemPrompt(String role, String instructions) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are ").append(role).append(".");

        if (instructions != null && !instructions.isEmpty()) {
            prompt.append(" ").append(instructions);
        }

        return prompt.toString();
    }
}
