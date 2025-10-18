# 🏗️ DevAssist Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT (curl/Postman)                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP POST
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ChatController                              │
│  ┌───────────────┬──────────────┬──────────────┬──────────────┐ │
│  │ /chat         │ /chat/sql    │ /chat/       │ /chat/       │ │
│  │               │              │ filesystem   │ multi-tool   │ │
│  └───────────────┴──────────────┴──────────────┴──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ChatService                               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  sendMessage()                                           │   │
│  │  sendSqlToolMessage()                                    │   │
│  │  sendFileSystemToolMessage()                             │   │
│  │  sendHttpToolMessage()                                   │   │
│  │  sendMultiToolMessage()  ← ALL TOOLS                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ChatClient (Spring AI)                      │
│                                                                  │
│  .prompt()                                                       │
│  .system(instruction)                                            │
│  .user(query)                                                    │
│  .tools(sqlTool, fileSystemTool, httpTool)  ← Tool Registration │
│  .advisors(PromptChatMemoryAdvisor)         ← Memory            │
│  .call()                                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    OpenAI API (GPT-4)                            │
│                                                                  │
│  1. Receives: prompt + available tools                           │
│  2. Decides: which tool(s) to call                               │
│  3. Returns: tool_calls OR final response                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  Tool Execution  │
                    └─────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   SqlTool     │    │ FileSystemTool│    │   HttpTool    │
├───────────────┤    ├───────────────┤    ├───────────────┤
│ sql_execute   │    │ read_file     │    │ http_get      │
│               │    │ list_files    │    │ http_post     │
│               │    │ file_info     │    │ check_url     │
└───────────────┘    └───────────────┘    └───────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   Database    │    │  File System  │    │ External APIs │
│   (H2)        │    │  (Project)    │    │  (Internet)   │
└───────────────┘    └───────────────┘    └───────────────┘
```

---

## Tool Registration Flow

```
Application Startup
        │
        ▼
┌─────────────────────────────────────────────┐
│  Spring Boot Component Scan                 │
│  Finds: @Component classes                  │
└─────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────┐
│  Spring AI Tool Scanner                     │
│  Finds: @Tool annotated methods             │
└─────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────┐
│  For Each @Tool Method:                     │
│  1. Extract method signature                │
│  2. Generate JSON schema from parameters    │
│  3. Create FunctionCallback                 │
│  4. Register in ToolRegistry                │
└─────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────┐
│  Tool Registry (In-Memory)                  │
│                                             │
│  sql_execute      → SqlTool::executeSql     │
│  read_file        → FileSystemTool::read    │
│  list_files       → FileSystemTool::list    │
│  file_info        → FileSystemTool::info    │
│  http_get         → HttpTool::get           │
│  http_post        → HttpTool::post          │
│  check_url        → HttpTool::check         │
└─────────────────────────────────────────────┘
```

---

## Request Flow (Multi-Tool Example)

```
1. User Request
   ↓
   POST /chat/multi-tool
   Body: { "task": "Find errors and show code" }

2. ChatController
   ↓
   chatController.chatMultiTool(request)
   → Generates sessionId
   → Calls ChatService

3. ChatService
   ↓
   chatService.sendMultiToolMessage(sessionId, request)
   → Builds system instruction
   → Configures ChatClient with ALL tools

4. ChatClient
   ↓
   chatClient.prompt()
     .system("You are DevAssist with SQL, FileSystem, HTTP tools...")
     .user("Find errors and show code")
     .tools(sqlTool, fileSystemTool, httpTool)
     .advisors(PromptChatMemoryAdvisor)
     .call()

5. Spring AI → OpenAI API
   ↓
   Request:
   {
     "model": "gpt-4",
     "messages": [
       { "role": "system", "content": "You are DevAssist..." },
       { "role": "user", "content": "Find errors and show code" }
     ],
     "tools": [
       { "type": "function", "function": { "name": "sql_execute", ... } },
       { "type": "function", "function": { "name": "read_file", ... } },
       ...
     ]
   }

6. OpenAI Decision
   ↓
   AI analyzes query and decides:
   "I need to:
    1. Query database for errors (sql_execute)
    2. Read the code file (read_file)"

7. First Tool Call
   ↓
   OpenAI returns:
   {
     "tool_calls": [{
       "function": {
         "name": "sql_execute",
         "arguments": "{\"query\":\"SELECT * FROM app_logs WHERE level='ERROR' LIMIT 1\"}"
       }
     }]
   }

8. Spring AI Executes Tool
   ↓
   SqlTool.executeSql(request)
   → Queries database
   → Returns: { "rowCount": 1, "rows": [...], "tookMs": 12 }

9. Spring AI → OpenAI (with tool result)
   ↓
   Request:
   {
     "messages": [
       ...,
       { "role": "tool", "content": "{\"rowCount\":1,...}" }
     ]
   }

