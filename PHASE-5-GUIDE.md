# Phase 5: Agentic AI - Tool Use (Function Calling) - Complete Guide

**Project:** LLM Agentic AI MVP
**Phase:** 5 of 7
**Status:** ✅ IMPLEMENTATION COMPLETE
**Date:** January 14, 2026

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [What You'll Learn](#what-youll-learn)
3. [Architecture](#architecture)
4. [Key Concepts](#key-concepts)
5. [Implementation Details](#implementation-details)
6. [API Reference](#api-reference)
7. [Testing Guide](#testing-guide)
8. [Troubleshooting](#troubleshooting)
9. [Next Steps](#next-steps)

---

## 🎯 Overview

Phase 5 introduces **Agentic AI with Tool Use** - the ability for an AI to autonomously use tools (functions) to complete complex tasks.

### What is Agentic AI?

Agentic AI refers to AI systems that can:
- **Make decisions** autonomously
- **Use tools** to accomplish goals
- **Break down complex tasks** into steps
- **Learn from results** and adapt their approach

Think of it like a human assistant who knows when to:
- Search for information (use search tool)
- Do math (use calculator tool)
- Check the database (use query tool)
- Look up the date (use date tool)

### What We Built

An AI agent that can:
1. **Understand** natural language tasks
2. **Decide** which tools to use
3. **Execute** multiple tools autonomously
4. **Synthesize** results into a final answer
5. **Log** all decisions for transparency

**Example:**
```
User: "Search for documents about neural networks and tell me how many we have"

Agent thinking:
  Step 1: Use search_documents tool to find neural network docs
  Step 2: Use database_query tool to count them
  Step 3: Synthesize answer

Answer: "I found 3 documents about neural networks in the database..."
```

---

## 📚 What You'll Learn

### Core Concepts

1. **Function Calling / Tool Use**
   - How LLMs can request to use external functions
   - Tool definition format (JSON Schema)
   - Multi-turn conversations with tool results

2. **Agent Loop Pattern**
   - Task → Think → Use Tools → Think → Answer
   - Iterative problem solving
   - Decision logging

3. **Tool Design**
   - Single Responsibility Principle for tools
   - Parameter validation
   - Error handling in tools

4. **Prompt Engineering for Agents**
   - System prompts that encourage tool use
   - Structured tool definitions
   - Result interpretation

### Technologies Used

- **Gemini Function Calling API** - LLM with native tool support
- **Spring Boot Components** - Dependency injection for tools
- **JSON Schema** - Tool parameter definitions
- **Multi-step Reasoning** - Agent loop implementation

---

## 🏗️ Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────┐
│                         User                                 │
│                 "Find docs about AI"                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   AgentController                            │
│              POST /api/agent/task                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    AgentService                              │
│                  (Orchestration)                             │
│                                                              │
│  ┌────────────────────────────────────────────────┐         │
│  │  Agent Loop (max 10 iterations)                │         │
│  │                                                 │         │
│  │  1. Call LLM with task + tool definitions      │         │
│  │  2. LLM decides: Use tool OR Give answer       │         │
│  │  3. If tool: Execute → Send result → Loop      │         │
│  │  4. If answer: Return final response           │         │
│  └────────────────────────────────────────────────┘         │
│                                                              │
└─────┬──────────────────┬──────────────────┬─────────────────┘
      │                  │                  │
      ▼                  ▼                  ▼
┌───────────┐    ┌──────────────┐   ┌─────────────┐
│  Gemini   │    │    Tools     │   │  Execution  │
│    API    │    │              │   │     Log     │
│           │    │ - Search     │   │             │
│ Function  │    │ - Calculate  │   │ Step 1: ... │
│  Calling  │    │ - Date       │   │ Step 2: ... │
│           │    │ - Database   │   │ Step 3: ... │
└───────────┘    └──────────────┘   └─────────────┘
```

### Component Breakdown

#### 1. AgentController
- **Location:** `src/main/java/com/ai/mvp/controller/AgentController.java`
- **Responsibilities:**
  - Expose REST API endpoint `/api/agent/task`
  - Validate user requests
  - Return agent responses
- **Endpoints:**
  - `POST /api/agent/task` - Execute task
  - `GET /api/agent/tools` - List available tools
  - `GET /api/agent/health` - Health check

#### 2. AgentService
- **Location:** `src/main/java/com/ai/mvp/service/AgentService.java`
- **Responsibilities:**
  - Orchestrate agent loop
  - Manage conversation history
  - Call LLM with tool definitions
  - Execute tools when requested
  - Build execution log
- **Key Methods:**
  - `executeTask()` - Main agent loop
  - `buildAgentRequest()` - Create LLM request with tools
  - `executeTool()` - Run a specific tool

#### 3. Tools (4 implementations)

**Tool Interface:**
```java
public interface Tool {
    String getName();
    String getDescription();
    Map<String, Object> getParametersSchema();
    ToolResult execute(Map<String, Object> args);
}
```

**Concrete Tools:**

| Tool | Purpose | Example Use |
|------|---------|-------------|
| **SearchDocumentsTool** | Search vector DB | "Find docs about AI" |
| **GetCurrentDateTool** | Get date/time | "What day is it?" |
| **CalculateTool** | Math operations | "Calculate 15% of 200" |
| **DatabaseQueryTool** | DB statistics | "How many documents?" |

#### 4. DTOs

**AgentTaskRequest:**
```java
{
    "task": "Search for neural networks",
    "maxToolCalls": 10,
    "temperature": 0.0
}
```

**AgentTaskResponse:**
```java
{
    "task": "...",
    "answer": "...",
    "success": true,
    "executionLog": [...],
    "llmCallCount": 3,
    "toolCallCount": 2,
    "totalTokens": 450,
    "totalExecutionTimeMs": 3200
}
```

**AgentExecutionStep:**
```java
{
    "type": "TOOL_CALL",
    "stepNumber": 1,
    "toolName": "search_documents",
    "toolArgs": {"query": "neural networks"},
    "executionTimeMs": 245
}
```

---

## 🔑 Key Concepts

### 1. Function Calling

**What is it?**
- LLMs can request to call external functions
- Functions are defined in JSON Schema format
- LLM sees: function name, description, parameters
- LLM returns: function name + arguments to call

**Example Flow:**
```
User: "What's 15% of 200?"

LLM thinks: "I need to calculate this"
LLM response: {
    "functionCall": {
        "name": "calculate",
        "args": {"expression": "200 * 0.15"}
    }
}

Agent executes: calculate("200 * 0.15") → 30

Agent sends result back to LLM

LLM: "15% of 200 is 30"
```

### 2. Tool Definition (JSON Schema)

Tools are defined in JSON Schema format so the LLM understands:
- What the tool does
- What parameters it needs
- What types those parameters are

**Example: Calculate Tool**
```json
{
    "name": "calculate",
    "description": "Perform mathematical calculations. Supports +, -, *, /, Math functions.",
    "parameters": {
        "type": "object",
        "properties": {
            "expression": {
                "type": "string",
                "description": "Math expression to evaluate. Examples: '15 * 20', 'Math.sqrt(144)'"
            }
        },
        "required": ["expression"]
    }
}
```

### 3. Agent Loop

The agent operates in a loop:

```
1. Start with user's task
2. Loop (max 10 times):
   a. Call LLM with conversation history + tool definitions
   b. LLM responds with:
      - Function calls → Execute tools, add results to conversation, continue loop
      - Text answer → Task complete, exit loop
3. Return final answer + execution log
```

**Why a loop?**
- Some tasks need multiple steps
- Tools might provide info that leads to using more tools
- Agent can "think" multiple times before answering

**Loop limit (10):**
- Prevents infinite loops
- Forces agent to be efficient
- Most tasks need 1-3 tool calls

### 4. Multi-Turn Conversation

The agent maintains a conversation history:

```
Turn 1 (User):
  "Search for documents about neural networks"

Turn 2 (Model):
  [Function call: search_documents]

Turn 3 (User/Tool Result):
  [Results from search_documents]

Turn 4 (Model):
  "I found 3 documents about neural networks..."
```

This allows the LLM to:
- Remember what tools it used
- See the results
- Make informed decisions about next steps

### 5. Decision Logging

Every step is logged:
- **THINKING** - LLM reasoning
- **TOOL_CALL** - Decision to use a tool
- **TOOL_RESULT** - Result from tool execution
- **FINAL_ANSWER** - Final response

This provides:
- **Transparency** - See how the agent works
- **Debugging** - Find where things went wrong
- **Trust** - User can verify the agent's process

---

## 💻 Implementation Details

### Tool Implementation Example

```java
@Component
public class CalculateTool implements Tool {

    @Override
    public String getName() {
        return "calculate";
    }

    @Override
    public String getDescription() {
        return "Perform mathematical calculations. Supports +, -, *, /, Math functions.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "expression", Map.of(
                    "type", "string",
                    "description", "Mathematical expression to evaluate"
                )
            ),
            "required", new String[]{"expression"}
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> args) {
        String expression = (String) args.get("expression");
        Object result = scriptEngine.eval(expression);
        double numResult = ((Number) result).doubleValue();

        return ToolResult.success(
            Map.of("result", numResult, "expression", expression),
            executionTime
        );
    }
}
```

### Agent Service Loop

```java
public AgentTaskResponse executeTask(AgentTaskRequest request) {
    List<GeminiApiRequest.Content> conversationHistory = new ArrayList<>();
    conversationHistory.add(createUserMessage(request.getTask()));

    for (int iteration = 0; iteration < maxToolCalls; iteration++) {
        // Call LLM
        GeminiApiRequest llmRequest = buildAgentRequest(conversationHistory, temperature);
        GeminiApiResponse llmResponse = geminiApiService.sendMessage(llmRequest);

        // Check if LLM wants to use tools
        if (llmResponse.hasFunctionCalls()) {
            // Execute each tool
            for (FunctionCall fc : llmResponse.getFunctionCalls()) {
                ToolResult result = executeTool(fc.getName(), fc.getArgs());

                // Add results to conversation
                conversationHistory.add(createFunctionResult(fc.getName(), result));
            }
        } else {
            // LLM provided final answer
            return buildSuccessResponse(llmResponse.getTextContent());
        }
    }

    return buildMaxIterationsResponse();
}
```

### Gemini Function Calling Format

**Request with Tools:**
```json
{
    "contents": [
        {"role": "user", "parts": [{"text": "Calculate 15% of 200"}]}
    ],
    "tools": [{
        "functionDeclarations": [{
            "name": "calculate",
            "description": "Perform math calculations",
            "parameters": {
                "type": "object",
                "properties": {
                    "expression": {"type": "string"}
                },
                "required": ["expression"]
            }
        }]
    }]
}
```

**Response with Function Call:**
```json
{
    "candidates": [{
        "content": {
            "parts": [{
                "functionCall": {
                    "name": "calculate",
                    "args": {"expression": "200 * 0.15"}
                }
            }]
        }
    }]
}
```

**Sending Function Result Back:**
```json
{
    "contents": [
        {"role": "user", "parts": [{"text": "Calculate 15% of 200"}]},
        {"role": "model", "parts": [{
            "functionCall": {
                "name": "calculate",
                "args": {"expression": "200 * 0.15"}
            }
        }]},
        {"role": "user", "parts": [{
            "functionResponse": {
                "name": "calculate",
                "response": {"success": true, "result": 30}
            }
        }]}
    ]
}
```

---

## 📡 API Reference

### POST /api/agent/task

Execute a task using the AI agent.

**Request:**
```json
{
    "task": "Search for documents about neural networks and tell me how many there are",
    "maxToolCalls": 10,
    "temperature": 0.0
}
```

**Parameters:**
- `task` (string, required) - Task description in natural language
- `maxToolCalls` (integer, optional) - Max tool calls allowed (default: 10)
- `temperature` (number, optional) - LLM temperature 0.0-1.0 (default: 0.0)

**Response:**
```json
{
    "task": "Search for documents about neural networks and tell me how many there are",
    "answer": "I found 3 documents about neural networks in the database. They cover topics including...",
    "success": true,
    "error": null,
    "executionLog": [
        {
            "type": "THINKING",
            "stepNumber": 1,
            "thinking": "Decided to use 1 tool(s): search_documents"
        },
        {
            "type": "TOOL_CALL",
            "stepNumber": 2,
            "toolName": "search_documents",
            "toolArgs": {
                "query": "neural networks",
                "topK": 10,
                "similarityThreshold": 0.5
            }
        },
        {
            "type": "TOOL_RESULT",
            "stepNumber": 3,
            "toolName": "search_documents",
            "toolResult": {
                "query": "neural networks",
                "totalResults": 3,
                "results": [...]
            },
            "toolSuccess": true,
            "executionTimeMs": 245
        },
        {
            "type": "FINAL_ANSWER",
            "stepNumber": 4,
            "thinking": "I found 3 documents about neural networks..."
        }
    ],
    "llmCallCount": 2,
    "toolCallCount": 1,
    "totalTokens": 450,
    "totalExecutionTimeMs": 1250,
    "provider": "GEMINI",
    "timestamp": "2026-01-14T10:30:00"
}
```

### GET /api/agent/tools

List all available tools.

**Response:**
```json
{
    "totalTools": 4,
    "tools": [
        "calculate",
        "database_query",
        "get_current_date",
        "search_documents"
    ],
    "description": "Tools available to the AI agent for task execution"
}
```

### GET /api/agent/health

Check agent service health.

**Response:**
```json
{
    "status": "UP",
    "service": "Agent Service (Phase 5)",
    "toolsAvailable": 4,
    "capabilities": [
        "Semantic document search",
        "Date/time queries",
        "Mathematical calculations",
        "Database statistics"
    ]
}
```

---

## 🧪 Testing Guide

### Prerequisites

1. **Oracle 23c running** (from Phase 3)
2. **Documents uploaded** (from Phase 3)
3. **Application started:**
   ```bash
   mvn spring-boot:run
   ```
4. **Gemini API key** configured in `.env`

### Test 1: Simple Date Query

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/task \
  -H "Content-Type: application/json" \
  -d '{
    "task": "What is today'\''s date?"
  }'
```

**Expected:**
- Tool used: `get_current_date`
- Answer includes current date
- 1-2 LLM calls
- 1 tool call

### Test 2: Simple Calculation

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/task \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Calculate 15% of 2500"
  }'
```

**Expected:**
- Tool used: `calculate`
- Answer: "375" or "15% of 2500 is 375"
- Execution log shows calculation expression

### Test 3: Database Statistics

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/task \
  -H "Content-Type: application/json" \
  -d '{
    "task": "How many documents are in the database?"
  }'
```

**Expected:**
- Tool used: `database_query`
- Answer includes document count
- May also show categories

### Test 4: Document Search

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/task \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Search for documents about neural networks and summarize what you find"
  }'
```

**Expected:**
- Tool used: `search_documents`
- Answer summarizes found documents
- Shows similarity scores in execution log

### Test 5: Multi-Step Task (Complex)

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/task \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Find all documents about AI, count how many there are, and calculate what percentage that is of the total documents"
  }'
```

**Expected:**
- Tools used: `search_documents`, `database_query`, `calculate`
- Multiple tool calls
- 3-5 LLM calls
- Answer includes count and percentage

### Test 6: Combined Date and Calculation

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/task \
  -H "Content-Type: application/json" \
  -d '{
    "task": "What year is it today and what is 2026 minus 2000?"
  }'
```

**Expected:**
- Tools used: `get_current_date`, `calculate`
- Answer: Current year and result (26)

### Test 7: No Tool Needed

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/task \
  -H "Content-Type: application/json" \
  -d '{
    "task": "What is the capital of France?"
  }'
```

**Expected:**
- NO tools used
- LLM answers directly from knowledge
- 1 LLM call, 0 tool calls
- Answer: "Paris"

### Test 8: List Available Tools

**Request:**
```bash
curl -X GET http://localhost:8080/api/agent/tools
```

**Expected:**
```json
{
    "totalTools": 4,
    "tools": [
        "calculate",
        "database_query",
        "get_current_date",
        "search_documents"
    ]
}
```

### Test 9: Agent Health Check

**Request:**
```bash
curl -X GET http://localhost:8080/api/agent/health
```

**Expected:**
```json
{
    "status": "UP",
    "service": "Agent Service (Phase 5)",
    "toolsAvailable": 4
}
```

### Validation Checklist

After running tests, verify:

- [ ] Agent successfully uses tools when appropriate
- [ ] Agent provides direct answers when no tools needed
- [ ] Execution log shows all steps clearly
- [ ] Tool calls include arguments and results
- [ ] Multi-step tasks work correctly
- [ ] Error handling works (invalid expressions, etc.)
- [ ] Token usage is tracked
- [ ] Execution time is reasonable
- [ ] Swagger UI shows agent endpoints

---

## 🔧 Troubleshooting

### Issue 1: Agent Always Returns Max Iterations Error

**Symptoms:**
```json
{
    "success": false,
    "error": "Max tool calls reached"
}
```

**Causes:**
- LLM stuck in loop calling same tool repeatedly
- Tools returning unhelpful results
- Task is too complex for agent

**Solutions:**
1. Increase `maxToolCalls` in request
2. Simplify the task
3. Check tool results in execution log
4. Verify tools are returning useful data

### Issue 2: Agent Not Using Tools

**Symptoms:**
- Agent answers directly without using tools
- Tools would be helpful but aren't used

**Causes:**
- Task phrasing doesn't suggest tool use
- LLM has knowledge to answer directly

**Solutions:**
1. Rephrase task to be more specific
   - Instead of: "Tell me about AI"
   - Try: "Search our documents about AI"
2. Be explicit about data source
   - "Check the database for..."
   - "Calculate..."
   - "Look up..."

### Issue 3: Tool Execution Fails

**Symptoms:**
```json
{
    "toolSuccess": false,
    "error": "Tool execution error: ..."
}
```

**Causes:**
- Invalid parameters from LLM
- Tool implementation bug
- Database/service unavailable

**Solutions:**
1. Check execution log for exact error
2. Verify database is running (for search/query tools)
3. Test tool parameters manually
4. Check application logs for stack traces

### Issue 4: Empty or Unhelpful Answers

**Symptoms:**
- Agent completes but answer is vague
- Tool results not incorporated into answer

**Causes:**
- LLM not synthesizing tool results properly
- Temperature too high (more random)

**Solutions:**
1. Set `temperature: 0.0` for more focused responses
2. Check execution log - did tools return data?
3. Rephrase task to be more specific

### Issue 5: Calculation Tool Errors

**Symptoms:**
```json
{
    "error": "Expression contains unsafe characters"
}
```

**Causes:**
- Expression contains non-math characters
- Security filter rejecting expression

**Solutions:**
1. Check what expression LLM generated
2. Verify expression uses only: numbers, +, -, *, /, Math functions
3. If legitimate expression rejected, review safety filter

### Issue 6: Search Tool Returns No Results

**Symptoms:**
```json
{
    "totalResults": 0,
    "results": []
}
```

**Causes:**
- No documents in database
- Query too specific
- Similarity threshold too high

**Solutions:**
1. Verify documents exist: `GET /api/documents`
2. Check similarity threshold (default 0.5)
3. Try broader search terms
4. Upload more documents

---

## 📊 Understanding Execution Logs

The execution log is the key to understanding agent behavior.

### Step Types

**THINKING:**
- Agent's reasoning about what to do
- Usually: "Decided to use X tool(s)"

**TOOL_CALL:**
- Agent calling a specific tool
- Includes tool name and arguments
- Example: `calculate` with `expression: "200 * 0.15"`

**TOOL_RESULT:**
- Result from tool execution
- Includes success/failure
- Execution time
- Result data

**FINAL_ANSWER:**
- Agent's final response
- Task complete

### Example Log Analysis

```json
{
    "executionLog": [
        {
            "type": "THINKING",
            "stepNumber": 1,
            "thinking": "Decided to use 2 tool(s): search_documents, database_query"
        },
        {
            "type": "TOOL_CALL",
            "stepNumber": 2,
            "toolName": "search_documents",
            "toolArgs": {"query": "neural networks", "topK": 5}
        },
        {
            "type": "TOOL_RESULT",
            "stepNumber": 3,
            "toolName": "search_documents",
            "toolResult": {"totalResults": 3, "results": [...]},
            "toolSuccess": true,
            "executionTimeMs": 245
        },
        {
            "type": "TOOL_CALL",
            "stepNumber": 4,
            "toolName": "database_query",
            "toolArgs": {"queryType": "document_count"}
        },
        {
            "type": "TOOL_RESULT",
            "stepNumber": 5,
            "toolName": "database_query",
            "toolResult": {"documentCount": 10},
            "toolSuccess": true,
            "executionTimeMs": 15
        },
        {
            "type": "FINAL_ANSWER",
            "stepNumber": 6,
            "thinking": "I found 3 documents about neural networks out of a total of 10 documents in the database..."
        }
    ]
}
```

**Analysis:**
1. Agent decided to use 2 tools
2. First searched for "neural networks" (found 3)
3. Then queried total document count (found 10)
4. Synthesized answer combining both results

---

## 🎓 Key Learnings from Phase 5

### 1. Tool Design Principles

**Single Responsibility:**
- Each tool does ONE thing well
- `calculate` only does math
- `search_documents` only searches
- Don't combine unrelated functions

**Clear Parameters:**
- Use descriptive parameter names
- Include helpful descriptions
- Validate inputs

**Robust Error Handling:**
- Return meaningful errors
- Don't crash on bad input
- Log errors for debugging

### 2. Prompt Engineering for Agents

**System Prompts:**
- Tell the agent it HAS tools available
- Encourage tool use when appropriate
- Provide clear guidelines

**Tool Descriptions:**
- Be specific about what tool does
- Include example use cases
- Mention limitations

### 3. Agent Limitations

**What Agents Can't Do:**
- Can't learn from previous conversations (stateless)
- Can't modify their own tools
- Limited by tool availability
- Bound by max iterations

**How to Handle:**
- Make tasks self-contained
- Provide all context in task description
- Create tools for common needs

### 4. When to Use Agentic AI

**Good Use Cases:**
- Multi-step tasks requiring different data sources
- Tasks that need calculation + retrieval
- Exploratory queries ("Find X and tell me Y about it")

**Bad Use Cases:**
- Simple Q&A (use RAG instead)
- Real-time chat (too slow)
- Tasks with no tools available

---

## 🚀 Next Steps

### Phase 6 Preview: Camunda Workflow Orchestration

While Phase 5 gives us tool use in a single request, Phase 6 will add:

1. **Long-running processes**
   - Tasks that span days/weeks
   - Human-in-the-loop approvals
   - Persistent state

2. **BPMN Workflows**
   - Visual process design
   - Conditional branching
   - Parallel execution

3. **Camunda Integration**
   - Workflow engine
   - Task management
   - Process monitoring

**Example Workflow:**
```
Document Upload → AI Classification → AI Extraction →
→ Human Review (if low confidence) → Save to Database → Notify User
```

### Enhancements to Phase 5

While Phase 5 is complete and fully functional, here are potential enhancements for future iterations:

#### High Priority Enhancements

1. **Additional Tools**
   - **WebSearchTool** - Search the internet for real-time information
   - **EmailTool** - Send emails or notifications
   - **FileOperationsTool** - Read/write files, manipulate documents
   - **WeatherTool** - Get current weather and forecasts
   - **TranslationTool** - Translate text between languages

2. **Enhanced Calculation Tool**
   - Add Math.sqrt(), Math.pow(), Math.sin/cos/tan support
   - Support for advanced functions (logarithms, exponentials)
   - Unit conversions (celsius ↔ fahrenheit, km ↔ miles)
   - Statistical functions (mean, median, standard deviation)

3. **Parallel Tool Execution**
   - Execute independent tools concurrently for faster responses
   - Example: Run search_documents + database_query in parallel
   - Reduce total execution time for multi-step tasks

4. **Tool Result Caching**
   - Cache tool results within same conversation
   - Avoid redundant database queries
   - Improve response times for repetitive tasks

#### Medium Priority Enhancements

5. **Tool Analytics & Monitoring**
   - Track tool usage statistics (most used, success rate)
   - Monitor tool execution times
   - Identify slow or failing tools
   - Dashboard for tool performance

6. **Custom Tool Creation API**
   - Allow users to define custom tools via API
   - Dynamic tool registration
   - Tool marketplace or plugin system

7. **Enhanced Error Recovery**
   - Automatic retry logic for transient failures
   - Fallback tools (if search fails, try alternative method)
   - Better error messages and suggestions

8. **Multi-Turn Task Memory**
   - Remember tool results across multiple user questions
   - Context carryover: "What about last year?" (remembers previous query)
   - Conversation state management

#### Low Priority Enhancements

9. **Tool Permissions & Security**
   - Role-based access control for tools
   - Sensitive tools require authorization
   - Audit log for tool usage

10. **Tool Composition**
    - Define composite tools (macros)
    - Example: "research_topic" = search + summarize + save
    - Reusable workflows without Camunda

11. **Streaming Responses**
    - Stream tool results as they arrive
    - Real-time progress updates
    - Better UX for long-running tools

12. **Claude API Integration**
    - Support Claude as alternative to Gemini for function calling
    - Multi-provider tool use
    - A/B testing different LLMs

#### Implementation Notes

- **Phase 6 Synergy:** Tool system will integrate with Camunda workflows
- **Backward Compatibility:** All enhancements should maintain existing API contracts
- **Testing:** Each enhancement requires comprehensive test cases
- **Documentation:** Update this guide with new capabilities

**Next Implementation Opportunity:** Phase 7 (Integration & Polish) could incorporate selected enhancements

---

## 📚 Additional Resources

### Documentation
- [Gemini Function Calling Guide](https://ai.google.dev/gemini-api/docs/function-calling)
- [JSON Schema Reference](https://json-schema.org/)
- [Agentic AI Patterns](https://www.anthropic.com/index/building-effective-agents)

### Related Files
- `AgentService.java` - Main orchestration logic
- `Tool.java` - Tool interface
- `GeminiApiRequest.java` - Function calling DTOs
- `AgentTaskResponse.java` - Response format

### Testing
- Swagger UI: http://localhost:8080/swagger-ui.html
- Agent Health: http://localhost:8080/api/agent/health
- List Tools: http://localhost:8080/api/agent/tools

---

## ✅ Phase 5 Completion Checklist

- [x] All 4 tools implemented ✅
- [x] Agent service orchestrating correctly ✅
- [x] Execution logging working ✅
- [x] Gemini function calling integrated ✅
- [x] API endpoints functional ✅
- [x] Tests passing (all 9 test cases) ✅ **9/9 TESTS PASSING**
- [x] Documentation complete ✅
- [x] Swagger UI updated ✅
- [x] Bug fixes completed (CalculateTool Java 21 compatibility) ✅
- [x] System prompt optimized for multi-step tasks ✅

**Phase 5 Status:** ✅ **COMPLETE AND FULLY TESTED** (Jan 15, 2026)

---

**END OF PHASE-5-GUIDE.MD**

*Last updated: 2026-01-15*
*Phase 5: Agentic AI - Tool Use*
*Status: ✅ Complete and Fully Tested - All 9/9 Tests Passing*
