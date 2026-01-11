# Phase 1: Foundation Setup + LLM Integration

> **🆓 PROVIDER UPDATE**: This project now uses **Google Gemini** as the PRIMARY provider (FREE tier with 1,500 requests/day). Anthropic Claude is available as an OPTIONAL secondary provider (paid tier). The multi-provider architecture allows easy switching between providers.

## 🎯 Learning Objectives

By the end of Phase 1, you will understand:

1. ✅ **What is an LLM** and how does it work at a high level
2. ✅ **How to integrate** with LLM APIs (Google Gemini as primary FREE provider)
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

### How Does an LLM Work?

```
Your Prompt → LLM's Neural Network → Generated Response
              (billions of parameters)
```

**Example with Google Gemini (our primary provider)**:
1. **You send a message** (text input)
2. **Gemini processes it** using its neural network
3. **Gemini generates a response** word by word (technically, token by token)
4. **You receive the response** with metadata (tokens used, etc.)

**Note**: This project supports multiple LLM providers:
- **Google Gemini** (PRIMARY): FREE tier with generous limits, gemini-2.5-flash model
- **Anthropic Claude** (OPTIONAL): Paid tier, claude-3-5-sonnet model

### Key Concepts:

#### Tokens
- Text is broken into "tokens" (roughly 4 characters = 1 token)
- Example: "Hello, world!" = ~3 tokens
- Why it matters: You pay per token!

#### Context Window
- Maximum amount of text an LLM can process at once
- **Google Gemini 2.5 Flash**: 1,000,000+ tokens (~750,000 words) - MASSIVE!
- **Claude 3.5 Sonnet**: 200,000 tokens (~150,000 words)
- Includes both your prompt AND the LLM's response

#### Temperature
- Controls randomness (0.0 to 1.0)
- **Low (0.0-0.3)**: Focused, deterministic, consistent
- **Medium (0.4-0.7)**: Balanced
- **High (0.8-1.0)**: Creative, random, varied

#### Stop Reason
- Why did the LLM stop responding?
- `end_turn` / `STOP`: Natural completion (finished the thought)
- `max_tokens` / `MAX_TOKENS`: Hit the token limit (response was cut off)
- Different providers use different naming conventions

### Choosing Between Providers

