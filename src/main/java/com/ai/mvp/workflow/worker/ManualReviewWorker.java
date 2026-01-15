package com.ai.mvp.workflow.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Job worker that simulates manual review for low-confidence documents.
 * <p>
 * In a production system, this would:
 * - Create a human task in Camunda Tasklist
 * - Send notifications to reviewers
 * - Wait for human input
 * <p>
 * For this MVP, it simulates manual review by:
 * - Logging the review request
 * - Automatically approving after a short delay
 * - Adding review metadata
 * </p>
 *
 * @since Phase 6
 */
@Component
public class ManualReviewWorker {

    private static final Logger log = LoggerFactory.getLogger(ManualReviewWorker.class);

    /**
     * Handles manual review jobs (simulated).
     *
     * @param job The activated job from Zeebe
     * @param documentContent The document content requiring review
     * @param category The classified category
     * @param confidence The classification confidence
     * @return Map of output variables (reviewStatus, reviewerNotes)
     */
    @JobWorker(type = "manual-review", autoComplete = true)
    public Map<String, Object> requestManualReview(
            final ActivatedJob job,
            @Variable(name = "documentContent") String documentContent,
            @Variable(name = "category") String category,
            @Variable(name = "confidence") Double confidence
    ) {
        long startTime = System.currentTimeMillis();
        log.info("👤 [ManualReviewWorker] Manual review requested for job: {}", job.getKey());

        try {
            // Apply defaults for optional variables
            if (category == null || category.isBlank()) {
                category = "UNKNOWN";
            }
            if (confidence == null) {
                confidence = 0.0;
            }

            // Simulate manual review process
            log.info("📝 Document requires manual review - Category: {}, Confidence: {}",
                     category, confidence);
            log.info("📄 Document preview: {}...",
                     documentContent != null && documentContent.length() > 100
                         ? documentContent.substring(0, 100)
                         : documentContent);

            // In production: Create task in Camunda Tasklist for human reviewer
            // For MVP: Simulate review with automatic approval

            // Simulate review delay (0.5 seconds)
            Thread.sleep(500);

            // Simulate reviewer decision (auto-approve for MVP)
            String reviewStatus = "APPROVED";
            String reviewerNotes = "Simulated manual review - auto-approved for MVP demonstration";

            log.info("✅ [ManualReviewWorker] Manual review complete - Status: {}", reviewStatus);

            // Return process variables
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("reviewStatus", reviewStatus);
            variables.put("reviewerNotes", reviewerNotes);
            variables.put("reviewedBy", "MVP_AUTO_REVIEWER");
            variables.put("reviewExecutionTimeMs", executionTime);
            variables.put("reviewCompletedAt", Instant.now().toString());

            // After manual review, set extracted data to placeholder
            variables.put("extractedData", "Manual review completed. Data extraction pending.");
            variables.put("dataQuality", 0.7);  // Assumed quality after manual review

            return variables;

        } catch (Exception e) {
            log.error("❌ [ManualReviewWorker] Manual review failed for job {}: {}",
                     job.getKey(), e.getMessage(), e);

            // Return rejection fallback
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("reviewStatus", "REJECTED");
            variables.put("reviewerNotes", "Review failed: " + e.getMessage());
            variables.put("reviewedBy", "SYSTEM");
            variables.put("reviewExecutionTimeMs", executionTime);
            variables.put("reviewCompletedAt", Instant.now().toString());
            variables.put("extractedData", "Review failed");
            variables.put("dataQuality", 0.0);

            return variables;
        }
    }
}
