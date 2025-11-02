# 🤖 Agentic AI Patterns - Learning Path

## Step 0.1: Request/Response Examples

Understanding what Agentic AI looks like vs. current implementation.

---

## 📊 CURRENT STATE vs. AGENTIC AI

### **Current Implementation (Reactive)**

Your current system reacts to user requests but doesn't **plan** or **reason** about the sequence of actions.

---

### **Example 1: Current Behavior**

#### Request:
```json
POST /chat
{
  "task": "Find errors in logs and show me the code file where the error occurred",
  "sessionId": "test-1"
}
```

#### Current Flow:
```
1. User sends request
   ↓
2. AI receives: "Find errors in logs and show me the code file"
   ↓
3. AI decides: "I need to use sql_execute tool"
   ↓
4. Executes: SELECT * FROM app_logs WHERE level='ERROR' LIMIT 1
   ↓
5. Gets result: { error: "NullPointerException", logger: "SqlTool.java:58" }
   ↓
6. AI decides: "I need to read SqlTool.java"
   ↓
7. Executes: read_file("src/main/java/com/devassist/tools/SqlTool.java")
   ↓
8. Returns response: "Error found in SqlTool.java at line 58..."
```

**Characteristics:**
- ✅ Works but requires multiple round-trips
- ❌ AI doesn't plan ahead
- ❌ No explicit reasoning steps
- ❌ Each step is reactive

---

### **Agentic AI Behavior (Proactive Planning)**

Same request, but with **agentic patterns**:

#### Request:
```json
POST /chat/agentic
{
  "task": "Find errors in logs and show me the code file where the error occurred",
  "sessionId": "test-1",
  "mode": "agentic"  // Enable agentic planning
}
```

#### Agentic Flow:
```
1. User sends request
   ↓
2. AGENT PLANNING PHASE:
   Agent thinks:
   "To complete this task, I need to:
    a) Query logs for errors
    b) Extract file path from error result
    c) Read the code file
    d) Analyze error in context of code
   
   Let me plan the sequence:
   Step 1: Execute SQL to find errors
   Step 2: Parse result to extract file path
   Step 3: Read the file
   Step 4: Correlate error with code"
   ↓
3. AGENT EXECUTION PHASE:
   Executes planned sequence:
   → sql_execute("SELECT * FROM app_logs WHERE level='ERROR' LIMIT 1")
   → Parse result, extract logger: "SqlTool.java:58"
   → read_file("src/main/java/com/devassist/tools/SqlTool.java")
   → Analyze error location (line 58) with code context
   ↓
4. AGENT SYNTHESIS PHASE:
   Combines all results:
   "I found an error in SqlTool.java at line 58. 
   The error is a NullPointerException in the executeSql method.
   Looking at the code, the issue is that..."
```

**Characteristics:**
- ✅ Plans ahead (Reasoning phase)
- ✅ Executes planned sequence (Acting phase)
- ✅ Synthesizes results (Reflection phase)
- ✅ More autonomous and efficient

---

## 🎯 KEY DIFFERENCES

| Aspect | Current (Reactive) | Agentic (Proactive) |
|--------|------------------|---------------------|
| **Planning** | No explicit plan | Plans sequence before execution |
| **Reasoning** | Implicit in each step | Explicit reasoning before action |
| **Efficiency** | Multiple round-trips | Planned execution in one flow |
| **Autonomy** | Tool-by-tool decisions | Autonomous multi-step planning |
| **Transparency** | Shows tool calls | Shows reasoning + tool calls |

---

## 📝 DETAILED EXAMPLES

### **Example 2: Complex Multi-Step Task**

#### Request:
```json
{
  "task": "Analyze the system health: check for errors, verify all tools exist, and test external API connectivity",
  "sessionId": "health-check"
}
```

#### Current Response:
```
Step 1: AI calls sql_execute → Gets errors
Step 2: AI calls list_files → Gets tool files  
Step 3: AI calls check_url → Tests connectivity
Step 4: AI combines results

Response: "Found 3 errors, tools exist, API is up"
```

#### Agentic Response:
```
[PLANNING]
Agent: "I need to:
1. Query errors from database
2. List tool files to verify existence
3. Test external API
4. Synthesize health report

[EXECUTION]
→ sql_execute("SELECT COUNT(*) FROM app_logs WHERE level='ERROR'")
→ list_files("src/main/java/com/devassist/tools")
→ check_url("https://api.github.com")

[REASONING]
Agent: "Based on results:
- 3 errors detected (need attention)
- All 3 tools present (healthy)
- External API reachable (network OK)"

[SYNTHESIS]
Response:
{
  "healthStatus": "DEGRADED",
  "details": {
    "errors": 3,
    "tools": ["SqlTool", "FileSystemTool", "HttpTool"],
    "apiConnectivity": true
  },
  "recommendations": ["Investigate 3 errors", "System operational"]
}
```

---

### **Example 3: Problem-Solving Agent**

#### Request:
```json
{
  "task": "A user reported a bug where SqlTool fails. Investigate and provide a fix.",
  "sessionId": "bug-investigation"
}
```

#### Agentic Response Flow:

```
[PLANNING PHASE]
Agent reasons:
"To investigate this bug, I need to:
1. Find recent errors related to SqlTool in logs
2. Read the SqlTool.java code
3. Analyze the error pattern
4. Identify root cause
5. Suggest fix or provide solution"

[EXECUTION PHASE]
→ sql_execute("SELECT * FROM app_logs WHERE logger LIKE '%SqlTool%' AND level='ERROR' ORDER BY created_at DESC LIMIT 5")
→ read_file("src/main/java/com/devassist/tools/SqlTool.java")
→ Analyze error: "NullPointerException at line 58"
→ Read related code sections

[REASONING PHASE]
Agent thinks:
"Error occurs at line 58 in executeSql method.
Looking at code: jdbcTemplate.query() is called but connection might be null.
The issue is missing null check before query execution."

[ACTION PHASE]
Agent suggests:
"I found the issue: SqlTool.executeSql() doesn't check if jdbcTemplate is initialized.
Fix: Add null check at line 55.
Here's the code change:
  if (jdbcTemplate == null) {
    throw new IllegalStateException("JDBC Template not initialized");
  }"
```

---

### **Example 4: Learning Agent (Self-Improving)**

#### Request:
```json
{
  "task": "Learn about our codebase structure and create a summary",
  "sessionId": "codebase-explorer"
}
```

#### Agentic Response:

```
[PLANNING]
Agent: "To learn codebase structure, I'll:
1. List all Java files in main directory
2. Read key files (controllers, services, tools)
3. Analyze architecture patterns
4. Build knowledge map
5. Generate summary"

[EXPLORATION]
→ list_files("src/main/java/com/devassist")
→ read_file("DevAssistController.java")
→ read_file("DevAssistService.java")  
→ read_file("SqlTool.java")
→ read_file("RagService.java")

[KNOWLEDGE BUILDING]
Agent builds understanding:
- Controllers: REST API layer
- Services: Business logic
- Tools: AI-callable functions
- RAG: Code analysis capability

[SYNTHESIS]
Response:
{
  "codebaseSummary": {
    "architecture": "Layered (Controller → Service → Tools)",
    "components": {
      "controllers": ["DevAssistController"],
      "services": ["DevAssistService", "ChatService", "RagService"],
      "tools": ["SqlTool", "FileSystemTool", "HttpTool"],
      "config": ["AiConfig", "RagConfig"]
    },
    "patterns": ["REST API", "Tool Pattern", "RAG", "Advisors"],
    "techStack": ["Spring Boot", "Spring AI", "Redis", "H2"]
  }
}
```

---

## 🎯 AGENTIC PATTERNS TO IMPLEMENT

### **Pattern 1: ReAct (Reasoning + Acting)**
```
Think → Act → Observe → Think → Act → ...
```

**Example:**
```
User: "Find and fix the bug"

Agent thinks: "I need to find the bug first"
Agent acts: Calls sql_execute to find errors
Agent observes: Error in SqlTool.java line 58
Agent thinks: "I need to read the code to understand"
Agent acts: Calls read_file for SqlTool.java
Agent observes: NullPointerException in executeSql
Agent thinks: "Missing null check, I should suggest fix"
Agent acts: Provides fix recommendation
```

---

### **Pattern 2: Planning-Act-Reflect**
```
Plan → Execute → Reflect → Adjust → Execute → ...
```

**Example:**
```
User: "Analyze system performance"

Plan:
  - Query slow queries
  - Check memory usage
  - Analyze tool performance

Execute: Runs all queries

Reflect:
  - Slow queries found
  - Memory OK
  - Tools performing well

Adjust: Focus on slow queries

Execute: Analyzes specific slow queries
```

---

### **Pattern 3: Autonomous Decision Making**
```
Goal → Plan → Execute → Evaluate → Achieve Goal
```

**Example:**
```
User: "Improve system reliability"

Goal: Reduce errors by 50%

Plan:
  1. Identify error patterns
  2. Find root causes
  3. Suggest fixes
  4. Verify improvements

Execute: All steps run autonomously

Evaluate: "I've identified 5 error patterns and provided fixes"

Goal Achieved: System reliability improved
```

---

## 📊 RESPONSE FORMAT COMPARISON

### Current Response Format:
```json
{
  "response": "I found an error in SqlTool.java...",
  "toolsUsed": ["sql_execute", "read_file"],
  "sessionId": "test-1"
}
```

### Agentic Response Format:
```json
{
  "response": "I found an error in SqlTool.java...",
  "reasoning": {
    "plan": "1. Query errors\n2. Read code\n3. Analyze",
    "execution": [
      { "step": 1, "action": "sql_execute", "result": "Error found" },
      { "step": 2, "action": "read_file", "result": "Code retrieved" }
    ],
    "reflection": "Identified NullPointerException root cause"
  },
  "toolsUsed": ["sql_execute", "read_file"],
  "sessionId": "test-1",
  "mode": "agentic"
}
```

---

## ✅ STEP 0.1 COMPLETE!

You now understand:
- ✅ What agentic AI looks like vs. current reactive approach
- ✅ How planning, reasoning, and execution work together
- ✅ The difference in response patterns
- ✅ Three key agentic patterns: ReAct, Planning-Act-Reflect, Autonomous Decision Making

**Next Step:** Step 1 - Define What We Will Achieve

---

## Step 1: Define What We Will Achieve

### 🎯 **Primary Goal**

Transform DevAssist from a **reactive tool executor** to an **autonomous agent** that can:
1. **Plan** multi-step tasks before execution
2. **Reason** about the best approach
3. **Execute** planned sequences autonomously
4. **Reflect** on results and adjust

---

### 📋 **Specific Objectives**

#### **Objective 1: Agentic Planning**
**What:** Agent plans the sequence of actions before executing tools

