# Phase 2: Prompt Engineering Techniques

> **🆓 PROVIDER UPDATE**: This project uses **Google Gemini** as the PRIMARY provider (FREE tier with 1,500 requests/day). All prompt engineering techniques work seamlessly with Gemini's API!

## 🎯 Learning Objectives

By the end of Phase 2, you will understand:

1. ✅ **What is Prompt Engineering** and why it matters
2. ✅ **Zero-Shot Prompting** - Direct questions without examples
3. ✅ **Few-Shot Prompting** - Learning from examples
4. ✅ **Chain-of-Thought Prompting** - Step-by-step reasoning
5. ✅ **Structured Output Prompting** - JSON/formatted responses
6. ✅ **When to use each technique** and how they compare
7. ✅ **How to construct effective prompts** that get better results

---

## 📚 What is Prompt Engineering?

### Definition

**Prompt Engineering** is the art and science of crafting inputs (prompts) to get the best possible outputs from Large Language Models (LLMs).

Think of it like this:
- **Bad prompt**: "Tell me about dogs" → Generic, unfocused response
- **Good prompt**: "List 5 dog breeds that are good for apartment living, with brief reasons why" → Specific, actionable response

### Why Does Prompt Engineering Matter?

The same LLM (like Gemini 2.5 Flash) can give you:
- ❌ Useless, vague answers with a bad prompt
- ✅ Precise, valuable answers with a good prompt

**The difference is NOT the model - it's the prompt!**

### Key Concepts

#### 1. Clarity Matters
```
❌ Bad: "What about cars?"
✅ Good: "Compare electric vs gas cars in terms of cost, maintenance, and environmental impact"
```

#### 2. Context Helps
```
❌ Bad: "Write code"
✅ Good: "Write a Java Spring Boot REST endpoint that returns a list of users from a database"
```

#### 3. Examples Guide Output
```
❌ Bad: "Classify this review: 'It's okay'"
✅ Good:
Review: "Excellent!" → Positive
Review: "Terrible!" → Negative
Review: "It's okay" → ?
```

#### 4. Structure Ensures Consistency
```
❌ Bad: "Extract the name and age"
✅ Good: "Extract and return ONLY this JSON: {\"name\": \"...\", \"age\": ...}"
```

### The 4 Techniques We'll Learn

| Technique | Best For | Temperature | Example Use Case |
|-----------|----------|-------------|------------------|
| **Zero-Shot** | Simple, direct questions | 0.7 | "Translate 'Hello' to Spanish" |
| **Few-Shot** | Consistent formatting | 0.3 | Sentiment analysis with examples |
| **Chain-of-Thought** | Complex reasoning | 0.2-0.3 | Math word problems |
| **Structured Output** | API integration | 0.1-0.2 | Extract data to JSON |

---

## 🔍 The 4 Prompt Engineering Techniques

### Technique 1: Zero-Shot Prompting

#### What is Zero-Shot?

**Zero-Shot** means asking the LLM directly without providing any examples or special formatting.

**The Philosophy**: "Just ask. The LLM has seen this before during training."

#### When to Use Zero-Shot

✅ **Use Zero-Shot When:**
- The task is straightforward
- The LLM has likely seen similar questions during training
- You want creative/varied responses
- Speed matters (fewer tokens = faster)

**Examples:**
- Translation: "Translate 'Good morning' to French"
- Simple Q&A: "What is Spring Boot?"
- Summarization: "Summarize this article in 3 sentences"
- Creative writing: "Write a haiku about programming"

❌ **Don't Use Zero-Shot When:**
- You need consistent formatting across multiple requests
- The task is ambiguous or unusual
- You need specific output structure (like JSON)

#### How Zero-Shot Works

**Your Input:**
```json
{
  "message": "What is the capital of France?"
}
```

**What Gets Sent to Gemini:**
```
What is the capital of France?
```

**LLM Response:**
```
The capital of France is Paris.
```

**Simple as that!** Zero-shot is just the raw question.

#### Real-World Example: Translation

**Prompt:**
```
Translate the following English text to Spanish:
"Hello, how are you today?"
```

**Response:**
```
Hola, ¿cómo estás hoy?
```

**Why it works:** Translation is a common task. Gemini has seen millions of translation examples during training.

---

### Technique 2: Few-Shot Prompting

#### What is Few-Shot?

**Few-Shot** means providing 2-5 examples of the task before asking the LLM to process your actual input.

**The Philosophy**: "Show me a few examples, and I'll understand the pattern."

#### When to Use Few-Shot

✅ **Use Few-Shot When:**
- You need consistent output formatting
- The task is ambiguous without examples
- You want to establish a specific pattern
- Custom formatting that the LLM hasn't seen before

**Examples:**
- Sentiment classification with custom labels
- Code refactoring with your team's style
- Data extraction with specific field names
- Custom categorization schemes

❌ **Don't Use Few-Shot When:**
- The task is already clear (zero-shot is simpler)
- You need complex reasoning (chain-of-thought is better)
- Examples are hard to create or don't fit the pattern

#### How Few-Shot Works

**Your Input:**
```json
{
  "task": "Classify the sentiment of product reviews",
  "examples": [
    {"input": "This product is amazing!", "output": "Positive"},
    {"input": "Terrible quality, waste of money", "output": "Negative"},
    {"input": "It's fine, nothing special", "output": "Neutral"}
  ],
  "input": "Best purchase ever! Highly recommend."
}
```

**What Gets Sent to Gemini:**
```
Classify the sentiment of product reviews

Input: "This product is amazing!"
Output: Positive

Input: "Terrible quality, waste of money"
Output: Negative

Input: "It's fine, nothing special"
Output: Neutral

Input: "Best purchase ever! Highly recommend."
Output:
```

