package com.ai.mvp.controller;

import com.ai.mvp.dto.prompt.*;
import com.ai.mvp.service.PromptEngineeringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Prompt Engineering demonstrations
 *
 * Phase 2: Prompt Engineering Techniques
 *
 * This controller exposes 4 endpoints, each demonstrating a different prompting technique:
 * 1. Zero-shot - Direct questions
 * 2. Few-shot - Learning from examples
 * 3. Chain-of-thought - Step-by-step reasoning
 * 4. Structured output - JSON/formatted responses
 *
 * Each endpoint shows:
 * - When to use that technique
 * - How to construct the prompt
 * - The actual prompt sent to the LLM
 * - The LLM's response
 *
 * Learning Points:
 * - Different prompting techniques produce different results
 * - Prompt construction is crucial for quality outputs
 * - Different techniques suit different use cases
 * - Seeing the full prompt helps understand how LLMs work
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt")
@RequiredArgsConstructor
@Tag(name = "Phase 2: Prompt Engineering", description = "Different prompting techniques for LLMs")
public class PromptController {

    private final PromptEngineeringService promptService;

    /**
     * POST /api/prompt/zero-shot - Zero-shot prompting
     *
     * The simplest approach - just ask directly without examples.
     *
     * Example Request:
     * {
     *   "message": "Translate 'Hello, how are you?' to French",
     *   "maxTokens": 100,
     *   "temperature": 0.7
     * }
     */
    @PostMapping("/zero-shot")
    @Operation(
            summary = "Zero-shot prompting",
            description = "Direct question without examples. Best for straightforward tasks the LLM knows well."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully received zero-shot response",
                    content = @Content(schema = @Schema(implementation = PromptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            )
    })
    public ResponseEntity<PromptResponse> zeroShot(@Valid @RequestBody PromptRequest request) {
        log.info("Zero-shot prompt request received");
        PromptResponse response = promptService.zeroShot(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/prompt/few-shot - Few-shot prompting with examples
     *
     * Provides examples to guide the LLM's response format.
     *
     * Example Request:
     * {
     *   "task": "Classify product review sentiment",
     *   "examples": [
     *     {"input": "Amazing product!", "output": "Positive"},
     *     {"input": "Terrible quality", "output": "Negative"}
     *   ],
     *   "input": "It's okay, nothing special",
     *   "maxTokens": 50,
     *   "temperature": 0.3
     * }
     */
    @PostMapping("/few-shot")
    @Operation(
            summary = "Few-shot prompting",
            description = "Provide examples to guide output format. Best for consistent formatting and ambiguous tasks."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully received few-shot response",
                    content = @Content(schema = @Schema(implementation = PromptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - check examples format",
                    content = @Content
            )
    })
    public ResponseEntity<PromptResponse> fewShot(@Valid @RequestBody FewShotRequest request) {
        log.info("Few-shot prompt request received with {} examples", request.getExamples().size());
        PromptResponse response = promptService.fewShot(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/prompt/chain-of-thought - Chain-of-thought prompting
     *
     * Asks the LLM to show its reasoning step-by-step.
     *
     * Example Request:
     * {
     *   "problem": "A store has 15 apples. They sell 40% in the morning and 1/3 of the remainder in the afternoon. How many apples are left?",
     *   "requestSteps": true,
     *   "reasoningPrompt": "Let's solve this step by step:",
     *   "maxTokens": 1000,
     *   "temperature": 0.2
     * }
     */
    @PostMapping("/chain-of-thought")
    @Operation(
            summary = "Chain-of-thought prompting",
            description = "Request step-by-step reasoning. Best for math, logic puzzles, and complex analysis."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully received chain-of-thought response",
                    content = @Content(schema = @Schema(implementation = PromptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            )
    })
    public ResponseEntity<PromptResponse> chainOfThought(@Valid @RequestBody ChainOfThoughtRequest request) {
        log.info("Chain-of-thought prompt request received");
        PromptResponse response = promptService.chainOfThought(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/prompt/structured-output - Structured output prompting
     *
     * Requests responses in specific formats (JSON, XML, CSV).
     *
     * Example Request:
     * {
     *   "task": "Extract key information from this job posting",
     *   "inputText": "Senior Java Developer at Tech Corp. 5+ years exp required. Salary: $120k-150k. Location: San Francisco.",
     *   "outputFormat": "{\"position\": \"...\", \"experience\": \"...\", \"salary\": \"...\", \"location\": \"...\"}",
     *   "formatType": "JSON",
     *   "maxTokens": 500,
     *   "temperature": 0.1
     * }
     */
    @PostMapping("/structured-output")
    @Operation(
            summary = "Structured output prompting",
            description = "Request JSON/XML/CSV output. Best for API integration and programmatic parsing."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully received structured output",
                    content = @Content(schema = @Schema(implementation = PromptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - check format specification",
                    content = @Content
            )
    })
    public ResponseEntity<PromptResponse> structuredOutput(@Valid @RequestBody StructuredOutputRequest request) {
        log.info("Structured output prompt request received for format: {}", request.getFormatType());
        PromptResponse response = promptService.structuredOutput(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/prompt/health - Health check for prompt engineering endpoints
     */
    @GetMapping("/health")
    @Operation(
            summary = "Prompt service health check",
            description = "Verify prompt engineering service is ready"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Prompt engineering service is ready! 🚀");
    }
}
