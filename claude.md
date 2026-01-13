# LLM Agentic AI MVP - Session Context & Reference

## Project Overview

**Goal**: Build an intelligent document Q&A system demonstrating LLM, Agentic AI, Prompt Engineering, and Vector Database concepts using Java Spring Boot, Oracle 23c AI, and Camunda.

**Timeline**: 7-day phased learning project with daily manager check-ins

**Tech Stack**:
- **Backend**: Java 21, Spring Boot 3.2.1
- **LLM**: Google Gemini API (FREE tier - Primary), Claude API (Paid - Optional)
- **Database**: Oracle 23c AI with Vector Search
- **Workflow**: Camunda 8.7 (Docker)
- **Build**: Maven 3.9

---

## Current Branch Status

**Active Branch**: `claude/llm-agentic-ai-mvp-bRhG6`

**Completed Phases**:
- ✅ Phase 1: LLM Integration (Multi-provider: Gemini/Claude)
- ✅ Phase 2: Prompt Engineering (4 techniques)
- ✅ Phase 3: Vector Database & Embeddings (Core implementation complete)

**Current Status**: Phase 3 testing and debugging in progress

---

## Phase 1: LLM Integration ✅

### What Was Built
- Multi-provider LLM architecture (Gemini FREE, Claude PAID)
- Basic chat endpoint with token tracking
- Request/Response DTOs
- Global exception handling

### Key Files
```
src/main/java/com/ai/mvp/
├── config/
│   ├── AnthropicConfig.java
│   ├── GeminiConfig.java
│   └── LlmProvider.java (enum: GEMINI, CLAUDE, OLLAMA)
├── controller/
│   └── ChatController.java
├── service/
│   ├── ChatService.java (multi-provider routing)
│   ├── ClaudeApiService.java
│   └── GeminiApiService.java
├── dto/
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   ├── ClaudeApiRequest/Response.java
│   └── gemini/GeminiApiRequest/Response.java
└── exception/
    ├── LlmApiException.java
    └── GlobalExceptionHandler.java
```

### Endpoints
- `POST /api/chat` - Send message to LLM (Gemini by default)
- `GET /api/chat/health` - Health check

### Key Configurations

**application.yml**:
```yaml
llm:
  provider: ${LLM_PROVIDER:GEMINI}

gemini:
  api-key: ${GEMINI_API_KEY}
  api-url: https://generativelanguage.googleapis.com/v1beta
  model: ${GEMINI_MODEL:gemini-2.5-flash}
  max-tokens: ${GEMINI_MAX_TOKENS:2048}
  temperature: 0.7
```

**.env** (required):
```properties
LLM_PROVIDER=GEMINI
GEMINI_API_KEY=your-api-key-here
GEMINI_MODEL=gemini-2.5-flash
```

### Important Notes
- **Gemini v1beta API** required for `systemInstruction` support
- **gemini-2.5-flash** model (discovered via API, not 1.5)
- FREE tier: 60 req/min, 1500 req/day
- Multi-provider pattern allows easy switching

### Testing
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain LLMs in one sentence", "maxTokens": 200}'
```

---

## Phase 2: Prompt Engineering ✅

### What Was Built
4 different prompting techniques with separate endpoints demonstrating when and how to use each approach.

### Key Files
```
src/main/java/com/ai/mvp/
├── controller/
│   └── PromptController.java (4 endpoints)
├── service/
│   └── PromptEngineeringService.java
├── util/
│   └── PromptTemplate.java (template builders)
└── dto/prompt/
    ├── PromptRequest.java (zero-shot)
    ├── FewShotRequest.java
    ├── ChainOfThoughtRequest.java
    ├── StructuredOutputRequest.java
    └── PromptResponse.java
