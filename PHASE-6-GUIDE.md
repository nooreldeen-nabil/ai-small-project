# Phase 6: Agentic AI - Camunda Workflows (BPMN Orchestration)

**Status:** ✅ COMPLETE
**Branch:** `claude/add-camunda-docker-compose-0FXHW`
**Date:** January 15, 2026
**Integration:** Phase 3 (Vector DB) + Phase 5 (AI Agent)

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Key Concepts](#key-concepts)
4. [BPMN Workflow](#bpmn-workflow)
5. [Job Workers](#job-workers)
6. [API Endpoints](#api-endpoints)
7. [Test Cases](#test-cases)
8. [Troubleshooting](#troubleshooting)
9. [Integration Guide](#integration-guide)
10. [Next Steps](#next-steps)

---

## 🎯 Overview

Phase 6 demonstrates **workflow orchestration** using Camunda Platform 8 (Zeebe). It showcases:

- **BPMN 2.0 workflows** for complex business processes
- **Conditional branching** based on AI confidence scores
- **Sequential execution** for dependent tasks
- **Multi-step document processing** with AI workers
- **Integration** with vector database and AI agents

### What Was Built

**1 BPMN Workflow + 5 Job Workers:**
- `document-processing.bpmn` - Document processing workflow
- `ClassificationWorker` - AI-powered document classification
- `DataExtractionWorker` - Structured data extraction
- `ManualReviewWorker` - Simulated manual review (low confidence)
- `SaveDocumentWorker` - Vector database integration
- `NotificationWorker` - Completion notifications

### Learning Objectives

✅ Understand workflow orchestration patterns
✅ Implement conditional branching (exclusive gateways)
✅ Integrate AI agents with workflow engines
✅ Handle sequential and parallel execution
✅ Implement error handling in workflows

---

## 🏗️ Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                     Camunda Platform 8                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Zeebe   │  │ Operate  │  │ Tasklist │  │ Identity │   │
│  │  Engine  │  │   UI     │  │   UI     │  │  (Auth)  │   │
│  │ :26500   │  │  :8081   │  │  :8082   │  │  :18080  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↕ gRPC
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Application (:8080)                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Zeebe Job Workers                       │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐   │   │
│  │  │ Classify   │  │  Extract   │  │   Review   │   │   │
│  │  │  Worker    │  │   Worker   │  │   Worker   │   │   │
│  │  └────────────┘  └────────────┘  └────────────┘   │   │
│  │  ┌────────────┐  ┌────────────┐                    │   │
│  │  │   Save     │  │   Notify   │                    │   │
│  │  │  Worker    │  │   Worker   │                    │   │
│  │  └────────────┘  └────────────┘                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                            ↕                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │          Integration Services                        │   │
│  │  ┌────────────┐         ┌────────────┐             │   │
│  │  │  Agent     │         │ Document   │             │   │
│  │  │  Service   │←────────│  Service   │             │   │
│  │  │ (Phase 5)  │         │ (Phase 3)  │             │   │
│  │  └────────────┘         └────────────┘             │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│  ┌────────────┐         ┌────────────┐                     │
│  │  Gemini    │         │ Oracle 23c │                     │
│  │    API     │         │ Vector DB  │                     │
│  └────────────┘         └────────────┘                     │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow

```
User Request → REST API → Workflow Service → Zeebe Engine
                                                  ↓
                                    [Start Workflow Instance]
                                                  ↓
                              Job Worker Polling Loop
                                                  ↓
                    ┌──────────────┬──────────────┬──────────────┐
                    ↓              ↓              ↓              ↓
            ClassificationWorker  DataExtractionWorker  SaveDocumentWorker
                    ↓              ↓              ↓              ↓
               [AI Agent]     [AI Agent]    [Vector DB]   [Notification]
                    ↓              ↓              ↓              ↓
            [Update Variables] [Update Variables] [Update Variables]
                    ↓              ↓              ↓              ↓
                              Complete Workflow
                                    ↓
                         [End Event - Success]
```

---

## 🧠 Key Concepts

### 1. Workflow Engine (Zeebe)

**What is Zeebe?**
- Cloud-native workflow engine for Camunda Platform 8
- Horizontally scalable, fault-tolerant
- Uses gRPC for communication (not REST)
- Stores workflow state persistently

**Key Features:**
- **Process Orchestration**: Coordinates multiple services
- **Visual Modeling**: BPMN 2.0 diagrams
- **High Throughput**: Handles thousands of workflow instances
- **Fault Tolerance**: Automatic retries and incident management

### 2. BPMN 2.0 (Business Process Model and Notation)

**Core Elements:**

| Element | Symbol | Purpose |
|---------|--------|---------|
| **Start Event** | ⭕ | Workflow entry point |
| **End Event** | ⭕ (thick border) | Workflow completion |
| **Service Task** | 📋 | Automated task (job worker) |
| **Exclusive Gateway** | ◆ with X | ONE path chosen (conditional) |
| **Parallel Gateway** | ◆ with + | ALL paths execute simultaneously |
| **Sequence Flow** | → | Connects elements |

### 3. Job Workers

**What is a Job Worker?**
- External service that polls Zeebe for jobs
- Executes business logic
- Returns results as process variables
- Decoupled from workflow engine

**Worker Pattern:**
```java
@JobWorker(type = "job-type", autoComplete = true)
public Map<String, Object> handleJob(
    final ActivatedJob job,
    @Variable(name = "inputVar") String input
) {
    // 1. Execute business logic
    String result = processInput(input);

    // 2. Return output variables
    return Map.of("outputVar", result);
}
```

### 4. Process Variables

**Scope and Visibility:**
- Variables flow through the workflow
- Set by job workers, read by subsequent tasks
- Serialized as JSON
- Support all JSON-compatible types

**Important:** In sequential execution, variables from previous tasks are visible to next tasks. In parallel execution, variables from parallel branches are NOT visible to each other until after the join gateway.

---

## 📊 BPMN Workflow

### document-processing.bpmn

**Visual Flow:**
```
Start → Classify → [High/Low Confidence?]
                          ↓
              ┌───────────┴───────────┐
              ↓                       ↓
         confidence >= 0.8      confidence < 0.8
              ↓                       ↓
        Extract Data           Manual Review
              ↓                       ↓
              └───────────┬───────────┘
                          ↓
                      [Merge]
                          ↓
                    Save Document
                          ↓
                   Send Notification
                          ↓
                         End
```

### Workflow Steps Explained

#### 1. **Start Event**
- Receives input variables: `documentContent`, `documentTitle`
- Triggers workflow instance creation

#### 2. **Classify Document** (Service Task)
- **Job Type:** `classify-document`
- **Worker:** `ClassificationWorker`
- **Input:** `documentContent`, `documentTitle`
- **Output:** `category`, `confidence`, `classificationSummary`
- **Logic:** Uses AI Agent to classify document and calculate confidence

#### 3. **High Confidence?** (Exclusive Gateway)
- **Decision Point:** Routes based on `confidence` variable
- **High Path:** `confidence >= 0.8` → Extract Data
- **Low Path:** `confidence < 0.8` → Manual Review

#### 4a. **Extract Data** (Service Task - High Confidence Path)
- **Job Type:** `extract-data`
- **Worker:** `DataExtractionWorker`
- **Input:** `documentContent`, `category`
- **Output:** `extractedData`, `dataQuality`
- **Logic:** Category-specific data extraction using AI

#### 4b. **Request Manual Review** (Service Task - Low Confidence Path)
- **Job Type:** `manual-review`
- **Worker:** `ManualReviewWorker`
- **Input:** `documentContent`, `category`, `confidence`
- **Output:** `reviewStatus`, `reviewerNotes`, `extractedData`
- **Logic:** Simulates manual review (auto-approves for MVP)

#### 5. **Merge** (Exclusive Gateway)
- **Purpose:** Merges high/low confidence paths back together
- **Output:** Single flow continues to save task
- **Critical:** Ensures only ONE path was taken

#### 6. **Save Document** (Service Task)
- **Job Type:** `save-document`
- **Worker:** `SaveDocumentWorker`
- **Input:** `documentContent`, `documentTitle`, `category`, `extractedData`
- **Output:** `documentId`, `documentSaved`, `chunksCreated`
- **Logic:** Saves to Oracle vector database (Phase 3 integration)

#### 7. **Send Notification** (Service Task)
- **Job Type:** `send-notification`
- **Worker:** `NotificationWorker`
- **Input:** `documentTitle`, `category`, `documentSaved`, `documentId`
- **Output:** `notificationSent`, `notificationChannel`, `notificationMessage`
- **Logic:** Simulates notification (email/Slack/Teams)

#### 8. **End Event**
- Workflow completes successfully
- All variables preserved for audit

### Gateway Logic Explained

**Why Exclusive Merge is Critical:**

❌ **WRONG (Deadlock):**
```
Classify → Exclusive Split → [Extract OR Review] → Parallel Split
                   ↓                                      ↑
            (1 path chosen)                    (expects 2 incoming)
                                                 = DEADLOCK!
```

✅ **CORRECT:**
```
Classify → Exclusive Split → [Extract OR Review] → Exclusive Merge → Save → Notify
                   ↓                                      ↓
            (1 path chosen)                         (merges 1 path)
                                                     ✓ Continues!
```

---

## 👷 Job Workers

### 1. ClassificationWorker

**File:** `src/main/java/com/ai/mvp/workflow/worker/ClassificationWorker.java`

**Purpose:** AI-powered document classification

**Key Features:**
- Uses AI Agent (Phase 5) for classification
- Structured prompt with category options
- Confidence scoring (0.0 - 1.0)
- Fallback to "GENERAL" category on error

**Prompt Structure:**
```
Classify the following document and provide:
1. Category (technical, business, legal, medical, general, or other)
2. Confidence score (0.0 to 1.0)
3. Brief summary

Response Format:
Category: [category name]
Confidence: [0.0-1.0]
Summary: [brief summary]
```

**Output Variables:**
- `category`: Document category (uppercase)
- `confidence`: Score from 0.0 to 1.0
- `classificationSummary`: Brief description
- `classificationExecutionTimeMs`: Processing time
- `agentTokensUsed`: LLM tokens consumed

**Error Handling:**
- Catches all exceptions
- Returns `UNKNOWN` category with 0.0 confidence
- Logs error details

### 2. DataExtractionWorker

**File:** `src/main/java/com/ai/mvp/workflow/worker/DataExtractionWorker.java`

**Purpose:** Extract structured information from documents

**Category-Specific Extraction:**

| Category | Extraction Focus |
|----------|-----------------|
| **TECHNICAL** | Technologies, specs, versions, architecture, metrics |
| **BUSINESS** | Objectives, stakeholders, financials, timelines, risks |
| **LEGAL** | Parties, terms, dates, obligations, regulations |
| **GENERAL** | People, organizations, dates, topics, facts |

**Data Quality Scoring:**
```java
lengthScore = min(content.length / 500, 1.0)      // 60% weight
structureScore = min((bulletPoints + sections) / 10, 1.0)  // 40% weight
dataQuality = lengthScore * 0.6 + structureScore * 0.4
```

**Output Variables:**
- `extractedData`: Structured summary with bullet points
- `dataQuality`: Quality score (0.0 - 1.0)
- `extractionExecutionTimeMs`: Processing time
- `extractionTokensUsed`: LLM tokens consumed

### 3. ManualReviewWorker

**File:** `src/main/java/com/ai/mvp/workflow/worker/ManualReviewWorker.java`

**Purpose:** Simulate manual review for low-confidence documents

**MVP Behavior:**
- Logs document details
- Simulates 0.5s review delay
- Auto-approves all documents
- Sets placeholder extracted data

**Production Ready:**
```java
// In production, this would:
// 1. Create task in Camunda Tasklist
// 2. Assign to human reviewer
// 3. Wait for human input
// 4. Continue workflow with reviewer's decision
```

**Output Variables:**
- `reviewStatus`: "APPROVED" (auto in MVP)
- `reviewerNotes`: Description of review
- `reviewedBy`: "MVP_AUTO_REVIEWER"
- `extractedData`: Placeholder data
- `dataQuality`: 0.7 (assumed after review)

### 4. SaveDocumentWorker

**File:** `src/main/java/com/ai/mvp/workflow/worker/SaveDocumentWorker.java`

**Purpose:** Save document to vector database (Phase 3 integration)

**Process:**
1. Create `DocumentUploadRequest`
2. Call `DocumentService.uploadDocument()` (Phase 3)
3. Document stored in Oracle 23c
4. Text chunked with overlap
5. Embeddings generated via Gemini
6. Chunks stored with vector embeddings
7. Return document ID and chunk count

**Output Variables:**
- `documentId`: UUID string (e.g., "380c9780-b7fe-4fb7-afa9-75ff8d59b31d")
- `documentSaved`: `true` on success, `false` on error
- `chunksCreated`: Number of chunks created
- `saveExecutionTimeMs`: Processing time
- `saveError`: Error message (only if failed)

**Error Handling:**
- Catches database errors, API errors, network issues
- Returns `documentSaved: false`
- Logs full error stack trace

### 5. NotificationWorker

**File:** `src/main/java/com/ai/mvp/workflow/worker/NotificationWorker.java`

**Purpose:** Send completion notifications

**Channel Routing:**
```java
Category          → Channel
─────────────────────────────
TECHNICAL         → "TECH_TEAM_SLACK"
BUSINESS          → "BUSINESS_EMAIL"
LEGAL             → "LEGAL_TEAM_EMAIL"
Save Failed       → "ALERT_CHANNEL"
Default           → "GENERAL_NOTIFICATION"
```

**Message Format:**

Success:
```
✅ Document 'Title' processed successfully!
Category: TECHNICAL
Document ID: 380c9780-b7fe-4fb7-afa9-75ff8d59b31d
Status: Saved and indexed for semantic search
```

Failure:
```
⚠️ Document 'Title' processed with issues.
Category: TECHNICAL
Status: Processing completed but save failed
```

**Output Variables:**
- `notificationSent`: `true` or `false`
- `notificationChannel`: Routing channel
- `notificationMessage`: Full message text
- `notificationExecutionTimeMs`: Processing time

---

## 🚀 API Endpoints

### Start Document Processing Workflow

**Endpoint:** `POST /api/workflow/start-document`

**Request:**
```json
{
  "title": "Machine Learning Best Practices",
  "content": "Machine learning enables systems to learn from data..."
}
```

**Response:**
```json
{
  "message": "Document processing workflow started",
  "processInstanceKey": 2251799813894090,
  "operateUrl": "http://localhost:8081/processes/2251799813894090",
  "tasklistUrl": "http://localhost:8082",
  "title": "Machine Learning Best Practices"
}
```

**Response Fields:**
- `processInstanceKey`: Unique workflow instance ID
- `operateUrl`: Direct link to monitor in Operate UI
- `tasklistUrl`: Tasklist for human tasks (future use)

### Get Process Status

**Endpoint:** `GET /api/workflow/status/{processInstanceKey}`

**Response:**
```json
{
  "processInstanceKey": 2251799813894090,
  "status": "running",
  "message": "Use Operate UI for detailed status: http://localhost:8081/processes/2251799813894090"
}
```

**Note:** For detailed status, use Camunda Operate UI or Zeebe API.

### Cancel Process

**Endpoint:** `POST /api/workflow/cancel/{processInstanceKey}`

**Response:**
```json
{
  "message": "Process instance canceled successfully",
  "processInstanceKey": 2251799813894090
}
```

**Effect:** Immediately terminates workflow, cancels all active jobs.

### Health Check

**Endpoint:** `GET /api/workflow/health`

**Response:**
```json
{
  "status": "UP",
  "message": "Workflow service is operational"
}
```

---

## 🧪 Test Cases

### Test Case 1: High Confidence Path (Automated)

**Scenario:** Technical document with clear content

**Request:**
```bash
curl -X POST http://localhost:8080/api/workflow/start-document \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Machine Learning Best Practices",
    "content": "Machine learning enables systems to learn from data. Best practices include quality data, feature engineering, cross-validation, and regular retraining. Neural networks, decision trees, and ensemble methods are common algorithms."
  }'
```

**Expected Flow:**
1. ✅ Classify → Category: TECHNICAL, Confidence: 0.95
2. ✅ High Confidence Path → Extract Data
3. ✅ Extract: Technologies, best practices, algorithms
4. ✅ Merge → Continue to Save
5. ✅ Save → Oracle DB with embeddings
6. ✅ Notify → Success message
7. ✅ End → Complete

**Expected Variables:**
```json
{
  "category": "TECHNICAL",
  "confidence": 0.95,
  "extractedData": "Technologies: ML, Neural networks...",
  "dataQuality": 0.85,
  "documentId": "380c9780-...",
  "documentSaved": true,
  "chunksCreated": 1,
  "notificationChannel": "TECH_TEAM_SLACK",
  "notificationMessage": "✅ Document ... processed successfully!"
}
```

### Test Case 2: Low Confidence Path (Manual Review)

**Scenario:** Ambiguous/unclear content

**Request:**
```bash
curl -X POST http://localhost:8080/api/workflow/start-document \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Unclear Document",
    "content": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
  }'
```

**Expected Flow:**
1. ✅ Classify → Category: GENERAL, Confidence: 0.50
2. ✅ Low Confidence Path → Manual Review
3. ✅ Review: Simulated (0.5s delay), auto-approve
4. ✅ Merge → Continue to Save
5. ✅ Save → Oracle DB
6. ✅ Notify → Success message
7. ✅ End → Complete

**Expected Variables:**
```json
{
  "category": "GENERAL",
  "confidence": 0.50,
  "reviewStatus": "APPROVED",
  "reviewerNotes": "Simulated manual review - auto-approved for MVP",
  "extractedData": "Manual review completed...",
  "dataQuality": 0.7,
  "documentId": "9f3a7e2c-...",
  "documentSaved": true
}
```

### Test Case 3: Multiple Chunks (Large Document)

**Scenario:** Long technical document requiring chunking

**Request:**
```bash
curl -X POST http://localhost:8080/api/workflow/start-document \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Deep Learning Architecture Guide",
    "content": "Deep learning is a subset of machine learning that uses artificial neural networks with multiple layers. Convolutional Neural Networks (CNNs) are particularly effective for image recognition tasks, using convolutional layers to detect features hierarchically. Recurrent Neural Networks (RNNs) excel at sequence prediction tasks like natural language processing. Long Short-Term Memory (LSTM) networks address the vanishing gradient problem in RNNs. Transformer architectures with attention mechanisms have revolutionized NLP, enabling models like BERT and GPT. Training deep networks requires techniques like dropout for regularization, batch normalization for stable training, and learning rate scheduling. Modern frameworks like TensorFlow and PyTorch provide high-level APIs for building complex architectures. GPUs accelerate training through parallel matrix operations. Transfer learning allows fine-tuning pre-trained models for specific tasks with limited data."
  }'
```

**Expected:**
- High confidence path
- `chunksCreated: 2` or `3` (content > 1000 chars triggers chunking)
- Multiple embedding generation calls
- Longer processing time

### Test Case 4: Business Document

**Scenario:** Business strategy document

**Request:**
```bash
curl -X POST http://localhost:8080/api/workflow/start-document \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Q4 2025 Business Strategy",
    "content": "Our Q4 business strategy focuses on three key objectives: revenue growth through market expansion, operational efficiency improvements, and customer retention initiatives. We will target emerging markets in Southeast Asia with a 15% budget allocation. Cost optimization through automation is expected to reduce operational expenses by 12%. Customer success programs aim to improve retention rates from 85% to 92%. Key stakeholders include the executive team, regional managers, and finance department."
  }'
```

**Expected:**
- Category: BUSINESS
- Extract: Objectives, budgets, stakeholders, metrics
- notificationChannel: "BUSINESS_EMAIL"
- High confidence extraction

---

## 🔧 Troubleshooting

### Issue 1: Workflow Stuck at Gateway

**Symptom:** Process doesn't complete, stuck at Gateway_Split

**Diagnosis:**
```bash
# Check Operate UI
# Look for "Incident" badge
# Check which task is waiting
```

**Common Causes:**
1. **Parallel gateway expecting multiple flows but only one arrives**
   - Solution: Use Exclusive Merge before Parallel Split
2. **Missing required variable**
   - Solution: Check job worker returns all required variables
3. **Job worker crashed**
   - Solution: Check application logs, restart worker

**Fix:** Already implemented - Exclusive Merge Gateway added

### Issue 2: Job Not Picked Up by Worker

**Symptom:** Task remains "active" but never completes

**Diagnosis:**
```bash
# Check application logs
grep "JobWorker.*registered" logs/application.log

# Check Zeebe connection
grep "Connected to Zeebe" logs/application.log
```

**Common Causes:**
1. **Worker not running** - Application not started
2. **Job type mismatch** - BPMN uses different type than @JobWorker
3. **Zeebe connection failed** - Check port 26500 accessible

**Solution:**
```bash
# Verify worker registration
mvn spring-boot:run | grep "JobWorker"
# Should see: "classify-document", "extract-data", etc.

# Verify Zeebe connection
curl http://localhost:9600/ready  # Zeebe health check
```

### Issue 3: Database Save Fails

**Symptom:** `documentSaved: false`, `saveError` variable present

**Diagnosis:**
```bash
# Check Oracle DB running
docker-compose ps
# oracle-23c should show "healthy"

# Check Gemini API key
echo $GEMINI_API_KEY
# Should not be empty

# Check logs
grep "SaveDocumentWorker.*failed" logs/application.log
```

**Common Causes:**
1. **Oracle DB not running** - Start docker-compose
2. **Gemini API key missing** - Set in .env file
3. **Network issue** - Check connectivity

**Solution:**
```bash
# Restart Oracle
docker-compose restart oracle-23c

# Verify .env has GEMINI_API_KEY
cat .env | grep GEMINI_API_KEY

# Test database connection
curl http://localhost:8080/actuator/health
```

### Issue 4: Classification Returns Low Confidence

**Symptom:** All documents go to Manual Review path

**Diagnosis:** Check `confidence` variable in Operate UI

**Common Causes:**
1. **Vague/short content** - AI can't classify confidently
2. **Non-English content** - Gemini may struggle
3. **Prompt issues** - Classification prompt needs tuning

**Solution:**
- Provide clearer, longer content
- Add more context in title
- Adjust confidence threshold in BPMN (change 0.8 to 0.7)

### Issue 5: High Token Usage

**Symptom:** Slow processing, high Gemini API costs

**Diagnosis:** Check `extractionTokensUsed`, `agentTokensUsed` variables

**Optimization:**
```java
// In workers, reduce content length
String truncatedContent = content.length() > 2000
    ? content.substring(0, 2000) + "..."
    : content;

// Lower temperature for classification
agentRequest.setTemperature(0.2);  // More deterministic

// Use smaller model (if needed)
// Switch from gemini-2.5-flash to gemini-1.5-flash
```

---

## 🔗 Integration Guide

### Phase 3 Integration (Vector Database)

**SaveDocumentWorker → DocumentService:**

```java
// Worker creates upload request
DocumentUploadRequest uploadRequest = new DocumentUploadRequest();
uploadRequest.setTitle(documentTitle);
uploadRequest.setContent(documentContent);
uploadRequest.setCategory(category);

// Calls Phase 3 service
var response = documentService.uploadDocument(uploadRequest);

// Returns document ID
String documentId = response.getDocumentId();  // UUID format
```

**What Happens:**
1. Document stored in `documents` table
2. Text chunked (1000 chars, 200 overlap)
3. Embeddings generated via Gemini (768 dimensions)
4. Chunks stored in `document_chunks` with vectors
5. Searchable via semantic search API

### Phase 5 Integration (AI Agent)

**ClassificationWorker → AgentService:**

```java
// Build classification task
AgentTaskRequest request = new AgentTaskRequest();
request.setTask(classificationPrompt);
request.setMaxToolCalls(5);      // Allow tool use
request.setTemperature(0.3);     // Consistent classification

// Execute agent
AgentTaskResponse response = agentService.executeTask(request);

// Parse structured result
String answer = response.getAnswer();
// Extract category, confidence, summary
```

**Agent Capabilities:**
- Can use tools (search, calculate, database query)
- Multi-turn reasoning
- Structured output parsing
- Token tracking

### Camunda Operate UI Integration

**Monitor Workflows:**
1. Open http://localhost:8081
2. Login: `demo` / `demo`
3. Navigate to "Processes"
4. Click "Document Processing Workflow"
5. View all instances
6. Click instance ID to see:
   - Visual flow diagram
   - Current state (green = complete, yellow = active)
   - All process variables
   - Execution timeline
   - Incidents (if any)

**Useful Views:**
- **Instance History:** Step-by-step execution log
- **Variables:** All data flowing through workflow
- **Incidents:** Errors and retries

---

## 📈 Performance Considerations

### Throughput

**Current Setup:**
- Worker threads: 2
- Max jobs active: 32
- Expected throughput: ~10-20 documents/minute

**Scaling Options:**
```yaml
# application.yml
camunda:
  client:
    zeebe:
      num-job-worker-execution-threads: 10  # More parallelism
      max-jobs-active: 100                   # More concurrent jobs
```

### Latency

**Typical Execution Times:**
- Classification: 2-3 seconds (AI call)
- Data Extraction: 3-5 seconds (AI call)
- Manual Review: 0.5 seconds (simulated)
- Save Document: 2-4 seconds (embeddings + DB)
- Notification: 0.3 seconds (simulated)

**Total: 8-15 seconds end-to-end**

### Cost Optimization

**Gemini API Costs:**
- Classification: ~300-500 tokens
- Extraction: ~500-800 tokens
- Total per document: ~800-1300 tokens

**Optimization:**
1. Truncate long documents
2. Lower temperature for classification
3. Cache embeddings (if repeated content)
4. Use batch processing for multiple documents

---

## ✅ Next Steps

### Phase 7: Integration & Polish

**Objectives:**
1. **Frontend UI** - React/Vue dashboard for workflow management
2. **Enhanced Monitoring** - Metrics, dashboards, alerts
3. **Advanced Workflows** - Error handling, retries, timeouts
4. **Production Readiness** - Security, logging, deployment
5. **Demo Preparation** - Showcase for non-technical managers

**Key Features to Add:**
- Upload multiple documents via UI
- View workflow status in real-time
- Search processed documents (Phase 3 semantic search)
- Ask questions about documents (Phase 4 RAG)
- Trigger workflows via API or UI
- Monitor system health

---

## 📚 Additional Resources

### Documentation
- **Camunda 8 Docs:** https://docs.camunda.io/docs/8.7/
- **Zeebe Client Java:** https://github.com/camunda/camunda-platform-sdk-java
- **BPMN 2.0 Tutorial:** https://camunda.com/bpmn/
- **Spring Zeebe Integration:** https://docs.camunda.io/docs/apis-tools/spring-zeebe-sdk/

### Tools
- **Camunda Modeler:** https://camunda.com/download/modeler/
- **Zeebe Monitor:** http://localhost:9600/ (built-in)
- **Operate UI:** http://localhost:8081
- **Tasklist UI:** http://localhost:8082

### Learning Resources
- BPMN Quick Reference: https://www.bpmnquickguide.com/
- Workflow Patterns: http://workflowpatterns.com/
- Zeebe Examples: https://github.com/camunda-community-hub/zeebe-client-examples

---

## 🎉 Phase 6 Complete!

**What You've Learned:**
✅ Workflow orchestration with Camunda/Zeebe
✅ BPMN 2.0 modeling and patterns
✅ Job worker pattern for microservices
✅ Conditional branching with exclusive gateways
✅ Sequential execution for dependent tasks
✅ Integration of AI agents with workflows
✅ Production-ready error handling

**What You've Built:**
✅ Complex document processing workflow
✅ 5 specialized job workers
✅ Integration with vector DB (Phase 3) and AI agent (Phase 5)
✅ REST API for workflow management
✅ Complete end-to-end document processing pipeline

**Ready for Phase 7:** Frontend UI + Demo Preparation! 🚀

---

**Last Updated:** 2026-01-15 - Session 9
**Status:** ✅ COMPLETE AND TESTED
**Next Phase:** Phase 7 (Integration & Polish)
