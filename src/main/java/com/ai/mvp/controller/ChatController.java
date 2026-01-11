package com.ai.mvp.controller;

import com.ai.mvp.dto.ChatRequest;
import com.ai.mvp.dto.ChatResponse;
import com.ai.mvp.service.ChatService;
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
 * REST Controller for chat functionality
 *
 * Phase 1: Simple Chat with Claude
 *
 * This controller exposes a single endpoint that allows users to:
 * 1. Send a message to Claude
 * 2. Get Claude's response
 * 3. See metadata (tokens used, model, etc.)
 *
 * Learning Points:
 * - How to create REST endpoints with Spring Boot
 * - How to validate input (@Valid)
 * - How to document APIs with Swagger/OpenAPI
 * - How to structure responses
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Phase 1: LLM Integration", description = "Simple chat with Claude API")
public class ChatController {

    private final ChatService chatService;

    /**
     * POST /api/chat - Send a message to Claude
     *
     * This is the main endpoint for Phase 1.
     *
     * Example Request:
     * {
     *   "message": "What is a vector database?",
     *   "systemPrompt": "You are a helpful AI tutor",
     *   "temperature": 0.7,
     *   "maxTokens": 1000
     * }
     *
     * Example Response:
     * {
     *   "response": "A vector database is...",
     *   "model": "claude-3-5-sonnet-20241022",
     *   "tokensUsed": 250,
     *   "inputTokens": 50,
     *   "outputTokens": 200,
     *   "timestamp": "2026-01-11T10:30:00",
     *   "stopReason": "end_turn"
     * }
     *
     * @param request The user's message and parameters
     * @return Claude's response with metadata
     */
    @PostMapping("/chat")
    @Operation(
            summary = "Chat with Claude",
            description = "Send a message to Claude and receive a response. " +
                    "This endpoint demonstrates basic LLM integration. " +
                    "Only 'message' field is required, others are optional."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully received response from Claude",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request (e.g., empty message)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid API key",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error or Anthropic API unavailable",
                    content = @Content
            )
    })
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request - Message preview: {}...",
                request.getMessage().substring(0, Math.min(50, request.getMessage().length())));

        // Call service to process the chat
        ChatResponse response = chatService.chat(request);

        // Calculate and log approximate cost
        double cost = chatService.calculateCost(response.getInputTokens(), response.getOutputTokens());
        log.info("Request completed - Cost: ${}, Tokens: {}",
                String.format("%.6f", cost), response.getTokensUsed());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/chat/health - Simple health check for chat functionality
     *
     * Use this to verify the chat service is configured correctly
     * without making an actual API call to Claude.
     */
    @GetMapping("/chat/health")
    @Operation(
            summary = "Chat service health check",
            description = "Verify chat service is configured and ready"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat service is ready! 🚀");
    }
}
