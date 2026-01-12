package com.ai.mvp.service;

import com.ai.mvp.config.GeminiConfig;
import com.ai.mvp.exception.LlmApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating vector embeddings using Google Gemini's text-embedding-004 model
 *
 * Phase 3: Vector Database & Embeddings
 *
 * What are embeddings?
 * ==================
 * Embeddings are numerical representations (vectors) of text that capture semantic meaning.
 * Words or sentences with similar meanings have similar vectors.
 *
 * Example:
 * - "cat" and "kitten" will have very similar embeddings
 * - "cat" and "car" will have very different embeddings
 *
 * Why use embeddings?
 * ==================
 * 1. Semantic Search: Find documents by meaning, not just keywords
 * 2. RAG (Retrieval Augmented Generation): Provide relevant context to LLMs
 * 3. Similarity Detection: Find duplicate or similar content
 * 4. Recommendation Systems: Recommend similar items
 *
 * Gemini text-embedding-004:
 * ==========================
 * - Dimensions: 768 (float32 values)
 * - Max input: 2048 tokens (~1500 words)
 * - Free tier: 1500 requests/day
 * - Best for: English text, but supports 100+ languages
 *
 * API Documentation:
 * https://ai.google.dev/api/embeddings
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final GeminiConfig config;
    private final ObjectMapper objectMapper;

    private OkHttpClient httpClient;

    // Gemini embedding model details
    private static final String EMBEDDING_MODEL = "text-embedding-004";
    private static final String EMBEDDING_API_PATH = "/models/" + EMBEDDING_MODEL + ":embedContent";
    private static final int EMBEDDING_DIMENSIONS = 768;

    /**
     * Initialize HTTP client after bean construction
     */
    @PostConstruct
    public void init() {
        // Create HTTP client with timeout settings
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                .build();

        log.info("✅ Embedding Service initialized with model: {}", EMBEDDING_MODEL);
    }

    /**
     * Generate vector embedding for a single text
     *
     * This method converts text into a 768-dimensional vector that represents its semantic meaning.
     *
     * Use cases:
     * - Embedding user queries for semantic search
     * - Embedding document chunks for storage
     * - Comparing text similarity
     *
     * @param text The text to embed (max ~1500 words)
     * @return A float array of 768 dimensions
     * @throws LlmApiException if the API call fails
     */
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        try {
            log.debug("Generating embedding for text of length: {} characters", text.length());

            // Build the request payload
            // Format: {"model": "models/text-embedding-004", "content": {"parts": [{"text": "text here"}]}}
            String requestJson = buildEmbeddingRequest(text);
            log.trace("Embedding request JSON: {}", requestJson);

            // Build the URL with API key as query parameter
            String url = String.format("%s%s?key=%s",
                    config.getApiUrl(),
                    EMBEDDING_API_PATH,
                    config.getApiKey());

            // Build HTTP request
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                // Check if request was successful
                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody);
                }

                // Parse response and extract embedding
                float[] embedding = parseEmbeddingResponse(responseBody);

                log.debug("Successfully generated embedding with {} dimensions", embedding.length);
                return embedding;
            }

        } catch (IOException e) {
            log.error("Network error calling Gemini Embedding API: {}", e.getMessage(), e);
            throw new LlmApiException("Network error calling Gemini Embedding API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error generating embedding: {}", e.getMessage(), e);
            throw new LlmApiException("Unexpected error generating embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embeddings for multiple texts in batch
     *
     * Note: Gemini doesn't support true batch embedding in one request,
     * so this method calls the API multiple times. For production,
     * consider implementing rate limiting or queuing.
     *
     * @param texts List of texts to embed
     * @return List of embeddings in the same order as input texts
     */
    public List<float[]> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("Texts list cannot be null or empty");
        }

        log.debug("Generating embeddings for {} texts", texts.size());

        List<float[]> embeddings = new ArrayList<>();
        int count = 0;

        for (String text : texts) {
            count++;
            log.debug("Processing text {}/{}", count, texts.size());

            float[] embedding = generateEmbedding(text);
            embeddings.add(embedding);

            // Add a small delay to avoid rate limiting (60 requests/minute = 1 per second)
            if (count < texts.size()) {
                try {
                    Thread.sleep(100); // 100ms delay between requests
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while generating embeddings", e);
                }
            }
        }

        log.debug("Successfully generated {} embeddings", embeddings.size());
        return embeddings;
    }

    /**
     * Build the JSON request payload for Gemini embedding API
     *
     * Request format:
     * {
     *   "model": "models/text-embedding-004",
     *   "content": {
     *     "parts": [{"text": "text here"}]
     *   }
     * }
     */
    private String buildEmbeddingRequest(String text) throws IOException {
        // Create request object manually to match exact API format
        var request = new java.util.HashMap<String, Object>();
        request.put("model", "models/" + EMBEDDING_MODEL);

        var parts = new java.util.ArrayList<java.util.HashMap<String, String>>();
        var part = new java.util.HashMap<String, String>();
        part.put("text", text);
        parts.add(part);

        var content = new java.util.HashMap<String, Object>();
        content.put("parts", parts);

        request.put("content", content);

        return objectMapper.writeValueAsString(request);
    }

    /**
     * Parse the embedding response from Gemini API
     *
     * Response format:
     * {
     *   "embedding": {
     *     "values": [0.1, 0.2, 0.3, ...]
     *   }
     * }
     */
    private float[] parseEmbeddingResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode valuesNode = root.path("embedding").path("values");

        if (valuesNode.isMissingNode() || !valuesNode.isArray()) {
            throw new LlmApiException("Invalid embedding response format: missing 'embedding.values' array");
        }

        // Convert JSON array to float array
        float[] embedding = new float[valuesNode.size()];
        for (int i = 0; i < valuesNode.size(); i++) {
            embedding[i] = (float) valuesNode.get(i).asDouble();
        }

        // Validate embedding dimensions
        if (embedding.length != EMBEDDING_DIMENSIONS) {
            log.warn("Expected {} dimensions but got {}", EMBEDDING_DIMENSIONS, embedding.length);
        }

        return embedding;
    }

    /**
     * Convert float array to JSON string format for database storage
     *
     * Oracle VECTOR type expects format: "[0.1, 0.2, 0.3, ...]"
     *
     * @param embedding The float array embedding
     * @return JSON string representation
     */
    public String embeddingToString(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("Embedding cannot be null or empty");
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Get the embedding model dimensions
     *
     * @return Number of dimensions (768 for text-embedding-004)
     */
    public int getEmbeddingDimensions() {
        return EMBEDDING_DIMENSIONS;
    }

    /**
     * Handle error responses from Gemini Embedding API
     */
    private void handleErrorResponse(int statusCode, String responseBody) {
        log.error("Gemini Embedding API error - Status: {}, Response: {}", statusCode, responseBody);

        String errorMessage;
        String errorType;

        try {
            // Try to parse error response
            var errorNode = objectMapper.readTree(responseBody);
            errorType = errorNode.path("error").path("code").asText("unknown");
            errorMessage = errorNode.path("error").path("message").asText("Unknown error");
        } catch (Exception e) {
            errorType = "unknown";
            errorMessage = "Failed to parse error response: " + responseBody;
        }

        // Map status codes to user-friendly messages
        switch (statusCode) {
            case 400:
                throw new LlmApiException("Invalid request to Gemini Embedding API: " + errorMessage,
                        "Gemini", statusCode, errorType);
            case 403:
                throw new LlmApiException("API key invalid or quota exceeded. Check your Gemini API key.",
                        "Gemini", statusCode, errorType);
            case 429:
                throw new LlmApiException("Rate limit exceeded. Gemini free tier: 1500 requests/day, 60/minute",
                        "Gemini", statusCode, errorType);
            case 500:
            case 502:
            case 503:
                throw new LlmApiException("Gemini service is temporarily unavailable. Please try again later.",
                        "Gemini", statusCode, errorType);
            default:
                throw new LlmApiException("Gemini Embedding API error: " + errorMessage,
                        "Gemini", statusCode, errorType);
        }
    }
}
