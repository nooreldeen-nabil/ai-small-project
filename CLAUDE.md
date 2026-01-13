# 🤖 Claude Session Reference Guide

**Project:** LLM Agentic AI MVP - 7-Phase Learning Project
**Last Updated:** 2026-01-13
**Current Phase:** Phase 3 (Vector Database & Embeddings) - COMPLETED ✅
**Current Branch:** `claude/develop-stable-nSxCU` (stable/default branch)

---

## 📋 Quick Status Overview

### ✅ Completed Phases

| Phase | Status | Completion Date | Key Features |
|-------|--------|----------------|--------------|
| **Phase 1** | ✅ Complete | Jan 2026 | LLM Integration (Gemini, Claude, Ollama) |
| **Phase 2** | ✅ Complete | Jan 2026 | Prompt Engineering (4 techniques) |
| **Phase 3** | ✅ Complete | Jan 13, 2026 | Vector Database & Semantic Search |

### 🚧 Upcoming Phases

| Phase | Status | Description |
|-------|--------|-------------|
| **Phase 4** | ⏳ Pending | RAG (Retrieval Augmented Generation) |
| **Phase 5** | ⏳ Pending | Agentic AI - Tool Use (Function Calling) |
| **Phase 6** | ⏳ Pending | Agentic AI - Camunda Workflows (BPMN) |
| **Phase 7** | ⏳ Pending | Integration & Polish (Frontend + Demo) |

### 🔀 Branch Structure

**Default Branch:** `claude/develop-stable-nSxCU` (stable, production-ready code)

**Historical Feature Branches:**
- `claude/llm-agentic-ai-mvp-bRhG6` - Initial implementation (Phases 1-3 with bugs)
- `claude/continue-llm-ai-mvp-xYiaT` - Bug fixes + Phase 3 completion

**Branch Consolidation:** (Jan 13, 2026)
The `claude/develop-stable-nSxCU` branch was created by consolidating the most stable code from both feature branches. It contains:
- ✅ All Phase 1-3 implementations
- ✅ All bug fixes (JDBC VECTOR, CLOB proxy, CLOB truncation)
- ✅ Complete documentation (CLAUDE.md + PHASE-3-GUIDE.md)
- ✅ Fully tested and validated code

---

## 🎯 Project Mission

**Goal:** Build an intelligent document Q&A system demonstrating 4 core AI concepts:
1. **LLM Integration** - Multi-provider chat (Gemini/Claude/Ollama)
2. **Prompt Engineering** - Zero-shot, Few-shot, Chain-of-Thought, Structured Output
3. **Vector Database** - Semantic search with Oracle 23c + embeddings
4. **Agentic AI** - Tool use + workflow orchestration with Camunda

**Target Audience:** Non-technical manager presentation
**Tech Stack:** Java 21, Spring Boot 3.2.1, Oracle 23c, Google Gemini, Docker

---

## 📂 Project Structure