10. OpenAI Second Decision
    ↓
    AI analyzes tool result:
    "Error is in SqlTool.java, I need to read that file"

11. Second Tool Call
    ↓
    OpenAI returns:
    {
      "tool_calls": [{
        "function": {
          "name": "read_file",
          "arguments": "{\"filePath\":\"src/main/java/com/devassist/tools/SqlTool.java\"}"
        }
      }]
    }

12. Spring AI Executes Second Tool
    ↓
    FileSystemTool.readFile(request)
    → Reads file
    → Returns: { "content": "package com.devassist...", ... }

13. Spring AI → OpenAI (with second tool result)
    ↓
    Request includes both tool results

14. OpenAI Final Response
    ↓
    AI generates final answer:
    "I found an error in SqlTool.java. The issue is in line 58..."

15. Response to User
    ↓
    ChatController returns:
    {
      "content": "I found an error in SqlTool.java...",
      "headers": { "X-Conversation-Id": "session-123" }
    }
```

---

## Memory Flow (Conversation History)

```
┌─────────────────────────────────────────────┐
│  PromptChatMemoryAdvisor (BEFORE)           │
│                                             │
│  1. Extract conversationId from context     │
│  2. Load last N messages from Redis         │
│  3. Prepend to current prompt               │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│  ChatClient sends to OpenAI                 │
│  (with conversation history)                │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│  PromptChatMemoryAdvisor (AFTER)            │
│                                             │
│  1. Extract conversationId                  │
│  2. Save user message to Redis              │
│  3. Save assistant response to Redis        │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│  Redis (Conversation Storage)               │
│                                             │
│  Key: "chat:memory:session-123"             │
│  Value: [                                   │
│    { "role": "user", "content": "..." },    │
│    { "role": "assistant", "content": "..." }│
│  ]                                          │
└─────────────────────────────────────────────┘
```

---

## Tool Orchestration Patterns

### Pattern 1: Single Tool
```
User: "Find errors in logs"
  ↓
AI: Uses sql_execute only
  ↓
Response: "Found 5 errors..."
```

### Pattern 2: Sequential Chaining
```
User: "Find errors and show code"
  ↓
AI: Step 1 → sql_execute (find errors)
  ↓
AI: Step 2 → read_file (read code)
  ↓
Response: "Error in SqlTool.java line 58..."
```

### Pattern 3: Parallel (Future)
```
User: "Check API health and query logs"
  ↓
AI: Parallel → check_url + sql_execute
  ↓
Response: "API is up, found 3 errors..."
```

### Pattern 4: Complex Orchestration
```
User: "List tools, read each one, check external API"
  ↓
AI: Step 1 → list_files (get tool files)
  ↓
AI: Step 2 → read_file (for each file)
  ↓
AI: Step 3 → check_url (external API)
  ↓
Response: "Found 3 tools: SqlTool, FileSystemTool, HttpTool. API is up."
```

---

## Current Tool Inventory

| Tool | Methods | Purpose | Status |
|------|---------|---------|--------|
| **SqlTool** | 1 | Database queries | ✅ Working |
| **FileSystemTool** | 3 | File operations | ✅ Working |
| **HttpTool** | 3 | External APIs | ✅ Working |
| **Total** | **7** | - | **100%** |

---

## Endpoints Summary

| Endpoint | Tools | Use Case | Example |
|----------|-------|----------|---------|
| `/chat` | None | Basic chat | "Explain Spring AI" |
| `/chat/sql` | SqlTool | DB queries | "Find errors in logs" |
| `/chat/filesystem` | FileSystemTool | File ops | "List Java files" |
| `/chat/http` | HttpTool | External APIs | "Check if Google is up" |
| `/chat/multi-tool` | ALL | Orchestration | "Find errors and show code" |

---

## Technology Stack

```
┌─────────────────────────────────────────────┐
│  Presentation Layer                         │
│  - REST API (Spring Web)                    │
│  - ChatController                           │
└─────────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────────┐
│  Service Layer                              │
│  - ChatService                              │
│  - PromptBuilderService                     │
└─────────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────────┐
│  AI Layer (Spring AI)                       │
│  - ChatClient                               │
│  - PromptChatMemoryAdvisor                  │
│  - Tool Registry                            │
└─────────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────────┐
│  Tool Layer                                 │
│  - SqlTool                                  │
│  - FileSystemTool                           │
│  - HttpTool                                 │
└─────────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────────┐
│  Data Layer                                 │
│  - H2 Database (app_logs)                   │
│  - Redis (conversation memory)              │
│  - File System (project files)              │
│  - External APIs (HTTP)                     │
└─────────────────────────────────────────────┘
```

---

## Next Steps

**MODULE 3: RAG Fundamentals**
- Add Vector Store
- Implement Embeddings
- Build Document Loaders
- Create RAG Pipeline
- Semantic Search

**Estimated Time:** 12 hours

---

**Last Updated:** 2025-10-18

