# ✅ Verify Chat Memory is Working

## Changes Made:

### 1. **Fixed Controller** - Line 65
**Before:**
```java
.advisors(adv -> adv.param(ChatMemory.CONVERSATION_ID,finalSessionId))
```

**After:**
```java
.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
```
**Key Change:** Moved `.advisors()` to AFTER `.user()` to ensure the system and user messages are set before the advisor processes them.

### 2. **Added Debug Logging**
- `RedisChatMemory.add()` now logs when messages are stored
- `RedisChatMemory.get()` now logs when messages are retrieved
- `ChatController` logs request start and completion

## Test Steps:

### Step 1: Start Application
```bash
# Make sure Redis is running
redis-cli ping  # Should return PONG

# Start app
export OPENAI_API_KEY="your-key-here"
./gradlew bootRun
```

### Step 2: First Message (Creates Conversation)
```bash
curl -X POST "http://localhost:9090/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "JAVA",
    "task": "What is Spring Boot?",
    "sessionId": "test-session-123"
  }'
```

**Expected Output:**
- AI response about Spring Boot
- Header: `X-Conversation-Id: test-session-123`

**Expected Logs:**
```
📖 RETRIEVING messages for conversationId=test-session-123
   Found 0 raw JSON entries in Redis
   Returning 0 Message objects to advisor
💾 ADDING 3 messages to Redis for conversationId=test-session-123
   (system message, user message, assistant message)
```

### Step 3: Check Redis
```bash
# See what's stored
curl "http://localhost:9090/ai/debug/memory/test-session-123"

# Or use Redis CLI
redis-cli LRANGE "devassist:conversation:test-session-123" 0 -1
```

**Expected:** Should see 3 JSON messages (system, user, assistant)

### Step 4: Second Message (Uses Context)
```bash
curl -X POST "http://localhost:9090/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "JAVA",
    "task": "Can you give me a simple example?",
    "sessionId": "test-session-123"
  }'
```

**Expected Output:**
- AI provides a Spring Boot example (it should remember we're talking about Spring Boot!)

**Expected Logs:**
```
📖 RETRIEVING messages for conversationId=test-session-123
   Found 3 raw JSON entries in Redis
   Returning 3 Message objects to advisor
💾 ADDING 2 messages to Redis for conversationId=test-session-123
   (new user message, new assistant message)
```

### Step 5: Third Message (Verify Context)
```bash
curl -X POST "http://localhost:9090/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "JAVA",
    "task": "What did I just ask about?",
    "sessionId": "test-session-123"
  }'
```

**Expected:** AI should say "You asked about Spring Boot and requested a simple example"

## Debugging If Not Working:

### Check 1: Verify Advisor is Retrieving History
Look for this in logs:
```
📖 RETRIEVING messages for conversationId=test-session-123
   Found N raw JSON entries in Redis
   Returning N Message objects to advisor
```

If you see **0 messages** on the second call, the messages aren't being stored.

### Check 2: Verify Messages Are Being Stored
Look for this in logs:
```
💾 ADDING N messages to Redis for conversationId=test-session-123
```

If you don't see this, the advisor isn't calling `chatMemory.add()`.

### Check 3: Enable More Detailed Logging
Add to `application.properties`:
```properties
logging.level.org.springframework.ai.chat.client.advisor=TRACE
logging.level.com.devassist=DEBUG
```

### Check 4: Inspect Raw Redis Data
```bash
redis-cli
> KEYS "devassist:conversation:*"
> LRANGE "devassist:conversation:test-session-123" 0 -1
> LLEN "devassist:conversation:test-session-123"
```

## What Should Happen:

1. **First Request:**
   - Advisor retrieves 0 messages (new conversation)
   - System message added by you
   - User message sent
   - OpenAI responds
   - Advisor stores: [system, user, assistant] = 3 messages

2. **Second Request:**
   - Advisor retrieves 3 messages from Redis
   - These 3 are included in the prompt to OpenAI
   - New user message added
   - OpenAI responds (with context!)
   - Advisor stores: [user, assistant] = 2 new messages
   - Total in Redis: 5 messages

3. **Third Request:**
   - Advisor retrieves 5 messages
   - All 5 sent to OpenAI for context
   - And so on...

## Success Criteria:

✅ On second/third requests, you see "Found N raw JSON entries" where N > 0  
✅ AI responses show awareness of previous conversation  
✅ Redis contains growing list of messages  
✅ Debug endpoint shows message history  

## If Still Not Working:

The issue might be in how MessageChatMemoryAdvisor is configured. Let me know what you see in the logs!