```
ai-small-project/
├── src/main/java/com/ai/mvp/
│   ├── controller/          # REST API endpoints
│   │   ├── ChatController.java              (Phase 1)
│   │   ├── PromptController.java            (Phase 2)
│   │   └── DocumentController.java          (Phase 3) ⭐ NEW
│   ├── service/             # Business logic
│   │   ├── LlmService.java                  (Phase 1)
│   │   ├── PromptTemplateService.java       (Phase 2)
│   │   ├── DocumentService.java             (Phase 3) ⭐ NEW
│   │   ├── EmbeddingService.java            (Phase 3) ⭐ NEW
│   │   └── VectorSearchService.java         (Phase 3) ⭐ NEW
│   ├── repository/          # Data access
│   │   ├── DocumentRepository.java          (Phase 3) ⭐ NEW
│   │   └── DocumentChunkRepository.java     (Phase 3) ⭐ NEW
│   ├── entity/              # JPA entities
│   │   ├── Document.java                    (Phase 3) ⭐ NEW
│   │   └── DocumentChunk.java               (Phase 3) ⭐ NEW
│   ├── dto/                 # Request/Response objects (16 DTOs)
│   ├── config/              # Configuration
│   └── exception/           # Custom exceptions
│
├── src/main/resources/
│   ├── application.yml      # Database & LLM config
│   └── db/
│       └── phase3-vector-schema.sql         (Phase 3) ⭐ NEW
│
├── docker/
│   └── oracle/
│       └── init/            # Database init scripts
│
├── Documentation/
│   ├── README.md            # Project overview (18KB)
│   ├── PROJECT-PLAN.md      # 7-phase roadmap
│   ├── PHASE-1-GUIDE.md     # LLM Integration guide
│   ├── PHASE-2-GUIDE.md     # Prompt Engineering guide (46KB)
│   ├── PHASE-3-GUIDE.md     # Vector DB guide ⭐ NEW (50KB)
│   ├── GEMINI-SETUP.md      # API setup instructions
│   ├── SETUP-INSTRUCTIONS.md
│   └── CLAUDE.md            # This file ⭐ YOU ARE HERE
│
├── docker-compose.yml       # Oracle 23c setup
├── .env.template            # Environment variables template
├── .env                     # Actual env vars (add GEMINI_API_KEY!)
└── pom.xml                  # Maven dependencies
```

---

## 🔑 Key Files & Their Purpose

### Phase 3 Files (Most Recent Work)

#### 1. `VectorSearchService.java` (⚠️ RECENTLY FIXED)
**Location:** `src/main/java/com/ai/mvp/service/VectorSearchService.java`
**Purpose:** Semantic search using vector similarity
**Recent Fix:** Fixed CLOB proxy casting error by adding `convertToString()` method
**Key Methods:**
- `search()` - Main semantic search endpoint
- `parseChunkFromRow()` - Parse Oracle query results
- `convertToString()` - Handle CLOB objects properly
- `calculateSimilarityScore()` - Convert distance to 0-1 similarity score

#### 2. `DocumentChunkRepository.java`
**Location:** `src/main/java/com/ai/mvp/repository/DocumentChunkRepository.java`
**Purpose:** Native SQL queries for vector operations
**Key Query:**
```java
@Query(nativeQuery = true)
List<Object[]> findSimilarChunks(
    @Param("queryEmbedding") String queryEmbedding,
    @Param("topK") int topK,
    @Param("category") String category
);
```

#### 3. `EmbeddingService.java`
**Location:** `src/main/java/com/ai/mvp/service/EmbeddingService.java`
**Purpose:** Generate 768-dimensional vectors via Gemini API
**Key Methods:**
- `generateEmbedding()` - Text → float[768]
- `embeddingToString()` - Convert to Oracle VECTOR format

#### 4. `DocumentService.java`
**Location:** `src/main/java/com/ai/mvp/service/DocumentService.java`
**Purpose:** Upload documents and chunk them
**Key Methods:**
- `uploadDocument()` - Main upload endpoint
- `chunkText()` - Split text with overlap

#### 5. `PHASE-3-GUIDE.md`
**Location:** `/home/user/ai-small-project/PHASE-3-GUIDE.md`
**Purpose:** Complete testing guide for Phase 3
**Contents:** Architecture, concepts, API docs, troubleshooting

---

## 🐛 Recent Bugs & Fixes

### Bug #1: JDBC VECTOR Column Handling ✅ FIXED
**Commit:** `03fdaa3`
**Date:** Jan 13, 2026 00:43
**Issue:** `ORA-17004` error when querying VECTOR columns
**Solution:** Modified `DocumentChunkRepository.findSimilarChunks()` to explicitly select columns instead of `c.*`, excluding the embedding column from SELECT results.

### Bug #2: CLOB Proxy Casting Error ✅ FIXED
**Commit:** `f0c5967`
**Date:** Jan 13, 2026 (Session 5)
**Issue:**
```
Error: class jdk.proxy2.$Proxy180 cannot be cast to class java.lang.String
Empty search results: {"results": [], "totalResults": 0}
```
**Root Cause:** Oracle returns CLOB columns as `oracle.sql.CLOB` proxy objects, not Strings. Direct casting `(String) row[3]` failed.
**Solution:** Added `convertToString()` helper method in `VectorSearchService.java` to properly read CLOB content.

