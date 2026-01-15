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
 * Job worker that sends notifications about document processing completion.
 * <p>
 * In a production system, this would:
 * - Send email notifications
 * - Post to Slack/Teams channels
 * - Trigger webhooks
 * - Update external systems
 * <p>
 * For this MVP, it simulates notifications by:
 * - Logging notification details
 * - Recording notification metadata
 * </p>
 *
 * @since Phase 6
 */
@Component
public class NotificationWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorker.class);

    /**
     * Handles notification jobs (simulated).
     *
     * @param job The activated job from Zeebe
     * @param documentTitle The document title
     * @param category The document category
     * @param documentSaved Whether the document was saved successfully
     * @param documentId The saved document ID
     * @return Map of output variables (notificationSent, notificationChannel)
     */
    @JobWorker(type = "send-notification", autoComplete = true)
    public Map<String, Object> sendNotification(
            final ActivatedJob job,
            @Variable(name = "documentTitle") String documentTitle,
            @Variable(name = "category") String category,
            @Variable(name = "documentSaved") Boolean documentSaved,
            @Variable(name = "documentId") String documentId
    ) {
        long startTime = System.currentTimeMillis();
        log.info("📧 [NotificationWorker] Sending notification for job: {}", job.getKey());

        try {
            // Apply defaults for optional variables
            if (documentTitle == null || documentTitle.isBlank()) {
                documentTitle = "Untitled Document";
            }
            if (category == null || category.isBlank()) {
                category = "UNKNOWN";
            }
            if (documentSaved == null) {
                documentSaved = false;
            }
            if (documentId == null || documentId.isBlank()) {
                documentId = "UNKNOWN";
            }

            // Simulate notification sending
            String notificationMessage = buildNotificationMessage(
                documentTitle, category, documentSaved, documentId
            );

            log.info("📨 Notification Details:");
            log.info("   Title: {}", documentTitle);
            log.info("   Category: {}", category);
            log.info("   Saved: {}", documentSaved);
            log.info("   Document ID: {}", documentId);
            log.info("   Message: {}", notificationMessage);

            // In production: Send actual notification
            // - Email via SendGrid/AWS SES
            // - Slack via webhook
            // - Teams via connector
            // - Custom webhook

            // Simulate notification delay (0.3 seconds)
            Thread.sleep(300);

            String notificationChannel = determineNotificationChannel(category, documentSaved);

            log.info("✅ [NotificationWorker] Notification sent via {} channel", notificationChannel);

            // Return process variables
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("notificationSent", true);
            variables.put("notificationChannel", notificationChannel);
            variables.put("notificationMessage", notificationMessage);
            variables.put("notificationExecutionTimeMs", executionTime);
            variables.put("notificationCompletedAt", Instant.now().toString());

            return variables;

        } catch (Exception e) {
            log.error("❌ [NotificationWorker] Notification failed for job {}: {}",
                     job.getKey(), e.getMessage(), e);

            // Return notification failure
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("notificationSent", false);
            variables.put("notificationChannel", "NONE");
            variables.put("notificationMessage", "Notification failed: " + e.getMessage());
            variables.put("notificationExecutionTimeMs", executionTime);
            variables.put("notificationCompletedAt", Instant.now().toString());

            return variables;
        }
    }

    /**
     * Builds a notification message based on document processing results.
     */
    private String buildNotificationMessage(
            String title, String category, boolean saved, String documentId
    ) {
        if (saved && documentId != null && !documentId.equals("UNKNOWN")) {
            return String.format(
                "✅ Document '%s' processed successfully!\n" +
                "Category: %s\n" +
                "Document ID: %s\n" +
                "Status: Saved and indexed for semantic search",
                title, category, documentId
            );
        } else {
            return String.format(
                "⚠️ Document '%s' processed with issues.\n" +
                "Category: %s\n" +
                "Status: Processing completed but save failed",
                title, category
            );
        }
    }

    /**
     * Determines the appropriate notification channel based on context.
     */
    private String determineNotificationChannel(String category, boolean saved) {
        // In production, route to different channels based on rules
        if (!saved) {
            return "ALERT_CHANNEL";  // High priority for failures
        }

        return switch (category.toUpperCase()) {
            case "TECHNICAL" -> "TECH_TEAM_SLACK";
            case "BUSINESS" -> "BUSINESS_EMAIL";
            case "LEGAL" -> "LEGAL_TEAM_EMAIL";
            default -> "GENERAL_NOTIFICATION";
        };
    }
}
