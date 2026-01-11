# Google Gemini API Setup Guide

## ✅ Why Switch to Gemini?

**Google Gemini** is a perfect replacement for Claude because:
- ✅ **Completely FREE** for learning (generous free tier)
- ✅ **No credit card required** for free tier
- ✅ **High quality** responses (Gemini 1.5 Flash)
- ✅ **Fast** inference
- ✅ **Easy to integrate**
- ✅ **60 requests/minute** - perfect for development!

---

## 🚀 Quick Setup (2 Minutes)

### Step 1: Update Your `.env` File

Open `.env` file in your project root and update it:

```properties
# LLM Provider Selection
LLM_PROVIDER=GEMINI

# Google Gemini API Configuration (FREE!)
GEMINI_API_KEY=your-gemini-api-key-here
GEMINI_MODEL=gemini-2.5-flash
GEMINI_MAX_TOKENS=2048

# Anthropic Claude API Configuration (optional - can be empty)
ANTHROPIC_API_KEY=
ANTHROPIC_MODEL=claude-3-5-sonnet-20241022
ANTHROPIC_MAX_TOKENS=4096
```

### Step 2: Restart Your Application

In IntelliJ:
1. Stop the running application (red ⏹️ button)
2. Start it again (green ▶️ button)

Or via command line:
```bash
mvn spring-boot:run
```

### Step 3: Test It!

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Explain what a Large Language Model is in 50 words\"}"
```

**Expected Response:**
```json
{
  "response": "A Large Language Model is an AI system...",
  "model": "gemini-1.5-flash",
  "tokensUsed": 120,
  "inputTokens": 20,
  "outputTokens": 100,
  "timestamp": "2026-01-11T...",
  "stopReason": "STOP"
}
```

---

## 🎯 What Changed?

### New Files:
- `src/main/java/com/ai/mvp/config/GeminiConfig.java` - Gemini configuration
- `src/main/java/com/ai/mvp/config/LlmProvider.java` - Provider enum
- `src/main/java/com/ai/mvp/service/GeminiApiService.java` - Gemini API client
- `src/main/java/com/ai/mvp/dto/gemini/GeminiApiRequest.java` - Request DTO
- `src/main/java/com/ai/mvp/dto/gemini/GeminiApiResponse.java` - Response DTO

### Modified Files:
- `src/main/java/com/ai/mvp/service/ChatService.java` - Now supports multiple providers
- `src/main/resources/application.yml` - Added Gemini config section
- `.env.template` - Added Gemini API key template

### Architecture:
```
ChatController
     ↓
ChatService (decides which provider to use)
     ↓
   ┌─────────────┬──────────────┐
   ↓             ↓              ↓
ClaudeApiService GeminiApiService OllamaApiService (future)
   ↓             ↓
Claude API    Gemini API
```

---

## 🔄 Switching Between Providers

You can easily switch between Claude and Gemini by changing the `.env` file:

### Use Gemini (FREE):
```properties
LLM_PROVIDER=GEMINI
GEMINI_API_KEY=your-gemini-api-key-here
```

### Use Claude (if you have credits):
```properties
LLM_PROVIDER=CLAUDE
ANTHROPIC_API_KEY=sk-ant-api03-...
```

**Then restart the application!**

---

## 📊 Gemini Free Tier Limits

**Free Tier (Perfect for Learning)**:
- ✅ 60 requests per minute
- ✅ 1,500 requests per day
- ✅ No credit card required
- ✅ No expiration

**For Your 1-Week Project**:
- Expected usage: ~500-1000 requests total
- **Cost: $0.00** (completely free!)

---

## 🆚 Gemini vs Claude Comparison

| Feature | Gemini 1.5 Flash | Claude 3.5 Sonnet |
|---------|------------------|-------------------|
| **Price** | FREE (60 rpm) | $3-15 per million tokens |
| **Speed** | Very Fast | Fast |
| **Quality** | Excellent | Excellent |
| **Context Window** | 1M tokens | 200K tokens |
| **Best For** | Learning, Development | Production, Complex Tasks |
| **API Key** | Free, no card needed | Paid account required |

**Verdict for Your MVP**: ✅ Gemini is perfect!

---

## 🧪 Testing With Gemini

All the same tests from Phase 1 work with Gemini!

### Test 1: Health Check
```bash
curl http://localhost:8080/api/chat/health
```

### Test 2: Simple Chat
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"What is a vector database?\"}"
```

### Test 3: With System Prompt
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Write a haiku about programming\", \"systemPrompt\": \"You are a creative poet\"}"
```

### Test 4: Temperature Control
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Tell me a creative story\", \"temperature\": 1.0}"
```

---

## ❓ Troubleshooting

### Error: "API key invalid"

**Problem**: Gemini API key is wrong or not set

**Solution**:
1. Verify API key in `.env` file
2. Get a new key: https://aistudio.google.com/app/apikey
3. Restart the application

---

### Error: "Resource has been exhausted"

**Problem**: Rate limit exceeded (60 requests/minute)

**Solution**:
- Wait 1 minute
- Or upgrade to paid tier (still very cheap)

---

### Error: "Provider GEMINI not found"

**Problem**: Application didn't restart properly

**Solution**:
1. Stop the application completely
2. Run: `mvn clean package`
3. Start again: `mvn spring-boot:run`

---

### Application still using Claude

**Problem**: `.env` file not updated or not loaded

**Solution**:
1. Check `.env` file has `LLM_PROVIDER=GEMINI`
2. Verify no spaces around the `=` sign
3. Completely restart IntelliJ and the application

---

## 📖 Additional Resources

- **Gemini API Docs**: https://ai.google.dev/docs
- **Get API Key**: https://aistudio.google.com/app/apikey
- **Pricing**: https://ai.google.dev/pricing
- **Models**: https://ai.google.dev/models/gemini

---

## 🎉 Benefits of This Setup

1. ✅ **Multi-Provider Support**: Easy to switch between LLMs
2. ✅ **Cost-Effective**: Use free tier for learning
3. ✅ **Production-Ready**: Can switch to paid Claude for production
4. ✅ **Same Interface**: Your code doesn't change, just the provider
5. ✅ **Learning Value**: Understand how different LLMs work

---

## 💡 Pro Tips

1. **Use Gemini for learning** - It's free and fast
2. **Use Claude for production** - When quality is critical and you have budget
3. **Compare responses** - Try the same prompt on both providers
4. **Track costs** - Even free tier has limits
5. **Read logs** - Logs show which provider is being used

---

## ✅ You're All Set!

Your application now supports **Google Gemini API** and it's **completely free**!

**Next Steps**:
1. ✅ Update your `.env` file with Gemini API key
2. ✅ Restart the application
3. ✅ Test with Swagger UI or curl
4. ✅ Continue with Phase 1 testing
5. ✅ Move to Phase 2 when ready!

**Happy coding! 🚀**