### Bug #3: CLOB Content Truncation ✅ FIXED
**Commit:** `f159609`
**Date:** Jan 13, 2026 (Session 5)
**Issue:**
```json
// Content truncated to ~150 chars instead of ~1000 chars
"content": "he output layer produces results...Deep le"
// Starts mid-word ("he") and ends mid-word ("le")
```
**Root Cause:** Using `BufferedReader.readLine()` for CLOB reading wasn't reliable - didn't read complete content.
**Solution:** Changed to `clob.getSubString(1, length)` which reads the entire CLOB content directly in one operation.

**Files Modified:**
```java
// VectorSearchService.java - Final CLOB reading approach
private String convertToString(Object value) throws SQLException {
    if (value instanceof Clob) {
        Clob clob = (Clob) value;
        long length = clob.length();
        return clob.getSubString(1, (int) length);  // Oracle 1-based indexing
    }
    return (String) value;
}
```

---

## 🧪 How to Test Phase 3

### Prerequisites

1. **Start Oracle 23c:**
```bash
docker-compose up -d
docker-compose ps  # Wait until "healthy"
```

2. **Configure Environment:**
```bash
cp .env.template .env
# Edit .env and add your GEMINI_API_KEY=AIzaSy...
```

3. **Build & Run:**
```bash
mvn clean package -DskipTests
java -jar target/llm-agentic-ai-mvp-1.0.0-SNAPSHOT.jar
# Or: mvn spring-boot:run
```

4. **Verify Startup:**
- Server: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

### Quick Test Sequence

**1. Upload Document:**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Neural Networks 101",
    "content": "Neural networks learn through backpropagation...",
    "category": "AI"
  }'
```

**2. Semantic Search:**
```bash
curl -X POST http://localhost:8080/api/documents/search/semantic \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How do AI models learn?",
    "topK": 5,
    "similarityThreshold": 0.5
  }'
```

**Expected Result:**
```json
{
  "query": "How do AI models learn?",
  "results": [
    {
      "documentTitle": "Neural Networks 101",
      "content": "Neural networks learn through backpropagation...",
      "similarityScore": 0.91,
      "category": "AI"
    }
  ],
  "totalResults": 1
}
```

**Detailed Testing:** See `PHASE-3-GUIDE.md` sections:
- Test 1-9: Complete test scenarios
- API Reference: All endpoints documented
- Troubleshooting: Common issues and solutions

---

## 🔧 Common Commands

### Docker
```bash
# Start Oracle
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f oracle-23c

# Stop
docker-compose down

# Stop + delete data (CAUTION!)
docker-compose down -v
```

### Maven
```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Test
mvn test

# Clean
mvn clean
```

### Git
```bash
# Current branch
git status

# View recent commits
git log --oneline -10

# Stage changes
git add .

# Commit (only when requested!)
git commit -m "Description"

# Push to stable branch
git push -u origin claude/develop-stable-nSxCU
```

### Database
```bash
# Connect to Oracle (from inside container)
docker exec -it oracle-23c-ai sqlplus ai_user/ai_password_2024@//localhost:1521/FREEPDB1

# Check documents
SELECT COUNT(*) FROM documents;

# Check chunks with embeddings
SELECT id, document_id, chunk_index, LENGTH(content), LENGTH(embedding)
FROM document_chunks;

# Test vector search directly
SELECT id, chunk_index,
       VECTOR_DISTANCE(TO_VECTOR(embedding),
                      TO_VECTOR('[0.1, 0.2, ...]'),
                      COSINE) as distance
FROM document_chunks
ORDER BY distance
FETCH FIRST 5 ROWS ONLY;
```

---

## 🚀 How to Continue from a New Session

### Step 1: Orient Yourself
```bash
# 1. Check current branch
git status

# 2. Review recent commits
git log --oneline -5

