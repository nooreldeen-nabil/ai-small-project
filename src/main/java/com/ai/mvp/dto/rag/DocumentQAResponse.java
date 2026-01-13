package com.ai.mvp.dto.rag;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for document Q&A (RAG)
 *
 * Phase 4: RAG Implementation
 * Contains the answer, source citations, and metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Document Q&A response with answer and citations")
public class DocumentQAResponse {

    @Schema(description = "The original question", example = "How do neural networks learn?")
    private String question;

    @Schema(description = "The generated answer based on retrieved documents",
            example = "Neural networks learn through a process called backpropagation...")
    private String answer;

    @Schema(description = "List of source citations used to generate the answer")
    private List<Citation> citations;

    @Schema(description = "Confidence level based on similarity scores")
    private ConfidenceLevel confidence;

    @Schema(description = "Number of documents searched", example = "10")
    private Integer documentsSearched;

    @Schema(description = "Number of relevant chunks found", example = "5")
    private Integer relevantChunks;

    @Schema(description = "Total tokens used in LLM request", example = "450")
    private Integer tokensUsed;

    @Schema(description = "Input tokens (context + question)", example = "350")
    private Integer inputTokens;

    @Schema(description = "Output tokens (answer)", example = "100")
    private Integer outputTokens;

    @Schema(description = "LLM provider used", example = "GEMINI")
    private String provider;

    @Schema(description = "Response timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Confidence level enumeration
     * Based on similarity scores of retrieved chunks
     */
    public enum ConfidenceLevel {
        @Schema(description = "High confidence (avg similarity > 0.8)")
        HIGH,

        @Schema(description = "Medium confidence (avg similarity 0.6 - 0.8)")
        MEDIUM,

        @Schema(description = "Low confidence (avg similarity 0.4 - 0.6)")
        LOW,

        @Schema(description = "Very low confidence (avg similarity < 0.4)")
        VERY_LOW,

        @Schema(description = "No relevant documents found")
        NONE
    }
}
