package com.ai.mvp.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Workflow Management Service for Camunda Platform 8 (Phase 6)
 *
 * This service provides operations for managing BPMN workflows in Zeebe:
 * - Starting process instances
 * - Querying process status
 * - Canceling running processes
 * - Publishing messages to processes
 *
 * Integration with Camunda 8 components:
 * - Zeebe: Workflow engine (start instances, send messages)
 * - Operate: Monitoring UI (http://localhost:8081)
 * - Tasklist: Human task management (http://localhost:8082)
 *
 * @author AI MVP Team
 * @version 1.0
 * @since Phase 6
 */
@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    @Autowired
    private ZeebeClient zeebeClient;

    /**
     * Starts a simple AI task workflow process.
     *
     * Process ID: simple-ai-task
     * Expected BPMN structure: Start → AI Task (ai-task) → End
     *
     * @param question The user's question for the AI agent
     * @return Process instance key (unique identifier)
     */
    public long startAiTaskProcess(String question) {
        return startAiTaskProcess(question, 10, 0.7);
    }

    /**
     * Starts a simple AI task workflow process with custom settings.
     *
     * @param question The user's question for the AI agent
     * @param maxToolCalls Maximum number of tool calls allowed
     * @param temperature LLM temperature for response generation
     * @return Process instance key (unique identifier)
     */
    public long startAiTaskProcess(String question, Integer maxToolCalls, Double temperature) {
        log.info("🚀 Starting AI task process | Question: {}",
                question.length() > 100 ? question.substring(0, 100) + "..." : question);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("question", question);
            variables.put("maxToolCalls", maxToolCalls);
            variables.put("temperature", temperature);

            ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("simple-ai-task")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            long processInstanceKey = event.getProcessInstanceKey();

            log.info("✅ AI task process started | Process Instance: {} | Process Definition: {}",
                    processInstanceKey,
                    event.getBpmnProcessId()
            );

            return processInstanceKey;

        } catch (Exception e) {
            log.error("❌ Failed to start AI task process | Question: {} | Error: {}",
                    question, e.getMessage(), e);
            throw new RuntimeException("Failed to start AI task process: " + e.getMessage(), e);
        }
    }

    /**
     * Starts a document processing workflow.
     *
     * Process ID: document-processing
     * Expected BPMN structure:
     *   Start → Classify → Decision Gateway → Extract → Save → Notify → End
     *
     * @param documentContent The document text to process
     * @param title The document title
     * @return Process instance key (unique identifier)
     */
    public long startDocumentProcessing(String documentContent, String title) {
        log.info("🚀 Starting document processing workflow | Title: {}", title);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentContent", documentContent);
            variables.put("title", title);

            ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("document-processing")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            long processInstanceKey = event.getProcessInstanceKey();

            log.info("✅ Document processing workflow started | Process Instance: {} | Title: {}",
                    processInstanceKey,
                    title
            );

            return processInstanceKey;

        } catch (Exception e) {
            log.error("❌ Failed to start document processing | Title: {} | Error: {}",
                    title, e.getMessage(), e);
            throw new RuntimeException("Failed to start document processing: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the status of a running process instance.
     *
     * Note: This is a simplified implementation. In production, you would query
     * the Operate API or use Zeebe's gRPC API to get detailed process status.
     *
     * @param processInstanceKey The process instance key
     * @return Map containing process status information
     */
    public Map<String, Object> getProcessStatus(long processInstanceKey) {
        log.debug("📊 Querying process status | Process Instance: {}", processInstanceKey);

        // For now, return basic information
        // In production, you would query Operate API or use Zeebe client to get actual status
        Map<String, Object> status = new HashMap<>();
        status.put("processInstanceKey", processInstanceKey);
        status.put("status", "running");
        status.put("message", "Use Operate UI for detailed status: http://localhost:8081/processes/" + processInstanceKey);

        return status;
    }

    /**
     * Cancels a running process instance.
     *
     * This operation immediately terminates the process, canceling all active jobs
     * and ending all active activities.
     *
     * @param processInstanceKey The process instance key to cancel
     */
    public void cancelProcess(long processInstanceKey) {
        log.info("🛑 Canceling process instance: {}", processInstanceKey);

        try {
            zeebeClient.newCancelInstanceCommand(processInstanceKey)
                    .send()
                    .join();

            log.info("✅ Process instance canceled successfully: {}", processInstanceKey);

        } catch (Exception e) {
            log.error("❌ Failed to cancel process instance: {} | Error: {}",
                    processInstanceKey, e.getMessage(), e);
            throw new RuntimeException("Failed to cancel process: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes a message to correlate with waiting processes.
     *
     * Useful for sending signals or data to processes that are waiting for messages.
     *
     * @param messageName The name of the message
     * @param correlationKey The correlation key to match waiting processes
     * @param variables Variables to send with the message
     */
    public void publishMessage(String messageName, String correlationKey, Map<String, Object> variables) {
        log.info("📨 Publishing message | Name: {} | Correlation Key: {}", messageName, correlationKey);

        try {
            zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)
                    .correlationKey(correlationKey)
                    .variables(variables)
                    .send()
                    .join();

            log.info("✅ Message published successfully");

        } catch (Exception e) {
            log.error("❌ Failed to publish message | Name: {} | Error: {}",
                    messageName, e.getMessage(), e);
            throw new RuntimeException("Failed to publish message: " + e.getMessage(), e);
        }
    }
}