**LLM Response:**
```
Positive
```

**Why it works:** The LLM sees the pattern (input → sentiment label) and applies it consistently.

#### Real-World Example: Email Classification

**Prompt:**
```
Classify emails into categories:

Input: "Meeting at 3pm tomorrow"
Output: Calendar

Input: "Your invoice is attached"
Output: Finance

Input: "Bug in production, urgent!"
Output: Technical

Input: "Happy birthday! 🎉"
Output:
```

**Response:**
```
Personal
```

---

### Technique 3: Chain-of-Thought Prompting

#### What is Chain-of-Thought?

**Chain-of-Thought** (CoT) asks the LLM to show its reasoning process step-by-step before giving the final answer.

**The Philosophy**: "Think out loud. Show your work like in a math class."

#### When to Use Chain-of-Thought

✅ **Use Chain-of-Thought When:**
- Solving math problems
- Logic puzzles and riddles
- Complex analysis requiring multiple steps
- You need to verify the reasoning
- Debugging and troubleshooting

**Examples:**
- "A store has 50 apples. They sell 30% in the morning and 1/4 of the remainder in the afternoon. How many left?"
- "If all bloops are razzies and all razzies are lazzies, are all bloops definitely lazzies?"
- "Why is my Spring Boot application not connecting to the database?"

❌ **Don't Use Chain-of-Thought When:**
- The answer is simple and direct
- You just need the final answer (reasoning adds cost/time)
- The task doesn't involve logical steps

#### How Chain-of-Thought Works

**Your Input:**
```json
{
  "problem": "A store has 15 apples. They sell 40% in the morning and 1/3 of the remainder in the afternoon. How many apples are left?",
  "requestSteps": true,
  "reasoningPrompt": "Let's solve this step by step:"
}
```

**What Gets Sent to Gemini:**
```
A store has 15 apples. They sell 40% in the morning and 1/3 of the remainder in the afternoon. How many apples are left?

Let's solve this step by step:
```

**LLM Response:**
```
Let's solve this step by step:

1. Start with 15 apples total

2. Morning sales: 40% of 15
   40% = 0.40
   15 × 0.40 = 6 apples sold

3. Apples remaining after morning: 15 - 6 = 9 apples

4. Afternoon sales: 1/3 of the remainder (9 apples)
   9 ÷ 3 = 3 apples sold

5. Final count: 9 - 3 = 6 apples

Answer: 6 apples are left.
```

**Why it works:** Breaking the problem into steps reduces errors and makes the answer verifiable.

#### Real-World Example: Logic Puzzle

**Prompt:**
```
Solve this logic puzzle step by step:

Alice is taller than Bob.
Bob is taller than Charlie.
Charlie is shorter than Diana.
Diana is shorter than Bob.

Who is the tallest?

Let's think through this step by step:
```

**Response:**
```
Let's think through this step by step:

1. From "Alice is taller than Bob": Alice > Bob
2. From "Bob is taller than Charlie": Bob > Charlie
3. Combining 1 and 2: Alice > Bob > Charlie
4. From "Diana is shorter than Bob": Bob > Diana
5. From "Charlie is shorter than Diana": Diana > Charlie
6. Combining all: Alice > Bob > Diana > Charlie

Therefore, Alice is the tallest.
```

#### The Magic of Chain-of-Thought

**Without CoT:**
```
Q: If 3 hens lay 3 eggs in 3 days, how many eggs do 12 hens lay in 12 days?
A: 48 eggs  ❌ WRONG!
```

**With CoT:**
```
Q: If 3 hens lay 3 eggs in 3 days, how many eggs do 12 hens lay in 12 days?

Let's think step by step:
1. 3 hens lay 3 eggs in 3 days
2. Rate per hen: 3 eggs ÷ 3 hens = 1 egg per hen in 3 days
3. Rate per hen per day: 1 egg ÷ 3 days = 1/3 egg per day
4. 12 hens for 12 days: 12 hens × 12 days × (1/3 egg/day) = 48 eggs

Wait, let me recalculate:
Actually, if 3 hens lay 3 eggs in 3 days, each hen lays 1 egg in 3 days.
So in 12 days, each hen lays 4 eggs.
12 hens × 4 eggs = 48 eggs. ✅ CORRECT!
```

---

### Technique 4: Structured Output Prompting

#### What is Structured Output?

**Structured Output** prompting requests responses in specific formats like JSON, XML, CSV, or YAML.

**The Philosophy**: "Give me data I can actually use in my code, not just text."

#### When to Use Structured Output

✅ **Use Structured Output When:**
- Integrating with other systems/APIs
- Storing data in databases
- Building data pipelines
- Parsing responses programmatically
- Need consistent field names and types

**Examples:**
- Extract job posting details to JSON
- Parse invoice data into structured format
- Convert unstructured text to database records
- API responses that need to be consumed by frontend

❌ **Don't Use Structured Output When:**
- You need a human-readable narrative response
- The output structure varies too much
- Simple Q&A where text is fine

#### How Structured Output Works

**Your Input:**
```json
{
  "task": "Extract key information from this job posting",
  "inputText": "Senior Java Developer at Tech Corp. 5+ years experience required. Salary: $120k-150k. Location: San Francisco, CA. Remote work available.",
  "outputFormat": "{\"position\": \"...\", \"experience\": \"...\", \"salary\": \"...\", \"location\": \"...\", \"remote\": ...}",
  "formatType": "JSON"
}
```

**What Gets Sent to Gemini:**
```
Extract key information from this job posting

Text to process:
"Senior Java Developer at Tech Corp. 5+ years experience required. Salary: $120k-150k. Location: San Francisco, CA. Remote work available."

Return the result in JSON format:
{"position": "...", "experience": "...", "salary": "...", "location": "...", "remote": ...}

IMPORTANT: Return ONLY the JSON - no additional text, explanations, or markdown formatting.
```

