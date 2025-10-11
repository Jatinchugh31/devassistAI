# Chat Memory Debug Test

## Issue: Context not being passed to OpenAI

The problem is likely in how the MessageChatMemoryAdvisor retrieves and includes the history.

## Key Points to Check:

### 1. **Advisor Configuration**
```java
// In AiConfig.java
.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
```
This is correct - the advisor is set as default.

### 2. **Controller Usage**
```java
// In ChatController.java line 60
.advisors(adv -> adv.param(ChatMemory.CONVERSATION_ID, finalSessionId))
```

**⚠️ POTENTIAL ISSUE**: You're using `.advisors()` which might be OVERRIDING the default advisors instead of adding to them!

## Solution:

Change from:
```java
.advisors(adv -> adv.param(ChatMemory.CONVERSATION_ID, finalSessionId))
```

To:
```java
.advisors(a -> a.param(MessageChatMemoryAdvisor.CONVERSATION_ID_KEY, finalSessionId))
```

OR better yet, keep the default advisors:
```java
String aiResponse = chatClient.prompt()
    .system(systemInstruction)
    .user(request.getTask())
    .call()
    .advisors(a -> a.param(MessageChatMemoryAdvisor.CONVERSATION_ID_KEY, finalSessionId))
    .content();
```

## Test Commands:

### 1. Check what's stored in Redis:
```bash
redis-cli KEYS "devassist:conversation:*"
redis-cli LRANGE "devassist:conversation:YOUR_SESSION_ID" 0 -1
```

### 2. Test the debug endpoint:
```bash
curl http://localhost:9090/ai/debug/memory/YOUR_SESSION_ID
```

### 3. Check logs for advisor activity:
Look for:
- "MessageChatMemoryAdvisor" in logs
- Messages being added to memory
- Messages being retrieved from memory

## Debugging Steps:

1. **Add more logging** to `RedisChatMemory.java`:
```java
@Override
public List<Message> get(String conversationId) {
    log.info("RETRIEVING messages for conversationId={}", conversationId);
    List<String> raw = repo.readAll(conversationId);
    log.info("Found {} raw messages in Redis", raw.size());
    // ... rest of method
    log.info("Returning {} Message objects", out.size());
    return out;
}
```

2. **Add logging to see advisor behavior**:
Enable DEBUG logging for Spring AI:
```properties
logging.level.org.springframework.ai.chat.client.advisor=DEBUG
logging.level.org.springframework.ai.chat.memory=DEBUG
```

3. **Check if MessageChatMemoryAdvisor is actually being called**:
The advisor should:
   - Call `chatMemory.get(conversationId)` BEFORE sending to OpenAI
   - Call `chatMemory.add(conversationId, messages)` AFTER getting response

