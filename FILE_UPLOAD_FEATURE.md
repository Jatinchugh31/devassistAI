# 📤 File Upload Feature

## 🎯 Overview

Upload files and let AI analyze them! The AI can:
- Analyze log files
- Summarize documents
- Review code files
- Extract information
- Compare multiple files

---

## 🚀 Features

### 1. Single File Upload
Upload one file and ask AI to analyze it.

### 2. Multiple File Upload
Upload multiple files and AI will analyze them together.

### 3. Smart Content Handling
- Text files: Full content analysis
- Binary files: Metadata only
- Large files: Automatic truncation
- Security: Blocks dangerous file types

### 4. Temporary Storage
- Files saved temporarily during processing
- Automatic cleanup after analysis
- Old files cleaned up after 1 hour

---

## 📋 API Endpoints

### 1. Upload Single File
```
POST /ai-test/upload
```

**Parameters:**
- `file` (required): The file to upload
- `task` (optional): What to do with the file (default: "Analyze this file")
- `sessionId` (optional): Session ID for conversation continuity

**Response:**
```json
{
  "fileName": "error.log",
  "fileSize": 12345,
  "analysis": "AI's analysis here...",
  "sessionId": "upload-abc123"
}
```

---

### 2. Upload Multiple Files
```
POST /ai-test/upload/multiple
```

**Parameters:**
- `files` (required): Array of files to upload
- `task` (optional): What to do with the files
- `sessionId` (optional): Session ID

**Response:**
```json
{
  "fileCount": 3,
  "analysis": "AI's analysis of all files...",
  "sessionId": "upload-multi-xyz789"
}
```

---

## 🧪 Testing

### Test 1: Upload Log File (curl)
```bash
# Create a test log file
echo "2025-10-18 ERROR: Database connection failed
2025-10-18 ERROR: Null pointer exception in UserService
2025-10-18 WARN: High memory usage detected" > test-error.log

# Upload and analyze
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@test-error.log" \
  -F "task=Analyze this log file and identify the main issues"
```

---

### Test 2: Upload Java File
```bash
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@src/main/java/com/devassist/tools/SqlTool.java" \
  -F "task=Review this code and suggest improvements"
```

---

### Test 3: Upload Multiple Files
```bash
curl -X POST http://localhost:9090/ai-test/upload/multiple \
  -F "files=@file1.log" \
  -F "files=@file2.log" \
  -F "files=@file3.log" \
  -F "task=Compare these log files and find common errors"
```

---

### Test 4: Using Postman/Swagger

1. **Open Swagger UI:** http://localhost:9090/swagger-ui/index.html
2. **Find** `/ai-test/upload` endpoint
3. **Click** "Try it out"
4. **Choose file** using file picker
5. **Enter task** (e.g., "Summarize this file")
6. **Execute**

---

## 📊 Supported File Types

### ✅ Text Files (Full Analysis)
- `.txt` - Text files
- `.log` - Log files
- `.java`, `.js`, `.ts`, `.py` - Code files
- `.json`, `.xml`, `.yml`, `.yaml` - Config files
- `.md` - Markdown files
- `.csv` - CSV files
- `.sql` - SQL files
- `.properties` - Properties files

### ⚠️ Binary Files (Metadata Only)
- `.pdf`, `.doc`, `.docx` - Documents
- `.jpg`, `.png`, `.gif` - Images
- `.zip`, `.tar`, `.gz` - Archives

### ❌ Blocked Files (Security)
- `.exe` - Executables
- `.dll` - Libraries
- `.bat`, `.cmd` - Batch files
- `.sh` - Shell scripts

---

## 🔒 Security & Limits

### File Size Limit
- **Maximum:** 10MB per file
- **Reason:** Prevent memory issues and API token limits

### Content Truncation
- **Single file:** 10,000 characters max
- **Multiple files:** 5,000 characters per file
- **Reason:** Stay within AI model token limits

### File Validation
- ✅ Checks file size
- ✅ Validates filename
- ✅ Blocks dangerous extensions
- ✅ Detects binary vs text files

### Temporary Storage
- Files saved to: `/tmp/devassist-uploads/`
- Deleted immediately after processing
- Old files cleaned up after 1 hour

---

## 💡 Use Cases

### 1. Log Analysis
```bash
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@application.log" \
  -F "task=Find all errors and suggest root causes"
```

