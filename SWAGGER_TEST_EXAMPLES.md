# 🧪 Swagger UI Test Examples

## FileSystem Tool Tests

### Test 1: List All Tool Files
**Endpoint:** POST `/ai-test/chat/filesystem`

**Request Body:**
```json
{
  "task": "List all files in the src/main/java/com/devassist/tools directory",
  "sessionId": "demo-1"
}
```

**Expected Response:**
AI will list: SqlTool.java, FileSystemTool.java, HttpTool.java, SqlSafety.java

---

### Test 2: Read SqlTool Code
**Endpoint:** POST `/ai-test/chat/filesystem`

**Request Body:**
```json
{
  "task": "Read the SqlTool.java file and explain what it does in simple terms",
  "sessionId": "demo-2"
}
```

**Expected Response:**
AI will read the file and explain: "SqlTool is a Spring AI tool that allows safe, read-only SQL queries..."

---

### Test 3: Get File Info
**Endpoint:** POST `/ai-test/chat/filesystem`

**Request Body:**
```json
{
  "task": "What is the size of the DevassistApplication.java file?",
  "sessionId": "demo-3"
}
```

**Expected Response:**
AI will call file_info tool and return file size, creation date, etc.

---

### Test 4: Read Application Properties
**Endpoint:** POST `/ai-test/chat/filesystem`

**Request Body:**
```json
{
  "task": "Read the application.properties file and tell me what database we're using",
  "sessionId": "demo-4"
}
```

**Expected Response:**
AI will read src/main/resources/application.properties and tell you about H2 database configuration.

---

## SQL Tool Tests

### Test 5: Query Logs
**Endpoint:** POST `/ai-test/chat/sql`

**Request Body:**
```json
{
  "task": "Find 1 error log from app_logs table and explain the issue",
  "sessionId": "demo-5"
}
```

---

## HTTP Tool Tests

### Test 6: Check URL
**Endpoint:** POST `/ai-test/chat/http`

**Request Body:**
```json
{
  "task": "Check if https://www.google.com is reachable",
  "sessionId": "demo-6"
}
```

---

### Test 7: Fetch API Data
**Endpoint:** POST `/ai-test/chat/http`

**Request Body:**
```json
{
  "task": "Make a GET request to https://jsonplaceholder.typicode.com/posts/1 and tell me what the post is about",
  "sessionId": "demo-7"
}
```

---

## Multi-Tool Tests (Tool Orchestration)

### Test 8: SQL + FileSystem Chaining
**Endpoint:** POST `/ai-test/chat/multi-tool`

**Request Body:**
```json
{
  "task": "Find 1 error from app_logs table, then read the SqlTool.java file and check if there are any issues in the code",
  "sessionId": "demo-8"
}
```

**Expected Behavior:**
1. AI calls `sql_execute` to find error
2. AI extracts logger name from error
3. AI calls `read_file` to read SqlTool.java
4. AI analyzes both results and provides comprehensive answer

---

### Test 9: FileSystem + HTTP Chaining
**Endpoint:** POST `/ai-test/chat/multi-tool`

**Request Body:**
```json
{
  "task": "List all tool files in the tools directory, then check if https://api.github.com is reachable",
  "sessionId": "demo-9"
}
```

**Expected Behavior:**
1. AI calls `list_files` to get tool files
2. AI calls `check_url` to verify GitHub API
3. AI combines results

---

## 🎯 Key Points

### FileSystem Tool:
- ❌ Does NOT upload files from your computer
- ✅ Reads files from the PROJECT directory
- ✅ Lists files in PROJECT folders
- ✅ Gets metadata of PROJECT files

### Security:
- All file operations are restricted to the project directory
- Cannot access files outside the project
- Read-only operations (no write/delete)

### File Paths:
- Use relative paths from project root
- Example: `src/main/java/com/devassist/DevassistApplication.java`
- Example: `build.gradle`
- Example: `README.md`

---

## 💡 Pro Tip

The AI decides which tool to use based on your query:
- "List files" → AI uses `list_files` tool
- "Read file" → AI uses `read_file` tool
- "File size" → AI uses `file_info` tool
- "Find errors and show code" → AI uses `sql_execute` + `read_file` (chaining!)

---

**Try these examples in Swagger UI now!** 🚀

