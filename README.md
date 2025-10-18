# 🤖 DevAssist AI - Learning Spring AI

> A comprehensive Spring AI learning project demonstrating Tools, Advisors, RAG, and Agent patterns.

## 📚 Project Overview

DevAssist is a hands-on learning project to master **Spring AI** concepts by building a real developer assistant with multiple AI capabilities.

### Current Status
- ✅ **27.3% Complete** (3/11 modules)
- ⏱️ **14 hours invested**
- 🎯 **74 hours remaining**

---

## 🎯 What We've Built So Far

### Module 1: SqlTool Deep Dive ✅
- Complete understanding of `@Tool` annotation
- JSON schema generation from Java records
- Type-safe parameter binding
- 15-step execution flow documented

### Module 1.5: Advisors Deep Dive ✅
- 6 built-in advisor types
- Custom advisor creation
- Advisor execution order
- Context map usage

### Module 2: Multiple Tools ✅
- **FileSystemTool** (3 methods: read_file, list_files, file_info)
- **HttpTool** (3 methods: http_get, http_post, check_url)
- **Tool Orchestration** (AI decides which tools to use)
- **Tool Chaining** (AI calls multiple tools in sequence)

---

## 🛠️ Tools Available

| Tool | Methods | Description |
|------|---------|-------------|
| **SqlTool** | `sql_execute` | Query app_logs database |
| **FileSystemTool** | `read_file`, `list_files`, `file_info` | File operations |
| **HttpTool** | `http_get`, `http_post`, `check_url` | External API calls |

**Total: 7 AI-callable tools**

---

## 🌐 API Endpoints

| Endpoint | Tools | Purpose |
|----------|-------|---------|
| `POST /ai-test/chat` | None | Basic chat without tools |
| `POST /ai-test/chat/sql` | SqlTool | Database queries & log analysis |
| `POST /ai-test/chat/filesystem` | FileSystemTool | File reading & navigation |
| `POST /ai-test/chat/http` | HttpTool | External API integration |
| `POST /ai-test/chat/multi-tool` | ALL | Tool orchestration & chaining |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Gradle
- OpenAI API Key
- Redis (for conversation memory)

### Setup

1. **Clone & Configure**
```bash
git clone <your-repo>
cd devassistAI

# Set OpenAI API key
export OPENAI_API_KEY="your-key-here"
```

2. **Start Redis**
```bash
redis-server
```

3. **Build & Run**
```bash
./gradlew clean build
./gradlew bootRun
```

4. **Test**
```bash
chmod +x test-all-tools.sh
./test-all-tools.sh
```

---

## 📖 Example Usage

### SQL Tool - Find Errors
```bash
curl -X POST http://localhost:9090/ai-test/chat/sql \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find 1 error log from app_logs table and explain the root cause",
    "sessionId": "demo-1"
  }'
```

**Response:**
```
I found an error in the app_logs table. The error occurred in SqlTool.java 
at line 58. The root cause is...
```

---

### FileSystem Tool - Read Code
```bash
curl -X POST http://localhost:9090/ai-test/chat/filesystem \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Read SqlTool.java and explain what it does",
    "sessionId": "demo-2"
  }'
```

**Response:**
```
SqlTool.java is a Spring AI tool that provides safe, read-only SQL query 
execution. It has three main components:
1. SqlRequest record for type-safe parameters
2. executeSql method annotated with @Tool
3. Security validation using SqlSafety class
...
```

---

### Multi-Tool - Orchestration
```bash
curl -X POST http://localhost:9090/ai-test/chat/multi-tool \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find errors in logs and show me the related code file",
    "sessionId": "demo-3"
  }'
```

**AI Execution:**
1. Calls `sql_execute` to find errors
2. Extracts logger name from error
3. Calls `read_file` to read the code
4. Analyzes both results
5. Provides comprehensive answer

---

## 🏗️ Architecture

```
Client
  ↓
ChatController (REST API)
  ↓
ChatService (Business Logic)
  ↓
ChatClient (Spring AI)
  ↓
OpenAI API (GPT-4)
  ↓
Tool Execution (SqlTool, FileSystemTool, HttpTool)
  ↓
Data Sources (Database, FileSystem, External APIs)
```

See [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed diagrams.

---

## 📚 Learning Resources

| Document | Description |
|----------|-------------|
| [LEARNING_PROGRESS.md](./LEARNING_PROGRESS.md) | Overall progress tracker |
| [MODULE_2_MULTIPLE_TOOLS.md](./MODULE_2_MULTIPLE_TOOLS.md) | Complete guide to multiple tools |
| [MODULE_2_SUMMARY.md](./MODULE_2_SUMMARY.md) | Quick summary |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | System architecture & flows |
| [test-all-tools.sh](./test-all-tools.sh) | Comprehensive test suite |

---

## 🎓 Key Concepts Learned

### 1. Tool Pattern
```java
@Tool(name = "read_file", description = "Read file contents")
public FileContent readFile(ReadFileRequest request) {
    // Spring AI automatically:
    // 1. Generates JSON schema from ReadFileRequest
    // 2. Sends to OpenAI
    // 3. Binds JSON response to ReadFileRequest
    // 4. Executes this method
    // 5. Returns result to AI
}
```

### 2. Multi-Method Tools
One class can have multiple `@Tool` methods. Each is registered independently.

```java
@Component
public class FileSystemTool {
    @Tool(name = "read_file") ...
    @Tool(name = "list_files") ...
    @Tool(name = "file_info") ...
}
```

### 3. Tool Orchestration
AI decides which tools to use and in what order:

```java
chatClient.prompt()
    .user("Find errors and show code")
    .tools(sqlTool, fileSystemTool, httpTool)  // AI chooses!
    .call();
```

### 4. Advisors
Interceptors for AI calls (BEFORE/AFTER):

```java
chatClient.prompt()
    .user("Hello")
    .advisors(PromptChatMemoryAdvisor)  // Adds conversation history
    .call();
```

---

## 🔮 What's Next?

### Module 3: RAG Fundamentals (Next)
- Embeddings
- Vector Stores
- Semantic Search
- Document Loaders
- RAG Pipeline

### Future Modules
- Advanced RAG patterns
- Streaming responses
- Error handling
- Testing strategies
- Observability
- Deployment

---

## 🎯 End Goal

After completing all modules with DevAssist, we'll build **FitMind AI** - a personalized health and nutrition assistant that uses:
- ✅ Multiple tools (learned)
- ✅ Tool orchestration (learned)
- ⏳ RAG for diet knowledge
- ⏳ User profile management
- ⏳ Calorie tracking
- ⏳ Meal planning

---

## 📊 Progress Tracker

```
[████████░░░░░░░░░░░░░░░░░░░░░░░░] 27.3%

Completed:
✅ Module 1: SqlTool Deep Dive
✅ Module 1.5: Advisors Deep Dive  
✅ Module 2: Multiple Tools

In Progress:
⚠️ Module 3: RAG Fundamentals

Pending:
⏳ Modules 4-11
```

---

## 🤝 Contributing

This is a learning project. Feel free to:
- Suggest improvements
- Report issues
- Share your learning journey

---

## 📝 License

MIT License - Feel free to use for learning!

---

## 🙏 Acknowledgments

- Spring AI Team
- OpenAI
- The amazing Spring community

---

**Last Updated:** 2025-10-18

**Next Session:** MODULE 3 - RAG Fundamentals 🚀

