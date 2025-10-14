# Spring AI Tools (Function Calling) - Complete Guide

## 📚 Table of Contents
1. [What is Function Calling?](#what-is-function-calling)
2. [Method 1: Annotation-Based Tools (@Tool)](#method-1-annotation-based-tools-tool)
3. [Method 2: Manual Function Definition](#method-2-manual-function-definition)
4. [Method 3: Dynamic Tool Registration](#method-3-dynamic-tool-registration)
5. [Method 4: Tool Callbacks](#method-4-tool-callbacks)
6. [Method 5: Bean-Based Tool Registration](#method-5-bean-based-tool-registration)
7. [Advanced Patterns](#advanced-patterns)
8. [Best Practices](#best-practices)

---

## What is Function Calling?

**Function Calling** (also called **Tool Calling**) allows the AI model to:
1. Decide when it needs external data or actions
2. Call your Java methods with appropriate parameters
3. Receive the results and incorporate them into its response

### Flow Diagram
```
User: "Find error logs from database"
    ↓
AI Model: "I need to query the database"
    ↓
AI calls: sql_execute(query="SELECT * FROM app_logs WHERE level='ERROR'")
    ↓
Your Java Tool executes the query
    ↓
Returns: { rowCount: 3, rows: [...] }
    ↓
AI analyzes results and responds to user
    ↓
User gets: "Found 3 errors: NullPointerException in OrderService..."
```

---

## Method 1: Annotation-Based Tools (@Tool)

**✅ What we're currently using!**

This is the **simplest and most recommended** approach.

### How It Works
1. Annotate a method with `@Tool`
2. Spring AI automatically registers it
3. AI model can call it when needed

### Example: Current SqlTool

```java
@Component
public class SqlTool {
    
    private final JdbcTemplate jdbc;
    
    @Tool(name = "sql_execute", 
          description = "Execute a safe, read-only SQL SELECT query...")
    public SqlResponse executeSql(SqlRequest request) {
        // Your implementation
        return new SqlResponse(rowCount, rows, tookMs);
    }
    
    // Record for type-safe parameters
    public record SqlRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The SQL SELECT query to execute")
        String query,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("Maximum number of rows")
        Integer maxRows
    ) {}
    
    public record SqlResponse(int rowCount, List<Map<String, Object>> rows, long tookMs) {}
}
```

### Registering with ChatClient

```java
@Service
public class ChatService {
    
    private final SqlTool sqlTool;
    private final ChatClient chatClient;
    
    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .tools(sqlTool)  // ← Register the tool
            .call()
            .content();
    }
}
```

### ✅ Pros
- Simple and clean
- Type-safe with Java records
- Automatic JSON schema generation
- IDE support (refactoring, navigation)

### ❌ Cons
- Requires Spring component
- Less flexibility for dynamic tools

---

## Method 2: Manual Function Definition

Create tools programmatically without annotations.

### Example: Weather Tool

```java
@Configuration
public class ToolConfig {
    
    @Bean
    public FunctionCallback weatherTool() {
        return FunctionCallback.builder()
            .function("get_weather", this::getWeather)
            .description("Get current weather for a location")
            .inputType(WeatherRequest.class)
            .build();
    }
    
    private WeatherResponse getWeather(WeatherRequest request) {
        // Call external weather API
        return new WeatherResponse(
            request.location(),
            72,
            "Sunny"
        );
    }
    
    record WeatherRequest(
        @JsonPropertyDescription("City name") String location
    ) {}
    
    record WeatherResponse(String location, int tempF, String condition) {}
}
```

### Usage

```java
@Service
public class ChatService {
    
    private final FunctionCallback weatherTool;
    
    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .function(weatherTool)  // ← Use FunctionCallback
            .call()
            .content();
    }
}
```

### ✅ Pros
- More control over function metadata
- Can create tools at runtime
- No need for separate component classes

### ❌ Cons
- More verbose
- Manual schema definition

---

## Method 3: Dynamic Tool Registration

Register tools conditionally based on user, context, or feature flags.

### Example: Conditional Tool Registry

```java
@Service
public class DynamicChatService {
    
    private final SqlTool sqlTool;
    private final FileSystemTool fileSystemTool;
    private final DeploymentTool deploymentTool;
    private final ChatClient chatClient;
    
    public String chat(String message, UserContext user) {
        // Build tool list based on user permissions
        List<Object> availableTools = new ArrayList<>();
        
        if (user.hasPermission("READ_DATABASE")) {
            availableTools.add(sqlTool);
        }
        
        if (user.hasPermission("READ_FILES")) {
            availableTools.add(fileSystemTool);
        }
        
        if (user.hasRole("ADMIN")) {
            availableTools.add(deploymentTool);
        }
        
        return chatClient.prompt()
            .user(message)
            .tools(availableTools.toArray())  // ← Dynamic tools
            .call()
            .content();
    }
}
```

### Use Cases
- **Role-based access**: Different tools for different user roles
- **Feature flags**: Enable/disable tools based on configuration
- **Context-aware**: Tools available based on conversation context
- **A/B testing**: Test different tool combinations

### ✅ Pros
- Security: Control tool access per user
- Flexibility: Adapt to runtime conditions
- Cost optimization: Fewer tools = cheaper API calls

### ❌ Cons
- More complex logic
- Need to manage permissions

---

## Method 4: Tool Callbacks

Handle tool execution with callbacks for logging, metrics, or error handling.

### Example: Tool Execution Wrapper

```java
@Component
public class MonitoredSqlTool {
    
    private final JdbcTemplate jdbc;
    private final MetricsService metrics;
    private final AuditLogger auditLogger;
    
    @Tool(name = "sql_execute", description = "Execute SQL query")
    public SqlResponse executeSql(SqlRequest request) {
        String toolName = "sql_execute";
        long startTime = System.currentTimeMillis();
        
        try {
            auditLogger.log("Tool called: " + toolName, request);
            
            // Execute the actual tool logic
            SqlResponse response = doExecuteSql(request);
            
            // Record success metrics
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordToolSuccess(toolName, duration);
            
            return response;
            
        } catch (Exception e) {
            // Record failure metrics
            metrics.recordToolFailure(toolName, e.getClass().getSimpleName());
            auditLogger.logError("Tool failed: " + toolName, e);
            
            // Return error response
            throw new ToolExecutionException("SQL execution failed", e);
        }
    }
    
    private SqlResponse doExecuteSql(SqlRequest request) {
        // Actual SQL execution logic
        List<Map<String, Object>> rows = jdbc.query(request.query(), 
            new ColumnMapRowMapper());
        return new SqlResponse(rows.size(), rows, 0);
    }
}
```

### ✅ Pros
- Comprehensive monitoring
- Audit trail
- Error tracking
- Performance metrics

### ❌ Cons
- More boilerplate code
- Need monitoring infrastructure

---

## Method 5: Bean-Based Tool Registration

Register multiple tools through configuration.

### Example: Centralized Tool Registry

```java
@Configuration
public class ToolsConfiguration {
    
    @Bean
    public List<Object> aiTools(
        SqlTool sqlTool,
        FileSystemTool fileSystemTool,
        HttpTool httpTool,
        GitTool gitTool
    ) {
        return List.of(sqlTool, fileSystemTool, httpTool, gitTool);
    }
}

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatClient chatClient;
    private final List<Object> aiTools;  // ← Injected from config
    
    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .tools(aiTools.toArray())  // ← Use all registered tools
            .call()
            .content();
    }
}
```

### ✅ Pros
- Centralized tool management
- Easy to add/remove tools
- Clear overview of available tools

### ❌ Cons
- All tools available to all requests (unless filtered)

---

## Advanced Patterns

### Pattern 1: Multi-Step Tool Chains

AI calls multiple tools in sequence.

```java
@Component
public class DataAnalysisTool {
    
    @Tool(name = "fetch_data", description = "Fetch data from database")
    public DataResult fetchData(FetchRequest request) {
        return new DataResult(/* data */);
    }
    
    @Tool(name = "analyze_data", description = "Analyze fetched data")
    public AnalysisResult analyzeData(AnalysisRequest request) {
        return new AnalysisResult(/* analysis */);
    }
    
    @Tool(name = "visualize_data", description = "Create visualization")
    public VisualizationResult visualizeData(VisualizationRequest request) {
        return new VisualizationResult(/* chart */);
    }
}
```

**AI Flow:**
```
User: "Analyze error logs and create a chart"
    ↓
AI: fetch_data(query="SELECT * FROM app_logs WHERE level='ERROR'")
    ↓
AI: analyze_data(data=<previous_result>)
    ↓
AI: visualize_data(analysis=<previous_result>)
    ↓
AI: "Here's the analysis with a chart showing..."
```

### Pattern 2: Conditional Tool Execution

AI decides which tool to use based on context.

```java
@Service
public class SmartChatService {
    
    private final DatabaseTool dbTool;
    private final ApiTool apiTool;
    private final FileTool fileTool;
    
    public String chat(String message, String dataSource) {
        Object[] tools = switch(dataSource) {
            case "database" -> new Object[]{dbTool};
            case "api" -> new Object[]{apiTool};
            case "files" -> new Object[]{fileTool};
            default -> new Object[]{dbTool, apiTool, fileTool};
        };
        
        return chatClient.prompt()
            .user(message)
            .tools(tools)
            .call()
            .content();
    }
}
```

### Pattern 3: Tool with State Management

Tools that maintain state across calls.

```java
@Component
@Scope("prototype")  // New instance per conversation
public class StatefulAnalysisTool {
    
    private final Map<String, Object> analysisCache = new HashMap<>();
    
    @Tool(name = "analyze_incremental", 
          description = "Analyze data incrementally")
    public AnalysisResult analyze(AnalysisRequest request) {
        // Use cached results from previous calls
        Object previousResult = analysisCache.get(request.sessionId());
        
        AnalysisResult result = performAnalysis(request, previousResult);
        
        // Cache for next call
        analysisCache.put(request.sessionId(), result);
        
        return result;
    }
}
```

### Pattern 4: Parallel Tool Execution

Execute multiple independent tools simultaneously.

```java
@Service
public class ParallelToolService {
    
    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .tools(weatherTool, stockTool, newsTool)  // All available
            .call()
            .content();
    }
}
```

**AI might call:**
```
Parallel:
  - weatherTool.getWeather("New York")
  - stockTool.getPrice("AAPL")
  - newsTool.getHeadlines("technology")

Then synthesizes: "Weather is 72°F, AAPL at $185, top tech news..."
```

---

## Best Practices

### 1. **Clear Tool Descriptions**

❌ Bad:
```java
@Tool(name = "get_data", description = "Gets data")
```

✅ Good:
```java
@Tool(name = "fetch_user_orders", 
      description = "Fetch all orders for a specific user from the orders database. " +
                    "Returns order ID, date, status, and total amount. " +
                    "Use this when user asks about their purchase history.")
```

### 2. **Use Strongly-Typed Parameters**

❌ Bad:
```java
@Tool(name = "search")
public String search(Map<String, Object> params) {
    String query = (String) params.get("q");  // Unsafe!
}
```

✅ Good:
```java
@Tool(name = "search")
public SearchResult search(SearchRequest request) {
    String query = request.query();  // Type-safe!
}

record SearchRequest(
    @JsonPropertyDescription("Search query") String query,
    @JsonPropertyDescription("Max results") int limit
) {}
```

### 3. **Add Validation and Error Handling**

```java
@Tool(name = "sql_execute")
public SqlResponse executeSql(SqlRequest request) {
    // Validate input
    if (request.query() == null || request.query().isBlank()) {
        throw new IllegalArgumentException("Query cannot be empty");
    }
    
    try {
        // Execute
        return performQuery(request);
    } catch (SQLException e) {
        // Return user-friendly error
        throw new ToolExecutionException(
            "Database query failed: " + e.getMessage()
        );
    }
}
```

### 4. **Limit Tool Complexity**

Keep each tool focused on **one task**.

❌ Bad: One massive tool
```java
@Tool(name = "do_everything")
public Object doEverything(Map<String, Object> params) {
    String action = (String) params.get("action");
    // 500 lines of if-else...
}
```

✅ Good: Multiple focused tools
```java
@Tool(name = "fetch_logs") 
public LogResult fetchLogs(LogRequest req) { }

@Tool(name = "analyze_logs")
public AnalysisResult analyzeLogs(AnalysisRequest req) { }

@Tool(name = "export_logs")
public ExportResult exportLogs(ExportRequest req) { }
```

### 5. **Document Available Tools**

Create a tool catalog for your team:

```java
/**
 * DevAssist AI - Available Tools
 * 
 * DATABASE TOOLS:
 * - sql_execute: Query app_logs table
 * 
 * FILE TOOLS:
 * - read_file: Read file contents
 * - write_file: Write to file
 * 
 * DEPLOYMENT TOOLS:
 * - deploy_service: Deploy to staging/prod
 * - rollback_service: Rollback deployment
 */
```

### 6. **Add Observability**

```java
@Aspect
@Component
public class ToolMonitoringAspect {
    
    @Around("@annotation(tool)")
    public Object monitorTool(ProceedingJoinPoint pjp, Tool tool) {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Tool {} executed in {}ms", tool.name(), duration);
            return result;
        } catch (Throwable e) {
            log.error("Tool {} failed: {}", tool.name(), e.getMessage());
            throw e;
        }
    }
}
```

### 7. **Test Your Tools**

```java
@SpringBootTest
class SqlToolTest {
    
    @Autowired
    private SqlTool sqlTool;
    
    @Test
    void testExecuteSql() {
        SqlRequest request = new SqlRequest(
            "SELECT * FROM app_logs WHERE level='ERROR'",
            10
        );
        
        SqlResponse response = sqlTool.executeSql(request);
        
        assertThat(response.rowCount()).isGreaterThan(0);
        assertThat(response.rows()).isNotEmpty();
    }
    
    @Test
    void testInvalidSql() {
        SqlRequest request = new SqlRequest("DROP TABLE app_logs", 10);
        
        assertThrows(IllegalArgumentException.class, 
            () -> sqlTool.executeSql(request)
        );
    }
}
```

---

## Comparison Table

| Method | Complexity | Flexibility | Type Safety | Best For |
|--------|-----------|-------------|-------------|----------|
| **@Tool Annotation** | ⭐ Low | ⭐⭐ Medium | ⭐⭐⭐ High | Most use cases |
| **Manual Function** | ⭐⭐ Medium | ⭐⭐⭐ High | ⭐⭐⭐ High | Custom metadata |
| **Dynamic Registration** | ⭐⭐⭐ High | ⭐⭐⭐ High | ⭐⭐⭐ High | Conditional tools |
| **Tool Callbacks** | ⭐⭐⭐ High | ⭐⭐ Medium | ⭐⭐⭐ High | Monitoring needs |
| **Bean Registry** | ⭐⭐ Medium | ⭐⭐ Medium | ⭐⭐⭐ High | Centralized config |

---

## Real-World Example: Complete Multi-Tool System

Let's build a complete system with multiple tools:

```java
// 1. Database Tool
@Component
public class DatabaseTool {
    @Tool(name = "query_logs", description = "Query application logs")
    public LogResult queryLogs(LogQuery query) {
        // Implementation
    }
}

// 2. Metrics Tool
@Component
public class MetricsTool {
    @Tool(name = "get_metrics", description = "Get system metrics")
    public MetricsResult getMetrics(MetricsQuery query) {
        // Implementation
    }
}

// 3. Notification Tool
@Component
public class NotificationTool {
    @Tool(name = "send_alert", description = "Send alert to team")
    public AlertResult sendAlert(AlertRequest request) {
        // Implementation
    }
}

// Service that uses all tools
@Service
@RequiredArgsConstructor
public class DevOpsAssistant {
    
    private final DatabaseTool dbTool;
    private final MetricsTool metricsTool;
    private final NotificationTool notificationTool;
    private final ChatClient chatClient;
    
    public String handleIncident(String userMessage, User user) {
        // Build tools based on user role
        List<Object> tools = new ArrayList<>();
        tools.add(dbTool);      // Everyone can query logs
        tools.add(metricsTool); // Everyone can see metrics
        
        if (user.hasRole("ADMIN")) {
            tools.add(notificationTool); // Only admins can send alerts
        }
        
        return chatClient.prompt()
            .system("You are a DevOps assistant. Help diagnose and resolve incidents.")
            .user(userMessage)
            .tools(tools.toArray())
            .call()
            .content();
    }
}
```

**Usage Example:**

```
User (Admin): "There's a spike in errors. Check logs and alert the team."

AI Flow:
1. Calls: query_logs(level="ERROR", timeRange="last_1h")
   → Finds 150 NullPointerExceptions
   
2. Calls: get_metrics(service="order-service", metric="error_rate")
   → Error rate jumped from 0.1% to 15%
   
3. Calls: send_alert(
     channel="incidents",
     message="Critical: Order service error rate spiked to 15%..."
   )
   → Alert sent

Response: "I found 150 NullPointerExceptions in the order service 
in the last hour. The error rate jumped from 0.1% to 15%. 
I've alerted the team in #incidents channel."
```

---

## Summary

### Quick Decision Guide

**Use @Tool annotation when:**
- Building standard CRUD tools
- Type safety is important
- You want simple, maintainable code
- ✅ **Recommended for 90% of cases**

**Use Manual Functions when:**
- Need custom function metadata
- Building tools dynamically
- Integrating with non-Spring code

**Use Dynamic Registration when:**
- Different users need different tools
- Tools depend on runtime conditions
- Security/permissions are critical

**Use Tool Callbacks when:**
- Need comprehensive monitoring
- Audit logging required
- Performance tracking needed

---

## Next Steps

1. **Try creating a new tool** - Start with a simple file reader tool
2. **Add monitoring** - Wrap your SQL tool with metrics
3. **Implement permissions** - Create role-based tool access
4. **Build a multi-tool flow** - Chain multiple tools together

Happy coding! 🚀