# 3. Check what's running
ps aux | grep java
docker-compose ps

# 4. Read this file (CLAUDE.md) completely
```

### Step 2: Understand Current State
```bash
# Read the phase guide for current phase
cat PHASE-3-GUIDE.md  # Currently on Phase 3

# Check project plan
cat PROJECT-PLAN.md
```

### Step 3: Identify Next Steps
```bash
# Check if Phase 3 is fully tested
# Read "Next Steps" section in PHASE-3-GUIDE.md

# If Phase 3 complete → Start Phase 4 (RAG)
# If Phase 3 has issues → Debug and fix
```

### Step 4: Start Working
- If continuing Phase 3: Follow test steps in `PHASE-3-GUIDE.md`
- If starting Phase 4: Read `PROJECT-PLAN.md` Phase 4 section
- Always update this `CLAUDE.md` file at end of session

---

## 📊 Technology Stack Reference

### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3.2.1
- **ORM:** Hibernate 6.4 + Spring Data JPA
- **Build:** Maven 3.9
- **API Docs:** SpringDoc OpenAPI 2.3.0 (Swagger)

### Database
- **Primary:** Oracle 23c Free (Docker)
- **Vector Support:** Native VECTOR(768, FLOAT32) type
- **Indexing:** HNSW algorithm for fast similarity search
- **Distance Metric:** COSINE

### LLM Providers
- **Primary:** Google Gemini 2.5 Flash (FREE)
  - Chat: `gemini-2.5-flash`
  - Embeddings: `text-embedding-004` (768 dimensions)
  - API: `https://generativelanguage.googleapis.com/v1beta`
  - Rate Limits: 60 req/min, 1500 req/day
- **Secondary:** Anthropic Claude 3.5 Sonnet (PAID, optional)
- **Tertiary:** Ollama (local, offline, optional)

### Infrastructure
- **Containerization:** Docker + Docker Compose
- **Network:** Bridge network `ai-mvp-network`
- **Volumes:** Persistent Oracle data storage

---

## 🎓 Key Concepts by Phase

### Phase 1: LLM Integration
**Concepts:**
- REST API integration with multiple LLM providers
- Request/response handling
- Streaming responses
- Token management
- Error handling

**Key Endpoints:**
- `POST /api/chat` - Chat with LLM

### Phase 2: Prompt Engineering
**Concepts:**
- Zero-shot prompting (direct questions)
- Few-shot prompting (learning from examples)
- Chain-of-thought (step-by-step reasoning)
- Structured output (JSON/formatted responses)
- Temperature tuning per technique

**Key Endpoints:**
- `POST /api/prompt/zero-shot`
- `POST /api/prompt/few-shot`
- `POST /api/prompt/chain-of-thought`
- `POST /api/prompt/structured-output`

### Phase 3: Vector Database & Embeddings ⭐ CURRENT
**Concepts:**
- Vector embeddings (text → 768-dimensional arrays)
- Semantic similarity (cosine distance)
- Document chunking with overlap
- HNSW indexing for fast search
- Oracle VECTOR column type
- Native SQL vector operations

**Key Endpoints:**
- `POST /api/documents/upload` - Upload & process document
- `POST /api/documents/search/semantic` - Semantic search
- `GET /api/documents` - List documents
- `GET /api/documents/{id}` - Get document
- `DELETE /api/documents/{id}` - Delete document

**Key Algorithms:**
```
Similarity Score = 1 - (cosine_distance / 2)

Cosine Distance Range: [0, 2]
- 0.0 = Identical vectors
- 1.0 = Orthogonal (90° apart)
- 2.0 = Opposite vectors

Similarity Score Range: [0, 1]
- 1.0 = 100% similar (identical)
- 0.5 = 50% similar
- 0.0 = 0% similar (opposite)
```

---

## 📋 Session History

### Session 1: Phase 1 Implementation
- **Date:** January 2026
- **Work Done:**
  - LLM multi-provider integration (Gemini, Claude, Ollama)
  - Chat API endpoint
  - Configuration management
  - Error handling