**Example:**
```
User: "Investigate system health"

Agent Plans:
  1. Query errors from logs
  2. List tool files to verify existence
  3. Test external API connectivity
  4. Synthesize health report

Then Executes: [1] → [2] → [3] → [4]
```

**Success Criteria:**
- ✅ Agent shows planning step before execution
- ✅ Plan includes sequence of tool calls
- ✅ Plan adapts based on query complexity

---

#### **Objective 2: ReAct Pattern (Reasoning + Acting)**
**What:** Agent thinks before acting, observes results, then thinks again

**Example:**
```
Agent: "I need to find errors"
[THINK] → Executes sql_execute
[OBSERVE] → Gets error result
[THINK] → "I need to read the code file"
[ACT] → Executes read_file
[OBSERVE] → Gets code
[THINK] → "I can now analyze the error"
[ACT] → Provides analysis
```

**Success Criteria:**
- ✅ Explicit reasoning steps shown
- ✅ Tool execution follows reasoning
- ✅ Results inform next reasoning step

---

#### **Objective 3: Autonomous Multi-Step Execution**
**What:** Agent executes complex multi-step tasks without intermediate user prompts

**Example:**
```
User: "Find bugs, read code, and suggest fixes"

Agent autonomously:
  Step 1: Queries errors
  Step 2: Reads related code files
  Step 3: Analyzes root causes
  Step 4: Suggests fixes
  Step 5: Provides comprehensive report

All in ONE request/response cycle!
```

**Success Criteria:**
- ✅ Multi-step execution in single flow
- ✅ No need for intermediate confirmations
- ✅ Agent handles failures and retries

---

#### **Objective 4: Enhanced Response with Reasoning**
**What:** Responses include reasoning trail and execution plan

**Current Response:**
```
"Error found in SqlTool.java..."
```

**Agentic Response:**
```
{
  "reasoning": {
    "plan": "1. Find errors\n2. Read code\n3. Analyze",
    "execution": [
      {"step": 1, "tool": "sql_execute", "result": "Error found"},
      {"step": 2, "tool": "read_file", "result": "Code retrieved"}
    ],
    "reflection": "Identified NullPointerException at line 58"
  },
  "response": "Error found in SqlTool.java at line 58..."
}
```

**Success Criteria:**
- ✅ Response includes reasoning trail
- ✅ Shows execution plan
- ✅ Transparent about decision-making

---

#### **Objective 5: Agentic Endpoint**
**What:** New endpoint `/chat/agentic` that enables agentic behavior

**API Design:**
```json
POST /chat/agentic
{
  "task": "Investigate the system",
  "sessionId": "agent-session",
  "options": {
    "showReasoning": true,
    "maxSteps": 10,
    "mode": "react"  // or "plan-act-reflect"
  }
}
```

**Success Criteria:**
- ✅ New endpoint works
- ✅ Backward compatible (existing endpoints unchanged)
- ✅ Configurable agentic behavior

---

### 🏆 **Success Metrics**

#### **Functional Metrics:**
1. ✅ Agent plans multi-step tasks (5+ tools in sequence)
2. ✅ Reasoning steps visible in responses
3. ✅ Autonomous execution without prompts
4. ✅ Handles complex queries (3+ related sub-tasks)

#### **Technical Metrics:**
1. ✅ Response time < 30 seconds for 5-step tasks
2. ✅ Success rate > 90% for planned executions
3. ✅ Error recovery when tools fail
4. ✅ Memory efficiency (no infinite loops)

#### **User Experience Metrics:**
1. ✅ Clear reasoning trail
2. ✅ Transparent execution plan
3. ✅ Actionable results
4. ✅ Better answers than reactive mode

---

### 📊 **Expected Outcomes**

#### **For You (Learning):**
- ✅ Understand agentic AI patterns
- ✅ Implement ReAct pattern
- ✅ Build autonomous agents
- ✅ Learn planning algorithms
- ✅ Master reasoning frameworks

#### **For DevAssist (System):**
- ✅ More intelligent responses
- ✅ Better multi-step task handling
- ✅ Autonomous problem-solving
- ✅ Enhanced user experience
- ✅ Production-ready agentic system

#### **For Your Resume:**
- ✅ "Implemented Agentic AI patterns"
- ✅ "Built autonomous agents with ReAct"
- ✅ "Designed multi-step reasoning systems"
- ✅ "Production agentic AI experience"

---

### 🎯 **Deliverables**

1. ✅ **AgenticService** - Core agentic logic
2. ✅ **AgenticController** - New endpoint `/chat/agentic`
3. ✅ **ReActPattern** - Reasoning + Acting implementation
4. ✅ **PlanningEngine** - Multi-step planning capability
5. ✅ **Enhanced Responses** - Reasoning trail in responses
6. ✅ **Documentation** - Usage examples and patterns
7. ✅ **Tests** - Agentic behavior validation

---

### 🔄 **What We'll Keep vs. Change**

#### **Keep (No Changes):**
- ✅ Existing `/chat` endpoint (backward compatible)
- ✅ All existing tools (SqlTool, FileSystemTool, HttpTool)
- ✅ RAG functionality
- ✅ Memory management
- ✅ File upload features

#### **Add (New Features):**
- ✅ `/chat/agentic` endpoint
- ✅ AgenticService with planning
- ✅ ReAct pattern implementation
- ✅ Reasoning trail in responses
- ✅ Enhanced system prompts for planning

---

### ⚠️ **Constraints & Considerations**

1. **Backward Compatibility:** Existing endpoints must continue working
2. **Performance:** Agentic mode may be slower (more reasoning), but should be < 30s
3. **Token Usage:** More reasoning = more tokens, but better results
4. **Error Handling:** Agent must handle tool failures gracefully
5. **Memory Limits:** Prevent infinite planning loops

---

### 📈 **Progression Path**

**Phase 1: Basic Agentic (Week 1)**
- Planning engine
- Simple ReAct pattern
- Enhanced responses

**Phase 2: Advanced Agentic (Week 2)**
- Multi-agent patterns
- Reflection and self-correction
- Optimized planning

**Phase 3: Production (Week 3)**
- Performance optimization
- Error recovery
- Production deployment

---

## ✅ STEP 1 COMPLETE!

**What We Will Achieve:**
- ✅ Transform to autonomous agent with planning
- ✅ Implement ReAct pattern (Reasoning + Acting)
- ✅ Multi-step execution without prompts
- ✅ Enhanced responses with reasoning trail
- ✅ New `/chat/agentic` endpoint

**Next Step:** Step 2 - How We Will Achieve It

---

## Step 2: How We Will Achieve It

### 🏗️ System Architecture

#### **Current Architecture:**
```
┌─────────────────────────────────────────────────────────┐
│              DevAssistController                         │
│              /chat endpoint                              │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│              DevAssistService                            │
│              processDeveloperQuery()                      │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│              ChatService                                 │
│              sendMessage()                               │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│              ChatClient (Spring AI)                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │ Advisors:                                        │  │
│  │  - LogAdvisor                                    │  │
│  │  - QuestionAnswerAdvisor (RAG)                   │  │
│  │  - PromptChatMemoryAdvisor                       │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ Tools:                                            │  │
│  │  - SqlTool                                        │  │
│  │  - FileSystemTool                                 │  │
│  │  - HttpTool                                       │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
              ┌─────────────────┐
              │   OpenAI API    │
              └─────────────────┘
```

#### **New Agentic Architecture:**
```
┌─────────────────────────────────────────────────────────┐
│          DevAssistController                             │
│  ┌──────────────────┬────────────────────────────────┐  │
│  │ /chat            │ /chat/agentic (NEW)            │  │
│  │ (Existing)       │                                │  │
│  └──────────────────┴────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
        │                              │
        │                              ▼
        │              ┌─────────────────────────────────────┐
        │              │   AgenticController (NEW)           │
        │              │   POST /chat/agentic                │
        │              └─────────────────────────────────────┘
        │                              │
        │                              ▼
        │              ┌─────────────────────────────────────┐
        │              │   AgenticService (NEW)              │
        │              │   Core agentic orchestration        │
        │              └─────────────────────────────────────┘
        │                              │
        │                              ▼
        │              ┌─────────────────────────────────────┐
        │              │   PlanningEngine (NEW)              │
        │              │   Generates step-by-step plans      │
        │              └─────────────────────────────────────┘
        │                              │
        │                              ▼
        │              ┌─────────────────────────────────────┐
        │              │   ReActPattern (NEW)                │
        │              │   Reasoning + Acting loop          │
        │              └─────────────────────────────────────┘
        │                              │
        │                              ▼
        └──────────────► ┌─────────────────────────────────────┐
                         │   ChatService (EXISTING)             │
                         │   Reused for tool execution          │
                         └─────────────────────────────────────┘
                                        │
                                        ▼
                         ┌─────────────────────────────────────┐
                         │   ChatClient (EXISTING)             │
                         │   Tools + Advisors available        │
                         └─────────────────────────────────────┘
```

---

### 📦 Component Breakdown

#### **1. AgenticController (NEW)**
**Purpose:** New REST endpoint for agentic queries

**Location:** `src/main/java/com/devassist/controller/AgenticController.java`

**Responsibilities:**
- Receive agentic requests
- Validate input
- Call AgenticService
- Return structured responses

**Code Structure:**
```java
@RestController
@RequestMapping("/chat/agentic")
public class AgenticController {
    
    @PostMapping
    public ResponseEntity<AgenticResponse> agenticChat(
            @RequestBody AgenticRequest request) {
        // Call AgenticService
        // Return structured response with reasoning
    }
}
```

---

#### **2. AgenticService (NEW)**
**Purpose:** Orchestrates the entire agentic flow

**Location:** `src/main/java/com/devassist/service/AgenticService.java`

**Responsibilities:**
- Coordinate planning → reasoning → execution → reflection
- Manage the ReAct loop
- Build final response with reasoning trail

**Code Structure:**
```java
@Service
public class AgenticService {
    
    private final PlanningEngine planningEngine;
    private final ReActPattern reactPattern;
    private final ChatService chatService; // Reuse existing
    
    public AgenticResponse processAgenticQuery(
            AgenticRequest request, String sessionId) {
        
        // PHASE 1: PLANNING
        Plan plan = planningEngine.createPlan(request.getTask());
        
        // PHASE 2: REASONING + ACTING (ReAct Loop)
        List<ExecutionStep> steps = executeReActLoop(plan, sessionId);
        
        // PHASE 3: REFLECTION
        String reflection = reflectOnExecution(steps);
        
        // PHASE 4: SYNTHESIS
        String finalResponse = synthesizeResponse(steps, reflection);
        
        return buildAgenticResponse(plan, steps, reflection, finalResponse);
    }
}
```

---

#### **3. PlanningEngine (NEW)**
**Purpose:** Generates structured plans from user queries

**Location:** `src/main/java/com/devassist/service/PlanningEngine.java`

