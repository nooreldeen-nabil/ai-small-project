package com.ai.mvp.controller;

import com.ai.mvp.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Workflow Controller for Camunda Platform 8 Integration (Phase 6)
 *
 * REST API endpoints for managing BPMN workflows and process instances.
 *
 * Available Operations:
 * - Start AI Task Workflow
 * - Start Document Processing Workflow
 * - Query Process Status
 * - Cancel Process Instance
 *
 * Camunda 8 UI Endpoints:
 * - Operate (Monitor): http://localhost:8081
 * - Tasklist (Human Tasks): http://localhost:8082
 * - Optimize (Analytics): http://localhost:8083
 *
 * @author AI MVP Team
 * @version 1.0
 * @since Phase 6
 */
@RestController
@RequestMapping("/api/workflow")
@Tag(name = "Workflow Management", description = "Camunda Platform 8 workflow operations")
public class WorkflowController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);

    @Autowired
    private WorkflowService workflowService;

    /**
     * Starts a simple AI task workflow.
     *
     * This endpoint creates a new process instance that executes an AI task using
     * the AgentService from Phase 5. The agent can autonomously use tools to answer
     * the question.
     *
     * Process: simple-ai-task
     * Flow: Start → AI Task (ai-task) → End
     *
     * Example Request:
     * {
     *   "question": "What is the capital of France?",
     *   "maxToolCalls": 10,
     *   "temperature": 0.7
     * }
     *
     * Example Response:
     * {
     *   "processInstanceKey": 2251799813685249,
     *   "message": "Workflow started successfully",
     *   "operateUrl": "http://localhost:8081/processes/2251799813685249"
     * }
     *
     * @param request Map containing question, maxToolCalls (optional), temperature (optional)
     * @return ResponseEntity with process instance key and monitoring URL
     */
    @PostMapping("/start")
    @Operation(summary = "Start AI Task Workflow",
               description = "Creates a workflow instance that executes an AI task using the agent from Phase 5")
    public ResponseEntity<Map<String, Object>> startAiTaskWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String question = (String) request.get("question");
            Integer maxToolCalls = request.containsKey("maxToolCalls")
                    ? ((Number) request.get("maxToolCalls")).intValue()
                    : 10;
            Double temperature = request.containsKey("temperature")
                    ? ((Number) request.get("temperature")).doubleValue()
                    : 0.7;

            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Question is required"
                ));
            }

            log.info("📥 Received AI task workflow request | Question: {}",
                    question.length() > 100 ? question.substring(0, 100) + "..." : question);

            long processInstanceKey = workflowService.startAiTaskProcess(question, maxToolCalls, temperature);

            return ResponseEntity.ok(Map.of(
                    "processInstanceKey", processInstanceKey,
                    "message", "Workflow started successfully",
                    "operateUrl", "http://localhost:8081/processes/" + processInstanceKey,
                    "question", question
            ));

        } catch (Exception e) {
            log.error("❌ Failed to start AI task workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "message", "Failed to start workflow"
            ));
        }
    }

    /**
     * Starts a document processing workflow.
     *
     * This endpoint creates a workflow that processes a document through multiple stages:
     * 1. AI Classification
     * 2. Confidence Check (Gateway)
     * 3. AI Data Extraction
     * 4. Save to Database
     * 5. Send Notification
     *
     * If confidence is low, the workflow waits for human review in Tasklist.
     *
     * Process: document-processing
     *
     * Example Request:
     * {
     *   "title": "Invoice #12345",
     *   "content": "Invoice for services rendered..."
     * }
     *
     * @param request Map containing title and content
     * @return ResponseEntity with process instance key
     */
    @PostMapping("/start-document")
    @Operation(summary = "Start Document Processing Workflow",
               description = "Creates a workflow that processes documents with AI classification and extraction")
    public ResponseEntity<Map<String, Object>> startDocumentWorkflow(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String content = request.get("content");

            if (title == null || content == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Title and content are required"
                ));
            }

            log.info("📥 Received document processing request | Title: {}", title);

            long processInstanceKey = workflowService.startDocumentProcessing(content, title);

            return ResponseEntity.ok(Map.of(
                    "processInstanceKey", processInstanceKey,
                    "message", "Document processing workflow started",
                    "operateUrl", "http://localhost:8081/processes/" + processInstanceKey,
                    "tasklistUrl", "http://localhost:8082",
                    "title", title
            ));

        } catch (Exception e) {
            log.error("❌ Failed to start document processing workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "message", "Failed to start document processing"
            ));
        }
    }

    /**
     * Gets the status of a running process instance.
     *
     * Note: For detailed status, use the Operate UI at http://localhost:8081
     *
     * @param processInstanceKey The process instance key
     * @return ResponseEntity with process status
     */
    @GetMapping("/status/{processInstanceKey}")
    @Operation(summary = "Get Process Status",
               description = "Retrieves the status of a running workflow process")
    public ResponseEntity<Map<String, Object>> getProcessStatus(
            @Parameter(description = "Process instance key")
            @PathVariable long processInstanceKey) {
        try {
            Map<String, Object> status = workflowService.getProcessStatus(processInstanceKey);
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("❌ Failed to get process status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Cancels a running process instance.
     *
     * This operation immediately terminates the process.
     *
     * @param processInstanceKey The process instance key to cancel
     * @return ResponseEntity with cancellation confirmation
     */
    @PostMapping("/cancel/{processInstanceKey}")
    @Operation(summary = "Cancel Process Instance",
               description = "Immediately terminates a running workflow process")
    public ResponseEntity<Map<String, Object>> cancelProcess(
            @Parameter(description = "Process instance key to cancel")
            @PathVariable long processInstanceKey) {
        try {
            log.info("🛑 Canceling process instance: {}", processInstanceKey);

            workflowService.cancelProcess(processInstanceKey);

            return ResponseEntity.ok(Map.of(
                    "message", "Process canceled successfully",
                    "processInstanceKey", processInstanceKey
            ));

        } catch (Exception e) {
            log.error("❌ Failed to cancel process: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Health check endpoint for workflow service.
     *
     * @return ResponseEntity with service status
     */
    @GetMapping("/health")
    @Operation(summary = "Workflow Service Health Check",
               description = "Checks if the workflow service is operational")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Camunda Platform 8 Workflow",
                "operate", "http://localhost:8081",
                "tasklist", "http://localhost:8082",
                "optimize", "http://localhost:8083"
        ));
    }
}