**LLM Response:**
```json
{
  "position": "Senior Java Developer",
  "experience": "5+ years",
  "salary": "$120k-150k",
  "location": "San Francisco, CA",
  "remote": true
}
```

**Why it works:**
- Clear format specification
- Very low temperature (0.1-0.2) for deterministic output
- System prompt enforces strict formatting
- Explicit instruction to return ONLY the format

#### Real-World Example: Extract Customer Data

**Prompt:**
```
Extract customer information from this message and return as JSON.

Text to process:
"Hi, my name is Sarah Johnson, email sarah.j@email.com, phone 555-0123. I'm interested in the Enterprise plan starting March 15th."

Return the result in JSON format:
{
  "name": "...",
  "email": "...",
  "phone": "...",
  "plan": "...",
  "startDate": "..."
}

IMPORTANT: Return ONLY the JSON - no additional text, explanations, or markdown formatting.
```

**Response:**
```json
{
  "name": "Sarah Johnson",
  "email": "sarah.j@email.com",
  "phone": "555-0123",
  "plan": "Enterprise",
  "startDate": "2026-03-15"
}
```

**Now you can parse this in your code:**
```java
ObjectMapper mapper = new ObjectMapper();
CustomerData customer = mapper.readValue(response, CustomerData.class);
// customer.getName() → "Sarah Johnson"
// customer.getEmail() → "sarah.j@email.com"
```

#### Common Pitfalls with Structured Output

❌ **Pitfall 1: LLM adds explanations**
```json
Here's the extracted data:
{
  "name": "John"
}
I found the name in the first sentence.
```

**Solution:** Add "IMPORTANT: Return ONLY the JSON - no additional text"

❌ **Pitfall 2: LLM wraps in markdown**
```markdown
```json
{"name": "John"}
```
```

**Solution:** Explicitly say "no markdown formatting" and use temperature 0.1

❌ **Pitfall 3: Inconsistent field names**
```json
{"name": "John"}  // vs  {"full_name": "John"}  // vs  {"userName": "John"}
```

**Solution:** Show exact field names in the format specification

---

## 🎯 When to Use Each Technique: Decision Matrix

### Quick Decision Tree

```
START: What are you trying to do?
│
├─ Simple question, straightforward task
│  └─ ✅ USE ZERO-SHOT
│     Example: "What is Spring Boot?"
│
├─ Need consistent formatting across many requests
│  └─ ✅ USE FEW-SHOT
│     Example: Classify 1000 emails the same way
│
├─ Complex problem requiring reasoning/math
│  └─ ✅ USE CHAIN-OF-THOUGHT
│     Example: Multi-step calculations, logic puzzles
│
└─ Need structured data for your code/database
   └─ ✅ USE STRUCTURED OUTPUT
      Example: Extract invoice data to JSON
```

### Comparison Table

| Aspect | Zero-Shot | Few-Shot | Chain-of-Thought | Structured Output |
|--------|-----------|----------|------------------|-------------------|
| **Complexity** | Simplest | Medium | Medium-High | Medium |
| **Setup Time** | None | Need examples | Need reasoning prompt | Need format spec |
| **Token Cost** | Lowest | Medium | Highest | Medium |
| **Consistency** | Varies | High | Medium | Very High |
| **Best Temperature** | 0.7 | 0.3 | 0.2-0.3 | 0.1-0.2 |
| **Use Case** | General Q&A | Pattern learning | Complex reasoning | Data extraction |

### Real-World Scenarios

#### Scenario 1: Customer Support Chatbot

**Task**: Classify customer inquiries into categories

**Best Technique**: **Few-Shot Prompting**

**Why**: You need consistent categorization across thousands of messages. Examples teach the LLM your specific categories.

```
Classify customer inquiries:

Input: "My order hasn't arrived yet"
Output: Shipping

Input: "How do I reset my password?"
Output: Account

Input: "I need a refund"
Output: Billing

Input: "Does this work with iOS?"
Output:
```

---

#### Scenario 2: Automated Invoice Processing

**Task**: Extract invoice details from email

**Best Technique**: **Structured Output Prompting**

**Why**: You need JSON to insert into database. Consistent fields, parseable format.

```json
{
  "task": "Extract invoice details",
  "inputText": "Invoice #INV-2024-001, Amount: $1,250, Due: Jan 30, 2026",
  "outputFormat": "{\"invoiceNumber\": \"...\", \"amount\": ..., \"dueDate\": \"...\"}",
  "formatType": "JSON"
}
```

---

#### Scenario 3: Education Platform Math Helper

**Task**: Solve word problems for students

**Best Technique**: **Chain-of-Thought Prompting**

**Why**: Students need to see the steps, not just the answer. Shows how to solve similar problems.

```
Problem: If a train travels 60 km/h for 2.5 hours, how far does it go?

Let's solve this step by step:
1. Speed = 60 km/h
2. Time = 2.5 hours
3. Distance = Speed × Time
4. Distance = 60 × 2.5 = 150 km

Answer: 150 km
```

---

#### Scenario 4: Language Translation Service

**Task**: Translate phrases between languages

**Best Technique**: **Zero-Shot Prompting**

**Why**: Translation is straightforward. The LLM already knows how to translate. No examples needed.

```
Translate to German: "Where is the nearest hospital?"
```

---

## 🧪 Testing Phase 2: Hands-On Examples

### Prerequisites

Make sure your application is running:
```bash
# Start the application
mvn spring-boot:run

# Verify it's running
curl http://localhost:8080/api/prompt/health
# Expected: "Prompt engineering service is ready! 🚀"
```

---

### Test 1: Zero-Shot Prompting ✅

