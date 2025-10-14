# Hands-On Tutorial: Building Your First Spring AI Tool

## 🎯 Goal
Build a **File System Tool** that lets AI read and list files.

## 📋 What You'll Learn
1. Create a tool from scratch
2. Define parameters with proper types
3. Register with ChatClient
4. Test with real AI requests
5. Add error handling

---

## Step 1: Create the Tool Class

Create: `src/main/java/com/devassist/tools/FileSystemTool.java`

```java
package com.devassist.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileSystemTool {
    
    // Define allowed directories for security
    private static final String BASE_DIR = "./data/";
    
    /**
     * List files in a directory
     */
    @Tool(name = "list_files",
          description = "List all files in a directory. " +
                       "Returns file names and sizes. " +
                       "Only works within the data/ directory for security.")
    public ListFilesResponse listFiles(ListFilesRequest request) {
        try {
            Path dirPath = Paths.get(BASE_DIR, request.directory()).normalize();
            
            // Security check: ensure path is within BASE_DIR
            if (!dirPath.startsWith(Paths.get(BASE_DIR).toAbsolutePath())) {
                throw new SecurityException("Access denied: path outside allowed directory");
            }
            
            List<FileInfo> files = Files.list(dirPath)
                .filter(Files::isRegularFile)
                .map(p -> new FileInfo(
                    p.getFileName().toString(),
                    getFileSize(p)
                ))
                .collect(Collectors.toList());
            
            return new ListFilesResponse("SUCCESS", files, files.size());
            
        } catch (IOException e) {
            return new ListFilesResponse("ERROR", List.of(), 0);
        }
    }
    
    /**
     * Read file contents
     */
    @Tool(name = "read_file",
          description = "Read the contents of a file. " +
                       "Returns the full file content as a string. " +
                       "Only works within the data/ directory for security.")
    public ReadFileResponse readFile(ReadFileRequest request) {
        try {
            Path filePath = Paths.get(BASE_DIR, request.filePath()).normalize();
            
            // Security check
            if (!filePath.startsWith(Paths.get(BASE_DIR).toAbsolutePath())) {
                throw new SecurityException("Access denied: path outside allowed directory");
            }
            
            String content = Files.readString(filePath);
            
            return new ReadFileResponse(
                "SUCCESS",
                content,
                content.length(),
                null
            );
            
        } catch (IOException e) {
            return new ReadFileResponse(
                "ERROR",
                null,
                0,
                "Failed to read file: " + e.getMessage()
            );
        }
    }
    
    // Helper method
    private long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }
    
    // Request/Response records
    
    public record ListFilesRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The directory path relative to data/ folder (e.g., 'logs' or 'reports')")
        String directory
    ) {}
    
    public record ListFilesResponse(
        String status,
        List<FileInfo> files,
        int count
    ) {}
    
    public record FileInfo(String name, long sizeBytes) {}
    
    public record ReadFileRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The file path relative to data/ folder (e.g., 'logs/app.log')")
        String filePath
    ) {}
    
    public record ReadFileResponse(
        String status,
        String content,
        int contentLength,
        String error
    ) {}
}
```

---

## Step 2: Add Tool to Chat Service

Update: `src/main/java/com/devassist/service/ChatService.java`

```java
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final SqlTool sqlTool;
    private final FileSystemTool fileSystemTool;  // ← Add this
    private final ChatClient chatClient;
    private final PromptBuilderService promptBuilderService;
    private final RedisChatMemoryRepository memoryRepository;
    
    // Existing methods...
    
    // New method for file operations
    public String sendFileToolMessage(String sessionId, AgentRequest request) {
        String systemInstruction = """
            You are FileAgent: an expert in file system operations.
            You have access to the data/ directory.
            
            Available tools:
            - list_files: List files in a directory
            - read_file: Read file contents
            
            Help users find and read files safely.
            """;
        
        return chatClient.prompt()
                .system(systemInstruction)
                .user(request.getTask())
                .tools(fileSystemTool)  // ← Register the tool
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}
```

---

## Step 3: Add Controller Endpoint

Update: `src/main/java/com/devassist/controller/ChatController.java`

