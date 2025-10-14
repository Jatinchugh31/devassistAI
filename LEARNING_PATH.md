# Spring AI Tools - Complete Learning Path

## 📚 Your Learning Journey

Congratulations on getting the SQL tool working! Here's your complete learning path with all resources.

---

## 🎯 What You've Accomplished

### ✅ Completed
1. **Fixed SQL Tool** - Resolved function naming and parameter binding issues
2. **Understood Error Messages** - Learned about OpenAI function naming rules
3. **Fixed Table Name Issue** - Added schema guidance for AI
4. **Working System** - AI can now query database and analyze logs

### 📖 Documentation Created
- `SQL_TOOL_FIXED.md` - How we fixed the initial issues
- `TABLE_NAME_FIX.md` - How we fixed the table name problem
- `SPRING_AI_TOOLS_GUIDE.md` - Complete guide to all tool patterns
- `TOOL_FLOW_EXPLAINED.md` - Visual flow diagrams
- `HANDS_ON_TUTORIAL.md` - Step-by-step tutorial
- `ToolExamples.java` - Code examples

---

## 📖 Recommended Reading Order

### For Beginners
Start here if you're new to Spring AI tools:

1. **Read First:** `TOOL_FLOW_EXPLAINED.md`
   - Understand the complete flow
   - See visual diagrams
   - Learn what happens under the hood

2. **Then Read:** `HANDS_ON_TUTORIAL.md`
   - Build your first tool (FileSystemTool)
   - Follow step-by-step instructions
   - Test with real examples

3. **Then Study:** `ToolExamples.java`
   - See 5 different implementation patterns
   - Copy-paste examples
   - Experiment with variations

4. **Finally Read:** `SPRING_AI_TOOLS_GUIDE.md`
   - Deep dive into advanced patterns
   - Best practices
   - Production considerations

### For Experienced Developers
Jump to what you need:

- **Quick Reference:** `SPRING_AI_TOOLS_GUIDE.md` → Comparison Table
- **Advanced Patterns:** `SPRING_AI_TOOLS_GUIDE.md` → Advanced Patterns section
- **Code Examples:** `ToolExamples.java`
- **Troubleshooting:** `SQL_TOOL_FIXED.md` + `TABLE_NAME_FIX.md`

---

## 🎓 Learning Modules

### Module 1: Understanding Tools (30 mins)
**Goal:** Understand what tools are and how they work

**Resources:**
- 📖 `TOOL_FLOW_EXPLAINED.md` (full read)
- 💻 Run your existing SQL tool test: `./test-sql-tool.sh`
- 🎯 Experiment: Change the user prompt and see how AI responds

**Exercise:**
```bash
# Try these different prompts:
curl -X POST 'http://localhost:9090/ai-test/chat/sql' -d '{
  "task": "How many error logs are there?",
  "sessionId": "learn-1"
}'

curl -X POST 'http://localhost:9090/ai-test/chat/sql' -d '{
  "task": "What is SQL?",
  "sessionId": "learn-2"
}'
```

**Question to Answer:**
- In which case did AI call the tool? Why?

---

### Module 2: Building Your First Tool (60 mins)
**Goal:** Create a FileSystemTool from scratch

**Resources:**
- 📖 `HANDS_ON_TUTORIAL.md` (complete tutorial)
- 💻 Follow step-by-step guide
- 🎯 Create test files and test endpoints

**Steps:**
1. Create `FileSystemTool.java`
2. Add to `ChatService`
3. Create controller endpoint
4. Create test files
5. Test with curl

**Success Criteria:**
- ✅ Tool compiles without errors
- ✅ Can list files
- ✅ Can read file contents
- ✅ AI provides intelligent responses

---

### Module 3: Exploring Different Patterns (45 mins)
**Goal:** Learn 5 different ways to implement tools

**Resources:**
- 📖 `SPRING_AI_TOOLS_GUIDE.md` → Method sections
- 💻 `ToolExamples.java` → Copy and experiment
- 🎯 Try each pattern

**Exercises:**

#### Exercise 1: Annotation-Based Tool
```java
// Copy WeatherTool from ToolExamples.java
// Register it with your ChatService
// Test: "What's the weather in London?"
```

#### Exercise 2: Dynamic Tool Selection
```java
// Create a service that selects tools based on user role
// Test with different roles: GUEST, USER, ADMIN
```