#### Simple Translation

**Request:**
```bash
curl -X POST http://localhost:8080/api/prompt/zero-shot \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Translate the following to French: Hello, how are you today?",
    "maxTokens": 100,
    "temperature": 0.7
  }'
```

**Expected Response:**
```json
{
  "response": "Bonjour, comment allez-vous aujourd'hui ?",
  "technique": "ZERO_SHOT",
  "fullPrompt": "Translate the following to French: Hello, how are you today?",
  "model": "gemini-2.5-flash",
  "tokensUsed": 45,
  "inputTokens": 15,
  "outputTokens": 30,
  "stopReason": "STOP",
  "estimatedCost": 0.0,
  "timestamp": "2026-01-11T15:30:00"
}
```

**What to Notice:**
- `technique`: "ZERO_SHOT" - confirms which technique was used
- `fullPrompt`: Shows exactly what was sent to Gemini
- `estimatedCost`: $0.00 - Gemini is FREE!
- The response is direct and simple

#### Try These Zero-Shot Prompts:

```bash
# 1. Simple definition
curl -X POST http://localhost:8080/api/prompt/zero-shot \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Docker? Explain in 2 sentences.", "maxTokens": 100}'

# 2. Code explanation
curl -X POST http://localhost:8080/api/prompt/zero-shot \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain what @Autowired does in Spring Boot", "maxTokens": 200}'

# 3. Creative task
curl -X POST http://localhost:8080/api/prompt/zero-shot \
  -H "Content-Type: application/json" \
  -d '{"message": "Write a haiku about coding", "maxTokens": 100, "temperature": 0.9}'
```

---

### Test 2: Few-Shot Prompting ✅

#### Sentiment Classification

**Request:**
```bash
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Classify the sentiment of product reviews",
    "examples": [
      {"input": "This product is amazing! Best purchase ever!", "output": "Positive"},
      {"input": "Terrible quality. Broke after one day.", "output": "Negative"},
      {"input": "It works fine, nothing special.", "output": "Neutral"}
    ],
    "input": "Decent product but shipping was slow. Would buy again.",
    "maxTokens": 50,
    "temperature": 0.3
  }'
```

**Expected Response:**
```json
{
  "response": "Neutral",
  "technique": "FEW_SHOT",
  "fullPrompt": "Classify the sentiment of product reviews\n\nInput: \"This product is amazing! Best purchase ever!\"\nOutput: Positive\n\nInput: \"Terrible quality. Broke after one day.\"\nOutput: Negative\n\nInput: \"It works fine, nothing special.\"\nOutput: Neutral\n\nInput: \"Decent product but shipping was slow. Would buy again.\"\nOutput:",
  "model": "gemini-2.5-flash",
  "tokensUsed": 125,
  "estimatedCost": 0.0,
  "timestamp": "2026-01-11T15:35:00"
}
```

**What to Notice:**
- `fullPrompt`: Shows how examples are formatted
- The LLM learned the pattern: Input → Output label
- Low temperature (0.3) ensures consistent classification
- Response is just the label, matching the examples

#### Try These Few-Shot Prompts:

```bash
# 1. Email categorization
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Categorize emails",
    "examples": [
      {"input": "Meeting tomorrow at 3pm", "output": "Calendar"},
      {"input": "Invoice attached for last month", "output": "Finance"},
      {"input": "Bug in production system", "output": "Technical"}
    ],
    "input": "Lunch with the team next Friday?",
    "maxTokens": 50
  }'

# 2. Programming language detection
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Identify the programming language",
    "examples": [
      {"input": "print(\"Hello\")", "output": "Python"},
      {"input": "System.out.println(\"Hello\");", "output": "Java"},
      {"input": "console.log(\"Hello\");", "output": "JavaScript"}
    ],
    "input": "echo \"Hello\";",
    "maxTokens": 20
  }'

# 3. Custom categorization
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Classify urgency level",
    "examples": [
      {"input": "System is down!", "output": "CRITICAL"},
      {"input": "Feature request for next sprint", "output": "LOW"},
      {"input": "Customer complaint about slow response", "output": "MEDIUM"}
    ],
    "input": "Security vulnerability discovered",
    "maxTokens": 20
  }'
```

---

### Test 3: Chain-of-Thought Prompting ✅

#### Math Word Problem

**Request:**
```bash
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "A store has 15 apples. They sell 40% in the morning and 1/3 of the remainder in the afternoon. How many apples are left?",
    "requestSteps": true,
    "reasoningPrompt": "Let'\''s solve this step by step:",
    "maxTokens": 500,
    "temperature": 0.2
  }'
```

**Expected Response:**
```json
{
  "response": "Let's solve this step by step:\n\n1. Start with 15 apples\n\n2. Morning sales: 40% of 15 apples\n   - 40% = 0.40\n   - 15 × 0.40 = 6 apples sold\n   - Remaining: 15 - 6 = 9 apples\n\n3. Afternoon sales: 1/3 of the remainder\n   - Remainder = 9 apples\n   - 1/3 of 9 = 9 ÷ 3 = 3 apples sold\n   - Remaining: 9 - 3 = 6 apples\n\nFinal Answer: 6 apples are left.",
  "technique": "CHAIN_OF_THOUGHT",
  "fullPrompt": "A store has 15 apples. They sell 40% in the morning and 1/3 of the remainder in the afternoon. How many apples are left?\n\nLet's solve this step by step:\n",
  "model": "gemini-2.5-flash",
  "tokensUsed": 250,
  "estimatedCost": 0.0,
  "timestamp": "2026-01-11T15:40:00"
}
```

**What to Notice:**
- The LLM shows each step of the calculation
- You can verify the logic at each step
- Very low temperature (0.2) for mathematical accuracy
- The reasoning is transparent and educational

