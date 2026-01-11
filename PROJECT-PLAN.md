# LLM Agentic AI MVP - Phased Learning Plan

## 🎯 Project Goal
Build an intelligent document Q&A system that demonstrates LLM, Agentic AI, Prompt Engineering, and Vector Database concepts.

---

## 📚 Project Phases (5-7 Days)

### **Phase 1: Foundation Setup + LLM Integration** (Day 1)
**Goal:** Understand LLMs and basic API integration

#### What You'll Learn:
- ✅ What is an LLM (Large Language Model)?
- ✅ How to call LLM APIs (Google Gemini as primary FREE provider)
- ✅ Request/Response structure
- ✅ Token management and streaming

#### What We'll Build:
- Spring Boot project skeleton
- REST endpoint: `/api/chat` - simple chat with LLM
- Multi-provider LLM configuration (Gemini + Claude support)
- Basic error handling

#### Deliverables:
- Working chatbot endpoint
- Understanding of LLM capabilities and limitations

---

### **Phase 2: Prompt Engineering Techniques** (Day 2)
**Goal:** Master different prompting strategies

#### What You'll Learn:
- ✅ Zero-shot prompting
- ✅ Few-shot prompting (providing examples)
- ✅ Chain-of-thought prompting
- ✅ System prompts vs user prompts
- ✅ Prompt templates and variables

#### What We'll Build:
- Multiple endpoints demonstrating different techniques:
  - `/api/prompt/zero-shot` - Direct questions
  - `/api/prompt/few-shot` - With examples
  - `/api/prompt/chain-of-thought` - Step-by-step reasoning
  - `/api/prompt/structured-output` - JSON responses
- Prompt template management
- Comparison dashboard

#### Deliverables:
- Working examples of each technique
- Understanding when to use which approach

---

### **Phase 3: Vector Database & Embeddings** (Day 3)
**Goal:** Understand semantic search and vector storage

#### What You'll Learn:
- ✅ What are embeddings? (vector representations of text)
- ✅ How vector databases work
- ✅ Similarity search (cosine similarity)
- ✅ Oracle 23c AI Vector Search capabilities
- ✅ RAG (Retrieval Augmented Generation) basics

#### What We'll Build:
- Oracle 23c setup with Vector support
- Embedding generation (using Claude or dedicated model)
- Document ingestion pipeline
- Vector storage in Oracle
- Semantic search endpoint: `/api/search/semantic`

#### Deliverables:
- Working vector database
- Ability to search documents by meaning (not just keywords)
- Understanding of embeddings

---

### **Phase 4: RAG Implementation** (Day 4)
**Goal:** Combine vector search with LLM for intelligent Q&A

#### What You'll Learn:
- ✅ Retrieval Augmented Generation (RAG) pattern
- ✅ How to provide context to LLMs
- ✅ Chunking strategies for large documents
- ✅ Context window management

#### What We'll Build:
- Document upload endpoint
- Automatic chunking and vectorization
- RAG endpoint: `/api/qa/document`
  - Retrieves relevant chunks from vector DB
  - Sends chunks + question to LLM
  - Returns accurate answer with sources
- Citation tracking

#### Deliverables:
- Working document Q&A system
- Understanding how to augment LLM with your own data

---

### **Phase 5: Agentic AI - Part 1 (Tool Use)** (Day 5)
**Goal:** Understand how AI agents make decisions and use tools

#### What You'll Learn:
- ✅ What is Agentic AI?
- ✅ Tool/Function calling in LLMs
- ✅ How agents decide which tools to use
- ✅ Multi-step reasoning

#### What We'll Build:
- Define tools (functions) the agent can use:
  - `search_documents` - Search vector DB
  - `get_current_date` - Get system info
  - `calculate` - Perform calculations
  - `database_query` - Query Oracle
- Agent endpoint: `/api/agent/task`
- Tool execution framework
- Decision logging

#### Deliverables:
- AI agent that can use multiple tools
- Understanding of function calling
- Logs showing agent's decision-making process

---

