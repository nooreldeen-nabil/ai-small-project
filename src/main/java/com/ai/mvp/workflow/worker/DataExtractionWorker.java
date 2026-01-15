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
 * Job worker that extracts structured data from documents using AI Agent.
 * <p>
 * This worker is triggered by the "extract-data" task in BPMN workflows.
 * It uses the AI Agent from Phase 5 to extract key information such as:
 * - Named entities (people, organizations, locations, dates)
 * - Key topics and themes
 * - Important facts and figures
 * - Action items and deadlines
 * </p>
 *
 * @see AgentService
 * @since Phase 6
 */
@Component
public class DataExtractionWorker {

    private static final Logger log = LoggerFactory.getLogger(DataExtractionWorker.class);

    @Autowired
    private AgentService agentService;

    /**
     * Handles data extraction jobs.
     *
     * @param job The activated job from Zeebe
     * @param documentContent The document content to extract data from
     * @param category The document category from classification
     * @return Map of output variables (extractedData, dataQuality)
     */
    @JobWorker(type = "extract-data", autoComplete = true)
    public Map<String, Object> extractData(
            final ActivatedJob job,
            @Variable(name = "documentContent") String documentContent,
            @Variable(name = "category") String category
    ) {
        long startTime = System.currentTimeMillis();
        log.info("📊 [DataExtractionWorker] Starting data extraction for job: {}", job.getKey());

        try {
            // Apply default for optional variables
            if (category == null || category.isBlank()) {
                category = "GENERAL";
            }

            // Construct extraction task for AI Agent
            String extractionTask = buildExtractionPrompt(category, documentContent);

            // Call AI Agent from Phase 5
            AgentTaskRequest agentRequest = new AgentTaskRequest();
            agentRequest.setTask(extractionTask);
            agentRequest.setMaxToolCalls(5);  // Allow tool use if needed
            agentRequest.setTemperature(0.2);  // Very low temperature for factual extraction

            log.debug("🤖 Calling AI Agent for data extraction...");
            AgentTaskResponse agentResponse = agentService.executeTask(agentRequest);

            // Parse extracted data
            String extractedData = agentResponse.getAnswer();
            double dataQuality = calculateDataQuality(extractedData);

            log.info("✅ [DataExtractionWorker] Extraction complete - Quality: {}", dataQuality);

            // Return process variables
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("extractedData", extractedData);
            variables.put("dataQuality", dataQuality);
            variables.put("extractionExecutionTimeMs", executionTime);
            variables.put("extractionCompletedAt", Instant.now().toString());
            variables.put("extractionTokensUsed", agentResponse.getTotalTokens());

            return variables;

        } catch (Exception e) {
            log.error("❌ [DataExtractionWorker] Extraction failed for job {}: {}",
                     job.getKey(), e.getMessage(), e);

            // Return empty extraction with error
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> variables = new HashMap<>();
            variables.put("extractedData", "Extraction failed: " + e.getMessage());
            variables.put("dataQuality", 0.0);
            variables.put("extractionExecutionTimeMs", executionTime);
            variables.put("extractionCompletedAt", Instant.now().toString());
            variables.put("extractionTokensUsed", 0);

            return variables;
        }
    }

    /**
     * Builds a structured prompt for data extraction based on document category.
     */
    private String buildExtractionPrompt(String category, String content) {
        // Truncate content if too long (max 3000 chars for extraction)
        String truncatedContent = content.length() > 3000
            ? content.substring(0, 3000) + "..."
            : content;

        // Category-specific extraction instructions
        String extractionInstructions = switch (category.toUpperCase()) {
            case "TECHNICAL" -> """
                Extract:
                - Technologies and tools mentioned
                - Technical specifications and requirements
                - Version numbers and dependencies
                - Architecture components
                - Performance metrics
                """;
            case "BUSINESS" -> """
                Extract:
                - Key business objectives
                - Stakeholders and organizations
                - Financial figures and budgets
                - Timelines and milestones
                - Risks and opportunities
                """;
            case "LEGAL" -> """
                Extract:
                - Parties involved
                - Legal terms and clauses
                - Dates and deadlines
                - Obligations and responsibilities
                - Regulatory references
                """;
            default -> """
                Extract:
                - Key people, organizations, and locations
                - Important dates and deadlines
                - Main topics and themes
                - Facts and figures
                - Action items
                """;
        };

        return String.format("""
            Extract structured information from the following %s document.

            %s

            Document Content:
            %s

            Provide a structured summary with clear sections and bullet points.
            """, category, extractionInstructions, truncatedContent);
    }

    /**
     * Calculates data quality score based on extraction completeness.
     */
    private double calculateDataQuality(String extractedData) {
        if (extractedData == null || extractedData.isBlank()) {
            return 0.0;
        }

        // Simple heuristic: longer, structured responses = higher quality
        int length = extractedData.length();
        int bulletPoints = countOccurrences(extractedData, "- ");
        int sections = countOccurrences(extractedData, ":");

        // Quality score based on content richness
        double lengthScore = Math.min(length / 500.0, 1.0);  // Max at 500 chars
        double structureScore = Math.min((bulletPoints + sections) / 10.0, 1.0);  // Max at 10 items

        return (lengthScore * 0.6 + structureScore * 0.4);  // Weighted average
    }

    /**
     * Counts occurrences of a substring.
     */
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