- **Commit:** `a6ac791 Phase 1: Foundation Setup + LLM Integration (COMPLETE)`
- **Guide:** PHASE-1-GUIDE.md

### Session 2: Phase 2 Implementation
- **Date:** January 2026
- **Work Done:**
  - 4 prompt engineering techniques
  - PromptTemplate utility service
  - Temperature tuning per technique
  - Token optimization
- **Commit:** `9bb8f0c Feat: Implement Phase 2 - Prompt Engineering with 4 techniques`
- **Guide:** PHASE-2-GUIDE.md

### Session 3: Phase 3 Implementation
- **Date:** January 2026
- **Work Done:**
  - Document upload & chunking
  - Embedding generation (Gemini)
  - Oracle VECTOR storage
  - HNSW indexing
  - Semantic search API
- **Commit:** `49ea6f9 Feat: Implement Phase 3 - Vector Database & Embeddings (Core Implementation)`

### Session 4: Phase 3 Bug Fix #1
- **Date:** Jan 13, 2026 00:43
- **Work Done:**
  - Fixed ORA-17004 JDBC VECTOR error
  - Modified repository query to exclude embedding column
  - Updated VectorSearchService result parsing
- **Commit:** `03fdaa3 Fix: Resolve Oracle JDBC VECTOR column handling in semantic search`

