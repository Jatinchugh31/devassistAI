# 🔄 Agentic AI: LLM Call Flow Explained

## ❓ Your Question: 

**"Do we call LLM model to Plan, and then pass that Plan again to LLM model - is it work like this?"**

**Answer: YES! Multiple LLM calls, each with different purposes.**

---

## 📊 Complete LLM Call Flow

### **Example Task:** "Find errors in logs and show me the code file"

```
┌─────────────────────────────────────────────────────────────┐
│                    LLM CALL #1: PLANNING                    │
│                                                             │
│ Input:                                                      │
│   "Find errors in logs and show me the code file"          │
│                                                             │
│ LLM Output (Planning):                                     │
│   "Step 1: Execute sql_execute to find errors             │
│    Step 2: Read the code file                              │
│    Step 3: Analyze the error location"                      │
│                                                             │
│ Result: Plan object with 3 steps                          │
└─────────────────────────────────────────────────────────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │   Parse Plan           │
            │   → Step 1: sql_execute│
            │   → Step 2: read_file  │
            │   → Step 3: analyze    │
            └───────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              REACT LOOP: For Each Step                      │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ LLM CALL #2: REASONING (Step 1)                        ││
│ │                                                         ││
│ │ Input:                                                  ││
│ │   Plan: "Step 1: Execute sql_execute to find errors"  ││
│ │   Context: Previous steps (empty for first step)       ││
│ │                                                         ││
│ │ LLM Output (Reasoning):                                ││
│ │   "I need to query the database to find errors.         ││
│ │    I'll use sql_execute tool with a query to           ││
│ │    SELECT errors from app_logs table."                 ││
│ │                                                         ││
│ │ Result: Reasoning text                                  ││
│ └─────────────────────────────────────────────────────────┘│
│                        │                                   │
│                        ▼                                   │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ TOOL EXECUTION (Step 1)                                  ││
│ │                                                         ││
│ │ Execute: sql_execute("SELECT * FROM app_logs...")       ││
│ │                                                         ││
│ │ Result: {rowCount: 1, rows: [{error: "NullPointer..."}]}││
│ └─────────────────────────────────────────────────────────┘│
│                        │                                   │
│                        ▼                                   │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ LLM CALL #3: REASONING (Step 2)                        ││
│ │                                                         ││
│ │ Input:                                                  ││
│ │   Plan: "Step 2: Read the code file"                   ││
│ │   Previous Step Result: Error found in SqlTool.java     ││
│ │   Reasoning from Step 1: "I need to query..."         ││
│ │                                                         ││
│ │ LLM Output (Reasoning):                                 ││
│ │   "I found an error in SqlTool.java. Now I need to     ││
│ │    read that file to analyze the error location.       ││
│ │    I'll use read_file tool."                            ││
│ │                                                         ││
│ │ Result: Reasoning text                                  ││
│ └─────────────────────────────────────────────────────────┘│
│                        │                                   │
│                        ▼                                   │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ TOOL EXECUTION (Step 2)                                  ││
│ │                                                         ││
│ │ Execute: read_file("SqlTool.java")                      ││
│ │                                                         ││
│ │ Result: {content: "package com.devassist..."}           ││
│ └─────────────────────────────────────────────────────────┘│
│                        │                                   │
│                        ▼                                   │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ LLM CALL #4: REASONING (Step 3)                        ││
│ │                                                         ││
│ │ Input:                                                  ││
│ │   Plan: "Step 3: Analyze the error location"           ││
│ │   Previous Results: Error + Code file                  ││
│ │                                                         ││
│ │ LLM Output (Reasoning):                                 ││
│ │   "I have both the error and the code. I can now       ││
│ │    analyze where the error occurs and provide           ││
│ │    a comprehensive explanation."                         ││
│ │                                                         ││
│ │ Result: Reasoning text                                  ││
│ └─────────────────────────────────────────────────────────┘│
│                        │                                   │
│                        ▼                                   │
│           (No tool execution for analysis)                │
│                        │                                   │
└────────────────────────┼───────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              LLM CALL #5: REFLECTION                        │
│                                                             │
│ Input:                                                      │
│   All ExecutionSteps:                                      │
│   - Step 1: Reasoning + sql_execute result                 │
│   - Step 2: Reasoning + read_file result                  │
│   - Step 3: Reasoning                                       │
│                                                             │
│ LLM Output (Reflection):                                   │
│   "I successfully found the error in SqlTool.java at      │
│    line 58. It's a NullPointerException in the executeSql  │
│    method. The error occurs because..."                    │
│                                                             │
│ Result: Reflection text                                     │
└─────────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              LLM CALL #6: SYNTHESIS (Final Answer)          │
│                                                             │
│ Input:                                                      │
│   Original Plan                                            │
│   All ExecutionSteps with results                          │
│   Reflection                                               │
│                                                             │
│ LLM Output (Final Answer):                                 │
│   "I found an error in SqlTool.java at line 58.            │
│    The error is a NullPointerException in the executeSql   │
│    method. Looking at the code, the issue is that..."     │
│                                                             │
│ Result: Final user-facing response                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 LLM Call Summary

For a **3-step task**, we make approximately **6 LLM calls**:

| Call # | Phase | Purpose | Input | Output |
|--------|-------|---------|-------|--------|
| **1** | Planning | Generate plan | User task | Plan with steps |
| **2** | Reasoning | Think before Step 1 | Plan + context | Reasoning text |
| **3** | Reasoning | Think before Step 2 | Plan + Step 1 result | Reasoning text |
| **4** | Reasoning | Think before Step 3 | Plan + previous results | Reasoning text |
| **5** | Reflection | Analyze all steps | All execution results | Reflection text |
| **6** | Synthesis | Final answer | Plan + Steps + Reflection | Final response |

**Note:** Step 3 might not need a tool execution (just reasoning), so we might have fewer tool calls.

---

## 🎯 Alternative Approach: Fewer LLM Calls

### **Option A: Current Design (Multiple Calls)** ⭐ CHOSEN
- ✅ More control
- ✅ Better reasoning at each step
- ✅ Transparent thinking process
- ❌ More tokens/cost
- ❌ Slightly slower

### **Option B: Single LLM Call with Structured Output**

**How it works:**
```
1. LLM CALL: Planning + Reasoning + Execution (all in one)
   
   Input: Task + System prompt (planning instructions)
   
   LLM Output (structured):
   {
     "plan": [...],
     "reasoning": [...],
     "toolCalls": [...],
     "finalAnswer": "..."
   }