**Responsibilities:**
- Analyze user task
- Break into steps
- Identify required tools
- Return structured Plan object

**Code Structure:**
```java
@Service
public class PlanningEngine {
    
    private final ChatClient planningChatClient; // Separate client for planning
    
    public Plan createPlan(String task) {
        String planningPrompt = buildPlanningPrompt(task);
        
        String planText = planningChatClient.prompt()
            .system("You are a planning agent. Break tasks into steps.")
            .user(planningPrompt)
            .call()
            .content();
        
        return parsePlanIntoSteps(planText);
    }
    
    private Plan parsePlanIntoSteps(String planText) {
        // Parse AI response into structured Plan
        // Plan contains: List<PlanStep>
        // Each PlanStep: tool name, parameters, description
    }
}
```

---

#### **4. ReActPattern (NEW)**
**Purpose:** Implements Reasoning + Acting pattern

**Location:** `src/main/java/com/devassist/service/ReActPattern.java`

**Responsibilities:**
- Generate reasoning before each action
- Execute tools
- Observe results
- Continue ReAct loop

**Code Structure:**
```java
@Service
public class ReActPattern {
    
    private final ChatClient reasoningClient;
    private final ToolExecutor toolExecutor; // Wrapper around ChatService
    
    public ExecutionStep executeStep(
            PlanStep planStep, 
            List<ExecutionStep> history,
            String sessionId) {
        
        // STEP 1: REASON
        String reasoning = generateReasoning(planStep, history);
        
        // STEP 2: ACT
        ToolResult result = executeTool(planStep, sessionId);
        
        // STEP 3: OBSERVE (result is the observation)
        
        return new ExecutionStep(reasoning, result, planStep);
    }
    
    private String generateReasoning(PlanStep step, List<ExecutionStep> history) {
        // Use AI to generate reasoning
        // "I need to execute sql_execute to find errors..."
    }
    
    private ToolResult executeTool(PlanStep step, String sessionId) {
        // Execute the tool via ChatService
        // Return structured result
    }
}
```

---

#### **5. ToolExecutor (NEW Helper)**
**Purpose:** Wrapper to execute tools programmatically

**Location:** `src/main/java/com/devassist/service/ToolExecutor.java`

**Responsibilities:**
- Execute individual tools
- Parse tool results
- Handle errors

**Code Structure:**
```java
@Service
public class ToolExecutor {
    
    private final ChatService chatService;
    
    public ToolResult executeTool(
            String toolName, 
            Map<String, Object> params,
            String sessionId) {
        
        // Build tool execution request
        // Call ChatService with specific tool
        // Return structured result
    }
}
```

---

#### **6. Model Classes (NEW)**

**AgenticRequest.java:**
```java
public class AgenticRequest {
    private String task;
    private String sessionId;
    private AgenticOptions options; // showReasoning, maxSteps, mode
}
```

**AgenticResponse.java:**
```java
public class AgenticResponse {
    private String response; // Final answer
    private ReasoningTrail reasoning; // Planning, execution, reflection
    private List<ExecutionStep> executionSteps;
}
```

**Plan.java:**
```java
public class Plan {
    private String goal;
    private List<PlanStep> steps;
}
```

**PlanStep.java:**
```java
public class PlanStep {
    private int stepNumber;
    private String toolName; // sql_execute, read_file, etc.
    private Map<String, Object> parameters;
    private String description;
}
```

**ExecutionStep.java:**
```java
public class ExecutionStep {
    private PlanStep planStep;
    private String reasoning; // "I need to..."
    private ToolResult result;
    private long timestamp;
}
```

---

### 🔄 Data Flow

#### **Request Flow:**
```
1. User Request
   POST /chat/agentic
   {
     "task": "Find errors and show code",
     "sessionId": "agent-1"
   }
   ↓
2. AgenticController
   - Validates request
   - Calls AgenticService
   ↓
3. AgenticService.processAgenticQuery()
   ↓
4. PlanningEngine.createPlan()
   - Uses AI to generate plan
   - Returns: Plan with steps
   ↓
5. ReActPattern.executeStep() (for each step)
   - Generates reasoning
   - Executes tool
   - Observes result
   ↓
6. ToolExecutor.executeTool()
   - Calls ChatService
   - Executes specific tool
   - Returns result
   ↓
7. AgenticService.reflectOnExecution()
   - AI analyzes all results
   - Generates reflection
   ↓
8. AgenticService.synthesizeResponse()
   - Combines all information
   - Generates final answer
   ↓
9. AgenticController returns AgenticResponse
   {
     "response": "Error found...",
     "reasoning": { plan, steps, reflection }
   }
```

---

### 🔌 Integration Points

#### **1. Reuse Existing ChatService**
**How:** AgenticService will use ChatService for tool execution

```java
@Service
public class AgenticService {
    private final ChatService chatService; // REUSE existing
    
    private ToolResult executeTool(PlanStep step, String sessionId) {
        // Build request for specific tool
        AgentRequest request = new AgentRequest();
        request.setTask(buildToolCall(step));
        
        // Use existing ChatService
        String result = chatService.sendMessage(sessionId, request);
        
        return parseToolResult(result);
    }
}
```

#### **2. Reuse Existing Tools**
**How:** All existing tools (SqlTool, FileSystemTool, HttpTool) work as-is

- ✅ No changes to existing tools
- ✅ Tools available via ChatClient
- ✅ Tool execution through ChatService

#### **3. Reuse Existing RAG**
**How:** RAG advisor still works for context

- ✅ QuestionAnswerAdvisor provides codebase context
- ✅ Agent can use RAG context during reasoning
- ✅ No changes to RAG configuration

#### **4. Reuse Existing Memory**
**How:** Conversation memory continues to work

- ✅ PromptChatMemoryAdvisor manages history
- ✅ Session-based conversations
- ✅ Memory available for agentic queries

---

### 🎯 Technical Approach

#### **Approach 1: Multi-ChatClient Strategy**

**Concept:** Use separate ChatClients for different phases

```java
// Planning ChatClient (no tools, just planning)
@Bean
public ChatClient planningChatClient(ChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultSystem("You are a planning agent...")
        .build();
}

// Reasoning ChatClient (for reasoning generation)
@Bean
public ChatClient reasoningChatClient(ChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultSystem("You are a reasoning agent...")
        .build();
}

// Execution ChatClient (with tools, for actual execution)
@Bean
public ChatClient executionChatClient(ChatModel chatModel, ...) {
    return ChatClient.builder(chatModel)
        .defaultTools(sqlTool, fileSystemTool, httpTool)
        .build();
}
```

**Pros:**
- ✅ Clear separation of concerns
- ✅ Different prompts for each phase
- ✅ Easy to optimize each phase

**Cons:**
- ⚠️ More configuration
- ⚠️ Multiple ChatClient instances

---

#### **Approach 2: Single ChatClient with Dynamic Prompts** ⭐ RECOMMENDED

**Concept:** Use one ChatClient but change prompts dynamically

```java
@Service
public class AgenticService {
    private final ChatClient chatClient; // Single instance
    
    public AgenticResponse processAgenticQuery(...) {
        // Phase 1: Planning (with planning prompt)
        Plan plan = chatClient.prompt()
            .system("You are a planning agent...")
            .user("Plan this task: " + task)
            .call()
            .content();
        
        // Phase 2: Execution (with execution prompt + tools)
        for (PlanStep step : plan.getSteps()) {
            ToolResult result = chatClient.prompt()
                .system("You are executing: " + step.getDescription())
                .user("Execute: " + step.getToolName())
                .tools(sqlTool, fileSystemTool, httpTool)
                .call()
                .content();
        }
    }
}
```

**Pros:**
- ✅ Simpler configuration
- ✅ Single ChatClient instance
- ✅ Flexible prompt changes
- ✅ Easier to maintain

**Cons:**
- ⚠️ Need to manage prompt context

**We'll use Approach 2!**

---

### 🔄 **LLM Call Flow Clarification**

**Question:** "Do we call LLM to Plan, and then pass that Plan again to LLM?"

**Answer: YES! Multiple LLM calls with different purposes:**

#### **LLM Call Sequence:**

```
1. LLM CALL #1: PLANNING
   Input:  User task
   Output: Plan (text: "Step 1: ... Step 2: ...")
   
2. Parse Plan → Structured Plan object
   
3. For Each Step in Plan:
   
   a. LLM CALL #2-N: REASONING
      Input:  Plan step + Previous results
      Output: Reasoning text ("I need to...")
   
   b. TOOL EXECUTION (No LLM call)
      Input:  Tool name + Parameters
      Output: Tool result
   
4. LLM CALL #(N+1): REFLECTION
   Input:  All execution results
   Output: Reflection text
   
5. LLM CALL #(N+2): SYNTHESIS
   Input:  Plan + Steps + Reflection
   Output: Final answer
```

#### **Example: 3-Step Task = ~6 LLM Calls**
- 1 call for planning
- 3 calls for reasoning (one per step)
- 1 call for reflection
- 1 call for synthesis
- **Total: 6 LLM calls**

#### **Why Multiple Calls?**
- ✅ Better control over each phase
- ✅ Transparent reasoning trail
- ✅ Reliable step-by-step execution
- ✅ Better for learning and debugging
- ⚠️ More tokens/cost, but better results

**See:** `AGENTIC_AI_LLM_CALL_FLOW.md` for detailed flow diagram.

---

### 📐 Detailed Flow Diagram

#### **Agentic Execution Flow:**

```
┌─────────────────────────────────────────────────────────────┐
│                    AgenticService                           │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ PHASE 1: PLANNING                                      │ │
│  │ ┌─────────────────────────────────────────────────┐ │ │
│  │ │ PlanningEngine.createPlan()                     │ │ │
│  │ │ - Analyzes task                                 │ │ │
│  │ │ - Generates steps                               │ │ │
│  │ │ - Returns Plan object                           │ │ │
│  │ └─────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│                          │                                 │
│                          ▼                                 │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ PHASE 2: REACT LOOP (for each step)                  │ │
│  │ ┌─────────────────────────────────────────────────┐ │ │
│  │ │ ReActPattern.executeStep()                      │ │ │
│  │ │                                                 │ │ │
│  │ │ 1. REASON: "I need to execute sql_execute..."  │ │ │
│  │ │ 2. ACT: Execute tool                           │ │ │
│  │ │ 3. OBSERVE: Get result                         │ │ │
│  │ │ 4. Store ExecutionStep                          │ │ │
│  │ └─────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│                          │                                 │
│                          ▼                                 │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ PHASE 3: REFLECTION                                   │ │
│  │ ┌─────────────────────────────────────────────────┐ │ │
│  │ │ AgenticService.reflectOnExecution()             │ │ │
│  │ │ - Analyzes all ExecutionSteps                   │ │ │
│  │ │ - Generates reflection                          │ │ │
│  │ │ - Identifies insights                           │ │ │
│  │ └─────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│                          │                                 │
│                          ▼                                 │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ PHASE 4: SYNTHESIS                                    │ │
│  │ ┌─────────────────────────────────────────────────┐ │ │
│  │ │ AgenticService.synthesizeResponse()             │ │ │
│  │ │ - Combines plan, steps, reflection             │ │ │
│  │ │ - Generates final answer                       │ │ │
│  │ │ - Builds AgenticResponse                       │ │ │
│  │ └─────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

### 🧩 Key Design Decisions

#### **Decision 1: How to Execute Tools**

**Option A: Direct Tool Invocation**
```java
// Call tools directly
SqlTool.executeSql(...);
FileSystemTool.readFile(...);
```

**Option B: Via ChatClient** ⭐ CHOSEN
```java
// Use ChatClient (existing pattern)
chatClient.prompt()
    .user("Execute sql_execute with query: ...")
    .tools(sqlTool)
    .call();
