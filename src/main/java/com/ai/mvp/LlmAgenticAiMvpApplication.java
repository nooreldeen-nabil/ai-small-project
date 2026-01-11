package com.ai.mvp;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * Main Spring Boot Application for LLM Agentic AI MVP
 *
 * This application demonstrates four key AI concepts:
 * 1. LLM (Large Language Models) - Multi-provider integration (Gemini FREE, Claude PAID)
 * 2. Prompt Engineering - Different prompting strategies and techniques
 * 3. Vector Database - Oracle 23c AI Vector Search for semantic similarity
 * 4. Agentic AI - Autonomous AI agents with tool use and workflow orchestration
 *
 * @author AI Learning Team
 * @version 1.0.0
 */
@Slf4j
@SpringBootApplication
public class LlmAgenticAiMvpApplication {

    /**
     * Main entry point for the application
     * Loads .env file before starting Spring Boot
     */
    public static void main(String[] args) {
        // Load .env file for local development
        loadEnvironmentVariables();

        // Start Spring Boot application
        SpringApplication.run(LlmAgenticAiMvpApplication.class, args);
    }

    /**
     * Load environment variables from .env file
     * This allows us to keep sensitive data (API keys) out of version control
     */
    private static void loadEnvironmentVariables() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Don't fail if .env doesn't exist (e.g., in production)
                    .load();

            // Set system properties from .env file
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            log.info("✅ Environment variables loaded from .env file");
        } catch (Exception e) {
            log.warn("⚠️ Could not load .env file: {}. Using system environment variables.", e.getMessage());
        }
    }

    /**
     * Event listener that runs after application startup
     * Displays important startup information
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String provider = env.getProperty("llm.provider", "GEMINI");

        log.info("");
        log.info("=".repeat(80));
        log.info("🚀 LLM Agentic AI MVP Application Started Successfully!");
        log.info("=".repeat(80));
        log.info("");
        log.info("📍 Application URLs:");
        log.info("   • Application:    http://localhost:{}{}", port, contextPath);
        log.info("   • API Docs:       http://localhost:{}{}/swagger-ui.html", port, contextPath);
        log.info("   • Health Check:   http://localhost:{}{}/actuator/health", port, contextPath);
        log.info("");
        log.info("📚 Learning Endpoints (Phase 1):");
        log.info("   • POST /api/chat                    - Chat with LLM");
        log.info("   • GET  /api/chat/health             - Health check");
        log.info("");
        log.info("🗄️  Database:");
        log.info("   • Oracle 23c AI Vector Search (prepared, not yet used)");
        log.info("");
        log.info("🤖 AI Provider: {} (Active)", provider);

        // Show active provider details
        if ("GEMINI".equalsIgnoreCase(provider)) {
            log.info("   • Google Gemini API ✅ FREE");
            log.info("   • Model: {}", env.getProperty("gemini.model", "gemini-2.5-flash"));
            log.info("   • Free Tier: 60 req/min, 1500 req/day");
        } else if ("CLAUDE".equalsIgnoreCase(provider)) {
            log.info("   • Anthropic Claude API");
            log.info("   • Model: {}", env.getProperty("anthropic.model", "claude-3-5-sonnet-20241022"));
            log.info("   • Paid Service: Track token usage");
        }

        log.info("");
        log.info("💡 Available Providers:");
        log.info("   • Gemini (Google) - FREE tier available");
        log.info("   • Claude (Anthropic) - High quality, paid");
        log.info("   • Switch via .env: LLM_PROVIDER=GEMINI or CLAUDE");
        log.info("");
        log.info("🔧 Profile: {}", String.join(", ", env.getActiveProfiles().length > 0 ?
                env.getActiveProfiles() : new String[]{"default"}));
        log.info("=".repeat(80));
        log.info("");
    }
}