```

### Endpoints
1. `POST /api/prompt/zero-shot` - Direct questions without examples
2. `POST /api/prompt/few-shot` - Learning from example patterns
3. `POST /api/prompt/chain-of-thought` - Step-by-step reasoning
4. `POST /api/prompt/structured-output` - JSON/formatted responses
5. `GET /api/prompt/health` - Health check

### The 4 Techniques

**1. Zero-Shot**: Simplest approach
```json
{"message": "Translate 'Hello' to French"}
```

**2. Few-Shot**: Provides examples
```json
{
  "task": "Classify sentiment",
  "examples": [
    {"input": "Great!", "output": "Positive"},
    {"input": "Terrible", "output": "Negative"}
  ],
  "input": "It's okay"
}
```

**3. Chain-of-Thought**: Step-by-step reasoning
```json
{
  "problem": "A store has 15 apples. They sell 40%. How many left?",
  "requestSteps": true
}
```

**4. Structured Output**: JSON responses
```json
{
  "task": "Extract job info",
  "inputText": "Senior Java Dev at Tech Corp. $120k.",
  "outputFormat": "{\"position\":\"...\",\"salary\":\"...\"}",
  "formatType": "JSON"
}
```

### Key Learnings
- Temperature matters: Low (0.2-0.3) for consistency, high (0.7-1.0) for creativity
- Few-shot teaches patterns through examples
- Chain-of-thought reduces errors in complex reasoning
- Structured output enables programmatic parsing
- All techniques work with FREE Gemini API

### Testing
Full test commands in `PHASE-2-GUIDE.md` (1,588 lines)

---

## Phase 3: Vector Database & Embeddings 🚧

### What Was Built
Complete semantic search system using Oracle 23c AI Vector Search with Gemini embeddings.

### Key Files
```
src/main/java/com/ai/mvp/
├── controller/
│   └── DocumentController.java (6 endpoints)
├── service/
│   ├── EmbeddingService.java (Gemini text-embedding-004)
│   ├── DocumentService.java (upload, chunking, storage)
│   └── VectorSearchService.java (semantic search)
├── repository/
│   ├── DocumentRepository.java
│   └── DocumentChunkRepository.java (native vector queries)
├── entity/
│   ├── Document.java
│   └── DocumentChunk.java (VECTOR column)
└── dto/vector/
    ├── DocumentUploadRequest/Response.java
    └── SemanticSearchRequest/Response.java