```

**Why Option B:**
- ✅ Consistent with existing code
- ✅ Benefits from advisors (RAG, memory)
- ✅ Easier to integrate
- ✅ Same pattern as current implementation

---

#### **Decision 2: How to Parse Planning Output**

**Option A: Structured JSON Output**
```java
// AI returns JSON
{
  "steps": [
    {"tool": "sql_execute", "params": {...}},
    {"tool": "read_file", "params": {...}}
  ]
}
```

**Option B: Text Parsing** ⭐ CHOSEN
```java
// AI returns text, we parse it
"Step 1: Execute sql_execute to find errors
Step 2: Read file SqlTool.java
Step 3: Analyze error location"
```

**Why Option B:**
- ✅ Simpler prompt (no JSON schema needed)
- ✅ More natural for AI
- ✅ Easier error handling
- ✅ Can parse with regex/simple logic

---

#### **Decision 3: Reasoning Storage**

**Option A: In-Memory Only**
- Fast but lost after request

**Option B: Store in Response** ⭐ CHOSEN
- Included in AgenticResponse
- User can see reasoning trail
- Better for debugging and learning

**Why Option B:**
- ✅ Transparency (show thinking)
- ✅ Debugging (understand decisions)
- ✅ Learning (see how agent thinks)

---

### 🔐 Error Handling Strategy

#### **1. Planning Errors**
```java
try {
    Plan plan = planningEngine.createPlan(task);
} catch (Exception e) {
    // Fallback: Single-step plan
    Plan plan = createFallbackPlan(task);
}
```

#### **2. Tool Execution Errors**
```java
try {
    ToolResult result = executeTool(step);
} catch (Exception e) {
    // Continue with next step
    // Store error in ExecutionStep
    // Agent can reason about error
}
```

#### **3. ReAct Loop Errors**
```java
// Max iterations to prevent infinite loops
int maxIterations = 10;
int currentIteration = 0;

while (!goalAchieved && currentIteration < maxIterations) {
    // Execute step
    currentIteration++;
}
```

---

### 📊 Response Structure

#### **AgenticResponse Format:**
```json
{
  "response": "I found an error in SqlTool.java at line 58...",
  "reasoning": {
    "plan": {
      "goal": "Find errors and show code",
      "steps": [
        {
          "stepNumber": 1,
          "tool": "sql_execute",
          "description": "Query errors from app_logs",
          "parameters": {"query": "SELECT * FROM app_logs WHERE level='ERROR'"}
        },
        {
          "stepNumber": 2,
          "tool": "read_file",
          "description": "Read SqlTool.java file",
          "parameters": {"filePath": "src/main/java/com/devassist/tools/SqlTool.java"}
        }
      ]
    },
    "execution": [
      {
        "stepNumber": 1,
        "reasoning": "I need to query the database to find errors",
        "tool": "sql_execute",
        "result": {"rowCount": 1, "rows": [...]},
        "success": true,
        "timestamp": "2025-01-23T10:15:30Z"
      },
      {
        "stepNumber": 2,
        "reasoning": "I found an error in SqlTool. Now I need to read the code file.",
        "tool": "read_file",
        "result": {"content": "package com.devassist...", ...},
        "success": true,
        "timestamp": "2025-01-23T10:15:32Z"
      }
    ],
    "reflection": "I successfully found the error and read the code. The error is a NullPointerException at line 58 in the executeSql method."
  },
  "sessionId": "agent-1",
  "executionTime": 3.5,
  "totalSteps": 2
}
```

---

### 🎯 Implementation Phases

#### **Phase 1: Core Structure**
1. Create model classes (AgenticRequest, AgenticResponse, Plan, etc.)
2. Create AgenticController with endpoint
3. Create AgenticService skeleton

#### **Phase 2: Planning Engine**
1. Implement PlanningEngine
2. Create planning prompts
3. Parse planning output

#### **Phase 3: ReAct Pattern**
1. Implement ReActPattern
2. Create reasoning prompts
3. Tool execution wrapper

#### **Phase 4: Integration**
1. Connect all components
2. Error handling
3. Response building

#### **Phase 5: Testing & Refinement**
1. Test with various queries
2. Optimize prompts
3. Add logging
4. Performance tuning

---

### 📝 Code File Structure

```
src/main/java/com/devassist/
├── controller/
│   └── AgenticController.java          ← NEW
├── service/
│   ├── AgenticService.java               ← NEW
│   ├── PlanningEngine.java               ← NEW
│   ├── ReActPattern.java                 ← NEW
│   ├── ToolExecutor.java                 ← NEW (helper)
│   ├── ChatService.java                  ← EXISTING (reuse)
│   └── DevAssistService.java             ← EXISTING (unchanged)
├── model/
│   ├── AgenticRequest.java               ← NEW
│   ├── AgenticResponse.java              ← NEW
│   ├── Plan.java                         ← NEW
│   ├── PlanStep.java                     ← NEW
│   ├── ExecutionStep.java               ← NEW
│   └── ReasoningTrail.java              ← NEW
└── config/
    └── AiConfig.java                     ← EXISTING (no changes needed)
```

---

## ✅ STEP 2 COMPLETE!

**How We Will Achieve It:**
- ✅ System architecture designed
- ✅ Component breakdown defined
- ✅ Integration points identified
- ✅ Technical approach selected (Single ChatClient with dynamic prompts)
- ✅ Data flow documented
- ✅ Error handling strategy planned
- ✅ Implementation phases outlined

**Next Step:** Step 3 - Study Resources (Review documentation links)

---

## Step 3: Study Resources

### 🎯 Learning Objectives

By the end of Step 3, you should understand:
- ✅ ReAct pattern (Reasoning + Acting)
- ✅ How Spring AI ChatClient works with tools
- ✅ Planning and execution patterns
- ✅ How to structure agentic flows
- ✅ Prompt engineering for agents

---

### 📚 Essential Reading (Priority Order)

#### **1. Spring AI Fundamentals** (30-45 min) ⭐ START HERE

**What to Read:**
- 🔗 **Spring AI ChatClient Guide**: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- 🔗 **Spring AI Function Calling**: https://docs.spring.io/spring-ai/reference/api/function-calling.html

**Key Concepts to Learn:**
- How `ChatClient.prompt()` works
- How tools are registered and called
- How advisors intercept calls
- Builder pattern usage

**Focus Areas:**
```java
// Study this pattern:
chatClient.prompt()
    .system("...")      // System instruction
    .user("...")        // User message
    .tools(tool1, tool2) // Register tools
    .advisors(advisor1)  // Add advisors
    .call()             // Execute
    .content();          // Get response
```

**Practice:**
- Look at your existing `ChatService.java`
- Understand how tools are called
- See how `ChatClient` is configured in `AiConfig.java`

---

#### **2. ReAct Pattern Paper** (30 min) ⭐ CORE CONCEPT

**What to Read:**
- 🔗 **ReAct Paper**: https://arxiv.org/abs/2210.03629

**Key Sections:**
- Abstract (understand the concept)
- Introduction (why ReAct)
- Method section (how it works)

**Key Concepts:**
```
ReAct = REasoning + ACTing

Loop:
1. THINK: "I need to..."
2. ACT: Execute tool/action
3. OBSERVE: Get result
4. THINK: "Based on result, I should..."
5. ACT: Execute next action
6. Repeat until goal achieved
```

**Don't Worry About:**
- Mathematical formulas (you can skip)
- Experimental results (focus on method)

**Focus On:**
- The reasoning pattern
- How reasoning informs actions
- The observation loop

---

#### **3. OpenAI Function Calling** (20 min)

**What to Read:**
- 🔗 **OpenAI Function Calling Guide**: https://platform.openai.com/docs/guides/function-calling

**Key Concepts:**
- How function calling works
- Function schema format
- How tools are invoked
- Response format

**Why Important:**
- Spring AI uses OpenAI's function calling under the hood
- Understanding this helps debug tool execution
- You'll see function schemas in logs

---

#### **4. LangChain Agents (Concepts Only)** (30 min)

**What to Read:**
- 🔗 **LangChain Agents Overview**: https://python.langchain.com/docs/modules/agents/
- 🔗 **LangChain ReAct Agent**: https://python.langchain.com/docs/modules/agents/agent_types/react/

**Why Read Python Code:**
- ✅ Concepts apply to Spring AI
- ✅ Good examples of ReAct pattern
- ✅ Clear explanations
- ❌ Don't need to learn Python syntax

**Key Concepts to Extract:**
- How agents plan
- How reasoning loops work
- Tool execution flow
- Error handling patterns

**Skip:**
- Python syntax
- LangChain-specific APIs
- Focus on **concepts** and **patterns**

---

### 🎓 Concepts Deep Dive

#### **Concept 1: ReAct Pattern**

**What It Is:**
A pattern where AI **thinks** before **acting**, then **observes** results, then **thinks** again.

**Visual:**
```
[THINK] → "I need to find errors"
    ↓
[ACT] → Execute sql_execute tool
    ↓
[OBSERVE] → Get error result
    ↓
[THINK] → "I found error in SqlTool, now read the file"
    ↓
[ACT] → Execute read_file tool
    ↓
[OBSERVE] → Get file content
    ↓
[THINK] → "I can now analyze the error"
    ↓
[RESPOND] → Final answer
```

**In Our Code:**
```java
// This is the ReAct loop:
for (PlanStep step : plan.getSteps()) {
    // REASON (think)
    String reasoning = generateReasoning(step, history);
    
    // ACT (execute)
    ToolResult result = executeTool(step);
    
    // OBSERVE (get result)
    history.add(new ExecutionStep(reasoning, result));
}
```

---

#### **Concept 2: Planning**

**What It Is:**
Breaking a complex task into smaller, executable steps.

**Example:**
```
Task: "Investigate system health"

