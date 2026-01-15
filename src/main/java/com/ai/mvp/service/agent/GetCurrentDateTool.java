package com.ai.mvp.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Tool to get current date and time
 *
 * This tool allows the agent to get current system time,
 * useful for time-based queries and calculations.
 *
 * Example use cases:
 * - "What day is it today?"
 * - "What time is it?"
 * - "How many days until Christmas?"
 *
 * Phase 5: Agentic AI - Tool Use
 */
@Slf4j
@Component
public class GetCurrentDateTool implements Tool {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getName() {
        return "get_current_date";
    }

    @Override
    public String getDescription() {
        return "Get the current date and time. Returns the current system time in various formats. " +
               "Use this when the user asks about the current date, time, or day of the week.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "format", Map.of(
                                "type", "string",
                                "description", "Format type: 'datetime' (full date and time), 'date' (date only), 'time' (time only), or 'full' (detailed breakdown)",
                                "enum", new String[]{"datetime", "date", "time", "full"}
                        ),
                        "timezone", Map.of(
                                "type", "string",
                                "description", "Timezone ID (e.g., 'America/New_York', 'Europe/London'). Defaults to system timezone."
                        )
                ),
                "required", new String[]{"format"}
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> args) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Executing get_current_date tool with args: {}", args);

            // Get parameters
            String format = getParameter(args, "format", String.class, "datetime");
            String timezoneStr = getParameter(args, "timezone", String.class, null);

            // Get timezone
            ZoneId timezone = timezoneStr != null ?
                    ZoneId.of(timezoneStr) : ZoneId.systemDefault();

            // Get current time in specified timezone
            LocalDateTime now = LocalDateTime.now(timezone);

            // Format based on requested format
            Object result = switch (format) {
                case "date" -> Map.of(
                        "date", now.format(DATE_FORMATTER),
                        "timezone", timezone.getId()
                );
                case "time" -> Map.of(
                        "time", now.format(TIME_FORMATTER),
                        "timezone", timezone.getId()
                );
                case "full" -> Map.of(
                        "datetime", now.format(DATE_TIME_FORMATTER),
                        "year", now.getYear(),
                        "month", now.getMonthValue(),
                        "monthName", now.getMonth().toString(),
                        "day", now.getDayOfMonth(),
                        "dayOfWeek", now.getDayOfWeek().toString(),
                        "hour", now.getHour(),
                        "minute", now.getMinute(),
                        "second", now.getSecond(),
                        "timezone", timezone.getId()
                );
                default -> Map.of(
                        "datetime", now.format(DATE_TIME_FORMATTER),
                        "timezone", timezone.getId()
                );
            };

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("get_current_date completed successfully in {}ms", executionTime);

            return ToolResult.success(result, executionTime);

        } catch (Exception e) {
            log.error("Error executing get_current_date: {}", e.getMessage(), e);
            return ToolResult.failure("Failed to get current date: " + e.getMessage());
        }
    }
}
