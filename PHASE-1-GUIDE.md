# Phase 1: Foundation Setup + LLM Integration

## 🎯 Learning Objectives

By the end of Phase 1, you will understand:

1. ✅ **What is an LLM** and how does it work at a high level
2. ✅ **How to integrate** with Anthropic Claude API
3. ✅ **Request/Response structure** of LLM APIs
4. ✅ **Token management** and cost implications
5. ✅ **Error handling** when working with external APIs
6. ✅ **Spring Boot architecture** and best practices

---

## 📚 Concept Review: Large Language Models (LLMs)

### What is an LLM?

A **Large Language Model** is an AI system that:
- Has been trained on vast amounts of text data (billions of words)
- Contains billions of parameters (neural network weights)
- Can understand context and generate human-like text
- Can perform various tasks: Q&A, summarization, coding, analysis, etc.

### How Does Claude Work?

```
Your Prompt → Claude's Neural Network → Generated Response
              (175 billion parameters)
```

1. **You send a message** (text input)
2. **Claude processes it** using its neural network
3. **Claude generates a response** word by word (technically, token by token)
4. **You receive the response** with metadata (tokens used, etc.)

### Key Concepts:

#### Tokens
- Text is broken into "tokens" (roughly 4 characters = 1 token)
- Example: "Hello, world!" = ~3 tokens
- Why it matters: You pay per token!

#### Context Window
- Maximum amount of text Claude can process at once
- Claude 3.5 Sonnet: 200,000 tokens (~150,000 words)
- Includes both your prompt AND Claude's response

#### Temperature
- Controls randomness (0.0 to 1.0)
- **Low (0.0-0.3)**: Focused, deterministic, consistent
- **Medium (0.4-0.7)**: Balanced
- **High (0.8-1.0)**: Creative, random, varied

#### Stop Reason
- Why did Claude stop responding?
- `end_turn`: Natural completion (finished the thought)
- `max_tokens`: Hit the token limit (response was cut off)

---

## 🏗️ Architecture Overview

Here's what we built in Phase 1:

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT                              │
│                  (Postman, curl, browser)                   │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP POST /api/chat
                         │ { "message": "Hello" }
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   ChatController                            │
│  • Validates request                                        │
│  • Calls ChatService                                        │
│  • Returns response                                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     ChatService                             │
│  • Business logic layer                                     │
│  • Converts ChatRequest → ClaudeApiRequest                  │
│  • Converts ClaudeApiResponse → ChatResponse                │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  ClaudeApiService                           │
│  • HTTP client for Anthropic API                            │
│  • Handles authentication (API key)                         │
│  • Manages timeouts and retries                             │
│  • Parses responses                                         │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS POST
                         │ https://api.anthropic.com/v1/messages
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Anthropic Claude API                       │
│               (External Service - The Cloud)                │
└─────────────────────────────────────────────────────────────┘
```

### Why This Layered Architecture?

1. **ChatController**: Handles HTTP concerns (validation, status codes)
2. **ChatService**: Business logic (converting DTOs, calculations)
3. **ClaudeApiService**: External API communication (HTTP, auth, parsing)

Benefits:
- **Separation of Concerns**: Each layer has one responsibility
- **Testability**: Can mock each layer independently
- **Maintainability**: Changes in one layer don't affect others
- **Reusability**: Services can be used by multiple controllers

---

## 📂 Project Structure

```
src/main/java/com/ai/mvp/
├── LlmAgenticAiMvpApplication.java    # Main Spring Boot application
├── config/
│   └── AnthropicConfig.java           # Claude API configuration
├── controller/
│   └── ChatController.java            # REST endpoints
├── service/
│   ├── ChatService.java               # Business logic
│   └── ClaudeApiService.java          # Claude API client
├── dto/
│   ├── ChatRequest.java               # User request
│   ├── ChatResponse.java              # User response
│   ├── ClaudeApiRequest.java          # Internal API request
│   ├── ClaudeApiResponse.java         # Internal API response
│   └── ErrorResponse.java             # Error format
└── exception/
    ├── ClaudeApiException.java        # Custom exception
    └── GlobalExceptionHandler.java    # Error handler