```

### Database Schema
**Location**: `src/main/resources/db/phase3-vector-schema.sql`

**Tables**:
```sql
-- Parent table
CREATE TABLE documents (
  id VARCHAR2(36) PRIMARY KEY,
  title VARCHAR2(255) NOT NULL,
  content CLOB,
  category VARCHAR2(100),
  chunk_count NUMBER,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- Child table with vector embeddings
CREATE TABLE document_chunks (
  id VARCHAR2(36) PRIMARY KEY,
  document_id VARCHAR2(36) REFERENCES documents(id) ON DELETE CASCADE,
  chunk_index NUMBER NOT NULL,
  content CLOB NOT NULL,
  embedding VECTOR(768, FLOAT32),  -- Gemini embeddings
  start_position NUMBER,
  end_position NUMBER,
  created_at TIMESTAMP
);

-- HNSW vector index for fast similarity search
CREATE VECTOR INDEX idx_document_chunks_embedding
  ON document_chunks(embedding)
  ORGANIZATION NEIGHBOR PARTITIONS
  DISTANCE COSINE
  WITH TARGET ACCURACY 95;
```

### Endpoints
1. `POST /api/documents/upload` - Upload and embed document
2. `POST /api/documents/search/semantic` - Semantic similarity search
3. `GET /api/documents` - List all documents
4. `GET /api/documents/{id}` - Get document by ID
5. `DELETE /api/documents/{id}` - Delete document
6. `GET /api/documents/health` - Health check

### Document Upload Process
1. Receive document text
2. Split into chunks (default: 1000 chars, 200 overlap)
3. Generate 768-dim vector embedding for each chunk using Gemini
4. Store in Oracle with VECTOR type
5. Return chunk count and stats

**Example**:
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introduction to Neural Networks",
    "content": "Neural networks are...",
    "category": "AI/ML",
    "chunkSize": 150,
    "chunkOverlap": 30
  }'
```

### Semantic Search Process
1. Convert query to 768-dim vector embedding
2. Calculate COSINE distance with all stored chunks
3. Return top K most similar chunks
4. Convert distance to similarity score (0-1 scale)

**Example**:
```bash
curl -X POST http://localhost:8080/api/documents/search/semantic \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How do AI models learn?",
    "topK": 3,
    "similarityThreshold": 0.5
  }'
```

### Vector Search Implementation

**Repository Query** (native SQL):
```java
@Query(value = """
    SELECT c.id, c.document_id, c.chunk_index, c.content,
           c.start_position, c.end_position, c.created_at,
           d.title, d.category,
           VECTOR_DISTANCE(TO_VECTOR(c.embedding), TO_VECTOR(:queryEmbedding), COSINE) as distance
    FROM document_chunks c
    JOIN documents d ON c.document_id = d.id
    WHERE (:category IS NULL OR d.category = :category)
    ORDER BY distance
    FETCH FIRST :topK ROWS ONLY
    """, nativeQuery = true)
List<Object[]> findSimilarChunks(String queryEmbedding, int topK, String category);
```

**Similarity Scoring**:
```java
// Cosine distance [0, 2] → Similarity [0, 1]
double similarity = 1.0 - (distance / 2.0);
// 0.0 distance = 1.0 similarity (identical)
// 1.0 distance = 0.5 similarity (orthogonal)
// 2.0 distance = 0.0 similarity (opposite)
```

### Embedding Service

**Model**: `text-embedding-004` (Google Gemini)
**Dimensions**: 768
**API**: `https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent`

**Request Format**:
```json
{
  "model": "models/text-embedding-004",
  "content": {
    "parts": [{"text": "your text here"}]
  }
}
```

**Response Format**:
```json
{
  "embedding": {
    "values": [0.1, 0.2, 0.3, ..., 0.768]
  }
}
```

### Known Issues & Fixes

#### Issue 1: VECTOR Column Type Mismatch
**Error**: Schema validation failed - wrong column type for `id` (RAW vs VARCHAR2)
**Cause**: Old tables from Phase 1 had different schema
**Fix**: Run `phase3-vector-schema.sql` in SQL Developer to recreate tables

#### Issue 2: JDBC VECTOR Conversion
**Error**: `ORA-17004: Invalid column type: JDBC 4.3 does not specify a default conversion for VECTOR`
**Cause**: Selecting `c.*` includes `embedding` column which JDBC can't convert
**Fix**: Explicitly select columns, exclude `embedding` from results
```sql
-- Bad: SELECT c.*, distance FROM ...
-- Good: SELECT c.id, c.content, ..., distance FROM ...
```

#### Issue 3: Oracle Proxy Objects
**Error**: `class jdk.proxy2.$Proxy180 cannot be cast to class java.lang.String`
**Cause**: Oracle JDBC returns proxy objects for VARCHAR2/CHAR columns
**Fix**: Use `String.valueOf()` instead of direct casting
```java
// Bad: chunk.setId((String) row[0]);
// Good: chunk.setId(String.valueOf(row[0]));
```

#### Issue 4: Vector Index Syntax
**Error**: `ORA-51914: Missing ORGANIZATION clause when creating a vector index`
**Cause**: Incorrect syntax for Oracle 23c vector index
**Fix**: Use proper syntax with ORGANIZATION clause
```sql
CREATE VECTOR INDEX idx_document_chunks_embedding
  ON document_chunks(embedding)
  ORGANIZATION NEIGHBOR PARTITIONS  -- Required!
  DISTANCE COSINE
  WITH TARGET ACCURACY 95;
```

### Current Testing Status
- ✅ Document upload working (5 chunks created, embeddings generated)
- ✅ Documents stored in Oracle successfully
- 🚧 Semantic search: Query executes, finds results, but parsing needs verification
- ⏳ Awaiting test confirmation after proxy object fix

### Next Steps
1. Test semantic search after proxy object fix
2. Verify similarity scores are accurate
3. Test category filtering
4. Test similarity threshold filtering
5. Complete PHASE-3-GUIDE.md documentation
6. Demo to manager

---

## Environment Setup

### Required Services

**Oracle 23c AI** (Docker):
```bash
docker ps | grep oracle23c
# Should show container running on port 1521
```

**Camunda 8.7** (Docker):
```bash
docker ps | grep camunda
# Should show: zeebe, operate, tasklist, connectors, elasticsearch
```

### Environment Variables (.env)
```properties
# LLM Provider
LLM_PROVIDER=GEMINI

# Gemini API (FREE)
GEMINI_API_KEY=your-key-here
GEMINI_MODEL=gemini-2.5-flash
GEMINI_MAX_TOKENS=2048

# Claude API (Optional)
ANTHROPIC_API_KEY=your-key-here
ANTHROPIC_MODEL=claude-3-5-sonnet-20241022

# Oracle Database
ORACLE_HOST=localhost
ORACLE_PORT=1521
ORACLE_SERVICE_NAME=FREEPDB1
ORACLE_USERNAME=ai_user
ORACLE_PASSWORD=ai_password_2024
```

### Application Startup
```bash
# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run

# Check logs for:
# ✅ Gemini API Service initialized
# ✅ Database tables available
# ✅ Application started on port 8080
```

---

## Architecture Patterns

### Multi-Provider LLM Pattern
```
ChatService (facade)
    ├─> GeminiApiService (FREE)
    └─> ClaudeApiService (PAID)
```

**Benefits**:
- Easy switching via config
- Consistent interface
- Cost optimization (use FREE for dev, PAID for prod)

### Vector Search Pattern
```
1. Upload: Text → Chunks → Embeddings → Oracle VECTOR
2. Search: Query → Embedding → COSINE distance → Top K results
```

**Why Chunking?**:
- Large docs exceed embedding model limits
- Smaller chunks = more precise search
- Overlap preserves context across boundaries

**Why Vector Search?**:
- Semantic similarity (meaning-based, not keyword)
- "neural networks" finds "deep learning", "AI models"
- Foundation for RAG (Retrieval Augmented Generation)

---

## Cost Analysis

### FREE Tier (Gemini)
- **Chat**: 60 req/min, 1500 req/day = $0.00
- **Embeddings**: 1500 req/day = $0.00
- **Total**: Perfect for learning and MVP!

### Paid Alternative (Claude)
- **Chat**: $3 per 1M input tokens, $15 per 1M output tokens
- **No embeddings**: Use Gemini embeddings even with Claude chat
- **Use when**: Production quality requirements, complex reasoning

---

## Documentation Files

### User Guides
- `README.md` - Concept explanations, architecture overview
- `PROJECT-PLAN.md` - 7-phase roadmap with tech stack
- `SETUP-VALIDATION.md` - Environment checklist
- `SETUP-INSTRUCTIONS.md` - Step-by-step setup
- `PHASE-1-GUIDE.md` - LLM integration guide (500+ lines)
- `PHASE-2-GUIDE.md` - Prompt engineering guide (1,588 lines)
- `PHASE-3-GUIDE.md` - Vector database guide (pending)
- `GEMINI-SETUP.md` - Gemini API setup instructions

### Technical Files
- `phase3-vector-schema.sql` - Oracle vector database schema (470 lines)
- `docker-compose.yml` - Oracle 23c AI setup
- `.env.template` - Environment variable template

---

## Git Workflow

### Branch Naming
- `claude/llm-agentic-ai-mvp-bRhG6` - Main development branch (current)
- Branch naming: `claude/{description}-{sessionId}`

### Commit Messages
Format: `Type: Brief description`

Types:
- `Feat:` - New feature
- `Fix:` - Bug fix
- `Docs:` - Documentation
- `Refactor:` - Code restructuring

Examples:
```
Feat: Implement Phase 2 - Prompt Engineering with 4 techniques
Fix: Resolve Oracle JDBC VECTOR column handling in semantic search
Docs: Update all documentation to reflect Gemini as primary FREE provider
```

### Push Requirements
- Always: `git push -u origin <branch-name>`
- Branch must start with `claude/` and end with session ID
- Retry on network failure (up to 4 times, exponential backoff)

---

## Common Commands

### Testing Endpoints

**Phase 1 - Basic Chat**:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain LLMs", "maxTokens": 200}'
```

**Phase 2 - Few-Shot**:
```bash
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Classify sentiment",
    "examples": [
      {"input": "Amazing!", "output": "Positive"},
      {"input": "Terrible", "output": "Negative"}
    ],
    "input": "It'\''s okay"
  }'