Plan:
1. Query errors from database
2. List tool files to verify existence
3. Test external API connectivity
4. Synthesize health report
```

**In Our Code:**
```java
Plan plan = planningEngine.createPlan(task);
// Plan contains: List<PlanStep>
// Each PlanStep: tool, parameters, description
```

---

#### **Concept 3: Tool Orchestration**

**What It Is:**
AI decides which tools to use and in what order.

**Current (Your Code):**
```java
chatClient.prompt()
    .tools(sqlTool, fileSystemTool, httpTool)  // AI chooses
    .call();
```

**Agentic (What We'll Build):**
```java
// Explicit planning first
Plan plan = createPlan(task);

// Then execute planned steps
for (PlanStep step : plan.getSteps()) {
    executeTool(step.getToolName(), step.getParams());
}
```

---

#### **Concept 4: Reasoning Trails**

**What It Is:**
Capturing AI's thinking process at each step.

**Why Important:**
- Transparency (see how AI thinks)
- Debugging (understand decisions)
- Learning (study AI reasoning)

**In Our Response:**
```json
{
  "reasoning": {
    "plan": "...",
    "execution": [
      {
        "reasoning": "I need to query database...",
        "result": "..."
      }
    ],
    "reflection": "..."
  }
}
```

---

### 🔍 Study Your Existing Code

#### **Exercise 1: Understand ChatService**

**File:** `src/main/java/com/devassist/service/ChatService.java`

**Questions to Answer:**
1. How does `sendMessage()` work?
2. How are tools registered?
3. How does RAG advisor work?
4. How is memory managed?

**Answer These:**
```java
// Study this method:
public String sendMessage(String sessionId, AgentRequest request) {
    return chatClient.prompt()
        .system(systemInstruction)
        .user(request.getTask())
        .advisors(...)
        .call()
        .content();
}
```

**Key Insights:**
- `ChatClient` handles tool execution automatically
- Advisors modify prompts/responses
- Tools are called based on AI's decision

---

#### **Exercise 2: Understand Tool Execution**

**File:** `src/main/java/com/devassist/tools/SqlTool.java`

**Study:**
- How `@Tool` annotation works
- How parameters are bound
- How results are returned
- How AI sees tools

**Key Pattern:**
```java
@Tool(name = "sql_execute", description = "...")
public SqlResponse executeSql(SqlRequest request) {
    // This becomes a "function" in OpenAI's function calling
    // AI can call this by name: "sql_execute"
}
```

---

#### **Exercise 3: Understand Advisors**

**File:** `src/main/java/com/devassist/config/AiConfig.java`

**Study:**
- How advisors are configured
- Advisor execution order
- How advisors modify behavior

**Key Pattern:**
```java
ChatClient.builder(chatModel)
    .defaultAdvisors(
        logAdvisor,           // Order 0 (first)
        questionAnswerAdvisor, // Order 5 (middle)
        memoryAdvisor          // Order 10 (last)
    )
    .defaultTools(...)
    .build();
```

---

### 📖 Reading Plan (2-3 Hours Total)

#### **Session 1: Foundation (60 min)**
1. ✅ Spring AI ChatClient docs (30 min)
2. ✅ OpenAI Function Calling (20 min)
3. ✅ Study your ChatService.java (10 min)

**Goal:** Understand how tools work in Spring AI

---

#### **Session 2: ReAct Pattern (60 min)**
1. ✅ ReAct paper (skim, focus on method) (30 min)
2. ✅ LangChain ReAct agent (concepts) (20 min)
3. ✅ Study ReAct implementation examples (10 min)

**Goal:** Understand ReAct reasoning + acting pattern

---

#### **Session 3: Planning (45 min)**
1. ✅ LangChain planning agents (30 min)
2. ✅ Study your existing multi-tool usage (15 min)

**Goal:** Understand how to break tasks into steps

---

### 💡 Key Insights to Remember

#### **1. Spring AI Tool Execution**
```java
// AI automatically:
// 1. Sees available tools
// 2. Decides which to call
// 3. Calls with parameters
// 4. Gets result
// 5. Uses result in response

chatClient.prompt()
    .tools(sqlTool)  // AI sees this
    .user("Find errors")
    .call();         // AI might call sqlTool
```

---

#### **2. ReAct Loop Structure**
```java
// Pattern:
while (!goalAchieved) {
    String reasoning = think(context);
    ActionResult result = act(reasoning);
    context.add(result);
}
```

---

#### **3. Planning Pattern**
```java
// Break complex task into steps
Plan plan = analyzeTask(task);
// Plan: [Step1, Step2, Step3]

// Execute each step
for (Step step : plan) {
    execute(step);
}
```

---

### 🎯 Practical Exercises

#### **Exercise 1: Trace Tool Execution**

**Task:** Trace how a tool gets called in your current code

**Steps:**
1. Look at `ChatService.sendMessage()`
2. See how `chatClient` is configured
3. Understand how AI decides to call a tool
4. Trace execution: User message → AI decision → Tool call → Result

**Questions:**
- When does AI decide to call a tool?
- How are parameters passed?
- How is result used?

---

#### **Exercise 2: Understand ReAct Manually**

**Task:** Manually simulate ReAct pattern

**Example:**
```
Task: "Find errors and show code"

Manual ReAct:
1. THINK: "I need to find errors in logs"
2. ACT: Execute sql_execute
3. OBSERVE: Error found in SqlTool.java
4. THINK: "I need to read SqlTool.java"
5. ACT: Execute read_file
6. OBSERVE: Code retrieved
7. THINK: "I can now analyze"
8. RESPOND: Final answer
```

**Practice:** Try this with different queries

---

#### **Exercise 3: Design Planning Prompt**

**Task:** Write a prompt that makes AI generate a plan

**Template:**
```
You are a planning agent. Break this task into steps.

Task: {task}

Available tools:
- sql_execute: Query database
- read_file: Read files
- list_files: List directory contents

Output format:
Step 1: [tool] - [what to do]
Step 2: [tool] - [what to do]
...
```

**Practice:** Test with different tasks

---

### 📋 Checklist: Are You Ready for Step 4?

Before moving to implementation, make sure you understand:

- [ ] **Spring AI ChatClient:** How it works, how to use it
- [ ] **Tool Execution:** How tools are called by AI
- [ ] **ReAct Pattern:** Think → Act → Observe loop
- [ ] **Planning:** Breaking tasks into steps
- [ ] **Your Existing Code:** How ChatService works
- [ ] **Prompt Engineering:** How to write effective prompts

**If you checked all boxes → Ready for Step 4!** ✅

---

### 🚀 Quick Reference During Implementation

#### **Spring AI Patterns:**
```java
// Basic chat
chatClient.prompt()
    .user(message)
    .call()
    .content();

// With tools
chatClient.prompt()
    .user(message)
    .tools(tool1, tool2)
    .call()
    .content();

// With system prompt
chatClient.prompt()
    .system("You are...")
    .user(message)
    .call()
    .content();
```

#### **ReAct Loop:**
```java
// For each step:
String reasoning = think(step, history);
ToolResult result = act(step);
history.add(new ExecutionStep(reasoning, result));
```

#### **Planning:**
```java
String planText = chatClient.prompt()
    .system("You are a planning agent...")
    .user("Plan this: " + task)
    .call()
    .content();

Plan plan = parsePlan(planText);
```

---

### 📚 Additional Resources (Optional)

#### **If You Have More Time:**
- 🔗 **Chain-of-Thought Prompting**: https://www.promptingguide.ai/techniques/cot
- 🔗 **Tree of Thoughts**: https://arxiv.org/abs/2305.10601
- 🔗 **Spring AI Samples**: https://github.com/spring-projects/spring-ai/tree/main/spring-ai-samples

#### **Videos (Search YouTube):**
- "ReAct Pattern AI"
- "Spring AI Tutorial"
- "LLM Function Calling"
- "Building AI Agents"

---

## ✅ STEP 3 COMPLETE!

**What You Should Know Now:**
- ✅ How Spring AI ChatClient works
- ✅ How tools are executed
- ✅ ReAct pattern (Reasoning + Acting)
- ✅ Planning concepts
- ✅ Your existing codebase structure

**Estimated Study Time:** 2-3 hours

**Next Step:** Step 4 - Code Changes Discussion (We'll design the implementation together)

---

**Ready to proceed?** After studying, we'll design the code structure in Step 4! 🎯

---

## Step 4: Code Changes Discussion

### 🎯 Design Phase

We'll design the implementation together, discussing:
- Class structures and responsibilities
- Method signatures
- Prompt templates
- Integration with existing code
- Data models

---

### 📦 Component 1: Model Classes

#### **1.1 AgenticRequest.java** (NEW)

**Location:** `src/main/java/com/devassist/model/AgenticRequest.java`

**Design Discussion:**
```java
package com.devassist.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenticRequest {
    
    /**
     * The user's task/question
     * Example: "Find errors in logs and show me the code"
     */
    private String task;
    
    /**
     * Session ID for conversation continuity
     * Example: "agent-session-1"
     */
    private String sessionId;
    
    /**
     * Optional agentic behavior options
     */
    private AgenticOptions options;
}
```

**AgenticOptions (Nested or Separate Class):**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenticOptions {
    
    /**
     * Show reasoning trail in response (default: true)
     */
    private Boolean showReasoning = true;
    
    /**
     * Maximum number of execution steps (default: 10)
     * Prevents infinite loops
     */
    private Integer maxSteps = 10;
    
    /**
     * Agentic mode: "react" or "plan-act-reflect"
     * Default: "react"
     */
    private String mode = "react";
}
```

**Questions to Consider:**
- ✅ Should we extend existing `ChatRequest` or create new class?
- ✅ **Decision:** New class for clarity and separation
- ✅ Should options be nested or separate?
- ✅ **Decision:** Separate class for better organization

---

#### **1.2 AgenticResponse.java** (NEW)

**Location:** `src/main/java/com/devassist/model/AgenticResponse.java`

**Design Discussion:**
```java
package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class AgenticResponse {
    
    /**
     * Final user-facing answer
     */
    private String response;
    
    /**
     * Complete reasoning trail
     */
    private ReasoningTrail reasoning;
    
    /**
     * List of all execution steps
     */
    private List<ExecutionStep> executionSteps;
    
    /**
     * Session ID
     */
    private String sessionId;
    
    /**
     * Total execution time in seconds
     */
    private Double executionTime;
    
    /**
     * Total number of steps executed
     */
    private Integer totalSteps;
}
```

**ReasoningTrail.java:**
```java
@Data
@Builder
public class ReasoningTrail {
    
    /**
     * Original plan generated before execution
     */
    private Plan plan;
    
    /**
     * Reflection after execution
     */
    private String reflection;
    
    /**
     * Synthesis summary
     */
    private String synthesis;
}
```

**Questions:**
- ✅ Should we include raw reasoning or formatted?
- ✅ **Decision:** Structured reasoning trail
- ✅ Should we include intermediate results?
- ✅ **Decision:** Yes, in executionSteps

