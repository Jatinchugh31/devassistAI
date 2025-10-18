# 🎓 MODULE 2: Building Multiple Tools

## 📚 Table of Contents
1. [Tool Architecture Patterns](#tool-architecture-patterns)
2. [FileSystemTool - Multi-Method Tool](#filesystemtool)
3. [HttpTool - External API Integration](#httptool)
4. [Tool Orchestration](#tool-orchestration)
5. [How Spring AI Registers Multiple Tools](#registration)
6. [Testing All Tools](#testing)

---

## 🎯 Tool Architecture Patterns

### Pattern 1: Single-Method Tool (SqlTool)

```java
@Component
public class SqlTool {
    
    @Tool(name = "sql_execute", description = "Execute SQL query")
    public SqlResponse executeSql(SqlRequest request) {
        // One tool = one method
    }
}
```

**Characteristics:**
- ✅ Simple and focused
- ✅ One responsibility
- ✅ Easy to test
- ❌ Limited functionality

---

### Pattern 2: Multi-Method Tool (FileSystemTool)

```java
@Component
public class FileSystemTool {
    
    @Tool(name = "read_file", description = "Read file contents")
    public FileContent readFile(ReadFileRequest request) {
        // Method 1
    }
    
    @Tool(name = "list_files", description = "List directory contents")
    public FileList listFiles(ListFilesRequest request) {
        // Method 2
    }
    
    @Tool(name = "file_info", description = "Get file metadata")
    public FileInfo getFileInfo(FileInfoRequest request) {
        // Method 3
    }
}
```

**Characteristics:**
- ✅ Related functionality grouped together
- ✅ Shared utilities (validation, security)
- ✅ Better code organization
- ✅ AI gets multiple related tools

**Key Point:** Spring AI scans ALL `@Tool` methods and registers them **separately**. Each method becomes an independent tool!

---

## 📁 FileSystemTool

### Overview
FileSystemTool provides 3 tools for file operations:
1. `read_file` - Read file contents
2. `list_files` - List directory contents
3. `file_info` - Get file metadata

### Tool 1: read_file

**Purpose:** Read file contents safely

**Request:**
```java
public record ReadFileRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Relative path to the file")
    String filePath
) {}
```

**Response:**
```java
public record FileContent(
    String path,
    String content,
    long sizeBytes,
    int lineCount,
    String message
) {}
```

**Example AI Usage:**
```
User: "Read the SqlTool.java file"

AI Decision:
1. Recognizes need for read_file tool
2. Generates: { "filePath": "src/main/java/com/devassist/tools/SqlTool.java" }
3. Receives file content
4. Analyzes and responds
```

**Safety Features:**
- ✅ Path validation (must be within project directory)
- ✅ File size limit (1MB max)
- ✅ Error handling (file not found, permission denied)

---

### Tool 2: list_files

**Purpose:** List directory contents with optional filtering

**Request:**
```java
public record ListFilesRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Relative directory path")
    String directoryPath,
    
    @JsonProperty(required = false)
    @JsonPropertyDescription("Optional file extension filter")
    String extension
) {}
```

**Response:**
```java
public record FileList(
    String directory,
    int fileCount,
    int directoryCount,
    List<FileItem> items,
    String message
) {}

public record FileItem(
    String name,
    String path,
    boolean isDirectory,
    long sizeBytes,
    String lastModified
) {}
```

**Example AI Usage:**
```
User: "List all Java files in the tools directory"

AI Decision:
1. Recognizes need for list_files tool
2. Generates: { 
     "directoryPath": "src/main/java/com/devassist/tools",
     "extension": ".java"
   }
3. Receives file list
4. Responds with summary
```

---

### Tool 3: file_info

**Purpose:** Get detailed file metadata

**Request:**
```java
public record FileInfoRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Relative path to file or directory")
    String path
) {}
```

**Response:**
```java
public record FileInfo(
    String path,
    boolean exists,
    boolean isFile,
    boolean isDirectory,
    long sizeBytes,
    String created,
    String lastModified,
    String lastAccessed,
    boolean readable,
    boolean writable,
    String message
) {}
```

**Example AI Usage:**
```
User: "When was the SqlTool.java file last modified?"

AI Decision:
1. Recognizes need for file_info tool
2. Generates: { "path": "src/main/java/com/devassist/tools/SqlTool.java" }
3. Receives metadata
4. Extracts lastModified timestamp
5. Responds with human-readable answer
```

---

## 🌐 HttpTool

### Overview
HttpTool provides 3 tools for HTTP operations:
1. `http_get` - Make GET requests
2. `http_post` - Make POST requests
3. `check_url` - Check URL reachability

### Tool 1: http_get

**Purpose:** Fetch data from external APIs

**Request:**
```java
public record HttpGetRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("The URL to send GET request to")
    String url,
    
    @JsonProperty(required = false)
    @JsonPropertyDescription("Optional headers")
    Map<String, String> headers
) {}
```

**Response:**
```java
public record HttpResponse(
    int statusCode,
    String statusText,
    String body,
    Map<String, String> responseHeaders,
    long responseTimeMs,
    String message
) {}
```

**Example AI Usage:**
```
User: "Fetch the latest post from JSONPlaceholder API"

AI Decision:
1. Recognizes need for http_get tool
2. Generates: { "url": "https://jsonplaceholder.typicode.com/posts/1" }
3. Receives API response
4. Parses JSON body
5. Responds with post details
```

---

### Tool 2: http_post

**Purpose:** Submit data to external APIs

**Request:**
```java
public record HttpPostRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("The URL to send POST request to")
    String url,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("Request body as JSON string")
    String body,
    
    @JsonProperty(required = false)
    @JsonPropertyDescription("Optional headers")
    Map<String, String> headers
) {}
```

**Example AI Usage:**
```
User: "Create a new post on JSONPlaceholder API with title 'Test' and body 'Hello'"

AI Decision:
1. Recognizes need for http_post tool
2. Generates: { 
     "url": "https://jsonplaceholder.typicode.com/posts",
     "body": "{\"title\":\"Test\",\"body\":\"Hello\",\"userId\":1}"
   }
3. Receives API response
4. Confirms creation
```

---

### Tool 3: check_url

**Purpose:** Health check for URLs

**Request:**
```java
public record CheckUrlRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("The URL to check")
    String url
) {}
```

**Response:**
```java
public record UrlStatus(
    String url,
    boolean reachable,
    int statusCode,
    long responseTimeMs,
    String message
) {}
```

**Example AI Usage:**
```
User: "Is Google up?"

AI Decision:
1. Recognizes need for check_url tool
2. Generates: { "url": "https://www.google.com" }
3. Receives status
4. Responds: "Yes, Google is up (200 OK, 45ms)"
```

---

## 🎭 Tool Orchestration

### What is Tool Orchestration?

**Tool Orchestration** = AI decides which tools to use and in what order

### Single-Tool Endpoints

```java
// Only SQL tool available
chatClient.prompt()
    .user("Find errors")
    .tools(sqlTool)  // ← AI can only use SQL
    .call();
```

**AI Behavior:**
- ✅ Can use: sql_execute
- ❌ Cannot use: read_file, http_get, etc.

---

### Multi-Tool Endpoint (Orchestration)

```java
// ALL tools available
chatClient.prompt()
    .user("Find errors and show related code")
    .tools(sqlTool, fileSystemTool, httpTool)  // ← AI can use ANY tool
    .call();
```

**AI Behavior:**
- ✅ Can use: sql_execute, read_file, list_files, file_info, http_get, http_post, check_url
- ✅ Can chain tools (use multiple in sequence)
- ✅ Decides which tools to use based on user query

---

### Tool Chaining Example

**User Query:** "Find errors in logs and show me the related code file"

**AI Execution Flow:**

```
Step 1: Analyze query
  → Need to: (1) Find errors, (2) Read code file
  → Tools needed: sql_execute + read_file

Step 2: Call sql_execute
  Request: { "query": "SELECT * FROM app_logs WHERE level = 'ERROR' LIMIT 1" }
  Response: { 
    "rowCount": 1, 
    "rows": [{ "logger": "com.devassist.tools.SqlTool", ... }]
  }

Step 3: Extract logger name
  → Logger: com.devassist.tools.SqlTool
  → File path: src/main/java/com/devassist/tools/SqlTool.java

Step 4: Call read_file
  Request: { "filePath": "src/main/java/com/devassist/tools/SqlTool.java" }
  Response: { "content": "package com.devassist.tools; ...", ... }

Step 5: Analyze both results
  → Found error in SqlTool
  → Read SqlTool code
  → Identified issue

Step 6: Generate response
  "I found an error in SqlTool.java. The issue is in line 58 where..."
```

**Key Points:**
- ✅ AI automatically chained 2 tools
- ✅ Used output from tool 1 as input for tool 2
- ✅ Combined results into coherent answer
- ✅ No manual orchestration needed!

---

## 🔧 How Spring AI Registers Multiple Tools

### Registration Process

```
1. Spring Boot starts
   ↓
2. Component scan finds @Component classes
   ↓
3. Spring AI scans for @Tool annotations
   ↓
4. For each @Tool method:
   a. Extract method signature
   b. Generate JSON schema from parameters
   c. Register as FunctionCallback
   ↓
5. Build tool registry
   ↓
6. When ChatClient.tools() is called:
   a. Look up tools by reference
   b. Add to ChatOptions
   c. Send to OpenAI API
```

---

### Example: FileSystemTool Registration

**Source Code:**
```java
@Component
public class FileSystemTool {
    
    @Tool(name = "read_file", description = "Read file contents")
    public FileContent readFile(ReadFileRequest request) { ... }
    
    @Tool(name = "list_files", description = "List directory contents")
    public FileList listFiles(ListFilesRequest request) { ... }
    
    @Tool(name = "file_info", description = "Get file metadata")
    public FileInfo getFileInfo(FileInfoRequest request) { ... }
}
```

**Spring AI Registration:**
```
Tool Registry:
├── read_file
│   ├── Description: "Read file contents"
│   ├── Parameters: { filePath: string (required) }
│   └── Handler: FileSystemTool::readFile
│
├── list_files
│   ├── Description: "List directory contents"
│   ├── Parameters: { directoryPath: string (required), extension: string (optional) }
│   └── Handler: FileSystemTool::listFiles
│
└── file_info
    ├── Description: "Get file metadata"
    ├── Parameters: { path: string (required) }
    └── Handler: FileSystemTool::getFileInfo
```

---

### When You Call `.tools(fileSystemTool)`

```java
chatClient.prompt()
    .tools(fileSystemTool)  // ← What happens here?
    .call();
```

**Internal Process:**

```
1. Spring AI looks up fileSystemTool bean
   ↓
2. Finds 3 @Tool methods
   ↓
3. Builds OpenAI tools array:
   [
     {
       "type": "function",
       "function": {
         "name": "read_file",
         "description": "Read file contents",
         "parameters": {
           "type": "object",
           "properties": {
             "filePath": { "type": "string", "description": "..." }
           },
           "required": ["filePath"]
         }
       }
     },
     {
       "type": "function",
       "function": {
         "name": "list_files",
         ...
       }
     },
     {
       "type": "function",
       "function": {
         "name": "file_info",
         ...
       }
     }
   ]
   ↓
4. Sends to OpenAI API
   ↓
5. OpenAI decides which tool(s) to call
   ↓
6. Spring AI executes the chosen method
   ↓
7. Returns result to OpenAI
   ↓
8. OpenAI generates final response
```

---

## 🧪 Testing All Tools

### Test Script: `test-all-tools.sh`

We created a comprehensive test script with 8 tests:

1. **SQL Tool** - Query logs
2. **FileSystem Tool** - List files
3. **FileSystem Tool** - Read file
4. **HTTP Tool** - Check URL
5. **HTTP Tool** - GET request
6. **Multi-Tool** - SQL + FileSystem
7. **Multi-Tool** - FileSystem + HTTP
8. **Multi-Tool** - All 3 tools

### Running Tests

```bash
# Make executable
chmod +x test-all-tools.sh

# Run all tests
./test-all-tools.sh
```

---

### Test Examples

#### Test 1: SQL Tool
```bash
curl -X 'POST' \
  'http://localhost:9090/ai-test/chat/sql' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find 1 error log from app_logs table and explain the root cause",
    "sessionId": "test-sql-1"
  }'
```

**Expected Flow:**
1. AI receives query
2. Calls `sql_execute` tool
3. Analyzes results
4. Explains root cause

---

#### Test 6: Multi-Tool (SQL + FileSystem)
```bash
curl -X 'POST' \
  'http://localhost:9090/ai-test/chat/multi-tool' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find 1 error from app_logs, then read SqlTool.java and explain if there are issues",
    "sessionId": "test-multi-1"
  }'
```

**Expected Flow:**
1. AI receives query
2. Calls `sql_execute` to find error
3. Extracts logger name from error
4. Calls `read_file` to read SqlTool.java
5. Analyzes both results
6. Provides comprehensive answer

---

## 📊 Summary

### What We Built

| Tool | Methods | Purpose |
|------|---------|---------|
| **SqlTool** | 1 | Query database logs |
| **FileSystemTool** | 3 | Read files, list directories, get metadata |
| **HttpTool** | 3 | Make HTTP requests, check URLs |

**Total:** 3 tool classes, 7 tool methods

---

### Key Learnings

1. ✅ **Multi-Method Tools**: One class can have multiple `@Tool` methods
2. ✅ **Independent Registration**: Each method is registered separately
3. ✅ **Tool Orchestration**: AI decides which tools to use and when
4. ✅ **Tool Chaining**: AI can call multiple tools in sequence
5. ✅ **Dynamic Selection**: AI chooses tools based on user query
6. ✅ **Shared Utilities**: Tool classes can share validation/security logic

---

### Endpoints Created

| Endpoint | Tools Available | Use Case |
|----------|----------------|----------|
| `/chat` | None | Basic chat (no tools) |
| `/chat/sql` | SqlTool | Database queries |
| `/chat/filesystem` | FileSystemTool | File operations |
| `/chat/http` | HttpTool | External APIs |
| `/chat/multi-tool` | ALL | Complex orchestration |

---

### Next Steps

**MODULE 3 will cover:**
- RAG (Retrieval-Augmented Generation)
- Vector Stores
- Embeddings
- Semantic Search
- Document Loaders

---

## 🎯 Quiz: Test Your Understanding

1. **Q:** How many tools are registered when you use `.tools(fileSystemTool)`?
   **A:** 3 tools (read_file, list_files, file_info)

2. **Q:** Can AI call multiple tools in one request?
   **A:** Yes! In multi-tool endpoints, AI can chain tools.

3. **Q:** What's the difference between single-tool and multi-tool endpoints?
   **A:** Single-tool: AI can only use one tool class. Multi-tool: AI can use multiple tool classes and orchestrate them.

4. **Q:** How does Spring AI know which method to call?
   **A:** OpenAI returns the tool name (e.g., "read_file"), and Spring AI looks up the corresponding method.

5. **Q:** Can one tool class have methods with different parameter types?
   **A:** Yes! Each method has its own request/response types.

---

**🎉 MODULE 2 COMPLETE!**

You now understand:
- ✅ Multi-method tools
- ✅ Tool registration
- ✅ Tool orchestration
- ✅ Tool chaining
- ✅ FileSystem operations
- ✅ HTTP integration
- ✅ Multi-tool endpoints

**Ready for MODULE 3: RAG & Vector Stores?** 🚀