```java
@RestController
@RequestMapping("/ai-test")
public class ChatController {
    
    @Autowired
    ChatService chatService;
    
    // Existing endpoints...
    
    @PostMapping("/chat/files")
    public ResponseEntity<?> chatFiles(@RequestBody AgentRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "anon-" + UUID.randomUUID();
        }
        
        String aiResponse = chatService.sendFileToolMessage(sessionId, request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Conversation-Id", sessionId);
        
        return ResponseEntity.ok().headers(headers).body(aiResponse);
    }
}
```

---

## Step 4: Create Test Data

Create some test files:

```bash
mkdir -p data/logs
mkdir -p data/reports

# Create sample log file
echo "2025-10-14 10:00:00 INFO Application started
2025-10-14 10:05:23 ERROR Database connection failed
2025-10-14 10:05:45 WARN Retrying connection...
2025-10-14 10:06:00 INFO Database connected successfully" > data/logs/app.log

# Create sample report
echo "Daily Report - 2025-10-14
=========================
Total Requests: 1,234
Errors: 5
Success Rate: 99.6%" > data/reports/daily-report.txt
```

---

## Step 5: Build and Run

```bash
# Build
./gradlew clean build -x test

# Run
./gradlew bootRun
```

---

## Step 6: Test Your Tool!

### Test 1: List Files

```bash
curl -X 'POST' \
  'http://localhost:9090/ai-test/chat/files' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "List all files in the logs directory",
    "sessionId": "file-test-1"
  }'
```

**Expected AI Response:**
```
I found 1 file in the logs directory:
- app.log (142 bytes)
```

### Test 2: Read File

```bash
curl -X 'POST' \
  'http://localhost:9090/ai-test/chat/files' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Read the app.log file and tell me about any errors",
    "sessionId": "file-test-2"
  }'
```

**Expected AI Response:**
```
I found 1 error in app.log:
- At 10:05:23: Database connection failed

The application then retried and successfully connected at 10:06:00.
```

### Test 3: Natural Language Query

```bash
curl -X 'POST' \
  'http://localhost:9090/ai-test/chat/files' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "What reports do we have? Show me the daily report.",
    "sessionId": "file-test-3"
  }'
```

**Expected AI Response:**
```
We have 1 report:
- daily-report.txt

Here's the daily report for October 14, 2025:
- Total Requests: 1,234
- Errors: 5
- Success Rate: 99.6%

Your system is performing well with a 99.6% success rate!
```

---

## Step 7: Understanding What Happened

### Behind the Scenes

When you sent: *"Read the app.log file and tell me about any errors"*

**AI's Internal Process:**

1. **Analyze Request**
   ```
   User wants: file contents + error analysis
   Available tools: list_files, read_file
   Need to: read_file("logs/app.log")
   ```

2. **Call Tool**
   ```json
   {
     "tool": "read_file",
     "parameters": {
       "filePath": "logs/app.log"
     }
   }
   ```

3. **Receive Response**
   ```json
   {
     "status": "SUCCESS",
     "content": "2025-10-14 10:00:00 INFO Application started\n...",
     "contentLength": 142
   }
   ```

4. **Analyze & Respond**
   ```
   Parse content → Find errors → Summarize for user
   ```

---

## Step 8: Add Error Handling (Bonus)

Let's make the tool more robust:

```java
@Tool(name = "read_file", description = "Read file contents")
public ReadFileResponse readFile(ReadFileRequest request) {
    // Validate input
    if (request.filePath() == null || request.filePath().isBlank()) {
        return new ReadFileResponse(
            "ERROR",
            null,
            0,
            "File path cannot be empty"
        );
    }
    
    // Check file size before reading
    Path filePath = Paths.get(BASE_DIR, request.filePath()).normalize();
    
    try {
        long fileSize = Files.size(filePath);
        
        // Prevent reading huge files
        if (fileSize > 1_000_000) {  // 1MB limit
            return new ReadFileResponse(
                "ERROR",
                null,
                0,
                "File too large (max 1MB)"
            );
        }
        
        String content = Files.readString(filePath);
        
        return new ReadFileResponse("SUCCESS", content, content.length(), null);
        
    } catch (IOException e) {
        return new ReadFileResponse(
            "ERROR",
            null,
            0,
            "Failed to read file: " + e.getMessage()
        );
    }
}
```