#### Exercise 3: Tool Chaining
```java
// Create 3 tools that work together:
// 1. fetch_data
// 2. analyze_data
// 3. generate_report
// Let AI call them in sequence
```

---

### Module 4: Advanced Patterns (90 mins)
**Goal:** Master production-ready patterns

**Resources:**
- 📖 `SPRING_AI_TOOLS_GUIDE.md` → Advanced Patterns
- 📖 `SPRING_AI_TOOLS_GUIDE.md` → Best Practices
- 💻 Build production-grade tools

**Topics:**
1. **Error Handling**
   - Graceful failures
   - User-friendly error messages
   - Retry logic

2. **Monitoring**
   - Tool execution metrics
   - Performance tracking
   - Audit logging

3. **Security**
   - Input validation
   - Path traversal prevention
   - Role-based access control

4. **Performance**
   - Caching tool results
   - Timeout handling
   - Rate limiting

---

## 🛠️ Practical Projects

### Project 1: DevOps Assistant (Beginner)
**Tools to Build:**
1. `log_analyzer` - Analyze log files
2. `metrics_fetcher` - Get system metrics
3. `alert_sender` - Send alerts

**User Queries:**
- "Are there any errors in the last hour?"
- "What's the CPU usage?"
- "Alert the team about the database issue"

---

### Project 2: Code Review Assistant (Intermediate)
**Tools to Build:**
1. `git_diff` - Get code changes
2. `code_analyzer` - Analyze code quality
3. `test_runner` - Run tests
4. `comment_generator` - Generate code comments

**User Queries:**
- "Review the latest commit"
- "Are there any code smells?"
- "Run tests for the OrderService"

---

### Project 3: Data Analysis Assistant (Advanced)
**Tools to Build:**
1. `sql_query` - Query database
2. `data_processor` - Process and transform data
3. `chart_generator` - Create visualizations
4. `report_exporter` - Export reports

**User Queries:**
- "Show me sales trends for the last quarter"
- "Create a chart comparing product performance"
- "Export this analysis to PDF"

---

## 🎯 Mastery Checklist

### Basic Level ⭐
- [ ] Understand tool flow (request → AI → tool → response)
- [ ] Create a simple tool with `@Tool` annotation
- [ ] Use strongly-typed parameters (Java records)
- [ ] Test tool with natural language queries
- [ ] Handle basic errors

### Intermediate Level ⭐⭐
- [ ] Build multiple tools in one service
- [ ] Implement dynamic tool selection
- [ ] Add monitoring and logging
- [ ] Implement security controls
- [ ] Test tool chaining (AI calls multiple tools)

### Advanced Level ⭐⭐⭐
- [ ] Create production-grade tools with full error handling
- [ ] Implement caching and performance optimization
- [ ] Build complex multi-tool orchestrations
- [ ] Add comprehensive observability
- [ ] Design tools for scalability

---

## 📊 Comparison: Different Approaches

| Approach | When to Use | Difficulty | Example |
|----------|-------------|------------|---------|
| **@Tool Annotation** | 90% of cases | ⭐ Easy | Your SqlTool |
| **Manual Function** | Custom metadata needed | ⭐⭐ Medium | Calculator |
| **Dynamic Selection** | Role-based access | ⭐⭐ Medium | Admin tools |
| **Tool Callbacks** | Monitoring needed | ⭐⭐⭐ Hard | Metrics wrapper |
| **Multi-Tool Chain** | Complex workflows | ⭐⭐⭐ Hard | Data pipeline |

---

## 🐛 Common Issues & Solutions

### Issue 1: AI Not Calling Tool
**Symptoms:** AI responds without calling the tool

**Causes:**
- Tool description unclear
- Question doesn't match tool purpose
- System instruction conflicts

**Solutions:**
```java
// ❌ Bad description
@Tool(name = "get_data", description = "Gets data")

// ✅ Good description
@Tool(name = "query_database", 
      description = "Query the app_logs database table to fetch application logs. " +
                    "Use this when user asks about logs, errors, or database contents.")
```

### Issue 2: Parameter Binding Fails
**Symptoms:** `null` parameters or `ClassCastException`

**Causes:**
- Using `Map<String, Object>` instead of records
- Missing `@JsonProperty` annotations
- Type mismatch

**Solutions:**
```java
// ❌ Bad
public String tool(Map<String, Object> params) { ... }

// ✅ Good
public Response tool(Request request) { ... }

record Request(
    @JsonProperty(required = true)
    @JsonPropertyDescription("The query string")
    String query
) {}
```

