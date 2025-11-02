package com.devassist.service;

import com.devassist.model.ExecutionStep;
import com.devassist.model.PlanStep;
import com.devassist.model.ToolResult;
import com.devassist.service.LLMFilterAdvisorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ReActPattern - Executes steps with Reasoning + Acting pattern
 * 
 * For each step:
 * 1. REASON: Generate reasoning before execution
 * 2. ACT: Execute the tool
 * 3. Record results with reasoning trail
 */
@Service
@Log4j2
public class ReActPattern {
    
    private final ChatClient agenticChatClient; // ChatClient WITHOUT tools for reasoning
    private final ToolExecutor toolExecutor;
    private final LLMFilterAdvisorService llmFilterAdvisorService;
    
    public ReActPattern(
            @Qualifier("agenticChatClient") ChatClient agenticChatClient,
            ToolExecutor toolExecutor,
            LLMFilterAdvisorService llmFilterAdvisorService) {
        this.agenticChatClient = agenticChatClient;
        this.toolExecutor = toolExecutor;
        this.llmFilterAdvisorService = llmFilterAdvisorService;
    }
    
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
            String reasoning = generateReasoning(planStep, history, sessionId);
            log.debug("💭 ReAct: Reasoning for step {}: {}", 
                    planStep.getStepNumber(), reasoning);
            
            // PHASE 2: ACT (Execute tool)
            // Let LLM decide which tool to use based on step description
            Map<String, Object> stepContext = new java.util.HashMap<>();
            stepContext.put("description", planStep.getDescription());
            stepContext.put("stepNumber", planStep.getStepNumber());
            if (planStep.getToolName() != null) {
                stepContext.put("suggestedTool", planStep.getToolName());
            }
            if (!planStep.getParameters().isEmpty()) {
                stepContext.putAll(planStep.getParameters());
            }
            
            ToolResult result = toolExecutor.executeStep(stepContext, sessionId);
            
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
            List<ExecutionStep> history,
            String sessionId) {
        
        String reasoningPrompt = buildReasoningPrompt(planStep, history);
        
        // Add session tracking and RAG filtering for better context
        String reasoning = agenticChatClient.prompt()
            .system(buildReasoningSystemPrompt())
            .user(reasoningPrompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
            .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION,
                    llmFilterAdvisorService.generateFilterExpression(planStep.getDescription())))
            .call()
            .content();
        
        return reasoning.trim();
    }
    
    /**
     * System prompt for reasoning - OPTIMIZED FOR SPEED
     */
    private String buildReasoningSystemPrompt() {
        return """
            Briefly explain (1 sentence) why this step is needed. Be concise.
            """;
    }
    
    /**
     * Build reasoning prompt with context - OPTIMIZED FOR SPEED
     */
    private String buildReasoningPrompt(
            PlanStep planStep,
            List<ExecutionStep> history) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Step: ").append(planStep.getDescription());
        
        if (!history.isEmpty() && history.size() <= 2) {
            // Only include recent context for speed
            ExecutionStep lastStep = history.get(history.size() - 1);
            prompt.append("\nPrevious: ").append(summarizeResult(lastStep.getResult()));
        }
        
        prompt.append("\nWhy execute this step? (1 sentence)");
        
        return prompt.toString();
    }
    
    /**
     * Summarize tool result for reasoning context
     */
    private String summarizeResult(ToolResult result) {
        if (result == null || result.getFormattedResult() == null) {
            return "No result";
        }
        
        String resultStr = result.getFormattedResult();
        // Limit length for context
        if (resultStr.length() > 200) {
            return resultStr.substring(0, 200) + "...";
        }
        return resultStr;
    }
}

