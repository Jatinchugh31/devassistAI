# 🐛 FileSystem Tool Debugging Guide

## Issue: Request Coming as Null

### Problem
When calling the FileSystem tool, the `request` parameter is null, meaning the AI is not sending the correct JSON parameters.

### Root Cause
The AI might not understand how to format the parameters for your tool.

---

## ✅ What We Fixed

### 1. Added Null Checks
```java
public FileContent readFile(ReadFileRequest request) {
    log.info("read_file tool called with request: {}", request);
    
    if (request == null || request.filePath() == null || request.filePath().isBlank()) {
        log.error("read_file received null or empty request");
        return new FileContent(
            "unknown",
            null,
            0,
            0,
            "Error: filePath is required"
        );
    }
    
    // ... rest of code
}
```

### 2. Better Logging
Now logs show:
- Full request object
- Null detection
- Clear error messages

---

## 🧪 How to Test

### Test 1: List Files (Correct Format)
```bash
curl -X POST http://localhost:9090/ai-test/chat/filesystem \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "List all Java files in the src/main/java/com/devassist/tools directory",
    "sessionId": "test-1"
  }'
```

**Expected AI behavior:**
```json
{
  "tool": "list_files",
  "parameters": {
    "directoryPath": "src/main/java/com/devassist/tools",
    "extension": ".java"
  }
}
```

---

### Test 2: Read File
```bash
curl -X POST http://localhost:9090/ai-test/chat/filesystem \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Read the SqlTool.java file",
    "sessionId": "test-2"
  }'
```

**Expected AI behavior:**
```json
{
  "tool": "read_file",
  "parameters": {
    "filePath": "src/main/java/com/devassist/tools/SqlTool.java"
  }
}
```

---

## 📋 Check the Logs

### What to Look For:

**1. LogAdvisor Output:**
```
╔═══════════════════════════════════════════════════════════════
║ 🤖 AI REQUEST
╠═══════════════════════════════════════════════════════════════
║ ⏰ Timestamp: 2025-10-18 15:30:45.123
║ 💬 User: List all Java files in the */devassist/* directory
║ 🛠️  Tools: list_files, read_file, file_info
╚═══════════════════════════════════════════════════════════════
```

**2. Tool Call Log:**
```
2025-10-18 15:30:46.123 [http-nio-9090-exec-1] INFO  FileSystemTool - list_files tool called with request: ListFilesRequest[directoryPath=src/main/java/com/devassist, extension=.java]
```

**3. If Null:**
```
2025-10-18 15:30:46.123 [http-nio-9090-exec-1] INFO  FileSystemTool - list_files tool called with request: null
2025-10-18 15:30:46.123 [http-nio-9090-exec-1] ERROR FileSystemTool - list_files received null or empty request
```

---

## 🔍 Debugging Steps

### Step 1: Check if Tool is Being Called
Look for this in logs:
```
list_files tool called with request: ...
```

If you don't see this, the AI is not calling the tool at all.

### Step 2: Check Request Value
If you see:
```
list_files tool called with request: null
```

The AI is calling the tool but not sending parameters.

### Step 3: Check AI's Tool Call
Look for Spring AI debug logs:
```
logging.level.org.springframework.ai=DEBUG
```

This will show what the AI is sending to the tool.

---

## 💡 Common Issues & Solutions

### Issue 1: AI Not Understanding the Path Format
**Problem:** Your query said `*/devassist/*` which is a glob pattern, not a directory path.

**Solution:** Be more specific:
```json
{
  "task": "List all Java files in the src/main/java/com/devassist/tools directory"
}
```

---

### Issue 2: Tool Description Not Clear
**Current description:**
```
"List all files and directories in a given path. Provide relative path from project root..."
```

**Better description:**
```
"List all files and directories. Parameter 'directoryPath' must be a relative path like 'src/main/java' or '.' for root. Parameter 'extension' is optional (e.g., '.java' to filter Java files only)."
```

---

### Issue 3: AI Choosing Wrong Tool
If the AI is calling `read_file` instead of `list_files`, the descriptions might be confusing.

**Solution:** Make descriptions more distinct:
- `list_files`: "List/show/display files in a directory"
- `read_file`: "Read/open/show contents of a specific file"

---

## 🛠️ Quick Fix: Improve Tool Descriptions

Update the `@Tool` description to be more explicit about parameters:

```java
@Tool(
    name = "list_files",
    description = "List all files and directories in a path. " +
                 "REQUIRED: directoryPath (string) - relative path from project root, e.g., 'src/main/java' or '.' for root. " +
                 "OPTIONAL: extension (string) - filter by extension, e.g., '.java' for Java files only. " +
                 "Returns: list of files with name, path, size, and last modified date."
)
```

---

## 🧪 Test Commands

### Test with Different Queries:

```bash
# Test 1: Simple list
curl -X POST http://localhost:9090/ai-test/chat/filesystem \
  -H 'Content-Type: application/json' \
  -d '{"task": "List files in src/main/java", "sessionId": "t1"}'

# Test 2: With filter
curl -X POST http://localhost:9090/ai-test/chat/filesystem \
  -H 'Content-Type: application/json' \
  -d '{"task": "Show me all Java files in src/main/java/com/devassist", "sessionId": "t2"}'

# Test 3: Root directory
curl -X POST http://localhost:9090/ai-test/chat/filesystem \
  -H 'Content-Type: application/json' \
  -d '{"task": "List all files in the project root", "sessionId": "t3"}'
```

---

## 📊 Expected vs Actual

| Your Query | Expected directoryPath | Your Query Had |
|------------|----------------------|----------------|
| "List all Java files in the */devassist/* directory" | `src/main/java/com/devassist` | `*/devassist/*` (glob pattern) |
| "List files in src/main/java" | `src/main/java` | ✅ Correct |
| "Show files in the tools folder" | `src/main/java/com/devassist/tools` | Ambiguous |

---

## ✅ Next Steps

1. **Start the app:** `./gradlew bootRun`
2. **Make a test request** with a clear directory path
3. **Check the logs** for the request object
4. **Share the logs** if still null

---

**The null checks are now in place, so you'll see exactly what's being received!** 🎯