src/main/resources/
├── application.yml                     # Main configuration
├── application-local.yml               # Local overrides
└── banner.txt                          # Startup banner
```

---

## 🚀 Running the Application

### Step 1: Start Oracle 23c (if not running)

```bash
cd C:\Users\nooreldeen.nabil\IdeaProjects\ai-small-project
docker-compose up -d oracle-23c

# Wait for healthy status
docker-compose ps
```

### Step 2: Open Project in IntelliJ

1. File → Open
2. Navigate to project directory
3. Wait for Maven to download dependencies (~2-3 minutes)
4. You should see "BUILD SUCCESS" in Maven tool window

### Step 3: Run the Application

**Option A: From IntelliJ**
1. Open `LlmAgenticAiMvpApplication.java`
2. Click the green ▶️ play button
3. Or right-click → Run

**Option B: From Command Line**
```bash
mvn spring-boot:run
```

### Step 4: Verify Startup

Look for this in the logs:
```
🚀 LLM Agentic AI MVP Application Started Successfully!
📍 Application URLs:
   • Application:    http://localhost:8080
   • API Docs:       http://localhost:8080/swagger-ui.html
   • Health Check:   http://localhost:8080/actuator/health
```

---

## 🧪 Testing Phase 1

### Test 1: Health Check ✅

**Purpose**: Verify the application is running

**URL**: `GET http://localhost:8080/api/chat/health`

**Using Browser**:
- Open: http://localhost:8080/api/chat/health
- Expected: `Chat service is ready! 🚀`

**Using curl**:
```bash
curl http://localhost:8080/api/chat/health
```

---

### Test 2: Simple Chat Request ✅

**Purpose**: Send your first message to Claude!

**URL**: `POST http://localhost:8080/api/chat`

**Request Body**:
```json
{
  "message": "What is a Large Language Model? Explain in simple terms."
}
```

**Using Postman**:
1. Create new request
2. Method: POST
3. URL: `http://localhost:8080/api/chat`
4. Headers: `Content-Type: application/json`
5. Body → Raw → JSON → Paste the request above
6. Click **Send**

**Using curl**:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"What is a Large Language Model? Explain in simple terms.\"}"
```

**Expected Response**:
```json
{
  "response": "A Large Language Model (LLM) is...",
  "model": "claude-3-5-sonnet-20241022",
  "tokensUsed": 245,
  "inputTokens": 45,
  "outputTokens": 200,
  "timestamp": "2026-01-11T15:30:00",
  "stopReason": "end_turn"
}
```

**What to Notice**:
- `response`: Claude's actual answer
- `tokensUsed`: Total tokens consumed
- `inputTokens`: Your message (prompt)
- `outputTokens`: Claude's response
- `stopReason`: Why Claude stopped (usually "end_turn")

---

### Test 3: Chat with System Prompt ✅

**Purpose**: Guide Claude's behavior with a system prompt

**Request**:
```json
{
  "message": "Write a haiku about programming",
  "systemPrompt": "You are a creative poet who loves technology"
}
```

**What's Different?**
- `systemPrompt` tells Claude how to behave
- Think of it as Claude's "personality" or "role"

**Try These System Prompts**:
- `"You are a helpful Java programming tutor"`
- `"You are a senior software architect"`
- `"You are a friendly AI that explains things to children"`

---

### Test 4: Controlling Temperature ✅

**Purpose**: Understand how temperature affects responses

**High Creativity (Temperature = 1.0)**:
```json
{
  "message": "Tell me a creative story about a robot",
  "temperature": 1.0,
  "maxTokens": 500
}
```

**Low Creativity (Temperature = 0.0)**:
```json
{
  "message": "What is 2 + 2?",
  "temperature": 0.0,
  "maxTokens": 100
}
```

**Experiment**:
1. Ask the same question with temperature 0.0, 0.5, and 1.0
2. Compare the responses
3. Notice: Lower temp = more consistent, Higher temp = more varied

---

### Test 5: Error Handling ✅

**Purpose**: See how the application handles errors

**Test 5a: Empty Message**
```json
{
  "message": ""
}
```

**Expected Response**: 400 Bad Request
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Invalid request parameters",
  "validationErrors": [
    "message: Message cannot be empty"
  ]
}
```