#### Try These Chain-of-Thought Prompts:

```bash
# 1. Percentage problem
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "A shirt costs $80. It'\''s on sale for 25% off. You have a coupon for an additional 10% off the sale price. What'\''s the final price?",
    "requestSteps": true,
    "maxTokens": 500
  }'

# 2. Logic puzzle
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "If all Bloops are Razzies and all Razzies are Lazzies, are all Bloops definitely Lazzies? Explain your reasoning.",
    "requestSteps": true,
    "maxTokens": 500
  }'

# 3. Time calculation
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "A meeting starts at 2:45 PM and lasts 2 hours 20 minutes. What time does it end?",
    "requestSteps": true,
    "reasoningPrompt": "Let'\''s calculate this step by step:",
    "maxTokens": 300
  }'

# 4. Database query analysis
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "A database query takes 500ms. You have 10,000 rows to process. If you process them sequentially, how long will it take? What if you batch them in groups of 100?",
    "requestSteps": true,
    "maxTokens": 600
  }'
```

---

### Test 4: Structured Output Prompting ✅

#### Extract Job Posting Data

**Request:**
```bash
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Extract key information from this job posting",
    "inputText": "Senior Java Developer at Tech Corp. 5+ years experience required. Salary: $120k-150k. Location: San Francisco, CA. Remote work available.",
    "outputFormat": "{\"position\": \"...\", \"company\": \"...\", \"experience\": \"...\", \"salary\": \"...\", \"location\": \"...\", \"remote\": true/false}",
    "formatType": "JSON",
    "maxTokens": 200,
    "temperature": 0.1
  }'
```

**Expected Response:**
```json
{
  "response": "{\"position\": \"Senior Java Developer\", \"company\": \"Tech Corp\", \"experience\": \"5+ years\", \"salary\": \"$120k-150k\", \"location\": \"San Francisco, CA\", \"remote\": true}",
  "technique": "STRUCTURED_OUTPUT",
  "fullPrompt": "Extract key information from this job posting\n\nText to process:\n\"Senior Java Developer at Tech Corp. 5+ years experience required. Salary: $120k-150k. Location: San Francisco, CA. Remote work available.\"\n\nReturn the result in JSON format:\n{\"position\": \"...\", \"company\": \"...\", \"experience\": \"...\", \"salary\": \"...\", \"location\": \"...\", \"remote\": true/false}\n\nIMPORTANT: Return ONLY the JSON - no additional text, explanations, or markdown formatting.",
  "model": "gemini-2.5-flash",
  "tokensUsed": 180,
  "estimatedCost": 0.0,
  "timestamp": "2026-01-11T15:45:00"
}
```

**What to Notice:**
- Response is ONLY JSON - no extra text
- Very low temperature (0.1) for deterministic output
- Field names match exactly what was requested
- You can parse this directly in your code!

**Parsing in Java:**
```java
String jsonResponse = response.getResponse();
ObjectMapper mapper = new ObjectMapper();
JobPosting job = mapper.readValue(jsonResponse, JobPosting.class);

System.out.println(job.getPosition());  // "Senior Java Developer"
System.out.println(job.getSalary());    // "$120k-150k"
```

#### Try These Structured Output Prompts:

```bash
# 1. Extract customer data
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Extract customer information",
    "inputText": "Contact: John Smith, john@email.com, phone 555-1234, interested in Enterprise plan, start date March 15",
    "outputFormat": "{\"name\": \"...\", \"email\": \"...\", \"phone\": \"...\", \"plan\": \"...\", \"startDate\": \"YYYY-MM-DD\"}",
    "formatType": "JSON",
    "maxTokens": 200
  }'

# 2. Parse invoice
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Extract invoice details",
    "inputText": "Invoice #INV-2024-001 dated January 10, 2026. Amount due: $1,250.00. Payment due by January 30, 2026.",
    "outputFormat": "{\"invoiceNumber\": \"...\", \"date\": \"YYYY-MM-DD\", \"amount\": 0.00, \"dueDate\": \"YYYY-MM-DD\"}",
    "formatType": "JSON",
    "maxTokens": 200
  }'

# 3. Extract meeting details
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Extract meeting information",
    "inputText": "Team standup tomorrow at 9:30 AM in Conference Room B. Attendees: Alice, Bob, Charlie. Agenda: Sprint planning",
    "outputFormat": "{\"title\": \"...\", \"time\": \"...\", \"location\": \"...\", \"attendees\": [], \"agenda\": \"...\"}",
    "formatType": "JSON",
    "maxTokens": 300
  }'

# 4. Product specification
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Extract product details",
    "inputText": "MacBook Pro 16-inch, M3 Max chip, 32GB RAM, 1TB SSD, Space Gray, $3,499",
    "outputFormat": "{\"product\": \"...\", \"specs\": {\"chip\": \"...\", \"ram\": \"...\", \"storage\": \"...\"}, \"color\": \"...\", \"price\": 0.00}",
    "formatType": "JSON",
    "maxTokens": 300
  }'
```

---

## 📊 Comparing Results: Same Task, Different Techniques

Let's solve the SAME problem using all 4 techniques to see the differences.

### The Task: Analyze a Customer Review

**Review Text**: "I ordered a laptop. Delivery took 2 weeks but the product itself is excellent. Would buy again despite the slow shipping."

---

#### Approach 1: Zero-Shot

**Prompt:**
```bash
curl -X POST http://localhost:8080/api/prompt/zero-shot \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Analyze this customer review: I ordered a laptop. Delivery took 2 weeks but the product itself is excellent. Would buy again despite the slow shipping.",
    "maxTokens": 200
  }'
```

