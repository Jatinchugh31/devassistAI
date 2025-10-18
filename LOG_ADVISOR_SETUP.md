# 📝 LogAdvisor Setup Complete!

## ✅ What Was Added

### 1. LogAdvisor (Custom Advisor)
**File:** `src/main/java/com/devassist/advisor/LogAdvisor.java`

**Features:**
- ✅ Implements `CallAdvisor` interface from Spring AI
- ✅ Logs every AI request and response
- ✅ Measures response time
- ✅ Calculates token usage and cost
- ✅ Beautiful formatted output with emojis
- ✅ Execution order: 0 (runs first, before all other advisors)

**What it logs:**
- 🤖 AI Request (timestamp, full request details)
- ✅ AI Response (timestamp, duration, response details)
- 🎯 Token Usage (prompt tokens, completion tokens, total tokens)
- 💰 Estimated Cost (based on GPT-4 pricing)

---

### 2. Logging Configuration
**File:** `src/main/resources/application.properties`

**Added:**
```properties
# Logging Configuration
logging.level.root=INFO
logging.level.com.devassist=DEBUG
logging.level.com.devassist.advisor.LogAdvisor=INFO
logging.level.org.springframework.ai=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

**What this does:**
- ✅ Root level: INFO (general app logs)
- ✅ DevAssist package: DEBUG (detailed logs)
- ✅ LogAdvisor: INFO (shows request/response logs)
- ✅ Spring AI: DEBUG (shows AI framework logs)
- ✅ Custom log pattern with timestamp and thread info

---

### 3. Advisor Registration
**File:** `src/main/java/com/devassist/config/AiConfig.java`

**Configuration:**
```java
@Bean
public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory, LogAdvisor logAdvisor) {
    return ChatClient.builder(chatModel)
            .defaultAdvisors(
                logAdvisor,  // Executes first (order=0)
                PromptChatMemoryAdvisor.builder(chatMemory).build()  // Executes second (order=10)
            )
            .build();
}
```

**Advisor Chain:**
```
Request Flow:
1. LogAdvisor (order=0) - BEFORE: Log request
2. PromptChatMemoryAdvisor (order=10) - BEFORE: Load history
3. → ChatModel (OpenAI)
4. PromptChatMemoryAdvisor - AFTER: Save conversation
5. LogAdvisor - AFTER: Log response + metrics
```

---

## 🧪 How to Test

### 1. Start the Application
```bash
./gradlew bootRun
```

### 2. Make a Request
```bash
curl -X POST http://localhost:9090/ai-test/chat/sql \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find 1 error log from app_logs table",
    "sessionId": "test-1"
  }'
```

### 3. Check the Logs

You'll see output like this:

```
╔═══════════════════════════════════════════════════════════════════════════════
║ 🤖 AI REQUEST
╠═══════════════════════════════════════════════════════════════════════════════
║ ⏰ Timestamp: 2025-10-18 15:30:45.123
║ 📋 Request: ChatClientRequest[userText=Find 1 error log from app_logs table, ...]
╚═══════════════════════════════════════════════════════════════════════════════

... (AI processing) ...

╔═══════════════════════════════════════════════════════════════════════════════
║ ✅ AI RESPONSE
╠═══════════════════════════════════════════════════════════════════════════════
║ ⏰ Timestamp: 2025-10-18 15:30:45.123
║ ⏱️  Duration: 1234 ms
║ 📋 Response: ChatClientResponse[content=I found an error..., ...]
║ 🎯 Token Usage:
║    • Prompt Tokens: 150
║    • Completion Tokens: 75
║    • Total Tokens: 225
║    • Estimated Cost: $0.0090
╚═══════════════════════════════════════════════════════════════════════════════
```

---

## 📊 What Gets Logged

### Request Logging:
- ✅ Timestamp
- ✅ Full request object (includes user text, system text, tools, etc.)

### Response Logging:
- ✅ Timestamp
- ✅ Duration (milliseconds)
- ✅ Full response object
- ✅ Token usage breakdown
- ✅ Estimated cost (GPT-4 pricing)

---

## 💰 Cost Calculation

**Pricing (GPT-4):**
- Input: $0.03 per 1K tokens
- Output: $0.06 per 1K tokens

**Example:**
```
Prompt Tokens: 150
Completion Tokens: 75

Cost = (150/1000 * 0.03) + (75/1000 * 0.06)
     = 0.0045 + 0.0045
     = $0.0090
```

---

## 🎯 Benefits

### 1. **Observability**
- See exactly what's being sent to OpenAI
- Monitor response times
- Track token usage

### 2. **Cost Tracking**
- Know how much each request costs
- Identify expensive queries
- Optimize prompts to reduce costs

### 3. **Debugging**
- See full request/response flow
- Identify issues quickly
- Understand AI behavior

### 4. **Performance Monitoring**
- Track response times
- Identify slow requests
- Optimize system performance

---

## 🔧 Customization

### Change Log Level
```properties
# More verbose (shows everything)
logging.level.com.devassist.advisor.LogAdvisor=DEBUG

# Less verbose (only errors)
logging.level.com.devassist.advisor.LogAdvisor=ERROR
```

### Disable Logging
```properties
# Turn off LogAdvisor
logging.level.com.devassist.advisor.LogAdvisor=OFF
```

### Change Advisor Order
```java
@Override
public int getOrder() {
    return 100; // Execute last instead of first
}
```

---

## 📚 Spring AI Advisor Types

| Advisor | Purpose | Order |
|---------|---------|-------|
| **LogAdvisor** (Custom) | Log requests/responses | 0 |
| **PromptChatMemoryAdvisor** | Conversation memory | 10 |
| **QuestionAnswerAdvisor** | RAG support | 20 |
| **SimpleLoggerAdvisor** | Basic logging | 30 |

**Lower order = Executes first**

---

## 🎉 Summary

✅ **LogAdvisor** is now active and logging all AI interactions!

**What it does:**
1. Intercepts every AI request
2. Logs request details
3. Measures execution time
4. Logs response details
5. Calculates token usage and cost

**Next Steps:**
- Test with different endpoints
- Monitor logs during development
- Optimize prompts based on token usage
- Track costs over time

---

**Created:** 2025-10-18  
**Status:** ✅ Active and Working