**Test 5b: Message Too Long**
```json
{
  "message": "A".repeat(10001)  // More than 10,000 characters
}
```

**Expected Response**: 400 Bad Request (validation error)

**Test 5c: Invalid API Key**
1. Stop the application
2. Edit `.env` file
3. Change `ANTHROPIC_API_KEY` to invalid value
4. Restart application
5. Try to send a message

**Expected Response**: 401 Unauthorized

---

## 📊 Understanding Token Usage and Cost

### Example Calculation

Let's say you make this request:
```json
{
  "message": "Explain quantum computing in 100 words"
}
```

Response:
```json
{
  "inputTokens": 50,
  "outputTokens": 150,
  "tokensUsed": 200
}
```

### Cost Breakdown

**Claude 3.5 Sonnet Pricing** (as of Jan 2026):
- Input tokens: $3 per million tokens
- Output tokens: $15 per million tokens

**This Request**:
- Input cost: (50 / 1,000,000) × $3 = $0.00015
- Output cost: (150 / 1,000,000) × $15 = $0.00225
- **Total cost: $0.00240** (less than a quarter of a cent!)

### Practical Implications

**1,000 requests like this = $2.40**

For your learning project, you'll probably use:
- ~100-500 requests during development
- **Total cost: $0.50 - $2.50** for the entire week!