```

**Phase 3 - Upload Document**:
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Neural Networks Guide",
    "content": "Neural networks are...",
    "category": "AI/ML"
  }'
```

**Phase 3 - Semantic Search**:
```bash
curl -X POST http://localhost:8080/api/documents/search/semantic \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How do AI models learn?",
    "topK": 3,
    "similarityThreshold": 0.5
  }'
```

### Database Operations

**Connect to Oracle**:
```bash
docker exec -it oracle23c sqlplus ai_user/ai_password_2024@FREEPDB1
```

**Check Vector Tables**:
```sql
DESC documents;
DESC document_chunks;

SELECT COUNT(*) FROM documents;
SELECT COUNT(*) FROM document_chunks;

-- View document chunks with similarity
SELECT d.title, c.chunk_index,
       SUBSTR(c.content, 1, 100) as preview
FROM documents d
JOIN document_chunks c ON d.id = c.document_id
ORDER BY d.created_at DESC, c.chunk_index;
```

**Recreate Vector Schema**:
```bash
# In SQL Developer: Open and run phase3-vector-schema.sql

# Or via command line:
docker exec -i oracle23c sqlplus ai_user/ai_password_2024@FREEPDB1 < \
  src/main/resources/db/phase3-vector-schema.sql
```

---

## Troubleshooting

### Application Won't Start

**Check 1**: Maven network issues
```bash
# Error: "Temporary failure in name resolution"
# Wait and retry - usually transient network issue
```

**Check 2**: Schema mismatch
```bash
# Error: "wrong column type encountered in column [id]"
# Solution: Run phase3-vector-schema.sql to recreate tables
```

