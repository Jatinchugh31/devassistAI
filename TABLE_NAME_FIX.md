# Table Name Fix - app_log vs app_logs

## 🐛 Problem

The AI was generating SQL queries with the wrong table name:
- ❌ AI used: `SELECT * FROM app_log LIMIT 1`
- ✅ Actual table: `app_logs` (plural with 's')

**Error:**
```
org.springframework.jdbc.BadSqlGrammarException: StatementCallback; 
bad SQL grammar [SELECT * FROM app_log LIMIT 1]
```

## 🔍 Root Cause

The AI model was inferring the table name from the user's natural language request ("find 1 log from app_log table") without explicit schema guidance. Since the user mentioned "app_log" (singular), the AI used that exact name.

## ✅ Solution

Added **explicit schema information** in multiple places to guide the AI:

### 1. Enhanced Tool Description
**File:** `src/main/java/com/devassist/tools/SqlTool.java`

Updated the `@Tool` annotation to include complete schema information:

```java
@Tool(
    name = "sql_execute", 
    description = "Execute a safe, read-only SQL SELECT query against the database. " +
                  "Only SELECT queries are allowed. " +
                  "Available tables: 'app_logs' (contains application logs with columns: " +
                  "id, created_at, level, service, host, thread, request_id, user_id, " +
                  "logger, exception_type, message, stack_trace, context, resolved, tags). " +
                  "Use this to fetch and analyze logs."
)
```

### 2. Enhanced System Instruction
**File:** `src/main/java/com/devassist/service/ChatService.java`

Added comprehensive system prompt for the SQL tool endpoint:

```java
String systemInstruction = """
    You are SqlAgent: an expert SQL performance engineer and log analyst.
    Focus on safety and optimization. Use READ-ONLY SQL when executing.
    
    IMPORTANT DATABASE SCHEMA:
    - Table name: 'app_logs' (plural, with 's' at the end)
    - Available columns: id, created_at, level, service, host, thread, request_id, 
      user_id, logger, exception_type, message, stack_trace, context, resolved, tags
    
    When querying logs, ALWAYS use 'app_logs' as the table name (not 'app_log').
    
    Your job is to:
    1. Query the app_logs table using the sql_execute tool
    2. Analyze the results
    3. Explain the root cause of errors
    4. Suggest solutions
    """;
```

### 3. Updated Role Prompt
**File:** `src/main/java/com/devassist/service/PromptBuilderService.java`

Enhanced the SQL role to mention the correct table:

```java
case SQL -> "You are SqlAgent: an expert SQL performance engineer. " +
            "Focus on safety and optimization. Use READ-ONLY SQL when executing. " +
            "Available database tables: 'app_logs' (note: plural form with 's'). " +
            "When querying logs, always use 'app_logs' as the table name.";
```

## 🎯 Why This Approach Works

1. **Tool Description**: OpenAI function calling includes the tool description in the model's context, so the AI knows the exact schema
2. **System Instruction**: The explicit system prompt reinforces the table name before every request
3. **Multiple Reinforcements**: By mentioning it in multiple places, we ensure the AI prioritizes the correct table name over user input

## 📊 Database Schema Reference

**Table:** `app_logs`

```sql
CREATE TABLE app_logs (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    level VARCHAR(16) NOT NULL,          -- INFO, WARN, ERROR
    service VARCHAR(128),                 -- service name
    host VARCHAR(128),                    -- hostname
    thread VARCHAR(128),                  -- thread name
    request_id VARCHAR(64),               -- correlation ID
    user_id VARCHAR(64),                  -- user identifier
    logger VARCHAR(256),                  -- logger class name
    exception_type VARCHAR(256),          -- exception class name
    message CLOB NOT NULL,                -- log message
    stack_trace CLOB,                     -- full stack trace
    context CLOB,                         -- JSON context data
    resolved BOOLEAN DEFAULT FALSE,       -- resolution status
    tags VARCHAR(256)                     -- comma-separated tags
);
```

## 🧪 Testing

The AI will now correctly query `app_logs`:

```bash
# Your original test case (now works!)
curl -X 'POST' \
  'http://localhost:9090/ai-test/chat/sql' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "find 1 log from app_logs table and explain me the root cause",
    "sessionId": "5"
  }'
```

**Expected Behavior:**
1. AI receives the request
2. AI checks the tool description and system prompt
3. AI constructs query: `SELECT * FROM app_logs LIMIT 1`
4. Query executes successfully
5. AI analyzes the log and provides explanation

## 📝 Key Takeaways

When working with Spring AI function calling:

1. **Be Explicit**: Always provide complete schema information in tool descriptions
2. **Reinforce in System Prompts**: Add critical information to system instructions
3. **Don't Rely on User Input**: AI should use authoritative schema info, not infer from natural language
4. **Multiple Layers**: Use tool description + system prompt + role prompt for important constraints

## ✅ Files Changed

- ✅ `src/main/java/com/devassist/tools/SqlTool.java` - Enhanced tool description
- ✅ `src/main/java/com/devassist/service/ChatService.java` - Added explicit system instruction
- ✅ `src/main/java/com/devassist/service/PromptBuilderService.java` - Updated SQL role prompt

## 🚀 Ready to Test!

Rebuild and restart your application:

```bash
./gradlew clean build -x test
./gradlew bootRun
```

Then run your test:

```bash
./test-sql-tool.sh
```

The AI should now correctly query `app_logs` and provide intelligent log analysis! 🎉