### Session 5: Phase 3 Bug Fixes #2 & #3 + Documentation
- **Date:** Jan 13, 2026 03:00+
- **Branch:** `claude/continue-llm-ai-mvp-xYiaT`
- **Work Done:**
  - Fixed CLOB proxy casting error (Bug #2)
  - Fixed CLOB content truncation (Bug #3)
  - Implemented proper CLOB reading with `getSubString()`
  - Created comprehensive PHASE-3-GUIDE.md (50KB)
  - Created CLAUDE.md session reference guide
  - User tested semantic search - results working correctly
  - Phase 3 fully functional and validated
- **Commits:**
  - `f0c5967` - Fix: Resolve CLOB proxy casting + Add comprehensive documentation
  - `f159609` - Fix: Resolve CLOB content truncation using getSubString() method

### Session 6: Branch Consolidation (CURRENT)
- **Date:** Jan 13, 2026
- **Branch:** `claude/develop-stable-nSxCU` (newly created stable/default branch)
- **Work Done:**
  - Analyzed both feature branches (`claude/llm-agentic-ai-mvp-bRhG6` and `claude/continue-llm-ai-mvp-xYiaT`)
  - Compared code differences - confirmed `claude/continue-llm-ai-mvp-xYiaT` most stable
  - Created new `claude/develop-stable-nSxCU` branch from stable code
  - Updated CLAUDE.md to reflect new branch structure
  - Documented branch consolidation and repository structure
  - Updated all git workflow instructions
  - Prepared for Phase 4 (RAG) work
- **Branch Status:**
  - ✅ `claude/develop-stable-nSxCU` - Stable/default branch (production-ready)
  - 📦 `claude/llm-agentic-ai-mvp-bRhG6` - Historical (kept for reference)
  - 📦 `claude/continue-llm-ai-mvp-xYiaT` - Historical (kept for reference)

---

## ⚠️ Important Notes for Future Sessions

### DO NOT
- ❌ Push directly to `claude/develop-stable-nSxCU` without testing - Create feature branches for new work
- ❌ Commit without explicit user request
- ❌ Create PRs without user confirmation
- ❌ Run destructive operations (force push, hard reset)
- ❌ Modify `.env` file (keep API keys safe)
- ❌ Delete Docker volumes without confirmation

### DO
- ✅ Always check `git status` first
- ✅ Read this CLAUDE.md file completely
- ✅ Read the current phase guide (PHASE-X-GUIDE.md)
- ✅ Work on `claude/develop-stable-nSxCU` branch for stable work, or create feature branches for experimental work
- ✅ Test thoroughly before committing
- ✅ Update this CLAUDE.md at end of session
- ✅ Document any new bugs/fixes clearly

### Git Workflow
```bash
# Check current branch
git status

# Work on stable branch (stable work)
git checkout claude/develop-stable-nSxCU

# Stage changes
git add <files>

# Commit (only when user requests)
git commit -m "Clear, descriptive message"

# Push to develop
git push -u origin develop

# For new features, create feature branch
git checkout -b claude/feature-name-sessionId

# Create PR (only when user requests)
gh pr create --title "Title" --body "Description"
```

---

## 🔍 Debugging Checklist

### If Semantic Search Returns Empty Results

1. **Check documents exist:**
```bash
curl http://localhost:8080/api/documents
```

2. **Check embeddings were generated:**
```sql
SELECT id, LENGTH(embedding) FROM document_chunks LIMIT 5;
-- Should show LENGTH(embedding) > 0
```

3. **Check similarity threshold:**
```json
// Try lowering threshold
{"query": "test", "similarityThreshold": 0.0}
```

4. **Check category filter:**
```json
// Remove category filter
{"query": "test", "category": null}
```

5. **Check application logs:**
```bash
# Look for errors in logs
tail -f logs/application.log
# Or console output
```

### If Application Won't Start

1. **Check Oracle is healthy:**
```bash
docker-compose ps
# Status should be "healthy" not "starting"
```

2. **Check .env file exists:**
```bash
cat .env
# Should contain GEMINI_API_KEY
```

3. **Check port 8080 is free:**
```bash
netstat -an | grep 8080
# Should be empty or show LISTENING
```

4. **Check logs for errors:**
```bash
mvn spring-boot:run
# Read startup logs carefully
```

### If Embeddings Fail

1. **Check Gemini API key:**
```bash
curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_KEY"
# Should return list of models
```

2. **Check rate limits:**
```
429 Too Many Requests = Exceeded 60 req/min
Solution: Wait 1 minute or reduce chunks
```

3. **Check network connectivity:**
```bash
ping generativelanguage.googleapis.com
```

---

## 📈 Progress Tracking

### Phase 3 Completion Criteria

- [x] Document upload endpoint working
- [x] Document chunking with overlap
- [x] Embedding generation via Gemini
- [x] Vector storage in Oracle VECTOR column
- [x] HNSW index created
- [x] Semantic search returning results
- [x] Similarity scoring (0.0-1.0)
- [x] Category filtering
- [x] Bug #1 fixed (JDBC VECTOR)
- [x] Bug #2 fixed (CLOB proxy)
- [x] PHASE-3-GUIDE.md created
- [ ] **FINAL TESTING REQUIRED** (User to run tests on local machine)
- [ ] Final commit after successful testing
- [ ] Phase 3 completion commit

### Next Phase Preview: RAG (Phase 4)

**Goal:** Combine semantic search (Phase 3) + LLM (Phase 1) to build intelligent Q&A

**Pipeline:**
```
User Question
    ↓
1. Semantic Search (find relevant chunks)
    ↓
2. Context Assembly (combine chunks)
    ↓
3. LLM Generation (answer with context)
    ↓
4. Citation Tracking (source attribution)
    ↓
Answer + Sources
```

**Implementation Steps:**
1. Create RAG service
2. Context window management
3. Citation tracking DTO
4. Multi-query strategies
5. Answer quality scoring
6. Create PHASE-4-GUIDE.md

---

## 🎯 Current Status Summary

**✅ What's Working:**
- All Phase 1 features (LLM integration)
- All Phase 2 features (Prompt engineering)
- All Phase 3 features (Vector database & semantic search) ⭐ FULLY TESTED
- Docker environment (Oracle 23c)
- Database schema with VECTOR support
- Bug #1: JDBC VECTOR column handling ✅
- Bug #2: CLOB proxy casting ✅
- Bug #3: CLOB content truncation ✅
- Semantic search returning full chunk content with correct similarity scores

**✅ Validated Features:**
- Document upload with chunking (1000 chars, 200 overlap)
- Embedding generation via Gemini (768 dimensions)
- Semantic search with vector similarity
- Similarity scores accurate (0.77-0.8 = 77-80% match)
- Results ranked correctly by relevance

**📝 What's Next:**
1. ~~Test Phase 3~~ ✅ COMPLETE
2. Mark Phase 3 as complete ✅
3. Start Phase 4 (RAG) - Retrieval Augmented Generation
4. Implement RAG pipeline (semantic search + LLM + citations)
5. Create PHASE-4-GUIDE.md

---

## 💡 Tips for Next Claude Session

### Starting a New Session

1. **Read this file first** (CLAUDE.md)
2. **Check git status:** `git status`
3. **Check recent commits:** `git log --oneline -5`
4. **Ask user:** "Where did we leave off? What should I focus on?"
5. **Read relevant guide:** PHASE-3-GUIDE.md or next phase guide

### During the Session

1. **Stay focused:** One phase at a time
2. **Test frequently:** Don't write code without testing
3. **Document as you go:** Update guides with new findings
4. **Commit strategically:** Only when user requests
5. **Update CLAUDE.md:** Add session notes at end

### Ending a Session

1. **Summarize what was done**
2. **List what's pending**
3. **Update this CLAUDE.md file** with session notes
4. **Commit if requested** (with clear message)
5. **State clear next steps** for next session

---

## 📞 Quick Reference

**Project Repository:** nooreldeen-nabil/ai-small-project
**Default Branch:** `claude/develop-stable-nSxCU` (stable/production-ready)
**Historical Branches:** `claude/llm-agentic-ai-mvp-bRhG6`, `claude/continue-llm-ai-mvp-xYiaT`
**Local Server:** http://localhost:8080
**Swagger UI:** http://localhost:8080/swagger-ui.html
**Database:** localhost:1521/FREEPDB1 (ai_user/ai_password_2024)
**Oracle EM Express:** https://localhost:5500/em

**Key Documentation Files:**
- `README.md` - Project overview
- `PROJECT-PLAN.md` - 7-phase roadmap
- `PHASE-1-GUIDE.md` - LLM integration (23KB)
- `PHASE-2-GUIDE.md` - Prompt engineering (46KB)
- `PHASE-3-GUIDE.md` - Vector database (50KB)
- `CLAUDE.md` - This file (session reference)

**Support:**
- Gemini API Docs: https://ai.google.dev/gemini-api/docs
- Oracle Vector Search: https://docs.oracle.com/en/database/oracle/oracle-database/23/vecse/
- Spring Boot: https://spring.io/projects/spring-boot

---

## ✨ Session Success Criteria

A successful session should:
- ✅ Complete at least 1 meaningful task
- ✅ Test all changes thoroughly
- ✅ Document new features/fixes
- ✅ Update this CLAUDE.md file
- ✅ Leave project in runnable state
- ✅ Provide clear next steps

---

**END OF CLAUDE.MD**

*Last updated: 2026-01-13 - Session 6 - Branch Consolidation*

---

## 🎉 PHASE 3 COMPLETE!

**Current Status:** ✅ Phase 3 FULLY TESTED & WORKING
**Next Phase:** Phase 4 (RAG - Retrieval Augmented Generation)
**Branch:** `claude/develop-stable-nSxCU` (stable/default)
**Latest Commits:**
- `f0c5967` - CLOB proxy casting fix + Documentation
- `f159609` - CLOB content truncation fix
- Branch consolidation completed (Jan 13, 2026)

**What Was Validated:**
```json
{
  "query": "How do AI models learn?",
  "results": [
    {
      "documentTitle": "Introduction to Neural Networks",
      "chunkIndex": 2,
      "content": "...full 1000 character chunks...",
      "similarityScore": 0.8,
      "category": "AI/ML"
    }
  ],
  "totalResults": 3
}
```

✅ Semantic search working
✅ Full chunk content (no truncation)
✅ Accurate similarity scores (0.77-0.8)
✅ Results ranked by relevance
✅ All 3 bugs fixed and tested

**Ready for Phase 4!** 🚀