### Issue 3: Invalid Function Name
**Symptoms:** `Invalid 'tools[0].function.name'` error

**Cause:** Function name contains invalid characters

**Solution:**
```java
// ❌ Invalid names
"sql.execute"    // contains dot
"get user"       // contains space
"fetch@logs"     // contains @

// ✅ Valid names
"sql_execute"
"get_user"
"fetch_logs"
```

### Issue 4: Tool Returns Error
**Symptoms:** AI says "The tool failed to execute"

**Causes:**
- Runtime exception in tool code
- Database/network issues
- Invalid input

**Solutions:**
```java
@Tool(name = "my_tool")
public Response myTool(Request request) {
    try {
        // Tool logic
        return new Response("SUCCESS", data);
    } catch (Exception e) {
        // Return structured error
        return new Response("ERROR", null, e.getMessage());
    }
}
```

---

## 🔗 External Resources

### Official Documentation
- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [OpenAI Function Calling](https://platform.openai.com/docs/guides/function-calling)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)

### Community
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)
- [Spring AI Discussions](https://github.com/spring-projects/spring-ai/discussions)
- [Stack Overflow - spring-ai tag](https://stackoverflow.com/questions/tagged/spring-ai)

### Related Topics
- Jackson JSON annotations
- Spring dependency injection
- OpenAI API
- JdbcTemplate usage

---

## 💡 Pro Tips

### Tip 1: Clear Descriptions
```java
// Be specific about what the tool does and when to use it
@Tool(name = "calculate_tax",
      description = "Calculate tax for a purchase. " +
                    "Requires amount and state code. " +
                    "Use when user asks about tax calculations, " +
                    "pricing with tax, or total cost.")
```

### Tip 2: Input Validation
```java
public Response tool(Request request) {
    // Validate early
    if (request.amount() <= 0) {
        return Response.error("Amount must be positive");
    }
    // Then process
}
```

### Tip 3: Structured Responses
```java
// Return structured data, not just strings
record Response(
    String status,      // SUCCESS, ERROR
    Data result,        // Actual data
    String message,     // Human-readable message
    long executionMs    // Performance metric
) {}
```

### Tip 4: Test Without AI First
```java
@Test
void testToolDirectly() {
    SqlTool tool = new SqlTool(jdbc, sqlSafety);
    SqlRequest request = new SqlRequest("SELECT * FROM app_logs", 10);
    
    SqlResponse response = tool.executeSql(request);
    
    assertThat(response.rowCount()).isGreaterThan(0);
}
```

### Tip 5: Monitor Tool Usage
```java
@Aspect
@Component
public class ToolMonitor {
    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Tool {} took {}ms", pjp.getSignature().getName(), duration);
            return result;
        } catch (Throwable e) {
            log.error("Tool {} failed: {}", pjp.getSignature().getName(), e.getMessage());
            throw e;
        }
    }
}
```

---

## 🎉 Next Steps

### Immediate Actions
1. ✅ Review `TOOL_FLOW_EXPLAINED.md` to solidify understanding
2. ✅ Complete `HANDS_ON_TUTORIAL.md` to build FileSystemTool
3. ✅ Experiment with `ToolExamples.java`

### This Week
1. Build 2-3 simple tools for your project
2. Add monitoring and error handling
3. Test with various natural language queries
4. Share your learnings with the team

### This Month
1. Build a complete multi-tool assistant
2. Implement security and role-based access
3. Add comprehensive observability
4. Deploy to production

---

## 📞 Getting Help

### When Stuck
1. **Check logs** - Spring AI provides detailed logging
2. **Test tool directly** - Call the tool method without AI
3. **Review error messages** - OpenAI errors are usually clear
4. **Simplify** - Start with minimal tool and add complexity

### Community Support
- GitHub Issues: [spring-projects/spring-ai](https://github.com/spring-projects/spring-ai/issues)
- Stack Overflow: Tag `spring-ai`
- Spring Community: [spring.io/community](https://spring.io/community)

---

## 🏆 Congratulations!

You now have:
- ✅ Working SQL tool
- ✅ Complete documentation
- ✅ Code examples
- ✅ Learning path
- ✅ Troubleshooting guide

**You're ready to build amazing AI-powered tools!** 🚀

Happy coding! 💻✨

