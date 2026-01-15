package com.ai.mvp.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

/**
 * Tool to perform mathematical calculations
 *
 * This tool allows the agent to perform mathematical operations,
 * useful for numeric queries and computations.
 *
 * Example use cases:
 * - "What is 15% of 200?"
 * - "Calculate the square root of 144"
 * - "How much is 1500 * 0.8?"
 *
 * Phase 5: Agentic AI - Tool Use
 */
@Slf4j
@Component
public class CalculateTool implements Tool {

    private final ScriptEngine scriptEngine;

    public CalculateTool() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.scriptEngine = manager.getEngineByName("JavaScript");
    }

    @Override
    public String getName() {
        return "calculate";
    }

    @Override
    public String getDescription() {
        return "Perform mathematical calculations. Supports basic arithmetic (+, -, *, /), " +
               "exponentiation (Math.pow), square root (Math.sqrt), and other Math functions. " +
               "Use this when the user asks for mathematical computations or numeric results.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "expression", Map.of(
                                "type", "string",
                                "description", "Mathematical expression to evaluate. Examples: '15 * 20', 'Math.sqrt(144)', 'Math.pow(2, 8)', '(100 - 25) * 1.5'"
                        )
                ),
                "required", new String[]{"expression"}
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> args) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Executing calculate tool with args: {}", args);

            // Get expression
            String expression = getParameter(args, "expression", String.class, null);
            if (expression == null || expression.isEmpty()) {
                return ToolResult.failure("Expression is required");
            }

            // Validate expression (basic security check)
            if (!isExpressionSafe(expression)) {
                return ToolResult.failure("Expression contains unsafe characters or operations");
            }

            // Evaluate expression
            Object evalResult = scriptEngine.eval(expression);

            // Convert result to number
            double result;
            if (evalResult instanceof Number) {
                result = ((Number) evalResult).doubleValue();
            } else {
                return ToolResult.failure("Evaluation did not produce a numeric result");
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("calculate completed successfully in {}ms - Result: {}", executionTime, result);

            return ToolResult.success(Map.of(
                    "expression", expression,
                    "result", result,
                    "formatted", formatResult(result)
            ), executionTime);

        } catch (Exception e) {
            log.error("Error executing calculate: {}", e.getMessage(), e);
            return ToolResult.failure("Failed to calculate: " + e.getMessage());
        }
    }

    /**
     * Basic security check for expression
     * Only allow numbers, basic operators, Math functions, parentheses, and whitespace
     */
    private boolean isExpressionSafe(String expression) {
        // Allow: numbers, operators, Math., parentheses, whitespace, dot, comma
        return expression.matches("[0-9+\\-*/().\\s,Math]+");
    }

    /**
     * Format result nicely
     */
    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.format("%.2f", result);
        }
    }
}
