# LLM Agentic AI MVP - Learning Project

## 🎓 Understanding the Core Concepts

This project is designed to help you **learn and demonstrate** four critical AI concepts through hands-on implementation.

---

## 📚 Concept 1: LLM (Large Language Model)

### What is it?
A **Large Language Model** is a type of artificial intelligence that has been trained on vast amounts of text data to understand and generate human-like text.

### Key Characteristics:
- **Pre-trained** on billions of words from the internet, books, and other sources
- Contains **billions of parameters** (neural network weights)
- Can understand **context** and generate **coherent responses**
- Requires significant computational resources

### Real-World Analogy:
Think of an LLM as a highly educated assistant who has read millions of books and can:
- Answer questions
- Write content
- Summarize information
- Translate languages
- Generate code
- Analyze sentiment

### Examples:
- **Google Gemini** (FREE - what we're using as primary) - Gemini 2.5 Flash
- **Anthropic Claude** (Paid - secondary option) - Claude 3.5 Sonnet
- OpenAI GPT-4
- Meta Llama

### How it Works in This Project:
```
User Question → API Call to LLM (Gemini/Claude) → LLM Processing → Intelligent Response
```

### Key Limitations:
- **Knowledge cutoff** - Only knows information up to training date
- **Hallucinations** - Can generate plausible but incorrect information
- **No real-time data** - Doesn't know current events without external data
- **Context window limits** - Can only process limited amount of text at once

### Why Use Gemini API (Primary)?
- **FREE** - 60 requests/min, 1500 requests/day
- 1M+ token context window (massive working memory)
- Fast inference with Gemini 2.5 Flash
- Built-in safety features
- Perfect for learning and MVP development

### Alternative: Claude API (Optional Paid)
- State-of-the-art reasoning capabilities
- 200K token context window
- Strong coding and analysis abilities
- Requires paid API access

---

## 📚 Concept 2: Prompt Engineering

### What is it?
**Prompt Engineering** is the art and science of crafting effective instructions (prompts) to get the best results from LLMs.

### Key Principle:
> "The quality of AI output is directly proportional to the quality of the input prompt"

### Major Techniques:

#### 1️⃣ **Zero-Shot Prompting**
Ask the LLM to perform a task without any examples.

**Example:**
```
Classify this email as spam or not spam:
"Congratulations! You've won $1,000,000! Click here now!"
```

**Use When:** Task is straightforward and well-understood by the model

---

#### 2️⃣ **Few-Shot Prompting**
Provide examples to guide the LLM's response format.

**Example:**
```
Classify the sentiment of these product reviews:

Review: "This product is amazing! Best purchase ever."
Sentiment: Positive

Review: "Terrible quality. Waste of money."
Sentiment: Negative

Review: "The delivery was slow but product is okay."
Sentiment: [AI completes this]
```

**Use When:** You need specific formatting or the task is ambiguous

---

#### 3️⃣ **Chain-of-Thought Prompting**
Ask the LLM to show its reasoning process step-by-step.

**Example:**
```
Solve this problem step by step:
A store has 15 apples. They sell 40% in the morning and 1/3 of the remainder in the afternoon. How many apples are left?

Let's think through this step by step:
1. Calculate morning sales
2. Calculate remainder
3. Calculate afternoon sales
4. Calculate final amount
```

**Use When:** Complex reasoning, math, or multi-step analysis needed

---

#### 4️⃣ **Structured Output Prompting**
Request responses in specific formats (JSON, XML, etc.)

**Example:**
```
Extract information from this text and return as JSON:
"John Doe, age 30, works as a Software Engineer at Tech Corp in San Francisco."

Return format:
{
  "name": "...",
  "age": ...,
  "occupation": "...",
  "company": "...",
  "location": "..."
}
```

**Use When:** Integrating with code, databases, or APIs

---

### Best Practices:
✅ Be specific and clear
✅ Provide context
✅ Use examples when needed
✅ Specify output format
✅ Break complex tasks into steps
✅ Iterate and refine prompts

❌ Don't be vague
❌ Don't assume implicit knowledge
❌ Don't ask multiple unrelated questions at once

---

## 📚 Concept 3: Vector Database & Embeddings

### What is a Vector Embedding?

An **embedding** is a numerical representation of data (text, images, etc.) as a high-dimensional vector.

### Real-World Analogy:
Imagine describing a person using numbers:
- Height: 175cm
- Weight: 70kg
- Age: 30

This creates a 3-dimensional vector: `[175, 70, 30]`

For text, embeddings have **1536 dimensions** (or more!), capturing semantic meaning.

### Example:
```
Text: "The cat sat on the mat"
Embedding: [0.023, -0.891, 0.234, ..., 0.445] (1536 numbers)

Text: "A feline rested on the rug"
Embedding: [0.019, -0.887, 0.229, ..., 0.441] (similar numbers!)
```

**Key Insight:** Similar meanings → Similar vectors

---

### What is a Vector Database?

A **Vector Database** is optimized for storing and searching high-dimensional vectors.

### Traditional Search vs Vector Search:

#### Traditional (Keyword) Search:
```
Query: "automobile repair"
Document: "car maintenance" ❌ No match (different words)
```

#### Vector (Semantic) Search:
```
Query: "automobile repair"
Query Vector: [0.12, -0.45, 0.78, ...]

Document: "car maintenance"
Doc Vector: [0.11, -0.44, 0.77, ...] ✅ MATCH! (similar meaning)
```

### Similarity Metrics:

#### Cosine Similarity
Measures the angle between vectors (used in our project)
- Range: -1 to 1
- 1 = identical
- 0 = unrelated
- -1 = opposite

**Formula:**
```
similarity = (A · B) / (||A|| × ||B||)
```

---

### Oracle 23c AI Vector Search

Oracle 23c includes **native vector support**:

```sql
-- Create table with vector column
CREATE TABLE document_chunks (
    id RAW(16) PRIMARY KEY,
    text CLOB,
    embedding VECTOR(1536, FLOAT32)  ← Native vector type!
);

-- Create vector index (HNSW algorithm)
CREATE VECTOR INDEX idx_chunks_vector ON document_chunks(embedding)
    ORGANIZATION NEIGHBOR PARTITIONS
    WITH DISTANCE COSINE;

-- Similarity search
SELECT text, VECTOR_DISTANCE(embedding, :query_vector, COSINE) as distance
FROM document_chunks
ORDER BY distance
FETCH FIRST 5 ROWS ONLY;
```

### Use Cases:
- **Semantic Search** - Find documents by meaning
- **Recommendation Systems** - Find similar products/content
- **RAG** (Retrieval Augmented Generation) - Augment LLM with your data
- **Duplicate Detection** - Find similar records
- **Clustering** - Group similar items

---

## 📚 Concept 4: Agentic AI

### What is it?

**Agentic AI** refers to AI systems that can:
1. **Understand** complex goals
2. **Plan** steps to achieve them
3. **Execute** actions autonomously
4. **Use tools** to interact with the world
5. **Adapt** based on results

### Traditional AI vs Agentic AI:

#### Traditional Chatbot:
```
User: "What's the weather?"
Bot: "I don't have access to weather data."
```

#### Agentic AI:
```
User: "What's the weather in Paris and should I bring an umbrella?"

Agent thinks:
1. I need current weather data
2. I have a tool: get_weather(location)
3. Let me use it

Agent: [Calls get_weather("Paris")]
Tool returns: "Rainy, 15°C, 80% humidity"

Agent: "It's currently rainy in Paris at 15°C. Yes, you should bring an umbrella!"
```

---

### Key Capabilities:

#### 1️⃣ **Tool/Function Calling**
Agent can use predefined functions to accomplish tasks.

**Example Tools:**
```python
def search_documents(query: str) -> List[Document]:
    """Search vector database for relevant documents"""

def calculate(expression: str) -> float:
    """Perform mathematical calculations"""

def query_database(sql: str) -> List[Dict]:
    """Execute SQL queries"""

def send_email(to: str, subject: str, body: str):
    """Send an email"""
```

**How it works:**
1. User gives task: "Find sales data for Q4 and calculate growth rate"
2. Agent decides: "I need to use query_database and calculate tools"
3. Agent calls: `query_database("SELECT sales FROM revenue WHERE quarter = 'Q4'")`
4. Agent gets: `[{"sales": 100000}, {"sales": 120000}]`
5. Agent calls: `calculate("(120000 - 100000) / 100000 * 100")`
6. Agent gets: `20.0`
7. Agent responds: "Q4 sales grew by 20%"

---

#### 2️⃣ **Multi-Step Reasoning**
Break complex tasks into smaller steps.

**Example:**
```
Task: "Summarize all documents about AI and create a report"

Agent's plan:
Step 1: Search for AI-related documents
Step 2: Read each document
Step 3: Extract key points
Step 4: Synthesize information
Step 5: Format as report
Step 6: Save to database
```

---

#### 3️⃣ **Self-Correction**
Learn from errors and retry with different approaches.

**Example:**
```
Agent: [Tries to query database]
Error: "Table 'sales' does not exist"

Agent thinks: "Let me search for available tables"
Agent: [Calls list_tables()]
Result: ["revenue", "expenses", "customers"]

Agent: "Ah, I should use 'revenue' table instead"
Agent: [Retries with correct table]
Success!
```

---

### Workflow Orchestration with Camunda

**Camunda** adds structure to long-running AI processes:

```
┌─────────────────────────────────────────────────┐
│          BPMN Workflow Example                  │
│    "Intelligent Document Processing"            │
├─────────────────────────────────────────────────┤
│                                                 │
│  1. [Start] Document Uploaded                   │
│       ↓                                         │
│  2. [AI Task] Classify Document Type            │
│       │                                         │
│       ├─ Invoice → Extract invoice data         │
│       ├─ Contract → Extract contract terms      │
│       └─ Resume → Extract candidate info        │
│       ↓                                         │
│  3. [AI Task] Confidence Check                  │
│       │                                         │
│       ├─ High (>90%) → Auto-approve             │
│       └─ Low (<90%) → Human review              │
│       ↓                                         │
│  4. [User Task] Human Review (if needed)        │
│       ↓                                         │
│  5. [AI Task] Store in Database                 │
│       ↓                                         │
│  6. [End] Process Complete                      │
│                                                 │
└─────────────────────────────────────────────────┘
```

**Benefits:**
- **Visual workflows** - See the entire process
- **Human-in-the-loop** - Combine AI + human judgment
- **Error handling** - Retry, escalate, or abort
- **Audit trail** - Track every step
- **Scalability** - Handle thousands of concurrent processes

---

## 🏗️ How These Concepts Work Together

### Real-World Example: Intelligent Document Q&A System

```
┌─────────────────────────────────────────────────────────────┐
│ User Question: "What were the key findings in the Q4 report?" │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────────────┐
│ 1. PROMPT ENGINEERING                                       │
│    Convert user question into optimized query               │
│    Technique: Chain-of-thought                              │
└────────────────────────┬───────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────────────┐
│ 2. VECTOR DATABASE                                          │
│    - Convert question to embedding vector                   │
│    - Search Oracle 23c for similar document chunks          │
│    - Return top 5 most relevant chunks                      │
└────────────────────────┬───────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────────────┐
│ 3. AGENTIC AI                                               │
│    Agent decides:                                           │
│    - Are retrieved chunks sufficient?                       │
│    - Should I search for more context?                      │
│    - Should I query database for structured data?           │
│    Tools used: search_documents, query_database             │
└────────────────────────┬───────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────────────┐
│ 4. LLM Provider (Gemini/Claude API)                         │
│    - Receives question + retrieved context                  │
│    - Generates comprehensive answer                         │
│    - Cites sources                                          │
└────────────────────────┬───────────────────────────────────┘
                         ↓
┌────────────────────────────────────────────────────────────┐
│ Response: "Based on the Q4 report (pages 12-15), the key   │
│ findings were: 1) Revenue grew 25% YoY, 2) Customer         │
│ retention improved to 94%, 3) New product line exceeded     │
│ expectations with $2M in sales."                            │
│                                                             │
│ Sources: Q4_Report.pdf (chunks 23, 24, 31)                  │
└────────────────────────────────────────────────────────────┘
```

---

## 🎯 What We'll Build in This Project

### Phase 1: LLM Integration
- Multi-provider LLM support (Gemini primary, Claude secondary)
- Direct API calls to LLM providers
- Token management
- Request/Response handling

### Phase 2: Prompt Engineering
- Implement all 4 techniques
- A/B testing different prompts
- Prompt template library

### Phase 3: Vector Database
- Oracle 23c AI Vector Search
- Document embedding pipeline
- Semantic search API

### Phase 4: RAG (Retrieval Augmented Generation)
- Combine vector search + LLM
- Document chunking strategies
- Citation tracking

### Phase 5: Agentic AI - Tools
- Function/tool calling
- Multi-tool workflows
- Decision logging

### Phase 6: Agentic AI - Camunda
- BPMN workflow design
- AI task workers
- Human-in-the-loop patterns

### Phase 7: Integration
- Complete end-to-end system
- Performance optimization
- Manager demo

---

## 📖 Learning Resources

### LLMs
- [Google Gemini API Documentation](https://ai.google.dev/docs) - PRIMARY (FREE)
- [Anthropic Claude Documentation](https://docs.anthropic.com/) - Secondary (Paid)
- [How LLMs Work - 3Blue1Brown](https://www.youtube.com/watch?v=wjZofJX0v4M)

### Prompt Engineering
- [Google Gemini Prompting Guide](https://ai.google.dev/gemini-api/docs/prompting-intro)
- [Anthropic Prompt Engineering Guide](https://docs.anthropic.com/claude/docs/prompt-engineering)
- [OpenAI Prompt Engineering Best Practices](https://platform.openai.com/docs/guides/prompt-engineering)

### Vector Databases
- [Oracle AI Vector Search Documentation](https://docs.oracle.com/en/database/oracle/oracle-database/23/vecse/)
- [Understanding Vector Embeddings](https://www.pinecone.io/learn/vector-embeddings/)

### Agentic AI
- [Google Gemini Function Calling](https://ai.google.dev/gemini-api/docs/function-calling)
- [LangChain Agents](https://python.langchain.com/docs/modules/agents/)
- [Anthropic Tool Use Guide](https://docs.anthropic.com/claude/docs/tool-use)

### Camunda
- [Camunda 8 Documentation](https://docs.camunda.io/)
- [BPMN 2.0 Tutorial](https://camunda.com/bpmn/)

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Backend** | Java 21, Spring Boot 3.2 | Application framework |
| **LLM** | Google Gemini 2.5 Flash (FREE, Primary) | AI reasoning and generation |
| **LLM Alt** | Anthropic Claude 3.5 Sonnet (Paid, Optional) | Alternative AI provider |
| **Database** | Oracle 23c Free | Vector storage & ACID transactions |
| **Workflow** | Camunda 8.7 | Process orchestration |
| **Build Tool** | Maven 3.9 | Dependency management |
| **IDE** | IntelliJ IDEA | Development environment |

---

## 🚀 Getting Started

1. Read this README completely
2. Follow `SETUP-INSTRUCTIONS.md`
3. Review `PROJECT-PLAN.md` for the 7-phase roadmap
4. Start Phase 1 when ready!

---

## 📞 Questions?

As you learn these concepts, document your questions and we'll address them during implementation. Understanding the "why" is as important as the "how"!

**Next:** Open `SETUP-INSTRUCTIONS.md` and prepare your environment! 🎉