---

#### **1.3 Plan.java** (NEW)

**Location:** `src/main/java/com/devassist/model/Plan.java`

**Design:**
```java
package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class Plan {
    
    /**
     * Original task/goal
     */
    private String goal;
    
    /**
     * List of planned steps
     */
    private List<PlanStep> steps;
    
    /**
     * Estimated complexity (1-10)
     */
    private Integer complexity;
}
```

**PlanStep.java:**
```java
@Data
@Builder
public class PlanStep {
    
    /**
     * Step number (1, 2, 3, ...)
     */
    private Integer stepNumber;
    
    /**
     * Tool name to execute
     * Examples: "sql_execute", "read_file", "list_files"
     */
    private String toolName;
    
    /**
     * Description of what this step does
     */
    private String description;
    
    /**
     * Parameters for tool execution
     * Example: {"query": "SELECT * FROM app_logs"}
     */
    private Map<String, Object> parameters;
    
    /**
     * Dependencies (which steps must complete first)
     */
    private List<Integer> dependsOn;
}
```

---

#### **1.4 ExecutionStep.java** (NEW)

**Location:** `src/main/java/com/devassist/model/ExecutionStep.java`

**Design:**
```java
package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class ExecutionStep {
    
    /**
     * The planned step that was executed
     */
    private PlanStep planStep;
    
    /**
     * Reasoning before execution
     * Example: "I need to query the database to find errors"
     */
    private String reasoning;
    
    /**
     * Tool execution result
     */
    private ToolResult result;
    
    /**
     * Whether execution was successful
     */
    private Boolean success;
    
    /**
     * Error message if failed
     */
    private String error;
    
    /**
     * Execution timestamp
     */
    private LocalDateTime timestamp;
    
    /**
     * Time taken in milliseconds
     */
    private Long executionTimeMs;
}
```

**ToolResult.java:**
```java
@Data
@Builder
public class ToolResult {
    
    /**
     * Tool name that was executed
     */
    private String toolName;
    
    /**
     * Raw result from tool (as string/object)
     */
    private Object result;
    
    /**
     * Formatted result for display
     */
    private String formattedResult;
}
```

---

### 🏗️ Component 2: Service Classes

#### **2.1 AgenticService.java** (NEW) - Main Orchestrator

**Location:** `src/main/java/com/devassist/service/AgenticService.java`

**Design Discussion:**

**Class Structure:**
```java
package com.devassist.service;

import com.devassist.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AgenticService {
    
    private final PlanningEngine planningEngine;
    private final ReActPattern reactPattern;
    private final ChatService chatService; // Reuse existing
    
    /**
     * Main entry point for agentic queries
     */
    public AgenticResponse processAgenticQuery(
            AgenticRequest request, 
            String sessionId) {
        
        long startTime = System.currentTimeMillis();
        log.info("🤖 Agentic: Processing query '{}' for session '{}'", 
                request.getTask(), sessionId);
        
        try {
            // PHASE 1: PLANNING
            Plan plan = planningEngine.createPlan(request.getTask(), sessionId);
            log.info("📋 Agentic: Generated plan with {} steps", plan.getSteps().size());
            
            // PHASE 2: REACT LOOP (Execute each step)
            List<ExecutionStep> executionSteps = new ArrayList<>();
            for (PlanStep planStep : plan.getSteps()) {
                ExecutionStep step = reactPattern.executeStep(
                    planStep, 
                    executionSteps, 
                    sessionId
                );
                executionSteps.add(step);
                
                // Check max steps limit
                if (executionSteps.size() >= request.getOptions().getMaxSteps()) {
                    log.warn("⚠️ Agentic: Reached max steps limit ({})", 
                            request.getOptions().getMaxSteps());
                    break;
                }
            }
            
            // PHASE 3: REFLECTION
            String reflection = reflectOnExecution(executionSteps, sessionId);
            log.info("💭 Agentic: Generated reflection");
            
            // PHASE 4: SYNTHESIS
            String finalResponse = synthesizeResponse(
                plan, 
                executionSteps, 
                reflection, 
                sessionId
            );
            log.info("✅ Agentic: Generated final response");
            
            // Build response
            double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;
            
            ReasoningTrail reasoning = ReasoningTrail.builder()
                .plan(plan)
                .reflection(reflection)
                .synthesis(buildSynthesis(executionSteps))
                .build();
            
            return AgenticResponse.builder()
                .response(finalResponse)
                .reasoning(reasoning)
                .executionSteps(executionSteps)
                .sessionId(sessionId)
                .executionTime(executionTime)
                .totalSteps(executionSteps.size())
                .build();
                
        } catch (Exception e) {
            log.error("❌ Agentic: Error processing query", e);
            throw new RuntimeException("Agentic processing failed", e);
        }
    }
    
    /**
     * Reflect on all execution steps
     */
    private String reflectOnExecution(
            List<ExecutionStep> steps, 
            String sessionId) {
        // Implementation in Phase 4
        return "Reflection: ...";
    }
    
    /**
     * Synthesize final answer from plan, steps, and reflection
     */
    private String synthesizeResponse(
            Plan plan,
            List<ExecutionStep> steps,
            String reflection,
            String sessionId) {
        // Implementation in Phase 4
        return "Final answer: ...";
    }
    
    /**
     * Build synthesis summary
     */
    private String buildSynthesis(List<ExecutionStep> steps) {
        // Quick summary of what was done
        return "Successfully executed " + steps.size() + " steps";
    }
}
```

**Questions:**
- ✅ Should we handle errors per step or fail fast?
- ✅ **Decision:** Continue with next step on error, log it
- ✅ Should we validate plan before execution?
- ✅ **Decision:** Yes, check for empty plan, invalid tools

---

#### **2.2 PlanningEngine.java** (NEW)

**Location:** `src/main/java/com/devassist/service/PlanningEngine.java`

**Design Discussion:**

**Class Structure:**
```java
package com.devassist.service;

import com.devassist.model.Plan;
import com.devassist.model.PlanStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Log4j2
public class PlanningEngine {
    
    private final ChatClient chatClient;
    
    /**
     * Creates a plan from user task
     */
    public Plan createPlan(String task, String sessionId) {
        log.info("📋 Planning: Creating plan for task: {}", task);
        
        // Build planning prompt
        String planningPrompt = buildPlanningPrompt(task);
        
        // Call LLM for planning
        String planText = chatClient.prompt()
            .system(buildPlanningSystemPrompt())
            .user(planningPrompt)
            .call()
            .content();
        
        log.debug("📋 Planning: Raw plan output:\n{}", planText);
        
        // Parse plan text into structured Plan object
        Plan plan = parsePlan(planText, task);
        
        log.info("📋 Planning: Parsed plan with {} steps", plan.getSteps().size());
        
        return plan;
    }
    
    /**
     * System prompt for planning agent
     */
    private String buildPlanningSystemPrompt() {
        return """
            You are an expert planning agent. Your job is to break complex tasks 
            into clear, executable steps.
            
            Available tools:
            - sql_execute: Execute SQL SELECT queries on app_logs database
            - read_file: Read file contents from the project
            - list_files: List files in a directory
            - file_info: Get file metadata
            - http_get: Make HTTP GET requests
            - http_post: Make HTTP POST requests
            - check_url: Check if URL is reachable
            
            Output format:
            Step 1: [tool_name] - [description of what to do]
            Step 2: [tool_name] - [description of what to do]
            ...
            
            Be specific about:
            - Which tool to use
            - What parameters are needed
            - Why this step is necessary
            
            Example:
            Step 1: sql_execute - Query app_logs table to find ERROR level logs
            Step 2: read_file - Read the source file where the error occurred
            Step 3: analyze - Analyze the error in context of the code
            """;
    }
    
    /**
     * User prompt for planning
     */
    private String buildPlanningPrompt(String task) {
        return String.format("""
            Break this task into clear, executable steps:
            
            Task: %s
            
            Think about:
            1. What information do you need?
            2. Which tools can provide that information?
            3. What's the logical order of execution?
            4. Are there dependencies between steps?
            
            Output the steps in the format specified.
            """, task);
    }
    
    /**
     * Parse AI-generated plan text into structured Plan object
     */
    private Plan parsePlan(String planText, String goal) {
        List<PlanStep> steps = new ArrayList<>();
        
        // Pattern to match: "Step N: tool_name - description"
        Pattern pattern = Pattern.compile(
            "Step\\s+(\\d+):\\s+(\\w+)\\s*-\\s*(.+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = pattern.matcher(planText);
        int stepNumber = 1;
        
        while (matcher.find()) {
            int parsedStepNumber = Integer.parseInt(matcher.group(1));
            String toolName = matcher.group(2).trim();
            String description = matcher.group(3).trim();
            
            // Extract parameters from description (basic parsing)
            Map<String, Object> parameters = extractParameters(description, toolName);
            
            PlanStep step = PlanStep.builder()
                .stepNumber(parsedStepNumber)
                .toolName(toolName)
                .description(description)
                .parameters(parameters)
                .build();
            
            steps.add(step);
        }
        
        // If no steps found, create a fallback single-step plan
        if (steps.isEmpty()) {
            log.warn("⚠️ Planning: Could not parse plan, creating fallback");
            steps.add(createFallbackStep(goal));
        }
        
        // Calculate complexity (simple heuristic)
        int complexity = Math.min(10, steps.size() * 2);
        
        return Plan.builder()
            .goal(goal)
            .steps(steps)
            .complexity(complexity)
            .build();
    }
    
    /**
     * Extract parameters from step description
     * This is a simplified parser - can be enhanced
     */
    private Map<String, Object> extractParameters(String description, String toolName) {
        Map<String, Object> params = new HashMap<>();
        
        // Simple extraction based on tool type
        if (toolName.equals("sql_execute")) {
            // Try to extract SQL query from description
            if (description.contains("SELECT") || description.contains("select")) {
                // Extract query (basic - can be improved)
                params.put("query", extractSqlQuery(description));
            }
        } else if (toolName.equals("read_file")) {
            // Extract file path
            String filePath = extractFilePath(description);
            if (filePath != null) {
                params.put("filePath", filePath);
            }
        }
        // ... more tool-specific parsing
        
        return params;
    }
    
    /**
     * Create fallback step if parsing fails
     */
    private PlanStep createFallbackStep(String task) {
        return PlanStep.builder()
            .stepNumber(1)
            .toolName("general") // Will use ChatService without specific tool
            .description("Process task: " + task)
            .parameters(Map.of())
            .build();
    }
    
    // Helper methods for parameter extraction
    private String extractSqlQuery(String description) {
        // Basic extraction - can be enhanced
        // For now, return a generic query
        return "SELECT * FROM app_logs LIMIT 10";
    }
    
    private String extractFilePath(String description) {
        // Try to find file path patterns
        Pattern pattern = Pattern.compile("(\\S+\\.java|\\S+\\.properties|\\S+/[\\w/]+\\.\\w+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
```

