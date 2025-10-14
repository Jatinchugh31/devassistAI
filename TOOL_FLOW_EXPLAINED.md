# Spring AI Tool Flow - Visual Explanation

## 🔄 Complete Request Flow

Let's trace what happens when you make a request with tools enabled.

---

## Example Request

```bash
curl -X POST 'http://localhost:9090/ai-test/chat/sql' \
  -d '{"task": "find 1 log from app_logs table", "sessionId": "5"}'
```

---

## Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. USER REQUEST                                                  │
│                                                                  │
│ POST /ai-test/chat/sql                                          │
│ Body: { "task": "find 1 log...", "sessionId": "5" }           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. CHAT CONTROLLER                                               │
│                                                                  │
│ @PostMapping("/chat/sql")                                       │
│ public ResponseEntity<?> chatSql(@RequestBody request) {       │
│     chatService.sendSqlToolMessage(sessionId, request);        │
│ }                                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. CHAT SERVICE                                                  │
│                                                                  │
│ String systemInstruction = """                                   │
│     You are SqlAgent...                                         │
│     Table: app_logs                                             │
│ """;                                                            │
│                                                                  │
│ return chatClient.prompt()                                      │
│     .system(systemInstruction)                                  │
│     .user(request.getTask())                                    │
│     .tools(sqlTool)  ← REGISTER TOOL                           │
│     .call()                                                     │
│     .content();                                                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. SPRING AI FRAMEWORK                                           │
│                                                                  │
│ • Reads @Tool annotation from SqlTool                           │
│ • Generates JSON schema for sql_execute function:               │
│                                                                  │
│   {                                                              │
│     "name": "sql_execute",                                      │
│     "description": "Execute a safe, read-only SQL...",          │
│     "parameters": {                                             │
│       "type": "object",                                         │
│       "properties": {                                           │
│         "query": { "type": "string", "description": "..." },   │
│         "maxRows": { "type": "integer", "description": "..." } │
│       },                                                        │
│       "required": ["query"]                                     │
│     }                                                           │
│   }                                                             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. OPENAI API REQUEST                                            │
│                                                                  │
│ POST https://api.openai.com/v1/chat/completions                 │
│ {                                                               │
│   "model": "gpt-4",                                             │
│   "messages": [                                                 │
│     {                                                           │
│       "role": "system",                                         │
│       "content": "You are SqlAgent... Table: app_logs"         │
│     },                                                          │
│     {                                                           │
│       "role": "user",                                           │
│       "content": "find 1 log from app_logs table"             │
│     }                                                           │
│   ],                                                            │
│   "tools": [                                                    │
│     {                                                           │
│       "type": "function",                                       │
│       "function": {                                             │
│         "name": "sql_execute",                                  │
│         "description": "Execute a safe, read-only SQL...",      │
│         "parameters": { ... }                                   │
│       }                                                         │
│     }                                                           │
│   ]                                                             │
│ }                                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. OPENAI AI MODEL REASONING                                     │
│                                                                  │
│ AI thinks:                                                       │
│ "User wants to find a log from app_logs table"                 │
│ "I have a tool called sql_execute available"                   │
│ "I should construct a SQL query"                               │
│ "Query: SELECT * FROM app_logs LIMIT 1"                        │
│ "I'll call the tool with this query"                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 7. OPENAI API RESPONSE (Tool Call Decision)                     │
│                                                                  │
│ {                                                               │
│   "choices": [{                                                 │
│     "message": {                                                │
│       "role": "assistant",                                      │
│       "content": null,                                          │
│       "tool_calls": [{                                          │
│         "id": "call_abc123",                                    │
│         "type": "function",                                     │
│         "function": {                                           │
│           "name": "sql_execute",                                │
│           "arguments": "{\"query\":\"SELECT * FROM app_logs LIMIT 1\"}" │
│         }                                                       │
│       }]                                                        │
│     }                                                           │
│   }]                                                            │
│ }                                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 8. SPRING AI FRAMEWORK (Tool Execution)                         │
│                                                                  │
│ • Receives tool_call from OpenAI                                │
│ • Parses: name="sql_execute", args={"query":"SELECT..."}       │
│ • Finds registered SqlTool bean                                 │
│ • Deserializes arguments to SqlRequest record                   │
│ • Calls: sqlTool.executeSql(request)                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 9. SQL TOOL EXECUTION                                            │
│                                                                  │
│ @Component                                                       │
│ public class SqlTool {                                          │
│                                                                  │
│     @Tool(name = "sql_execute", ...)                           │
│     public SqlResponse executeSql(SqlRequest request) {        │
│                                                                  │
│         // 1. Validate query                                    │
│         sqlSafety.ensureSelectOnly(request.query());           │
│                                                                  │
│         // 2. Apply row limit                                   │
│         String limited = sqlSafety.applyRowLimit(query, 1);    │
│                                                                  │
│         // 3. Execute query                                     │
│         List<Map> rows = jdbc.query(limited, ...);             │
│                                                                  │
│         // 4. Return result                                     │
│         return new SqlResponse(rows.size(), rows, 50);         │
│     }                                                           │
│ }                                                               │
│                                                                  │
│ Executes: SELECT * FROM app_logs LIMIT 1                       │
│                                                                  │
│ Database returns:                                               │
│ [                                                               │
│   {                                                             │
│     "id": 1,                                                    │
│     "created_at": "2025-10-14 18:45:12",                       │
│     "level": "INFO",                                            │
│     "message": "User login succeeded for user=alice",          │
│     ...                                                         │
│   }                                                             │
│ ]                                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 10. TOOL RESULT BACK TO SPRING AI                               │
│                                                                  │
│ SqlResponse(                                                     │
│   rowCount = 1,                                                 │
│   rows = [{id: 1, level: "INFO", message: "User login..."}],   │
│   tookMs = 50                                                   │
│ )                                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 11. SPRING AI → OPENAI (Tool Result)                            │
│                                                                  │
│ POST https://api.openai.com/v1/chat/completions                 │
│ {                                                               │
│   "messages": [                                                 │
│     { "role": "system", "content": "You are SqlAgent..." },    │
│     { "role": "user", "content": "find 1 log..." },            │
│     {                                                           │
│       "role": "assistant",                                      │
│       "tool_calls": [{                                          │
│         "function": { "name": "sql_execute", ... }             │
│       }]                                                        │
│     },                                                          │
│     {                                                           │
│       "role": "tool",                                           │
│       "tool_call_id": "call_abc123",                           │
│       "content": "{\"rowCount\":1,\"rows\":[{...}],\"tookMs\":50}" │
│     }                                                           │
│   ],                                                            │
│   "tools": [...]                                                │
│ }                                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 12. OPENAI AI MODEL (Final Response)                            │
│                                                                  │
│ AI analyzes tool result:                                        │
│ "I received 1 log entry"                                        │
│ "It's an INFO level log"                                        │
│ "Message: User login succeeded for user=alice"                 │
│ "This is a successful login, not an error"                     │
│ "I should explain this to the user"                            │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 13. OPENAI API RESPONSE (Final Answer)                          │
│                                                                  │
│ {                                                               │
│   "choices": [{                                                 │
│     "message": {                                                │
│       "role": "assistant",                                      │
│       "content": "I found 1 log entry from the app_logs table:\n\n" + │
│                  "**Log Details:**\n" +                         │
│                  "- Level: INFO\n" +                            │
│                  "- Time: 2025-10-14 18:45:12\n" +             │
│                  "- Message: User login succeeded for user=alice\n\n" + │
│                  "**Analysis:**\n" +                            │
│                  "This is a successful user login event, not an error. " + │
│                  "The user 'alice' successfully logged in at 18:45."  │
│     }                                                           │
│   }]                                                            │
│ }                                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 14. SPRING AI FRAMEWORK                                          │
│                                                                  │
│ • Receives final response from OpenAI                           │
│ • Extracts content from message                                 │
│ • Returns to ChatService                                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 15. CHAT SERVICE RETURNS                                         │
│                                                                  │
│ return chatClient.prompt()...call().content();                 │
│                                                                  │
│ Returns String: "I found 1 log entry..."                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 16. CONTROLLER RESPONSE                                          │
│                                                                  │
│ return ResponseEntity.ok()                                       │
│     .headers(headers)                                           │
│     .body(aiResponse);                                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 17. HTTP RESPONSE TO CLIENT                                      │
│                                                                  │
│ HTTP/1.1 200 OK                                                 │
│ X-Conversation-Id: 5                                            │
│ Content-Type: text/plain                                        │
│                                                                  │
│ I found 1 log entry from the app_logs table:                   │
│                                                                  │
│ **Log Details:**                                                │
│ - Level: INFO                                                   │
│ - Time: 2025-10-14 18:45:12                                    │
│ - Message: User login succeeded for user=alice                 │
│                                                                  │
│ **Analysis:**                                                   │
│ This is a successful user login event, not an error.           │
│ The user 'alice' successfully logged in at 18:45.              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Points to Understand

