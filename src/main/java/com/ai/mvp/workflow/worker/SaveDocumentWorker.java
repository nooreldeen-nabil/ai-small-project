package com.ai.mvp.workflow.worker;

import com.ai.mvp.dto.vector.DocumentUploadRequest;
import com.ai.mvp.service.DocumentService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Job worker that saves processed documents to the vector database.
 * <p>
 * This worker integrates with Phase 3 (Vector Database) to:
 * - Store document content
 * - Generate embeddings
 * - Chunk text for semantic search
 * - Store metadata (category, confidence, extracted data)
 * </p>
 *
 * @see DocumentService Phase 3 document storage
 * @since Phase 6
 */
@Component
public class SaveDocumentWorker {

    private static final Logger log = LoggerFactory.getLogger(SaveDocumentWorker.class);

    @Autowired
    private DocumentService documentService;

    /**
     * Handles document saving jobs.
     *
     * @param job The activated job from Zeebe
     * @param documentContent The document content to save
     * @param documentTitle The document title
     * @param category The classified category
     * @param extractedData The extracted data summary
     * @return Map of output variables (documentId, saved)
     */
    @JobWorker(type = "save-document", autoComplete = true)
    public Map<String, Object> saveDocument(
            final ActivatedJob job,
            @Variable(name = "documentContent") String documentContent,
            @Variable(name = "documentTitle") String documentTitle,
            @Variable(name = "category") String category,
            @Variable(name = "extractedData") String extractedData
    ) {
        long startTime = System.currentTimeMillis();
        log.info("💾 [SaveDocumentWorker] Starting document save for job: {}", job.getKey());

        try {
            // Apply defaults for optional variables
            if (documentTitle == null || documentTitle.isBlank()) {
                documentTitle = "Untitled Document - " + Instant.now().toString();
            }
            if (category == null || category.isBlank()) {
                category = "GENERAL";
            }
            if (extractedData == null) {
                extractedData = "";
            }

            // Prepare document for storage using Phase 3 DocumentService
            DocumentUploadRequest uploadRequest = new DocumentUploadRequest();
            uploadRequest.setTitle(documentTitle);
            uploadRequest.setContent(documentContent);
            uploadRequest.setCategory(category);

            log.debug("📤 Uploading document to vector database...");

            // Save document to vector database (Phase 3)
            // This will:
            // 1. Store document in Oracle DB
            // 2. Chunk text with overlap
            // 3. Generate embeddings via Gemini API
            // 4. Store chunks with vector embeddings for semantic search
            var documentResponse = documentService.uploadDocument(uploadRequest);

            Long documentId = documentResponse.getDocumentId();

            log.info("✅ [SaveDocumentWorker] Document saved successfully - ID: {}, Chunks: {}",
                     documentId, documentResponse.getChunksCreated());

            // Return process variables
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentId", documentId);
            variables.put("documentSaved", true);
            variables.put("chunksCreated", documentResponse.getChunksCreated());
            variables.put("saveExecutionTimeMs", executionTime);
            variables.put("saveCompletedAt", Instant.now().toString());

            return variables;

        } catch (Exception e) {
            log.error("❌ [SaveDocumentWorker] Save failed for job {}: {}",
                     job.getKey(), e.getMessage(), e);

            // Return save failure
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentId", -1L);
            variables.put("documentSaved", false);
            variables.put("chunksCreated", 0);
            variables.put("saveError", e.getMessage());
            variables.put("saveExecutionTimeMs", executionTime);
            variables.put("saveCompletedAt", Instant.now().toString());

            return variables;
        }
    }
}