**AI will:**
- Parse log entries
- Identify error patterns
- Suggest root causes
- Recommend fixes

---

### 2. Code Review
```bash
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@MyService.java" \
  -F "task=Review this code for bugs and improvements"
```

**AI will:**
- Analyze code structure
- Find potential bugs
- Suggest improvements
- Check best practices

---

### 3. Document Summarization
```bash
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@requirements.txt" \
  -F "task=Summarize the key requirements"
```

**AI will:**
- Read document
- Extract key points
- Create summary
- Highlight important items

---

### 4. Configuration Analysis
```bash
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@application.properties" \
  -F "task=Check this configuration for issues"
```

**AI will:**
- Parse configuration
- Identify issues
- Suggest improvements
- Check for security problems

---

### 5. Multiple File Comparison
```bash
curl -X POST http://localhost:9090/ai-test/upload/multiple \
  -F "files=@prod.log" \
  -F "files=@staging.log" \
  -F "task=Compare these logs and find differences"
```

**AI will:**
- Analyze both files
- Find differences
- Identify unique errors
- Suggest which environment has issues

---

## 🎨 Example Responses

### Example 1: Log Analysis
**Request:**
```bash
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@error.log" \
  -F "task=Analyze errors"
```

**Response:**
```json
{
  "fileName": "error.log",
  "fileSize": 2456,
  "analysis": "I analyzed the log file and found 3 main issues:\n\n1. **Database Connection Failures** (5 occurrences)\n   - Root cause: Connection pool exhausted\n   - Solution: Increase max connections\n\n2. **Null Pointer Exceptions** (3 occurrences)\n   - Location: UserService.java line 45\n   - Solution: Add null checks\n\n3. **High Memory Usage** (2 warnings)\n   - Cause: Memory leak in cache\n   - Solution: Implement cache eviction",
  "sessionId": "upload-abc123"
}
```

---

### Example 2: Code Review
**Request:**
```bash
curl -X POST http://localhost:9090/ai-test/upload \
  -F "file=@UserService.java" \
  -F "task=Review code quality"
```

**Response:**
```json
{
  "fileName": "UserService.java",
  "fileSize": 5678,
  "analysis": "Code review findings:\n\n**Strengths:**\n- Well-structured class\n- Good use of dependency injection\n- Proper exception handling\n\n**Issues:**\n1. Missing null checks in getUserById()\n2. No input validation in createUser()\n3. Consider using Optional instead of null returns\n\n**Suggestions:**\n- Add @Validated annotation\n- Implement pagination for getAll()\n- Add unit tests",
  "sessionId": "upload-xyz789"
}
```

---

## 🔧 How It Works

### Flow Diagram:
```
1. User uploads file
   ↓
2. FileUploadController receives file
   ↓
3. FileUploadService validates file
   ↓
4. Save file temporarily
   ↓
5. Read file content
   ↓
6. Build enhanced prompt with file content
   ↓
7. Send to ChatService
   ↓
8. AI analyzes file content
   ↓
9. Return analysis to user
   ↓
10. Delete temporary file
```

---

## 📝 Implementation Details

### FileUploadController
- Handles HTTP file upload
- Manages multipart requests
- Builds AI prompts
- Returns structured responses

### FileUploadService
- Saves files temporarily
- Validates file types and sizes
- Reads file content
- Cleans up old files

### Integration with ChatService
- Uses existing chat infrastructure
- Maintains conversation history
- Supports all AI features (memory, tools, etc.)

---

## 🚀 Next Steps

### Test the Feature:
1. **Restart app:** `./gradlew bootRun`
2. **Create test file:** `echo "test content" > test.txt`
3. **Upload:** Use curl command above
4. **Check response:** AI's analysis

### Advanced Usage:
- Combine with conversation memory
- Upload file, then ask follow-up questions
- Use with multi-tool endpoint for complex analysis

---

## ✅ Summary

**What We Built:**
- ✅ Single file upload endpoint
- ✅ Multiple file upload endpoint
- ✅ File validation and security
- ✅ Temporary storage with cleanup
- ✅ AI integration for analysis
- ✅ Support for text and binary files

**Benefits:**
- 📤 Easy file upload via API
- 🤖 AI-powered file analysis
- 🔒 Secure file handling
- 🧹 Automatic cleanup
- 💬 Conversation continuity

---

**Ready to test!** 🎯