### 🔑 Tool Registration (Step 4)

Spring AI automatically:
1. Scans `@Tool` annotations
2. Inspects method signature
3. Reads `@JsonPropertyDescription` annotations
4. Generates JSON schema
5. Sends schema to OpenAI

**You don't manually write the schema!**

### 🤖 AI Decision Making (Step 6)

The AI model decides:
- **WHETHER** to call a tool
- **WHICH** tool to call (if multiple available)
- **WHAT** parameters to pass

You don't control this - the AI decides based on:
- System instruction
- Tool descriptions
- User's question
- Conversation context

### 🔄 Multiple Tool Calls

AI can call tools multiple times:

```
User: "Find errors in logs and check if they're resolved"

AI calls:
1. sql_execute(query="SELECT * FROM app_logs WHERE level='ERROR'")
2. sql_execute(query="SELECT * FROM app_logs WHERE resolved=true")
3. Synthesizes answer from both results
```

### ⚠️ No Tool Call

Sometimes AI decides NOT to call tools:

```
User: "What is SQL?"

AI response: Direct answer (no tool call needed)
```

---

## Comparison: With vs Without Tools

### WITHOUT Tools (Regular Chat)

```
User: "What errors are in the database?"
  ↓
AI: "I don't have access to your database. 
     I can't see your errors."
```

