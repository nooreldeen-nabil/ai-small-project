package com.ai.mvp.config;

/**
 * Enum representing different LLM providers
 *
 * This allows the application to support multiple LLM APIs
 * and switch between them via configuration.
 */
public enum LlmProvider {
    /**
     * Anthropic Claude API
     * Pros: High quality, large context window
     * Cons: Paid only
     */
    CLAUDE,

    /**
     * Google Gemini API
     * Pros: Free tier available, fast
     * Cons: Newer, less battle-tested
     */
    GEMINI,

    /**
     * Ollama (Local LLM)
     * Pros: Completely free, private, no API keys
     * Cons: Requires local setup, slower
     */
    OLLAMA
}