---

## Step 9: Combine Multiple Tools

Now let's use **both SQL and File tools together**!

```java
@Service
public class ChatService {
    
    private final SqlTool sqlTool;
    private final FileSystemTool fileSystemTool;
    private final ChatClient chatClient;
    
    // New: Multi-tool endpoint
    public String sendMultiToolMessage(String sessionId, AgentRequest request) {
        String systemInstruction = """
            You are DevAssist: a full-stack developer assistant.
            
            You have access to:
            - Database tools (query app_logs table)
            - File system tools (read/list files in data/ directory)
            
            Use the appropriate tools to help the user.
            You can use multiple tools in sequence if needed.
            """;
        
        return chatClient.prompt()
                .system(systemInstruction)
                .user(request.getTask())
                .tools(sqlTool, fileSystemTool)  // ← Both tools!
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}
```

**Add endpoint:**

```java
@PostMapping("/chat/all-tools")
public ResponseEntity<?> chatAllTools(@RequestBody AgentRequest request) {
    String sessionId = request.getSessionId();
    if (sessionId == null || sessionId.isBlank()) {
        sessionId = "anon-" + UUID.randomUUID();
    }
    
    String aiResponse = chatService.sendMultiToolMessage(sessionId, request);
    
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Conversation-Id", sessionId);
    
    return ResponseEntity.ok().headers(headers).body(aiResponse);
}
```

**Test Multi-Tool:**

```bash
curl -X 'POST' \
  'http://localhost:9090/ai-test/chat/all-tools' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Compare errors in the database with errors in the app.log file. Are they consistent?",
    "sessionId": "multi-test-1"
  }'
```

**AI will:**
1. Call `sql_execute` to get database errors
2. Call `read_file` to get log file errors
3. Compare and analyze
4. Provide comprehensive answer

---

## 🎓 What You Learned

### ✅ Core Concepts
- [x] Create a tool with `@Tool` annotation
- [x] Define strongly-typed parameters using records
- [x] Register tools with ChatClient
- [x] Handle errors gracefully
- [x] Test with natural language queries
- [x] Combine multiple tools

### ✅ Best Practices
- [x] Security: Validate paths and prevent directory traversal
- [x] Error handling: Return structured error responses
- [x] Clear descriptions: Help AI understand when to use tools
- [x] Type safety: Use records instead of Map<String, Object>

### ✅ Advanced Patterns
- [x] Single tool usage
- [x] Multiple tool orchestration
- [x] Tool chaining (AI calls multiple tools in sequence)

---

## 🚀 Next Challenges

Try building these tools yourself:

### Challenge 1: HTTP Tool
Build a tool that calls external REST APIs.

**Hints:**
- Use `RestTemplate` or `WebClient`
- Add timeout handling
- Cache responses
- Tool name: `http_get`, `http_post`

### Challenge 2: Calculator Tool
Build a tool that performs complex calculations.

**Hints:**
- Support: add, subtract, multiply, divide, power, sqrt
- Handle division by zero
- Return formatted results
- Tool name: `calculate`

### Challenge 3: Git Tool
Build a tool that reads git information.

**Hints:**
- Get commit history: `git log`
- Get current branch: `git branch --show-current`
- Get file changes: `git diff`
- Tool name: `git_info`

### Challenge 4: Code Analyzer Tool
Build a tool that analyzes Java code.

**Hints:**
- Count lines of code
- Find TODO comments
- Detect common patterns
- Tool name: `analyze_code`

---

## 📚 Further Reading

- [SPRING_AI_TOOLS_GUIDE.md](./SPRING_AI_TOOLS_GUIDE.md) - Complete guide
- [ToolExamples.java](./src/main/java/com/devassist/examples/ToolExamples.java) - Code examples
- [Spring AI Docs](https://docs.spring.io/spring-ai/reference/) - Official docs

---

## 🎉 Congratulations!

You've successfully built your first Spring AI Tool! You now understand:
- How tools work
- How to create them
- How to test them
- How to combine them

Keep experimenting and building more tools! 🚀