**Flow:**
```
User → Controller → ChatService → OpenAI → Response
(No tool calls)
```

### WITH Tools

```
User: "What errors are in the database?"
  ↓
AI calls: sql_execute("SELECT * FROM app_logs WHERE level='ERROR'")
  ↓
AI: "Found 3 errors:
     1. NullPointerException in OrderService
     2. Database timeout in CatalogService
     3. OutOfMemoryError in StreamConsumer"
```

**Flow:**
```
User → Controller → ChatService → OpenAI
  ↓
OpenAI decides to call tool
  ↓
Spring AI executes SqlTool
  ↓
Tool result back to OpenAI
  ↓
OpenAI generates final answer
  ↓
Response to user
```

---

## Multi-Tool Example

### Request
```
User: "Compare database errors with file errors"
```

### Flow with 2 Tools

```
┌──────────────────────────────────────────┐
│ User Request                              │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ AI Receives:                              │
│ - sql_execute tool                        │
│ - read_file tool                          │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ AI Calls Tool 1                           │
│ sql_execute(                              │
│   query="SELECT * FROM app_logs           │
│          WHERE level='ERROR'"             │
│ )                                         │
└────────────┬─────────────────────────────┘
             │
             ▼ [Returns database errors]
             │
             ▼
┌──────────────────────────────────────────┐
│ AI Calls Tool 2                           │
│ read_file(                                │
│   filePath="logs/app.log"                 │
│ )                                         │
└────────────┬─────────────────────────────┘
             │
             ▼ [Returns file errors]
             │
             ▼
┌──────────────────────────────────────────┐
│ AI Analyzes Both Results                  │
│ - Compares errors                         │
│ - Finds matches                           │
│ - Identifies discrepancies                │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ Final Response                            │
│ "Both sources show the same 3 errors.    │
│  They are consistent. Database has        │
│  additional metadata like timestamps."    │
└──────────────────────────────────────────┘
```

---

## Error Handling Flow

### What if Tool Fails?

```
┌────────────────────────────────┐
│ AI Calls: sql_execute(...)     │
└──────────┬─────────────────────┘
           │
           ▼
┌────────────────────────────────┐
│ SqlTool Execution               │
│ → Query validation fails        │
│ → Throws exception              │
└──────────┬─────────────────────┘
           │
           ▼
┌────────────────────────────────┐
│ Exception caught by Spring AI   │
│ → Converts to error message     │
│ → Sends error to OpenAI         │
└──────────┬─────────────────────┘
           │
           ▼
┌────────────────────────────────┐
│ AI receives error               │
│ "Query validation failed: ..."  │
└──────────┬─────────────────────┘
           │
           ▼
┌────────────────────────────────┐
│ AI explains to user:            │
│ "I couldn't execute the query   │
│  because it contains forbidden  │
│  operations. Only SELECT        │
│  queries are allowed."          │
└────────────────────────────────┘
```

---

## Summary

### The Magic ✨

1. **You write:** Simple Java method with `@Tool`
2. **Spring AI:** Converts to JSON schema
3. **OpenAI:** Decides when to call
4. **Spring AI:** Executes your method
5. **OpenAI:** Analyzes result
6. **User:** Gets intelligent answer

### Why This is Powerful 💪

- **AI decides** when tools are needed
- **Type-safe** parameters (Java records)
- **Automatic** schema generation
- **Seamless** integration
- **Natural language** interface

### You Control 🎛️

- ✅ Tool implementation
- ✅ Tool descriptions
- ✅ Which tools are available
- ✅ Error handling
- ✅ Security/validation

### AI Controls 🤖

- ✅ Whether to call tools
- ✅ Which tool to call
- ✅ What parameters to pass
- ✅ How to interpret results
- ✅ How to explain to user

---

That's the complete flow! Now you understand exactly what happens under the hood. 🎓