### **Phase 6: Agentic AI - Part 2 (Camunda Integration)** (Day 6)
**Goal:** Orchestrate complex AI workflows with Camunda

#### What You'll Learn:
- ✅ Why use workflow orchestration?
- ✅ BPMN basics (Business Process Model Notation)
- ✅ Human-in-the-loop patterns
- ✅ Long-running AI processes
- ✅ Camunda Zeebe integration

#### What We'll Build:
- BPMN workflow: "Intelligent Document Processing"
  1. Document Upload
  2. AI Classification (LLM determines document type)
  3. AI Extraction (Extract key information)
  4. Human Review Task (if confidence low)
  5. Storage in Oracle
- Camunda job workers in Spring Boot
- Tasklist integration for human tasks

#### Deliverables:
- Working Camunda workflow
- Understanding of AI + workflow orchestration
- Visual process in Camunda Operate

---

### **Phase 7: Integration & Polish** (Day 7)
**Goal:** Complete end-to-end demo and prepare presentation

#### What We'll Build:
- Simple frontend (HTML/JS or Thymeleaf)
- Complete demo flow combining all concepts
- Documentation for your manager
- Architecture diagram
- Performance metrics

#### Deliverables:
- Full working MVP
- Presentation-ready demo
- Clear documentation of concepts

---

## 🛠️ Technical Architecture (Final State)

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (Simple UI)                     │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│              Spring Boot Application (REST API)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Chat       │  │   Prompt     │  │   Vector     │      │
│  │  Service     │  │  Engineering │  │   Search     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │     RAG      │  │   Agent      │  │   Camunda    │      │
│  │   Service    │  │   Service    │  │   Client     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└───────┬──────────────────┬─────────────────┬────────────────┘
        │                  │                 │
        ▼                  ▼                 ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│    Google    │   │  Oracle 23c  │   │  Camunda 8.7 │
│  Gemini API  │   │   Database   │   │    Zeebe     │
│   (PRIMARY)  │   │ (Vector DB)  │   │              │
└──────────────┘   └──────────────┘   └──────────────┘
```

---

## 📦 Technology Stack

- **Backend:** Java 21, Spring Boot 3.2+
- **LLM:** Google Gemini API (FREE tier - Primary), Claude API (Optional paid)
- **Database:** Oracle 23c AI with Vector Search
- **Workflow:** Camunda 8.7 (your Docker setup)
- **Build Tool:** Maven
- **IDE:** IntelliJ IDEA

---

## 📋 Daily Check-ins with Manager

Each day, you'll be able to demonstrate:
- **Day 1:** Basic LLM integration and chat
- **Day 2:** Different prompting strategies and their use cases
- **Day 3:** Vector database and semantic search
- **Day 4:** Document Q&A with RAG
- **Day 5:** AI agent using tools
- **Day 6:** Workflow orchestration with Camunda
- **Day 7:** Complete integrated system

---

## 🎓 Key Concepts You'll Master

### 1. **LLM (Large Language Model)**
- Pre-trained neural networks with billions of parameters
- Understand and generate human-like text
- Examples: Google Gemini, Claude, GPT-4, Llama
- Use cases: Chat, summarization, analysis, code generation

### 2. **Agentic AI**
- AI that can make decisions and take actions autonomously
- Uses tools/functions to accomplish complex tasks
- Breaks down problems into steps
- Can interact with external systems

### 3. **Prompt Engineering**
- Art and science of crafting effective prompts
- Techniques to get better, more reliable outputs
- Critical skill for working with LLMs
- Different strategies for different tasks

### 4. **Vector Database**
- Stores data as mathematical vectors (embeddings)
- Enables semantic search (meaning-based, not keyword)
- Fast similarity search at scale
- Foundation of modern AI applications (RAG, recommendations)

---

## Next Steps

1. ✅ Complete the SETUP-VALIDATION.md checklist
2. ✅ Share validation results with me
3. ✅ I'll help fix any issues
4. ✅ We'll start Phase 1 together!

---

## Questions?

Before we start, do you have any questions about:
- The phased approach?
- The concepts we'll cover?
- The timeline?
- The architecture?
