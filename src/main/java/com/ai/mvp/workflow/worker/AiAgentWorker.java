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
 * AI Agent Job Worker for Camunda Platform 8 (Phase 6)
 *
 * This worker handles "ai-task" jobs from Zeebe workflows by delegating to the AgentService
 * from Phase 5. The agent can autonomously use tools to complete complex tasks.
 *
 * Job Type: ai-task
 *
 * Input Variables:
 * - question (String): The user's question or task for the AI agent
 * - maxToolCalls (Integer, optional): Maximum number of tool calls allowed (default: 10)
 * - temperature (Double, optional): LLM temperature for response generation (default: 0.7)
 *
 * Output Variables:
 * - answer (String): The AI agent's answer
 * - tokensUsed (Integer): Total tokens consumed
 * - toolCallCount (Integer): Number of tools called
 * - success (Boolean): Whether the task completed successfully
 * - executionTimeMs (Long): Time taken to execute the task
 *
 * @author AI MVP Team
 * @version 1.0
 * @since Phase 6
 */
@Component
public class AiAgentWorker {

    private static final Logger log = LoggerFactory.getLogger(AiAgentWorker.class);

    @Autowired
    private AgentService agentService;

    /**
     * Handles AI task jobs from Zeebe workflows.
     *
     * This method is automatically invoked when a job with type "ai-task" becomes available.
     * The job is fetched from Zeebe, executed using the AgentService, and completed with results.
     *
     * @param job The activated job from Zeebe
     * @param question The user's question or task
     * @param maxToolCalls Maximum tool calls allowed (optional, defaults to 10)
     * @param temperature LLM temperature (optional, defaults to 0.7)
     * @return Map of process variables to set after job completion
     */
    @JobWorker(type = "ai-task", autoComplete = true)
    public Map<String, Object> handleAiTask(
            final ActivatedJob job,
            @Variable(name = "question") String question,
            @Variable(name = "maxToolCalls") Integer maxToolCalls,
            @Variable(name = "temperature") Double temperature
    ) {
        // Apply default values for optional parameters
        if (maxToolCalls == null) {
            maxToolCalls = 10;
        }
        if (temperature == null) {
            temperature = 0.7;
        }
        long startTime = System.currentTimeMillis();

        log.info("📝 Processing AI task job: {} | Process Instance: {} | Question: {}",
                job.getKey(),
                job.getProcessInstanceKey(),
                question.length() > 100 ? question.substring(0, 100) + "..." : question
        );

        try {
            // Create agent task request using Phase 5 AgentService
            AgentTaskRequest request = new AgentTaskRequest();
            request.setTask(question);
            request.setMaxToolCalls(maxToolCalls);
            request.setTemperature(temperature);

            log.debug("🤖 Calling AgentService with maxToolCalls={}, temperature={}",
                    maxToolCalls, temperature);

            // Execute the AI task
            AgentTaskResponse response = agentService.executeTask(request);

            long executionTime = System.currentTimeMillis() - startTime;

            // Prepare output variables for the workflow
            Map<String, Object> variables = new HashMap<>();
            variables.put("answer", response.getAnswer());
            variables.put("tokensUsed", response.getTotalTokens());
            variables.put("toolCallCount", response.getToolCallCount());
            variables.put("success", response.isSuccess());
            variables.put("executionTimeMs", executionTime);
            variables.put("completedAt", Instant.now().toString());

            log.info("✅ AI task completed successfully | Job: {} | Execution: {}ms | Tools: {} | Tokens: {}",
                    job.getKey(),
                    executionTime,
                    response.getToolCallCount(),
                    response.getTotalTokens()
            );

            log.debug("📤 Answer: {}",
                    response.getAnswer().length() > 200
                        ? response.getAnswer().substring(0, 200) + "..."
                        : response.getAnswer()
            );

            return variables;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            log.error("❌ AI task failed | Job: {} | Error: {} | Execution: {}ms",
                    job.getKey(),
                    e.getMessage(),
                    executionTime,
                    e
            );

            // Return error information as variables
            Map<String, Object> errorVariables = new HashMap<>();
            errorVariables.put("success", false);
            errorVariables.put("answer", "Error: " + e.getMessage());
            errorVariables.put("error", e.getMessage());
            errorVariables.put("executionTimeMs", executionTime);
            errorVariables.put("completedAt", Instant.now().toString());

            // Throw exception to create an incident in Zeebe
            // This allows for retry logic and monitoring in Operate
            throw new RuntimeException("AI task execution failed: " + e.getMessage(), e);
        }
    }
}