### Tips to Save Money:
1. Set `maxTokens` appropriately (don't use 4096 if you need 100)
2. Use lower temperature for factual questions (faster, shorter)
3. Cache responses during development (implement in Phase 2)
4. Use system prompts to guide shorter responses

---

## 🔍 Exploring the API Documentation

### Swagger UI

1. Open: http://localhost:8080/swagger-ui.html
2. You'll see interactive API documentation
3. Try the "Try it out" button!

Features:
- Test endpoints directly from browser
- See request/response schemas
- View all available endpoints
- No Postman needed!

### Steps to Test via Swagger:
1. Go to http://localhost:8080/swagger-ui.html
2. Find "Phase 1: LLM Integration"
3. Click on `POST /api/chat`
4. Click "Try it out"
5. Edit the request body
6. Click "Execute"
7. See the response below!

---

## 🐛 Troubleshooting

### Problem: Application won't start

**Error**: "Port 8080 already in use"

**Solution**:
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /F /PID <PID>

# Or change port in .env
SERVER_PORT=8081
```

---

### Problem: "Invalid API key" error

**Symptoms**: 401 Unauthorized responses

**Solution**:
1. Check `.env` file exists
2. Verify `ANTHROPIC_API_KEY` is correct
3. Key should start with `sk-ant-api03-`
4. No extra spaces or quotes around the key
5. Restart application after changing `.env`

---

### Problem: Oracle connection errors

**Symptoms**: Application fails to start with database errors

**For Phase 1**: We're not using the database yet!

**Quick Fix**:
1. Edit `application.yml`
2. Comment out datasource section temporarily:
```yaml
# spring:
#   datasource:
#     url: ...
```
3. Restart application

Or just make sure Oracle is running:
```bash
docker-compose up -d oracle-23c
```

---

### Problem: Maven build fails

**Symptoms**: "Cannot resolve dependencies"

**Solution**:
1. In IntelliJ: Maven tool window → Reload button (↻)
2. Or: `mvn clean install -U`
3. Check internet connection
4. Try deleting `~/.m2/repository` and rebuild

---

### Problem: Slow responses from Claude

**Possible Causes**:
1. Large `maxTokens` setting
2. Network latency
3. Claude API is under heavy load

**Solutions**:
- Set reasonable `maxTokens` (100-500 for most queries)
- Check logs for actual response time
- Increase timeout in `application.yml` if needed

---

## 📖 Code Walkthrough

### How Does a Request Flow Through the Code?

Let's trace a request step by step:

#### 1. User sends request
```json
POST /api/chat
{
  "message": "Hello Claude!"
}
```

#### 2. ChatController receives it (ChatController.java:72)
```java
public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request)
```
- `@Valid` triggers validation
- If message is empty, validation fails → 400 error
- If valid, proceeds to service

#### 3. ChatService processes it (ChatService.java:35)
```java
public ChatResponse chat(ChatRequest chatRequest)
```
- Creates ClaudeApiRequest using helper method
- Calls ClaudeApiService

#### 4. ClaudeApiService sends to Claude (ClaudeApiService.java:74)
```java
public ClaudeApiResponse sendMessage(ClaudeApiRequest request)
```
- Converts request to JSON
- Adds authentication headers (API key)
- Sends HTTP POST to Anthropic
- Waits for response
- Parses JSON response

#### 5. Response flows back up
- ClaudeApiResponse → ChatService → ChatController → User
- Each layer transforms the data appropriately

---

## 🎓 Key Learnings from Phase 1

### Conceptual Learnings:

1. **LLMs are APIs**: They're external services you call via HTTP
2. **Tokens matter**: They determine both speed and cost
3. **Temperature controls creativity**: Low = consistent, High = creative
4. **System prompts guide behavior**: Like giving Claude a role to play
5. **Error handling is critical**: External APIs can fail in many ways

### Technical Learnings:

1. **Layered architecture**: Controller → Service → External API
2. **DTOs for data transfer**: Separate internal and external formats
3. **Configuration management**: Use .env for secrets, YAML for settings
4. **Exception handling**: Global handler provides consistent errors
5. **REST API design**: Clear endpoints, proper status codes, good documentation

### Spring Boot Learnings:

1. **@RestController**: Creates REST endpoints
2. **@Service**: Business logic components
3. **@Configuration**: Configuration beans
4. **@Valid**: Input validation
5. **@RestControllerAdvice**: Global exception handling

---

## ✅ Phase 1 Checklist

Before moving to Phase 2, make sure you can:

- [ ] Start the Spring Boot application successfully
- [ ] Send a simple chat request via Postman
- [ ] Understand the response structure (tokens, model, etc.)
- [ ] Use system prompts to guide Claude's behavior
- [ ] Adjust temperature and see different responses
- [ ] Handle errors (empty message, invalid params)
- [ ] View API docs in Swagger UI
- [ ] Calculate approximate cost of requests
- [ ] Explain how the request flows through the code
- [ ] Understand the three-layer architecture

---

## 🎯 Manager Demo - Phase 1

### What to Show:

1. **Quick Architecture Overview** (2 minutes)
   - Show the architecture diagram
   - Explain the three layers

2. **Live Demo** (5 minutes)
   - Open Swagger UI
   - Send a simple question: "What is Spring Boot?"
   - Show the response with token counts
   - Demonstrate system prompt: "You are a Java expert"
   - Show error handling: Send empty message

3. **Cost Discussion** (2 minutes)
   - Explain token-based pricing
   - Show example calculation
   - Mention the cost is very low for learning

4. **Key Takeaways** (1 minute)
   - Successfully integrated with Claude API
   - Built foundation for more advanced features
   - Ready to move to Phase 2: Prompt Engineering

### Talking Points:
- "We've successfully integrated with Anthropic's Claude API"
- "The application follows clean architecture principles"
- "Token-based pricing means costs scale with usage"
- "This foundation supports all upcoming phases"

---

## 🚀 Next: Phase 2

Tomorrow we'll explore **Prompt Engineering**:
- Zero-shot prompting
- Few-shot prompting
- Chain-of-thought prompting
- Structured output prompting

You'll build endpoints that demonstrate each technique and understand when to use which approach!

**Take a break, test the application, and be ready for Phase 2!** 🎉
