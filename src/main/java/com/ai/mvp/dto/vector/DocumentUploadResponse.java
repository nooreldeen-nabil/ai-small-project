package com.ai.mvp.dto.vector;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO after uploading a document
 *
 * Phase 3: Vector Database & Embeddings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after document upload and processing")
public class DocumentUploadResponse {

    @Schema(description = "Document ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String documentId;

    @Schema(description = "Document title", example = "Introduction to LLMs")
    private String title;

    @Schema(description = "Number of chunks created", example = "12")
    private Integer chunksCreated;

    @Schema(description = "Vector dimension size", example = "768")
    private Integer vectorDimension;

    @Schema(description = "Processing status", example = "SUCCESS")
    private String status;

    @Schema(description = "Processing message", example = "Document successfully processed and stored")
    private String message;

    @Schema(description = "Upload timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