**Check 3**: Missing API keys
```bash
# Error: "API key is required"
# Solution: Check .env file has GEMINI_API_KEY set
```

### Semantic Search Errors

**Error**: ORA-17004 VECTOR conversion
```
# Cause: Query includes embedding column
# Fix: Applied in DocumentChunkRepository.java
# - Explicitly select columns, exclude embedding
```

**Error**: Proxy object cast exception
```
# Cause: Oracle returns proxy objects
# Fix: Applied in VectorSearchService.java
# - Use String.valueOf() instead of direct cast
```

**Error**: Empty results
```
# Check 1: Are there documents in database?
SELECT COUNT(*) FROM documents;

# Check 2: Is similarity threshold too high?
# Try: "similarityThreshold": 0.0

# Check 3: Check logs for parsing errors
# Look for: "Error parsing search result row"
```

### Gemini API Issues

**Error**: Invalid API key
```
# Solution: Generate new key at https://aistudio.google.com/app/apikey
# Note: Old keys may be disabled if exposed in git
```

**Error**: 404 Model not found
```
# Use: gemini-2.5-flash (not 1.5)
# API: v1beta (not v1, for systemInstruction support)
```

---

## Future Phases (Planned)

### Phase 4: RAG Implementation
- Combine vector search + LLM chat
- Document Q&A with sources
- Citation tracking

### Phase 5: Agentic AI - Tool Use
- Function calling
- Multi-tool workflows
- Decision logging

### Phase 6: Agentic AI - Camunda
- BPMN workflows
- Human-in-the-loop
- Process orchestration

### Phase 7: Integration & Demo
- Frontend UI
- Complete demo flow
- Manager presentation

---

## Key Learnings

### Technical
1. **Multi-provider architecture** enables cost optimization and flexibility
2. **Prompt engineering** dramatically affects output quality
3. **Vector search** enables semantic similarity (meaning-based search)
4. **Chunking with overlap** preserves context and improves search accuracy
5. **Oracle 23c native VECTOR** type provides excellent performance
6. **HNSW indexing** enables fast approximate nearest neighbor search

### Practical
1. **Gemini FREE tier** perfect for learning and development
2. **Temperature** matters: 0.2-0.3 for consistency, 0.7-1.0 for creativity
3. **Few-shot examples** teach patterns effectively
4. **Chain-of-thought** reduces errors in complex reasoning
5. **Structured output** enables programmatic integration
6. **JDBC VECTOR handling** requires explicit column selection

### Business
1. **$0 development cost** using Gemini FREE tier
2. **Scalable architecture** ready for production (switch to Claude)
3. **Demonstrable value** for manager demos
4. **Phased approach** allows incremental learning and delivery

---

## Session Continuity

### When Starting New Session

1. **Pull latest code**:
   ```bash
   git fetch origin
   git checkout claude/llm-agentic-ai-mvp-bRhG6
   git pull origin claude/llm-agentic-ai-mvp-bRhG6
   ```

2. **Check application status**:
   ```bash
   mvn spring-boot:run
   # Verify all services start successfully
   ```

3. **Verify services**:
   ```bash
   # Oracle
   docker ps | grep oracle23c

   # Camunda
   docker ps | grep camunda
   ```

4. **Test basic functionality**:
   ```bash
   # Phase 1
   curl http://localhost:8080/api/chat/health

   # Phase 2
   curl http://localhost:8080/api/prompt/health

   # Phase 3
   curl http://localhost:8080/api/documents/health
   ```

5. **Check current phase status** in this file

### Current Work Context

**Phase**: 3 (Vector Database)
**Status**: Testing semantic search after proxy object fix
**Last Issue**: Oracle proxy object casting in result parsing
**Last Fix**: Changed to `String.valueOf()` in `VectorSearchService.parseChunkFromRow()`
**Pending**: Verify semantic search returns correct results

**Next Steps**:
1. Test semantic search endpoint
2. Verify similarity scores are accurate
3. Test filtering (category, threshold)
4. Create PHASE-3-GUIDE.md
5. Prepare manager demo

---

## Contact & Support

**Project**: LLM Agentic AI MVP
**Developer**: Nooreldeen Nabil
**Environment**: Windows, IntelliJ IDEA
**Java**: 21
**Spring Boot**: 3.2.1
**Oracle**: 23c Free
**Camunda**: 8.7

---

*Last Updated*: 2026-01-13 02:50:00
*Session*: claude/llm-agentic-ai-mvp-bRhG6
*Phase*: 3 - Vector Database & Embeddings (Testing)