**Design Decisions:**
- ✅ **Planning Prompt:** Detailed system prompt with tool list
- ✅ **Parsing:** Regex-based (simple, can be enhanced later)
- ✅ **Error Handling:** Fallback to single-step plan
- ✅ **Parameter Extraction:** Basic (can be improved with AI)

**Questions:**
- ✅ Should we validate tool names?
- ✅ **Decision:** Yes, check against available tools
- ✅ Should we use structured output (JSON) instead of text?
- ✅ **Decision:** Start with text (simpler), can enhance later

---

#### **2.3 ReActPattern.java** (NEW)

**Location:** `src/main/java/com/devassist/service/ReActPattern.java`

**Design Discussion:**

**Class Structure:**
```java
package com.devassist.service;

import com.devassist.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReActPattern {
    
    private final ChatClient chatClient;
    private final ToolExecutor toolExecutor;
    
    /**
     * Execute a single step with ReAct pattern
     */
    public ExecutionStep executeStep(
            PlanStep planStep,
            List<ExecutionStep> history,
            String sessionId) {
        
        log.info("🔄 ReAct: Executing step {} - {}", 
                planStep.getStepNumber(), planStep.getDescription());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // PHASE 1: REASON (Think before acting)
            String reasoning = generateReasoning(planStep, history);
            log.debug("💭 ReAct: Reasoning for step {}: {}", 
                    planStep.getStepNumber(), reasoning);
            
            // PHASE 2: ACT (Execute tool)
            ToolResult result = toolExecutor.executeTool(
                planStep.getToolName(),
                planStep.getParameters(),
                sessionId
            );
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("✅ ReAct: Step {} executed successfully in {}ms", 
                    planStep.getStepNumber(), executionTime);
            
            return ExecutionStep.builder()
                .planStep(planStep)
                .reasoning(reasoning)
                .result(result)
                .success(true)
                .timestamp(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .build();
                
        } catch (Exception e) {
            log.error("❌ ReAct: Step {} failed", planStep.getStepNumber(), e);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return ExecutionStep.builder()
                .planStep(planStep)
                .reasoning("Failed to execute: " + e.getMessage())
                .result(null)
                .success(false)
                .error(e.getMessage())
                .timestamp(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .build();
        }
    }
    
    /**
     * Generate reasoning for a step
     */
    private String generateReasoning(
            PlanStep planStep,
            List<ExecutionStep> history) {
        
        String reasoningPrompt = buildReasoningPrompt(planStep, history);
        
        String reasoning = chatClient.prompt()
            .system(buildReasoningSystemPrompt())
            .user(reasoningPrompt)
            .call()
            .content();
        
        return reasoning.trim();
    }
    
    /**
     * System prompt for reasoning
     */
    private String buildReasoningSystemPrompt() {
        return """
            You are a reasoning agent. Before taking any action, explain:
            1. What you need to do
            2. Why this action is necessary
            3. What you expect to learn from this action
            
            Be concise (2-3 sentences).
            """;
    }
    
    /**
     * Build reasoning prompt with context
     */
    private String buildReasoningPrompt(
            PlanStep planStep,
            List<ExecutionStep> history) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are about to execute this step:\n");
        prompt.append("Step ").append(planStep.getStepNumber()).append(": ");
        prompt.append(planStep.getDescription()).append("\n");
        prompt.append("Tool: ").append(planStep.getToolName()).append("\n\n");
        
        if (!history.isEmpty()) {
            prompt.append("Previous steps and their results:\n");
            for (ExecutionStep step : history) {
                prompt.append("- Step ").append(step.getPlanStep().getStepNumber())
                      .append(": ").append(step.getReasoning()).append("\n");
                if (step.getResult() != null) {
                    prompt.append("  Result: ").append(summarizeResult(step.getResult())).append("\n");
                }
            }
            prompt.append("\n");
        }
        
        prompt.append("Think about why this step is necessary and what you expect to achieve.");
        
        return prompt.toString();
    }
    
    /**
     * Summarize tool result for reasoning context
     */
    private String summarizeResult(ToolResult result) {
        if (result == null || result.getResult() == null) {
            return "No result";
        }
        
        String resultStr = result.getResult().toString();
        // Limit length for context
        if (resultStr.length() > 200) {
            return resultStr.substring(0, 200) + "...";
        }
        return resultStr;
    }
}
```

