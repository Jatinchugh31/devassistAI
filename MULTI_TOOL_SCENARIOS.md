# 🎭 Multi-Tool Orchestration Scenarios

## 🎯 Overview

The **multi-tool endpoint** (`/ai-test/chat/multi-tool`) gives AI access to ALL tools simultaneously:
- ✅ SqlTool (query database)
- ✅ FileSystemTool (read files, list directories)
- ✅ HttpTool (make HTTP requests)

The AI **autonomously decides** which tools to use, in what order, and how to combine the results!

---

## 📊 Updated Database

We added **10 new log entries** to `app_logs` table representing DevAssist errors:

| Service | Error Type | Description |
|---------|-----------|-------------|
| devassist | SqlTool | Invalid SQL query |
| devassist | FileSystemTool | File not found |
| devassist | HttpTool | Connection timeout |
| devassist | ChatService | AI rate limit exceeded |
| devassist | RedisChatMemoryRepository | Redis connection failed |
| devassist | FileUploadService | File size exceeded |
| devassist | LogAdvisor | Request metrics (INFO) |
| devassist | MemoryMonitor | High memory usage (WARN) |
| devassist | SqlTool | Slow query (WARN) |
| devassist | ChatService | Multi-tool success (INFO) |

**Total logs in database:** 18 (8 original + 10 new)

---

## 🎭 Multi-Tool Orchestration Patterns

### Pattern 1: SQL → FileSystem
**User Query:** "Find errors in SqlTool, then read the SqlTool.java file"

**AI Execution:**
```
Step 1: sql_execute
  Query: SELECT * FROM app_logs WHERE logger LIKE '%SqlTool%' AND level = 'ERROR'
  Result: Found 1 error (invalid column)

Step 2: read_file
  Path: src/main/java/com/devassist/tools/SqlTool.java
  Result: Read file content

Step 3: Analysis
  Combine: Error message + Code content
  Output: "The error occurs because..."
```

---

### Pattern 2: FileSystem → SQL
**User Query:** "List all tool files, then check if there are errors for each"

**AI Execution:**
```
Step 1: list_files
  Path: src/main/java/com/devassist/tools
  Result: [SqlTool.java, FileSystemTool.java, HttpTool.java]

Step 2: sql_execute (for each tool)
  Query 1: SELECT * FROM app_logs WHERE logger LIKE '%SqlTool%' AND level = 'ERROR'
  Query 2: SELECT * FROM app_logs WHERE logger LIKE '%FileSystemTool%' AND level = 'ERROR'
  Query 3: SELECT * FROM app_logs WHERE logger LIKE '%HttpTool%' AND level = 'ERROR'

Step 3: Summary
  Output: "SqlTool: 1 error, FileSystemTool: 1 error, HttpTool: 1 error"
```

---

### Pattern 3: SQL → HTTP
**User Query:** "Find HTTP errors, then check if the URL is reachable"

**AI Execution:**
```
Step 1: sql_execute
  Query: SELECT * FROM app_logs WHERE logger LIKE '%HttpTool%' AND level = 'ERROR'
  Result: Found error with URL in context

Step 2: Extract URL from context JSON
  Parse: {"url":"https://api.example.com/data","timeoutMs":10000}
  URL: https://api.example.com/data

Step 3: check_url
  URL: https://api.example.com/data
  Result: Reachable or not

Step 4: Analysis
  Output: "The URL that failed is currently [reachable/not reachable]"
```

---

### Pattern 4: All 3 Tools
**User Query:** "Query errors, read code, check external API"

**AI Execution:**
```
Step 1: sql_execute
  Query: SELECT * FROM app_logs WHERE service = 'devassist' AND level = 'ERROR'
  Result: 7 errors

Step 2: read_file
  Path: src/main/java/com/devassist/tools/FileSystemTool.java
  Result: File content

Step 3: check_url
  URL: https://www.google.com
  Result: Reachable (200 OK)

Step 4: Comprehensive Report
  Combine all results
  Output: Detailed health report
```

---

## 🧪 Test Scenarios

### Scenario 1: Error Investigation
```bash
curl -X POST http://localhost:9090/ai-test/chat/multi-tool \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find all ERROR logs from devassist service related to SqlTool, then read the SqlTool.java file and explain if the errors are related to the code",
    "sessionId": "test-1"
  }'
```

**Expected AI Behavior:**
1. Calls `sql_execute` to find SqlTool errors
2. Calls `read_file` to read SqlTool.java
3. Analyzes both and provides explanation

---

### Scenario 2: Performance Analysis
```bash
curl -X POST http://localhost:9090/ai-test/chat/multi-tool \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find WARN logs about slow SQL queries, then read the SqlTool.java file and suggest optimizations",
    "sessionId": "test-2"
  }'
```

**Expected AI Behavior:**
1. Calls `sql_execute` to find slow query warnings
2. Calls `read_file` to read SqlTool.java
3. Suggests code optimizations

---

### Scenario 3: System Health Check
```bash
curl -X POST http://localhost:9090/ai-test/chat/multi-tool \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Perform a health check: 1) Query app_logs for all devassist errors, 2) List all tool files, 3) Check if https://api.github.com is reachable",
    "sessionId": "test-3"
  }'
```

**Expected AI Behavior:**
1. Calls `sql_execute` to get errors
2. Calls `list_files` to verify tools exist
3. Calls `check_url` to test external connectivity
4. Provides comprehensive health report

---

### Scenario 4: Error Correlation
```bash
curl -X POST http://localhost:9090/ai-test/chat/multi-tool \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Query app_logs for all ERROR logs from devassist, group by tool, and identify patterns",
    "sessionId": "test-4"
  }'
```

