package com.ai.mvp.workflow.worker;

import com.ai.mvp.dto.agent.AgentTaskRequest;
import com.ai.mvp.dto.agent.AgentTaskResponse;
import com.ai.mvp.service.AgentService;
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
 * Job worker that classifies documents using AI Agent.
 * <p>
 * This worker is triggered by the "classify-document" task in BPMN workflows.
 * It uses the AI Agent from Phase 5 to analyze document content and determine:
 * - Document category (e.g., "technical", "business", "legal")
 * - Classification confidence (0.0 - 1.0)
 * - Processing recommendations
 * </p>
 *
 * @see AgentService
 * @since Phase 6
 */
@Component
public class ClassificationWorker {

    private static final Logger log = LoggerFactory.getLogger(ClassificationWorker.class);

    @Autowired
    private AgentService agentService;

    /**
     * Handles document classification jobs.
     *
     * @param job The activated job from Zeebe
     * @param documentContent The document content to classify
     * @param documentTitle Optional document title
     * @return Map of output variables (category, confidence, recommendation)
     */
    @JobWorker(type = "classify-document", autoComplete = true)
    public Map<String, Object> classifyDocument(
            final ActivatedJob job,
            @Variable(name = "documentContent") String documentContent,
            @Variable(name = "documentTitle") String documentTitle
    ) {
        long startTime = System.currentTimeMillis();
        log.info("🔍 [ClassificationWorker] Starting classification for job: {}", job.getKey());

        try {
            // Apply default for optional variables
            if (documentTitle == null || documentTitle.isBlank()) {
                documentTitle = "Untitled Document";
            }

            // Construct classification task for AI Agent
            String classificationTask = buildClassificationPrompt(documentTitle, documentContent);

            // Call AI Agent from Phase 5
            AgentTaskRequest agentRequest = new AgentTaskRequest();
            agentRequest.setTask(classificationTask);
            agentRequest.setMaxToolCalls(5);  // Allow tool use for classification
            agentRequest.setTemperature(0.3);  // Lower temperature for more consistent classification

            log.debug("🤖 Calling AI Agent for classification...");
            AgentTaskResponse agentResponse = agentService.executeTask(agentRequest);

            // Parse AI response to extract classification and confidence
            ClassificationResult result = parseClassificationResult(agentResponse.getAnswer());

            log.info("✅ [ClassificationWorker] Classification complete - Category: {}, Confidence: {}",
                     result.category, result.confidence);

            // Return process variables for next steps
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("category", result.category);
            variables.put("confidence", result.confidence);
            variables.put("classificationSummary", result.summary);
            variables.put("classificationExecutionTimeMs", executionTime);
            variables.put("classificationCompletedAt", Instant.now().toString());
            variables.put("agentTokensUsed", agentResponse.getTotalTokens());

            return variables;

        } catch (Exception e) {
            log.error("❌ [ClassificationWorker] Classification failed for job {}: {}",
                     job.getKey(), e.getMessage(), e);

            // Return low confidence fallback
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("category", "UNKNOWN");
            variables.put("confidence", 0.0);
            variables.put("classificationSummary", "Classification failed: " + e.getMessage());
            variables.put("classificationExecutionTimeMs", executionTime);
            variables.put("classificationCompletedAt", Instant.now().toString());
            variables.put("agentTokensUsed", 0);

            return variables;
        }
    }

    /**
     * Builds a structured prompt for document classification.
     */
    private String buildClassificationPrompt(String title, String content) {
        // Truncate content if too long (max 2000 chars for classification)
        String truncatedContent = content.length() > 2000
            ? content.substring(0, 2000) + "..."
            : content;

        return String.format("""
            Classify the following document and provide:
            1. Category (technical, business, legal, medical, general, or other specific category)
            2. Confidence score (0.0 to 1.0) indicating how certain you are
            3. Brief summary (1-2 sentences)

            Document Title: %s

            Document Content:
            %s

            Respond in this exact format:
            Category: [category name]
            Confidence: [0.0-1.0]
            Summary: [brief summary]
            """, title, truncatedContent);
    }

    /**
     * Parses the AI Agent's classification response.
     */
    private ClassificationResult parseClassificationResult(String aiResponse) {
        ClassificationResult result = new ClassificationResult();

        try {
            // Parse structured response
            String[] lines = aiResponse.split("\n");

            for (String line : lines) {
                line = line.trim();

                if (line.startsWith("Category:")) {
                    result.category = line.substring("Category:".length()).trim().toUpperCase();
                } else if (line.startsWith("Confidence:")) {
                    String confidenceStr = line.substring("Confidence:".length()).trim();
                    result.confidence = Double.parseDouble(confidenceStr);
                } else if (line.startsWith("Summary:")) {
                    result.summary = line.substring("Summary:".length()).trim();
                }
            }

            // Validate and apply defaults
            if (result.category == null || result.category.isBlank()) {
                result.category = "GENERAL";
            }
            if (result.confidence < 0.0 || result.confidence > 1.0) {
                result.confidence = 0.5; // Default to medium confidence
            }
            if (result.summary == null || result.summary.isBlank()) {
                result.summary = "No summary available";
            }

        } catch (Exception e) {
            log.warn("⚠️ Failed to parse structured response, using fallback classification", e);
            // Fallback: Use entire response as summary
            result.category = "GENERAL";
            result.confidence = 0.6;
            result.summary = aiResponse.length() > 200
                ? aiResponse.substring(0, 200) + "..."
                : aiResponse;
        }

        return result;
    }

    /**
     * Internal class to hold classification results.
     */
    private static class ClassificationResult {
        String category = "GENERAL";
        double confidence = 0.5;
        String summary = "";
    }
}