**Response:**
```
This is a mixed review. The customer had a positive experience with the product quality (calling it "excellent") but a negative experience with delivery speed (2 weeks). Overall sentiment leans positive since they would purchase again. Suggests shipping process needs improvement.
```

**Pros**: Natural, comprehensive answer
**Cons**: Unstructured, varies each time, hard to parse

---

#### Approach 2: Few-Shot

**Prompt:**
```bash
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Classify review sentiment",
    "examples": [
      {"input": "Great product, fast shipping!", "output": "Positive"},
      {"input": "Product broke immediately, terrible.", "output": "Negative"},
      {"input": "Product is good but delivery was late.", "output": "Mixed"}
    ],
    "input": "I ordered a laptop. Delivery took 2 weeks but the product itself is excellent. Would buy again despite the slow shipping.",
    "maxTokens": 50
  }'
```

**Response:**
```
Mixed
```

**Pros**: Consistent label, matches your categories
**Cons**: Loses detail, only get the label

---

#### Approach 3: Chain-of-Thought

**Prompt:**
```bash
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "Analyze this review and determine overall sentiment: I ordered a laptop. Delivery took 2 weeks but the product itself is excellent. Would buy again despite the slow shipping.",
    "requestSteps": true,
    "reasoningPrompt": "Let'\''s analyze this step by step:",
    "maxTokens": 400
  }'
```

**Response:**
```
Let's analyze this step by step:

1. Negative aspects:
   - Delivery took 2 weeks (shipping complaint)
   - Explicitly mentions "slow shipping"

2. Positive aspects:
   - Product quality is "excellent"
   - Would purchase again (strong positive signal)
   - "despite the slow shipping" shows product quality outweighs shipping issue

3. Overall sentiment analysis:
   - The customer is ultimately satisfied
   - Product satisfaction > shipping dissatisfaction
   - Intent to repurchase is a strong positive indicator

Conclusion: Mixed review with overall POSITIVE sentiment. The customer values product quality over shipping speed.
```

**Pros**: Shows reasoning, transparent logic, educational
**Cons**: Verbose, more tokens = slower and costlier (but FREE with Gemini!)

---

#### Approach 4: Structured Output

**Prompt:**
```bash
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Analyze customer review and extract key aspects",
    "inputText": "I ordered a laptop. Delivery took 2 weeks but the product itself is excellent. Would buy again despite the slow shipping.",
    "outputFormat": "{\"productSentiment\": \"Positive/Negative/Neutral\", \"shippingSentiment\": \"Positive/Negative/Neutral\", \"overallSentiment\": \"Positive/Negative/Mixed\", \"wouldRepurchase\": true/false}",
    "formatType": "JSON",
    "maxTokens": 200
  }'
```

**Response:**
```json
{
  "productSentiment": "Positive",
  "shippingSentiment": "Negative",
  "overallSentiment": "Mixed",
  "wouldRepurchase": true
}
```

**Pros**: Structured, parseable, specific fields, consistent
**Cons**: Requires defining structure upfront

---

### Which Technique Should You Use?

For this review analysis task:

- **Zero-Shot**: Good for human-readable reports
- **Few-Shot**: Best for consistent categorization
- **Chain-of-Thought**: Best for understanding the reasoning
- **Structured Output**: Best for dashboard/database integration

**The answer: It depends on your use case!**

---

## 💡 Best Practices and Common Pitfalls

### Best Practices

#### 1. Be Specific
```
❌ Bad: "Tell me about Java"
✅ Good: "Explain Java's Stream API with 2 code examples"
```

#### 2. Use Low Temperature for Consistency
```java
// For factual, consistent outputs
temperature = 0.1 - 0.3

// For creative, varied outputs
temperature = 0.7 - 0.9
```

#### 3. Provide Context
```
❌ Bad: "Fix this code"
✅ Good: "This Java Spring Boot code throws NullPointerException. Identify the issue and suggest a fix: [code]"
```

#### 4. Iterate and Refine
```
First attempt → Test → Adjust prompt → Test again → Success!
```

Prompt engineering is a SKILL. You get better with practice!

#### 5. Show, Don't Tell (Few-Shot)
```
❌ Bad: "Use formal tone"
✅ Good: Show 3 examples of formal responses
```

#### 6. Request Step-by-Step for Complex Tasks
```
Add: "Let's think through this step by step:"
```

#### 7. Be Explicit with Format
```
❌ Bad: "Return as JSON"
✅ Good: "Return ONLY this JSON with no additional text: {\"name\": \"...\", \"age\": ...}"
```

---

### Common Pitfalls

#### Pitfall 1: Vague Prompts
```
❌ "Tell me about Spring"
```
**Problem**: Too broad, will get generic response

**Fix**:
```
✅ "Explain Spring Boot's @Autowired annotation with a code example"
```

---

#### Pitfall 2: Too Few Examples (Few-Shot)
```
❌ Only 1 example
```
**Problem**: LLM can't identify the pattern with just one example

**Fix**:
```
✅ Provide 3-5 examples showing the pattern clearly
```

---

#### Pitfall 3: Inconsistent Examples
```
❌ Example 1: "Input: X\nOutput: Y"
    Example 2: "X -> Y"
    Example 3: "When given X, respond with Y"
```
**Problem**: Different formats confuse the LLM

**Fix**:
```
✅ Use EXACTLY the same format for all examples
```

---

#### Pitfall 4: High Temperature for Structured Output
```
❌ temperature = 0.9 for JSON extraction
```
**Problem**: High randomness creates invalid JSON

**Fix**:
```
✅ temperature = 0.1 - 0.2 for deterministic structured output
```

---

#### Pitfall 5: Not Requesting Steps for Complex Problems
```
❌ "What's 15% of 250 plus 30% of 180?"
```
**Problem**: LLM might rush and make arithmetic errors

