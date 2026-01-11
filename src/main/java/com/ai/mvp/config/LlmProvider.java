package com.ai.mvp.config;

/**
 * Enum representing different LLM providers
 *
 * This allows the application to support multiple LLM APIs
 * and switch between them via configuration.
 */
public enum LlmProvider {
    /**
     * Google Gemini API (PRIMARY - FREE)
     * Pros: FREE tier (60 req/min, 1500 req/day), fast, 1M+ token context
     * Cons: Newer provider (but excellent quality)
     * Best for: Learning, development, MVP projects
     */
    GEMINI,

    /**
     * Anthropic Claude API (OPTIONAL - PAID)
     * Pros: High quality reasoning, 200K context window, battle-tested
     * Cons: Paid only (no free tier)
     * Best for: Production, high-quality requirements
     */
    CLAUDE,

    /**
     * Ollama (Local LLM)
     * Pros: Completely free, private, no API keys
     * Cons: Requires local setup, slower, lower quality
     * Best for: Offline development, privacy-critical apps
     */
    OLLAMA
}