#### Use Gemini (PRIMARY - FREE) When:
✅ Learning and development
✅ Cost is a concern (it's FREE!)
✅ Need fast responses
✅ Need huge context window (1M+ tokens)
✅ Building prototypes and MVPs
✅ Rate limits are acceptable (1,500/day, 15/minute)

#### Use Claude (OPTIONAL - PAID) When:
💰 Need highest quality responses
💰 Complex reasoning tasks
💰 Better instruction following required
💰 Commercial production use
💰 Need higher rate limits
💰 Budget allows for paid API

**For this learning project: Stick with Gemini (FREE)!**

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
│  • Multi-provider support (routes to Gemini or Claude)     │
│  • Converts between internal and provider formats          │
└────────────────┬───────────────────────┬────────────────────┘
                 │                       │
                 ▼                       ▼
┌─────────────────────────────┐  ┌─────────────────────────────┐
│    GeminiApiService         │  │   ClaudeApiService          │
│    (PRIMARY - FREE)         │  │   (OPTIONAL - PAID)         │
│  • Google Gemini API client │  │  • Anthropic Claude client  │
│  • FREE tier!               │  │  • Paid API key required    │
│  • 1M+ token context        │  │  • 200K token context       │
└──────────────┬──────────────┘  └──────────────┬──────────────┘
               │ HTTPS POST                     │ HTTPS POST
               │ generativelanguage.googleapis  │ api.anthropic.com
               ▼                                 ▼
┌─────────────────────────────┐  ┌─────────────────────────────┐
│   Google Gemini API         │  │   Anthropic Claude API      │
│   (External - FREE Tier)    │  │   (External - Paid)         │
└─────────────────────────────┘  └─────────────────────────────┘
```

### Why This Layered Architecture?

1. **ChatController**: Handles HTTP concerns (validation, status codes)
2. **ChatService**: Business logic (converting DTOs, routing between providers)
3. **GeminiApiService / ClaudeApiService**: Provider-specific API communication

Benefits:
- **Separation of Concerns**: Each layer has one responsibility
- **Multi-Provider Support**: Easy to switch between Gemini and Claude
- **Testability**: Can mock each layer independently
- **Maintainability**: Changes in one layer don't affect others
- **Reusability**: Services can be used by multiple controllers
- **Cost Flexibility**: Start with FREE Gemini, upgrade to Claude if needed

---

## 📂 Project Structure

```
src/main/java/com/ai/mvp/
├── LlmAgenticAiMvpApplication.java    # Main Spring Boot application
├── config/
│   ├── GeminiConfig.java              # Gemini API configuration (PRIMARY)
│   └── AnthropicConfig.java           # Claude API configuration (OPTIONAL)
├── controller/
│   └── ChatController.java            # REST endpoints
├── service/
│   ├── ChatService.java               # Business logic + multi-provider routing
│   ├── GeminiApiService.java          # Gemini API client (PRIMARY - FREE)
│   └── ClaudeApiService.java          # Claude API client (OPTIONAL - PAID)
├── dto/
│   ├── ChatRequest.java               # User request
│   ├── ChatResponse.java              # User response
│   ├── GeminiApiRequest.java          # Gemini API request format
│   ├── GeminiApiResponse.java         # Gemini API response format
│   ├── ClaudeApiRequest.java          # Claude API request format
│   ├── ClaudeApiResponse.java         # Claude API response format
│   └── ErrorResponse.java             # Error format
└── exception/
    ├── GeminiApiException.java        # Gemini exception
    ├── ClaudeApiException.java        # Claude exception
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

**Purpose**: Send your first message to the LLM (Gemini by default)!

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

**Expected Response** (Using Gemini - PRIMARY FREE provider):
```json
{
  "response": "A Large Language Model (LLM) is...",
  "model": "gemini-2.5-flash",
  "tokensUsed": 245,
  "inputTokens": 45,
  "outputTokens": 200,
  "timestamp": "2026-01-11T15:30:00",
  "stopReason": "STOP"
}
```

**What to Notice**:
- `response`: The LLM's actual answer
- `model`: "gemini-2.5-flash" (FREE tier) or "claude-3-5-sonnet-20241022" (paid)
- `tokensUsed`: Total tokens consumed (FREE with Gemini!)
- `inputTokens`: Your message (prompt)
- `outputTokens`: The LLM's response
- `stopReason`: Why it stopped ("STOP" for Gemini, "end_turn" for Claude)

---

### Test 3: Chat with System Prompt ✅

**Purpose**: Guide the LLM's behavior with a system prompt

**Request**:
```json
{
  "message": "Write a haiku about programming",
  "systemPrompt": "You are a creative poet who loves technology"
}
```

**What's Different?**
- `systemPrompt` tells the LLM how to behave
- Think of it as the LLM's "personality" or "role"
- Works with both Gemini and Claude!

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

### 🆓 **Google Gemini (PRIMARY - FREE Tier)**

**Gemini 2.5 Flash Pricing** (as of Jan 2026):
- Input tokens: **FREE** up to 1,500 requests per day
- Output tokens: **FREE** up to 1,500 requests per day
- Rate limit: 15 requests per minute (RPM)

**This Request with Gemini**:
- Input cost: **$0.00** (FREE!)
- Output cost: **$0.00** (FREE!)
- **Total cost: $0.00** ✨

### Practical Implications with Gemini

**1,000 requests = $0.00** (COMPLETELY FREE!)

For your learning project:
- ~100-500 requests during development
- **Total cost: $0.00** for the entire week! 🎉

---

### 💰 **Anthropic Claude (OPTIONAL - Paid Tier)**

**Claude 3.5 Sonnet Pricing** (as of Jan 2026):
- Input tokens: $3 per million tokens
- Output tokens: $15 per million tokens

**This Request with Claude**:
- Input cost: (50 / 1,000,000) × $3 = $0.00015
- Output cost: (150 / 1,000,000) × $15 = $0.00225
- **Total cost: $0.00240** (less than a quarter of a cent)

**1,000 requests with Claude = $2.40**

### Why Use Claude?
- Higher quality responses for complex tasks
- Better instruction following
- Longer context if you pay for Claude 3 Opus
- Commercial use without rate limits

**For learning: Use Gemini (FREE). For production: Consider Claude (PAID).**

### Tips to Optimize Usage:
1. **Start with Gemini (FREE)** - No cost for learning and development!
2. Set `maxTokens` appropriately (don't use 4096 if you need 100)
3. Use lower temperature for factual questions (faster, shorter)
4. Cache responses during development (implement in Phase 2)
5. Use system prompts to guide shorter responses
6. **Switch to Claude** only if you need higher quality for specific tasks

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

**Symptoms**: 401 Unauthorized or API authentication errors

**Solution for Gemini (PRIMARY)**:
1. Check `.env` file exists
2. Verify `GEMINI_API_KEY` is correct
3. Key should start with `AIza...`
4. No extra spaces or quotes around the key
5. Restart application after changing `.env`

**Solution for Claude (OPTIONAL)**:
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

### Problem: Slow responses from LLM

**Possible Causes**:
1. Large `maxTokens` setting
2. Network latency
3. Provider API is under heavy load
4. Claude is slower than Gemini (but higher quality)

**Solutions**:
- Set reasonable `maxTokens` (100-500 for most queries)
- Check logs for actual response time
- Increase timeout in `application.yml` if needed
- **Try Gemini if Claude is slow** - Gemini is generally faster!

---

## 📖 Code Walkthrough

### How Does a Request Flow Through the Code?

Let's trace a request step by step:

#### 1. User sends request
```json
POST /api/chat
{
  "message": "Hello!"
}
```

#### 2. ChatController receives it (ChatController.java:72)
```java
public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request)
```
- `@Valid` triggers validation
- If message is empty, validation fails → 400 error
- If valid, proceeds to service

#### 3. ChatService processes it (ChatService.java)
```java
public ChatResponse chat(ChatRequest chatRequest)
```
- Routes to appropriate provider (Gemini by default)
- Creates provider-specific API request
- Calls GeminiApiService or ClaudeApiService

#### 4. Provider API Service sends to LLM
**GeminiApiService (PRIMARY - FREE)**:
```java
public GeminiApiResponse sendMessage(GeminiApiRequest request)
```
- Converts request to Gemini format
- Adds API key authentication
- Sends HTTP POST to Google Generative AI API
- Parses JSON response

**ClaudeApiService (OPTIONAL - PAID)**:
```java
public ClaudeApiResponse sendMessage(ClaudeApiRequest request)
```
- Converts request to Claude format
- Adds authentication headers (API key)
- Sends HTTP POST to Anthropic API
- Parses JSON response

#### 5. Response flows back up
- Provider Response → ChatService → ChatController → User
- Each layer transforms the data appropriately
- User doesn't need to know which provider was used!

---

## 🎓 Key Learnings from Phase 1

### Conceptual Learnings:

1. **LLMs are APIs**: They're external services you call via HTTP
2. **Multiple providers exist**: Gemini (FREE), Claude (PAID), GPT, etc.
3. **Tokens matter**: They determine both speed and cost (or FREE with Gemini!)
4. **Temperature controls creativity**: Low = consistent, High = creative
5. **System prompts guide behavior**: Like giving the LLM a role to play
6. **Error handling is critical**: External APIs can fail in many ways
7. **Start FREE, scale paid**: Use Gemini for development, Claude for production if needed

### Technical Learnings:

1. **Layered architecture**: Controller → Service → Multiple Provider APIs
2. **Multi-provider pattern**: Abstract provider differences, unified interface
3. **DTOs for data transfer**: Separate internal and external formats per provider
4. **Configuration management**: Use .env for secrets, YAML for settings
5. **Exception handling**: Global handler provides consistent errors across providers
6. **REST API design**: Clear endpoints, proper status codes, good documentation

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
- [ ] Understand the response structure (tokens, model, provider, etc.)
- [ ] Know that Gemini is PRIMARY (FREE) and Claude is OPTIONAL (PAID)
- [ ] Use system prompts to guide the LLM's behavior
- [ ] Adjust temperature and see different responses
- [ ] Handle errors (empty message, invalid params)
- [ ] View API docs in Swagger UI
- [ ] Understand Gemini's FREE tier vs Claude's PAID tier
- [ ] Explain how the request flows through the multi-provider architecture
- [ ] Understand the layered architecture with multiple LLM providers

---

## 🎯 Manager Demo - Phase 1

### What to Show:

1. **Quick Architecture Overview** (2 minutes)
   - Show the architecture diagram
   - Explain the three layers

2. **Live Demo** (5 minutes)
   - Open Swagger UI
   - Send a simple question: "What is Spring Boot?"
   - Show the response with Gemini (FREE tier!)
   - Point out the model: "gemini-2.5-flash"
   - Demonstrate system prompt: "You are a Java expert"
   - Show error handling: Send empty message

3. **Cost Discussion** (2 minutes)
   - **Highlight Gemini is FREE** - 1,500 requests/day at no cost!
   - Explain Claude as optional paid alternative
   - Show the cost comparison
   - Emphasize: "We can develop for FREE, scale to paid if needed"

4. **Key Takeaways** (1 minute)
   - Successfully integrated with Google Gemini (FREE) as primary provider
   - Claude available as optional paid alternative
   - Multi-provider architecture supports easy switching
   - Built foundation for more advanced features
   - Ready to move to Phase 2: Prompt Engineering

### Talking Points:
- "We've integrated with Google Gemini's FREE tier as our primary provider"
- "Multi-provider architecture allows switching to Claude if needed"
- "The application follows clean architecture principles"
- "Zero cost for development and learning with Gemini!"
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