**Fix**:
```
✅ "Solve step by step: What's 15% of 250 plus 30% of 180?"
```

---

#### Pitfall 6: Expecting Perfect JSON Without Instructions
```
❌ "Return as JSON"
```
**Problem**: LLM adds explanations: "Here's the JSON: {...} I extracted..."

**Fix**:
```
✅ "Return ONLY the JSON - no additional text, explanations, or markdown formatting."
```

---

#### Pitfall 7: Not Validating Structured Output
```java
❌ String json = response.getResponse();
   // Use directly without validation
```
**Problem**: LLM might return invalid JSON occasionally

**Fix**:
```java
✅ try {
     ObjectMapper mapper = new ObjectMapper();
     MyData data = mapper.readValue(json, MyData.class);
   } catch (JsonProcessingException e) {
     // Handle invalid JSON, maybe retry with lower temperature
   }
```

---

## 🎓 Key Learnings from Phase 2

### Conceptual Learnings

1. **Prompt Engineering is a Skill**
   - It's not magic - it's about clear communication
   - You get better with practice and experimentation
   - Small changes in prompts can have big impacts on results

2. **Different Techniques for Different Tasks**
   - Zero-shot: Simple, direct questions
   - Few-shot: Pattern learning with examples
   - Chain-of-thought: Complex reasoning problems
   - Structured output: Integration and data extraction

3. **Temperature Matters**
   - Low (0.1-0.3): Consistent, factual, deterministic
   - Medium (0.4-0.7): Balanced
   - High (0.8-1.0): Creative, varied, random

4. **Context and Examples Guide Output**
   - More context = better results
   - Good examples teach the LLM exactly what you want
   - Consistent formatting in examples = consistent output

5. **Structured Output Requires Strictness**
   - Explicitly request format
   - Use very low temperature
   - Add system prompts to enforce structure
   - Always validate the output

### Technical Learnings

1. **Prompt Construction**
   - How to build effective zero-shot prompts
   - How to format few-shot examples
   - How to request step-by-step reasoning
   - How to specify structured output formats

2. **API Integration**
   - Each technique uses the same ChatService
   - Different prompts produce different results
   - PromptResponse includes the full prompt for debugging
   - You can see exactly what was sent to Gemini

3. **Token Management**
   - Zero-shot uses fewest tokens (cheapest) - FREE with Gemini!
   - Few-shot adds moderate token overhead
   - Chain-of-thought uses most tokens (reasoning is verbose)
   - Structured output is token-efficient if done right

4. **Real-World Applications**
   - Customer support automation (few-shot)
   - Data extraction pipelines (structured output)
   - Educational tools (chain-of-thought)
   - Translation services (zero-shot)

### Spring Boot Learnings

1. **Service Layer Pattern**
   - `PromptEngineeringService` orchestrates prompt construction
   - Uses `ChatService` to actually call the LLM
   - Separates prompt engineering logic from API communication

2. **DTO Design**
   - Different request DTOs for different techniques
   - `PromptResponse` includes technique type and full prompt
   - Validation ensures required fields are present

3. **Utility Classes**
   - `PromptTemplate` class encapsulates prompt construction logic
   - Reusable methods for building prompts
   - Easy to test and modify

---

## ✅ Phase 2 Checklist

Before moving to Phase 3, make sure you can:

- [ ] Explain what prompt engineering is and why it matters
- [ ] Identify when to use zero-shot vs few-shot vs chain-of-thought vs structured output
- [ ] Write effective zero-shot prompts for simple tasks
- [ ] Create few-shot examples that establish clear patterns
- [ ] Request step-by-step reasoning for complex problems
- [ ] Specify structured output formats (JSON) with proper constraints
- [ ] Test all 4 endpoints via curl or Postman
- [ ] Understand how temperature affects output
- [ ] Parse and validate structured JSON responses
- [ ] Recognize common pitfalls and how to avoid them

**Practical Exercise**: Try creating your own prompt for each technique:
1. Zero-shot: Ask a simple factual question
2. Few-shot: Classify something with 3 examples
3. Chain-of-thought: Solve a math word problem
4. Structured output: Extract data to JSON

---

## 🎯 Manager Demo - Phase 2

### What to Show:

#### 1. Quick Overview (2 minutes)
   - Explain: "Prompt engineering is how we communicate effectively with LLMs"
   - Show the 4 techniques and when to use each
   - Emphasize: "Same LLM, different prompts = vastly different results"

#### 2. Live Demo (7 minutes)

**Demo Flow:**

**Part A: Zero-Shot (1 min)**
```bash
curl -X POST http://localhost:8080/api/prompt/zero-shot \
  -H "Content-Type: application/json" \
  -d '{"message": "What is microservices architecture?"}'
```
**Say**: "Simple questions work great with zero-shot. The LLM just answers directly."

---

**Part B: Few-Shot (2 min)**
```bash
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Classify support ticket urgency",
    "examples": [
      {"input": "System is completely down", "output": "CRITICAL"},
      {"input": "Feature request for next quarter", "output": "LOW"},
      {"input": "Customer unable to login", "output": "HIGH"}
    ],
    "input": "Security vulnerability reported by customer"
  }'
```
**Say**: "Few-shot lets us teach custom classifications. Here we classify support tickets into our urgency levels - CRITICAL, HIGH, or LOW. The LLM learns from our examples."

Show the `fullPrompt` field: "See how the examples are structured? This is what teaches the LLM."

---

**Part C: Chain-of-Thought (2 min)**
```bash
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "Our API can handle 1000 requests per second. If each request takes 50ms to process, how many concurrent workers do we need?",
    "requestSteps": true
  }'
```
**Say**: "For complex calculations or reasoning, chain-of-thought shows the step-by-step logic. This is crucial for verifying correctness."

