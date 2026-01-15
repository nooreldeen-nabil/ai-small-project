package com.ai.mvp.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Stack;

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
 *
 * Note: Uses a simple expression evaluator since JavaScript engine
 * is not available in Java 21+ by default.
 */
@Slf4j
@Component
public class CalculateTool implements Tool {

    @Override
    public String getName() {
        return "calculate";
    }

    @Override
    public String getDescription() {
        return "Perform mathematical calculations. Supports basic arithmetic (+, -, *, /), " +
               "parentheses for grouping, and decimal numbers. " +
               "Use this when the user asks for mathematical computations or numeric results. " +
               "Examples: '15 * 20', '0.15 * 2500', '(100 - 25) * 1.5', '2026 - 2000'";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "expression", Map.of(
                                "type", "string",
                                "description", "Mathematical expression to evaluate. Supports +, -, *, /, parentheses, and decimal numbers. Examples: '15 * 20', '0.15 * 2500', '(100 - 25) * 1.5'"
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

            // Clean up expression
            expression = expression.trim().replaceAll("\\s+", "");

            // Validate expression (basic security check)
            if (!isExpressionSafe(expression)) {
                return ToolResult.failure("Expression contains unsafe characters. Only +, -, *, /, (), and numbers are allowed.");
            }

            // Evaluate expression
            double result = evaluateExpression(expression);

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
     * Only allow numbers, basic operators, parentheses, decimal point
     */
    private boolean isExpressionSafe(String expression) {
        // Allow: numbers, operators +, -, *, /, parentheses, decimal point
        return expression.matches("[0-9+\\-*/().]+");
    }

    /**
     * Evaluate a mathematical expression using operator precedence
     * Supports +, -, *, /, and parentheses
     */
    private double evaluateExpression(String expression) {
        return evaluate(expression.toCharArray(), 0)[0];
    }

    /**
     * Recursive expression evaluator with operator precedence
     * Returns [result, next_index]
     */
    private double[] evaluate(char[] tokens, int index) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        while (index < tokens.length) {
            char c = tokens[index];

            if (Character.isDigit(c) || c == '.') {
                // Parse number
                StringBuilder sb = new StringBuilder();
                while (index < tokens.length && (Character.isDigit(tokens[index]) || tokens[index] == '.')) {
                    sb.append(tokens[index++]);
                }
                values.push(Double.parseDouble(sb.toString()));
                continue;
            } else if (c == '(') {
                // Recursive evaluation for parentheses
                double[] result = evaluate(tokens, index + 1);
                values.push(result[0]);
                index = (int) result[1];
                continue;
            } else if (c == ')') {
                // End of parentheses group
                break;
            } else if (isOperator(c)) {
                // Handle operator precedence
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(c);
            }

            index++;
        }

        // Apply remaining operators
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        return new double[]{values.pop(), index};
    }

    /**
     * Check if character is an operator
     */
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    /**
     * Check operator precedence
     */
    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    /**
     * Apply an operator to two operands
     */
    private double applyOperator(char operator, double b, double a) {
        switch (operator) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            default: throw new IllegalArgumentException("Unknown operator: " + operator);
        }
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