```

**Pros:**
- ✅ Fewer API calls
- ✅ Faster
- ✅ Lower cost

**Cons:**
- ❌ Less control
- ❌ Harder to debug
- ❌ Can't observe intermediate thinking
- ❌ Less reliable

**We'll use Option A (Multiple Calls) because:**
- Better for learning
- More transparent
- Better control
- Aligns with ReAct pattern

---

## 💡 Optimization: Hybrid Approach

We can optimize by **combining some phases**:

### **Optimized Flow:**

```
1. LLM CALL #1: Planning
   → Generate plan

2. For each step:
   a. LLM CALL #2-N: Reasoning (quick, concise)
   b. TOOL EXECUTION: Execute tool
   
3. LLM CALL #(N+1): Reflection + Synthesis (combined)
   → Final answer with reflection
```

**This reduces from 6 calls to ~4 calls for 3-step task.**

---

## 🔧 Implementation Details

### **How Planning Works:**
```java
PlanningEngine.createPlan(task) {
    // LLM CALL #1
    String planText = chatClient.prompt()
        .system("You are a planning agent. Break tasks into steps.")
        .user("Plan this: " + task)
        .call()
        .content();
    
    // Parse text → Plan object
    return parsePlan(planText);
}
```

### **How ReAct Loop Works:**
```java
ReActPattern.executeStep(planStep, history, sessionId) {
    // LLM CALL #2-N (one per step)
    String reasoning = chatClient.prompt()
        .system("You are reasoning about next action.")
        .user("Plan step: " + planStep + 
              "\nPrevious results: " + history)
        .call()
        .content();
    
    // Execute tool (NO LLM CALL)
    ToolResult result = executeTool(planStep.toolName, 
                                    planStep.parameters);
    
    return new ExecutionStep(reasoning, result);
}
```

### **How Reflection Works:**
```java
AgenticService.reflectOnExecution(steps) {
    // LLM CALL #(N+1)
    String reflection = chatClient.prompt()
        .system("Analyze the execution results.")
        .user("Steps executed:\n" + formatSteps(steps))
        .call()
        .content();
    
    return reflection;
}
```

### **How Synthesis Works:**
```java
AgenticService.synthesizeResponse(plan, steps, reflection) {
    // LLM CALL #(N+2) - FINAL
    String finalAnswer = chatClient.prompt()
        .system("Provide final comprehensive answer.")
        .user("Plan: " + plan + 
              "\nExecution: " + steps + 
              "\nReflection: " + reflection)
        .call()
        .content();
    
    return finalAnswer;
}
```

---

## 📈 Cost & Performance Considerations

### **Token Usage:**
- **Planning:** ~500 tokens
- **Reasoning (per step):** ~300 tokens × N steps
- **Reflection:** ~400 tokens
- **Synthesis:** ~500 tokens

**Total for 3-step task: ~2,300 tokens**

### **Performance:**
- **Planning:** ~2 seconds
- **Reasoning per step:** ~1.5 seconds
- **Tool execution:** ~0.5 seconds (depends on tool)
- **Reflection:** ~2 seconds
- **Synthesis:** ~2 seconds

**Total for 3-step task: ~10-12 seconds**

---

## 🎯 Summary

**Yes, you're correct!**

**Flow:**
1. ✅ **LLM Call #1:** Plan generation
2. ✅ **LLM Call #2-N:** Reasoning before each step (N = number of steps)
3. ✅ **Tool Execution:** Execute tool (NO LLM call)
4. ✅ **LLM Call #(N+1):** Reflection
5. ✅ **LLM Call #(N+2):** Final synthesis

**Key Points:**
- Multiple LLM calls serve different purposes
- Plan is parsed and used to guide execution
- Reasoning happens before each tool execution
- Reflection analyzes all results
- Synthesis generates final answer

**Why Multiple Calls?**
- ✅ Better control
- ✅ Transparent reasoning
- ✅ Reliable execution
- ✅ Better learning (you see each step)

---

## 🔄 Alternative: Single Call Approach

If you want **fewer calls**, we could do:

```
1. LLM CALL: Everything in one go
   - Planning
   - Reasoning
   - Tool execution requests
   - Final answer
```

But this loses the **transparency and control** that makes agentic AI powerful.

**Recommendation:** Stick with multiple calls for learning and better results! 🎯