**Point out**: "Notice it breaks down the problem. This helps us catch errors and understand the reasoning."

---

**Part D: Structured Output (2 min)**
```bash
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Extract information from customer message",
    "inputText": "Hi, this is Sarah from Acme Corp. Email: sarah@acme.com. We need 50 licenses starting Feb 1st. Budget is $10,000.",
    "outputFormat": "{\"name\": \"...\", \"company\": \"...\", \"email\": \"...\", \"quantity\": 0, \"startDate\": \"...\", \"budget\": 0}",
    "formatType": "JSON"
  }'
```
**Say**: "Structured output is game-changing for automation. We get JSON we can directly insert into our database or CRM."

**Show the response**: "See? Clean JSON, no extra text, ready to parse in our code."

---

#### 3. Real-World Value (2 minutes)

**Use Cases You Can Implement:**
- **Customer Support**: Auto-classify tickets with few-shot
- **Data Extraction**: Parse emails/documents to JSON with structured output
- **Financial Analysis**: Solve complex calculations with chain-of-thought
- **Content Generation**: Generate marketing copy with zero-shot

**Cost Discussion:**
- "All of this is running on Gemini's FREE tier!"
- "1,500 requests per day at no cost"
- "For production scale, we can switch to Claude (paid) with one config change"

---

#### 4. Key Takeaways (1 minute)
- ✅ Implemented 4 industry-standard prompt engineering techniques
- ✅ Each technique solves different real-world problems
- ✅ All FREE with Gemini for development and learning
- ✅ Multi-provider architecture allows easy scaling
- ✅ Foundation ready for Phase 3: Advanced patterns and RAG

---

### Talking Points:

**Opening:**
"Prompt engineering is the most important skill when working with LLMs. The same AI can give terrible or excellent results - it all depends on how you ask."

**During Demo:**
- "Notice how we can see the fullPrompt - this transparency helps us debug and improve"
- "The temperature setting controls consistency - low for factual, high for creative"
- "Each technique has its sweet spot - we're learning when to use which"

**Closing:**
"These techniques are industry-standard. Companies like OpenAI, Anthropic, and Google all recommend these patterns. We now have a production-ready implementation that's FREE to develop with and ready to scale."

---

## 🚀 Next: Phase 3

### What's Coming in Phase 3:

**Phase 3: Advanced Patterns & RAG (Retrieval Augmented Generation)**
- Conversation history and multi-turn dialog
- Context injection and RAG basics
- Function calling (LLM triggers actions in your code)
- Streaming responses for real-time UI updates
- Error recovery and retry strategies

**Get Ready:**
Phase 3 will build on your prompt engineering skills to create truly interactive, context-aware AI applications!

---

## 📚 Further Reading

### Recommended Resources:

1. **OpenAI Prompt Engineering Guide**
   - https://platform.openai.com/docs/guides/prompt-engineering

2. **Google's Prompt Engineering Best Practices**
   - https://ai.google.dev/docs/prompt_best_practices

3. **Anthropic's Prompting Guide**
   - https://docs.anthropic.com/claude/docs/prompt-engineering

4. **Learn Prompting (Free Course)**
   - https://learnprompting.org/

5. **Prompt Engineering Papers**
   - "Chain-of-Thought Prompting Elicits Reasoning in Large Language Models" (Wei et al., 2022)
   - "Large Language Models are Zero-Shot Reasoners" (Kojima et al., 2022)

---

## 🎉 Congratulations!

You've completed Phase 2! You now understand:

✅ The fundamentals of prompt engineering
✅ Four essential prompting techniques
✅ When to use each technique
✅ How to construct effective prompts
✅ How to handle structured outputs
✅ Real-world applications of each technique

**Remember**: Prompt engineering is a SKILL. The more you practice, the better you get!

---

**Take a break, experiment with different prompts, and prepare for Phase 3!** 🚀

---

## Appendix: Quick Reference

### Quick Command Reference

```bash
# Health check
curl http://localhost:8080/api/prompt/health

# Zero-shot
curl -X POST http://localhost:8080/api/prompt/zero-shot \
  -H "Content-Type: application/json" \
  -d '{"message": "Your question here", "maxTokens": 200}'

# Few-shot
curl -X POST http://localhost:8080/api/prompt/few-shot \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Task description",
    "examples": [{"input": "...", "output": "..."}],
    "input": "Text to classify"
  }'

# Chain-of-thought
curl -X POST http://localhost:8080/api/prompt/chain-of-thought \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "Problem to solve",
    "requestSteps": true,
    "maxTokens": 500
  }'

# Structured output
curl -X POST http://localhost:8080/api/prompt/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "task": "What to extract",
    "inputText": "Text to process",
    "outputFormat": "{\"field\": \"...\"}",
    "formatType": "JSON"
  }'
```

### Temperature Quick Guide

| Temperature | Use For | Example |
|-------------|---------|---------|
| 0.0 - 0.2 | Structured output, math | JSON extraction, calculations |
| 0.2 - 0.4 | Chain-of-thought, few-shot | Reasoning, classifications |
| 0.5 - 0.7 | Zero-shot, general Q&A | Documentation, explanations |
| 0.8 - 1.0 | Creative tasks | Story writing, brainstorming |

### Technique Selection Flowchart

```
Is it a simple, direct question?
├─ YES → Zero-Shot
└─ NO → Need consistent formatting?
    ├─ YES → Few-Shot
    └─ NO → Need reasoning steps?
        ├─ YES → Chain-of-Thought
        └─ NO → Need structured data?
            ├─ YES → Structured Output
            └─ NO → Start with Zero-Shot, iterate
```

---

**End of Phase 2 Guide** | **Version 1.0** | **Last Updated: January 11, 2026**