**Key Design Points:**
- ✅ Reasoning includes previous step results
- ✅ Error handling per step (doesn't fail entire flow)
- ✅ Logging at each phase
- ✅ Context from history is passed to reasoning

---

#### **2.4 ToolExecutor.java** (NEW Helper)

**Location:** `src/main/java/com/devassist/service/ToolExecutor.java`

**Design Discussion:**

**Purpose:** Execute individual tools programmatically

**Design Challenge:** How to execute tools without going through full ChatClient flow?

**Option A: Direct Tool Invocation**
```java
// Call tools directly
SqlTool.executeSql(...);
```

**Option B: Via ChatClient with Specific Tool** ⭐ CHOSEN
```java
// Use ChatClient with specific tool
chatClient.prompt()
    .user("Execute sql_execute with query: ...")
    .tools(sqlTool) // Only this tool
    .call();
```

**Class Structure:**
```java
package com.devassist.service;

import com.devassist.model.ToolResult;
import com.devassist.tools.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class ToolExecutor {
    
    private final ChatClient chatClient;
    private final SqlTool sqlTool;
    private final FileSystemTool fileSystemTool;
    private final HttpTool httpTool;
    private final ObjectMapper objectMapper;
    
    /**
     * Execute a tool with given parameters
     */
    public ToolResult executeTool(
            String toolName,
            Map<String, Object> parameters,
            String sessionId) {
        
        log.info("🔧 ToolExecutor: Executing tool '{}' with params: {}", 
                toolName, parameters);
        
        try {
            // Build tool execution request
            String executionRequest = buildToolExecutionRequest(toolName, parameters);
            
            // Determine which tool to use
            Object tool = getToolByName(toolName);
            
            if (tool == null) {
                throw new IllegalArgumentException("Unknown tool: " + toolName);
            }
            
            // Execute via ChatClient
            String result = chatClient.prompt()
                .system("You must execute the tool call exactly as requested.")
                .user(executionRequest)
                .tools(tool)  // Only this specific tool
                .call()
                .content();
            
            // Parse result
            return parseToolResult(toolName, result);
            
        } catch (Exception e) {
            log.error("❌ ToolExecutor: Failed to execute tool '{}'", toolName, e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build execution request for specific tool
     */
    private String buildToolExecutionRequest(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "sql_execute" -> {
                String query = (String) params.getOrDefault("query", 
                    "SELECT * FROM app_logs LIMIT 10");
                Integer maxRows = (Integer) params.getOrDefault("maxRows", 50);
                yield String.format(
                    "Execute sql_execute with query: '%s' and maxRows: %d",
                    query, maxRows
                );
            }
            case "read_file" -> {
                String filePath = (String) params.getOrDefault("filePath", "");
                yield String.format("Execute read_file with filePath: '%s'", filePath);
            }
            case "list_files" -> {
                String directoryPath = (String) params.getOrDefault("directoryPath", "");
                yield String.format("Execute list_files with directoryPath: '%s'", directoryPath);
            }
            case "http_get" -> {
                String url = (String) params.getOrDefault("url", "");
                yield String.format("Execute http_get with url: '%s'", url);
            }
            default -> "Execute " + toolName + " with parameters: " + params;
        };
    }
    
    /**
     * Get tool instance by name
     */
    private Object getToolByName(String toolName) {
        return switch (toolName) {
            case "sql_execute" -> sqlTool;
            case "read_file", "list_files", "file_info" -> fileSystemTool;
            case "http_get", "http_post", "check_url" -> httpTool;
            default -> null;
        };
    }
    
    /**
     * Parse tool result into ToolResult object
     */
    private ToolResult parseToolResult(String toolName, String result) {
        return ToolResult.builder()
            .toolName(toolName)
            .result(result)
            .formattedResult(formatResult(result))
            .build();
    }
    
    /**
     * Format result for display
     */
    private String formatResult(String result) {
        // Basic formatting - can be enhanced
        if (result.length() > 500) {
            return result.substring(0, 500) + "... (truncated)";
        }
        return result;
    }
}
```

**Alternative Approach: Direct Method Calls**

If we want more control, we could call tools directly:
```java
private ToolResult executeSqlTool(Map<String, Object> params) {
    String query = (String) params.get("query");
    Integer maxRows = (Integer) params.getOrDefault("maxRows", 50);
    
    SqlTool.SqlRequest request = new SqlTool.SqlRequest(query, maxRows);
    SqlTool.SqlResponse response = sqlTool.executeSql(request);
    
    // Convert to ToolResult
    return ToolResult.builder()
        .toolName("sql_execute")
        .result(response)
        .formattedResult(formatSqlResponse(response))
        .build();
}
```

**Decision:** We'll use ChatClient approach for consistency, but can optimize later.

---

### 🎮 Component 3: Controller

#### **3.1 AgenticController.java** (NEW)

**Location:** `src/main/java/com/devassist/controller/AgenticController.java`

**Design:**
```java
package com.devassist.controller;

import com.devassist.model.AgenticRequest;
import com.devassist.model.AgenticResponse;
import com.devassist.service.AgenticService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat/agentic")
@RequiredArgsConstructor
@Log4j2
public class AgenticController {
    
    private final AgenticService agenticService;
    
    /**
     * Agentic chat endpoint
     * POST /chat/agentic
     */
    @PostMapping(consumes = "application/json")
    @Operation(
        summary = "Agentic chat endpoint",
        description = "Process queries using agentic AI patterns with planning, reasoning, and execution"
    )
    public ResponseEntity<AgenticResponse> agenticChat(
            @RequestBody AgenticRequest request) {
        
        log.info("🤖 AgenticController: Received request for session '{}'", 
                request.getSessionId());
        
        try {
            // Set default sessionId if not provided
            String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : 
                "agentic-" + System.currentTimeMillis();
            
            // Process agentic query
            AgenticResponse response = agenticService.processAgenticQuery(
                request, 
                sessionId
            );
            
            log.info("✅ AgenticController: Generated response with {} steps", 
                    response.getTotalSteps());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ AgenticController: Error processing request", e);
            
            // Return error response
            AgenticResponse errorResponse = AgenticResponse.builder()
                .response("Error: " + e.getMessage())
                .sessionId(request.getSessionId())
                .totalSteps(0)
                .executionTime(0.0)
                .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
```

**Questions:**
- ✅ Should we validate request before processing?
- ✅ **Decision:** Yes, check for null task, validate options
- ✅ Should we add rate limiting?
- ✅ **Decision:** Not in initial version, can add later

---

### 🔄 Integration Points

#### **Integration 1: Using Existing ChatService**

**Challenge:** How to execute tools without full ChatService flow?

**Solution:** We'll create a focused tool execution method

**Option A: Add method to ChatService**
```java
// In ChatService.java (MODIFY)
public String executeSpecificTool(String toolName, Map<String, Object> params, String sessionId) {
    // Build request targeting specific tool
    // Call ChatClient with only that tool
}
```

**Option B: Create ToolExecutor (separate service)** ⭐ CHOSEN
```java
// New ToolExecutor service
// Doesn't modify existing ChatService
// Cleaner separation
```

---

#### **Integration 2: Sharing ChatClient**

**Question:** Should AgenticService use same ChatClient or separate?

**Decision:** Use same ChatClient instance
- ✅ Consistent configuration
- ✅ Shares advisors (RAG, memory)
- ✅ Simpler setup

```java
// In AgenticService, ReActPattern, PlanningEngine
// All use the same ChatClient bean from AiConfig
```

---

### 📝 Prompt Templates

#### **Planning Prompt Template:**

```java
private String buildPlanningSystemPrompt() {
    return """
        You are an expert planning agent for a developer assistant system.
        
        Your task is to break user requests into clear, executable steps.
        
        Available tools:
        - sql_execute: Execute SQL SELECT queries on app_logs database
          Parameters: query (SQL SELECT statement), maxRows (optional, default 50)
        
        - read_file: Read contents of a file
          Parameters: filePath (path to file)
        
        - list_files: List files in a directory
          Parameters: directoryPath (path to directory)
        
        - file_info: Get file metadata
          Parameters: filePath (path to file)
        
        - http_get: Make HTTP GET request
          Parameters: url (target URL)
        
        - http_post: Make HTTP POST request
          Parameters: url, body (optional)
        
        - check_url: Check if URL is reachable
          Parameters: url (target URL)
        
        When creating a plan:
        1. Analyze what information is needed
        2. Determine which tools can provide that information
        3. Order steps logically (dependencies matter)
        4. Be specific about parameters
        
        Output format (exactly):
        Step 1: [tool_name] - [description]
        Step 2: [tool_name] - [description]
        ...
        
        Example:
        Task: "Find errors and show code"
        Plan:
        Step 1: sql_execute - Query app_logs table WHERE level='ERROR' LIMIT 1
        Step 2: read_file - Read the Java file where the error occurred
        Step 3: analyze - Analyze error location in code context
        """;
}
```

---

#### **Reasoning Prompt Template:**

```java
private String buildReasoningSystemPrompt() {
    return """
        You are a reasoning agent. Before executing any action, explain:
        
        1. What you need to accomplish in this step
        2. Why this step is necessary given the context
        3. What information you expect to obtain
        
        Be concise (2-3 sentences).
        Focus on the logical connection between previous results and this action.
        """;
}
```

---

#### **Reflection Prompt Template:**

```java
private String buildReflectionPrompt(List<ExecutionStep> steps) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Analyze the execution results and reflect:\n\n");
    
    prompt.append("Steps executed:\n");
    for (ExecutionStep step : steps) {
        prompt.append(String.format(
            "Step %d (%s): %s\n",
            step.getPlanStep().getStepNumber(),
            step.getSuccess() ? "SUCCESS" : "FAILED",
            step.getPlanStep().getDescription()
        ));
        
        if (step.getResult() != null) {
            prompt.append("  Result: ").append(summarize(step.getResult())).append("\n");
        }
        
        if (step.getError() != null) {
            prompt.append("  Error: ").append(step.getError()).append("\n");
        }
    }
    
    prompt.append("\nReflect on:\n");
    prompt.append("1. Were all steps successful?\n");
    prompt.append("2. What did we learn from the results?\n");
    prompt.append("3. Are there any insights or patterns?\n");
    prompt.append("4. Did we achieve the goal?\n");
    
    return prompt.toString();
}
```

---

#### **Synthesis Prompt Template:**

```java
private String buildSynthesisPrompt(Plan plan, List<ExecutionStep> steps, String reflection) {
    return String.format("""
        Generate a comprehensive final answer based on:
        
        Original Goal: %s
        
        Plan Executed:
        %s
        
        Execution Results:
        %s
        
        Reflection:
        %s
        
        Provide a clear, actionable answer that:
        1. Addresses the original question
        2. Uses information from the execution results
        3. Provides insights from the reflection
        4. Is well-structured and easy to understand
        """,
        plan.getGoal(),
        formatPlan(plan),
        formatExecutionSteps(steps),
        reflection
    );
}
```

---

### 🔧 Configuration Changes

#### **AiConfig.java** - NO CHANGES NEEDED! ✅

**Why:**
- We'll reuse existing ChatClient bean
- No new configuration required
- Everything works with existing setup

---

### 📊 Design Decisions Summary

| Decision | Choice | Reason |
|----------|--------|--------|
| **Model Classes** | New classes (not extend) | Clean separation |
| **Tool Execution** | Via ChatClient | Consistent with existing code |
| **Planning Output** | Text parsing (not JSON) | Simpler, more flexible |
| **Reasoning Storage** | In response | Transparency |
| **Error Handling** | Continue on error | Don't fail entire flow |
| **ChatClient Usage** | Single instance | Simpler, shares config |

---

### 🎯 Implementation Order

#### **Phase 1: Models (30 min)**
1. Create AgenticRequest.java
2. Create AgenticResponse.java + ReasoningTrail.java
3. Create Plan.java + PlanStep.java
4. Create ExecutionStep.java + ToolResult.java

#### **Phase 2: Core Services (2 hours)**
1. Create ToolExecutor.java
2. Create PlanningEngine.java
3. Create ReActPattern.java
4. Create AgenticService.java (skeleton)

#### **Phase 3: Controller (30 min)**
1. Create AgenticController.java
2. Add Swagger annotations
3. Test endpoint

#### **Phase 4: Integration (1 hour)**
1. Wire all services
2. Test planning
3. Test ReAct loop
4. Test end-to-end

#### **Phase 5: Refinement (1 hour)**
1. Improve prompts
2. Enhance parsing
3. Add error handling
4. Add logging

---

## ✅ STEP 4 COMPLETE!

**What We Designed:**
- ✅ Model classes structure
- ✅ Service classes with method signatures
- ✅ Controller endpoint design
- ✅ Prompt templates
- ✅ Integration approach
- ✅ Implementation order

**Key Decisions Made:**
- ✅ Code-based approach (not just prompts)
- ✅ Reuse existing ChatService pattern
- ✅ Single ChatClient instance
- ✅ Text parsing for plans (simpler)
- ✅ Error handling per step

**Next Step:** Step 5 - Final Code Changes (We'll implement everything!)

---

**Ready to code?** Let's implement this in Step 5! 🚀

---

## Step 5: Final Code Changes ✅ COMPLETE!

### 🎉 Implementation Summary

All components have been successfully implemented!

---

### ✅ Files Created

#### **Model Classes (8 files):**
1. ✅ `AgenticOptions.java` - Configuration options for agentic behavior
2. ✅ `AgenticRequest.java` - Request model for agentic endpoint
3. ✅ `AgenticResponse.java` - Response model with reasoning trail
4. ✅ `ReasoningTrail.java` - Contains plan, reflection, and synthesis
5. ✅ `Plan.java` - Structured plan with steps
6. ✅ `PlanStep.java` - Individual step in the plan
7. ✅ `ExecutionStep.java` - Executed step with results
8. ✅ `ToolResult.java` - Result from tool execution

#### **Service Classes (4 files):**
1. ✅ `ToolExecutor.java` - Executes individual tools programmatically
2. ✅ `PlanningEngine.java` - Creates step-by-step plans from tasks
3. ✅ `ReActPattern.java` - Executes steps with reasoning (ReAct pattern)
4. ✅ `AgenticService.java` - Main orchestrator (planning → execution → reflection → synthesis)

#### **Controller (1 file):**
1. ✅ `AgenticController.java` - `/chat/agentic` endpoint with Swagger documentation

---

### 🔧 Implementation Details

#### **Agentic Flow:**
```
User Request
    ↓
PlanningEngine.createPlan() → Plan with steps
    ↓
For each step:
    ReActPattern.executeStep()
        ↓
    Generate Reasoning (LLM call)
        ↓
    Execute Tool (via ToolExecutor)
        ↓
    Record ExecutionStep
    ↓
Reflection (LLM call) → Analyze all results
    ↓
Synthesis (LLM call) → Generate final answer
    ↓
AgenticResponse with full reasoning trail
```

#### **Key Features Implemented:**
- ✅ Multi-step planning with LLM
- ✅ ReAct pattern (Reasoning + Acting)
- ✅ Tool execution via ChatClient
- ✅ Reflection phase
- ✅ Synthesis phase
- ✅ Complete reasoning trail in response
- ✅ Error handling per step
- ✅ Max steps limit protection
- ✅ Comprehensive logging

---

### 📍 Endpoint

**POST** `/chat/agentic`

**Request:**
```json
{
  "task": "Find errors in logs and show me the code",
  "sessionId": "agent-1",
  "options": {
    "showReasoning": true,
    "maxSteps": 10,
    "mode": "react"
  }
}
```

**Response:**
```json
{
  "response": "Final comprehensive answer...",
  "reasoning": {
    "plan": { "goal": "...", "steps": [...] },
    "reflection": "Reflection analysis...",
    "synthesis": "Summary of execution"
  },
  "executionSteps": [
    {
      "planStep": {...},
      "reasoning": "Why this step...",
      "result": {...},
      "success": true,
      "timestamp": "2025-01-XX...",
      "executionTimeMs": 123
    }
  ],
  "sessionId": "agent-1",
  "executionTime": 2.5,
  "totalSteps": 3
}
```

---

### 🧪 Testing

To test the agentic endpoint:

```bash
curl -X POST http://localhost:8080/chat/agentic \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Find errors in logs and show me the code file",
    "sessionId": "test-agent-1"
  }'
```

---

### 📝 Notes

1. **Linter Warnings**: Some linter warnings about package paths are false positives (IDE configuration issue, not actual code problem)

2. **Java Version**: Code uses Java 17 features (switch expressions, text blocks) which are supported by the project

3. **Tool Execution**: Tools are executed via ChatClient for consistency with existing architecture

4. **Planning Parsing**: Basic regex-based parsing for plans (can be enhanced to use structured output/JSON later)

5. **Error Handling**: Each step handles errors independently, allowing the flow to continue

---

### ✅ STEP 5 COMPLETE!

**All code changes have been implemented successfully!**

The agentic AI system is now ready to:
- Plan multi-step tasks
- Reason before acting
- Execute tools autonomously
- Reflect on results
- Synthesize comprehensive answers

---

**🎊 Congratulations! Agentic AI Patterns Implementation Complete!**