**Expected AI Behavior:**
1. Calls `sql_execute` with GROUP BY query
2. Analyzes patterns
3. Identifies correlations

---

### Scenario 5: Code Review with Context
```bash
curl -X POST http://localhost:9090/ai-test/chat/multi-tool \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find the most recent ERROR for FileSystemTool, read FileSystemTool.java, and explain what might be causing that error",
    "sessionId": "test-5"
  }'
```

**Expected AI Behavior:**
1. Calls `sql_execute` with ORDER BY created_at DESC LIMIT 1
2. Calls `read_file` for FileSystemTool.java
3. Correlates error with code
4. Explains root cause

---

## 🎯 Key Benefits

### 1. **Autonomous Decision Making**
- AI decides which tools to use
- No manual orchestration needed
- Adapts to query complexity

### 2. **Tool Chaining**
- AI calls tools in sequence
- Uses output from tool 1 as input for tool 2
- Combines results intelligently

### 3. **Context Awareness**
- AI understands relationships between tools
- Extracts data from one tool for another
- Maintains context across tool calls

### 4. **Comprehensive Analysis**
- Combines data from multiple sources
- Provides holistic insights
- Identifies patterns and correlations

---

## 📋 Available Tools Summary

### SqlTool
```
Tool: sql_execute
Purpose: Query app_logs database
Parameters: query (string), maxRows (int, optional)
Use Cases: Find errors, analyze logs, get metrics
```

### FileSystemTool
```
Tools: read_file, list_files, file_info
Purpose: Read project files, list directories
Parameters: filePath or directoryPath (string)
Use Cases: Read code, list files, get file metadata
```

### HttpTool
```
Tools: http_get, http_post, check_url
Purpose: Make HTTP requests, check URLs
Parameters: url (string), body (string, optional)
Use Cases: Check connectivity, fetch data, test APIs
```

---

## 🚀 How to Test

### 1. Restart Application
```bash
./gradlew bootRun
```

### 2. Run Test Suite
```bash
./test-multi-tool-scenarios.sh
```

### 3. Or Test Individually
```bash
curl -X POST http://localhost:9090/ai-test/chat/multi-tool \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Your multi-tool query here",
    "sessionId": "test-1"
  }'
```

---

## 💡 Tips for Effective Multi-Tool Queries

### ✅ Good Queries:
```
"Find errors in SqlTool, then read the code and explain"
"List all tools, then check if there are errors for each"
"Query slow queries, read the code, and suggest optimizations"
"Find HTTP errors, extract the URL, and check if it's reachable"
```

### ❌ Avoid:
```
"Just find errors" (too vague, single tool is enough)
"Do everything" (too broad, AI might get confused)
"Check logs" (not specific about what to check)
```

### 🎯 Best Practices:
1. **Be specific** about what you want
2. **Mention multiple actions** to trigger multi-tool
3. **Provide context** (which service, which tool, what timeframe)
4. **Ask for analysis** not just data retrieval

---

## 📊 Example Outputs

### Example 1: Error Investigation
**Query:** "Find SqlTool errors and read the code"

**Response:**
```
I found 1 ERROR in the app_logs table for SqlTool:

Error Details:
- Time: 2025-10-18 10:15:45
- Exception: java.sql.SQLException
- Message: SQL execution failed: Invalid column name in query
- Context: {"query":"SELECT invalid_column FROM app_logs","maxRows":50}

I then read the SqlTool.java file. The error occurs because:

1. The SqlTool doesn't validate column names before execution
2. The error happens at line 65 in the executeSql method
3. Recommendation: Add column name validation before executing queries

Code snippet from SqlTool.java:
```java
public SqlResponse executeSql(SqlRequest request) {
    // Missing: column validation here
    List<Map<String, Object>> rows = jdbc.query(limitedQuery, ...);
}
```

Suggested fix: Add a method to validate column names against the schema.
```

---

### Example 2: System Health Check
**Query:** "Perform a comprehensive health check"

**Response:**
```
System Health Report:

1. DATABASE ANALYSIS:
   - Total errors in devassist service: 7
   - Breakdown:
     * SqlTool: 1 error (invalid query)
     * FileSystemTool: 1 error (file not found)
     * HttpTool: 1 error (connection timeout)
     * ChatService: 1 error (rate limit)
     * RedisChatMemoryRepository: 1 error (connection failed)
     * FileUploadService: 1 error (file too large)
   - Warnings: 2 (memory usage, slow query)

2. FILE SYSTEM CHECK:
   - All tool files present:
     ✅ SqlTool.java
     ✅ FileSystemTool.java
     ✅ HttpTool.java
   - Total: 3 tools operational

3. EXTERNAL CONNECTIVITY:
   - https://api.github.com: ✅ Reachable (200 OK, 45ms)

OVERALL STATUS: ⚠️ DEGRADED
- Critical: Redis connection issues
- Action Required: Check Redis server
- Recommendation: Implement retry logic for transient failures
```

---

## ✅ Summary

**What We Built:**
- ✅ 10 new log entries for DevAssist errors
- ✅ Multi-tool orchestration examples
- ✅ Test script with 10 scenarios
- ✅ Comprehensive documentation

**Benefits:**
- 🤖 AI autonomously orchestrates tools
- 🔗 Automatic tool chaining
- 📊 Comprehensive analysis
- 🎯 Context-aware responses

**Next Steps:**
1. Restart app to load new data
2. Run test-multi-tool-scenarios.sh
3. Try your own multi-tool queries!

---

**Ready to test multi-tool orchestration!** 🚀

