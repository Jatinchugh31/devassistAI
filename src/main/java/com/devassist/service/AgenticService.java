package com.devassist.service;

import com.devassist.model.*;
import com.devassist.service.LLMFilterAdvisorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AgenticService - Main orchestrator for agentic AI patterns
 * 
 * Implements the full agentic flow:
 * 1. PLANNING: Create step-by-step plan
 * 2. EXECUTION: Execute each step with ReAct pattern
 * 3. REFLECTION: Reflect on execution results
 * 4. SYNTHESIS: Generate final comprehensive answer
 */
@Service
@Log4j2
public class AgenticService {
    
    private final PlanningEngine planningEngine;
    private final ReActPattern reactPattern;
    private final ChatClient agenticChatClient; // ChatClient WITHOUT tools for reflection/synthesis
    private final ChatClient chatClientWithTools; // ChatClient WITH tools for direct mode
    private final LLMFilterAdvisorService llmFilterAdvisorService;
    
    public AgenticService(
            PlanningEngine planningEngine,
            ReActPattern reactPattern,
            @Qualifier("agenticChatClient") ChatClient agenticChatClient,
            ChatClient chatClientWithTools, // @Primary ChatClient with tools
            LLMFilterAdvisorService llmFilterAdvisorService) {
        this.planningEngine = planningEngine;
        this.reactPattern = reactPattern;
        this.agenticChatClient = agenticChatClient;
        this.chatClientWithTools = chatClientWithTools;
        this.llmFilterAdvisorService = llmFilterAdvisorService;
    }
    
    /**
     * Main entry point for agentic queries
     */
    public AgenticResponse processAgenticQuery(
            AgenticRequest request, 
            String sessionId) {
        
        long startTime = System.currentTimeMillis();
        log.info("🤖 Agentic: Processing query '{}' for session '{}'", 
                request.getTask(), sessionId);
        
        // Set default options if not provided
        AgenticOptions options = request.getOptions();
        if (options == null) {
            options = new AgenticOptions();
        }
        
        try {
            // PHASE 1: PLANNING
            Plan plan = planningEngine.createPlan(request.getTask(), sessionId);
            log.info("📋 Agentic: Generated plan with {} steps", plan.getSteps().size());
            
            // Validate plan
            if (plan.getSteps().isEmpty()) {
                throw new RuntimeException("Planning failed: No steps generated");
            }
            
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
                if (executionSteps.size() >= options.getMaxSteps()) {
                    log.warn("⚠️ Agentic: Reached max steps limit ({})", 
                            options.getMaxSteps());
                    break;
                }
            }
            
            // PHASE 3: SYNTHESIS (skip reflection for speed - just synthesize directly)
            String finalResponse = synthesizeResponse(
                plan, 
                executionSteps, 
                null, // Skip reflection phase for speed
                sessionId
            );
            log.info("✅ Agentic: Generated final response");
            
            // Build response
            double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;
            
            // Only include reasoning/details if requested
            AgenticResponse.AgenticResponseBuilder responseBuilder = AgenticResponse.builder()
                .response(finalResponse)
                .sessionId(sessionId)
                .executionTime(executionTime)
                .totalSteps(executionSteps.size());
            
            // Only add detailed reasoning if showReasoning is true
            if (Boolean.TRUE.equals(options.getShowReasoning())) {
                String reflection = buildQuickReflection(executionSteps); // Quick reflection without LLM call
                ReasoningTrail reasoning = ReasoningTrail.builder()
                    .plan(plan)
                    .reflection(reflection)
                    .synthesis(buildSynthesis(executionSteps))
                    .build();
                responseBuilder.reasoning(reasoning)
                    .executionSteps(executionSteps);
            }
            
            return responseBuilder.build();
                
        } catch (Exception e) {
            log.error("❌ Agentic: Error processing query", e);
            throw new RuntimeException("Agentic processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reflect on all execution steps
     */
    private String reflectOnExecution(
            List<ExecutionStep> steps, 
            String sessionId) {
        
        String reflectionPrompt = buildReflectionPrompt(steps);
        
        // Add session tracking and RAG filtering for better context
        String reflection = agenticChatClient.prompt()
            .system(buildReflectionSystemPrompt())
            .user(reflectionPrompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
            .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION,
                    llmFilterAdvisorService.generateFilterExpression(buildReflectionQuery(steps))))
            .call()
            .content();
        
        return reflection.trim();
    }
    
    /**
     * System prompt for reflection - OPTIMIZED FOR SPEED
     */
    private String buildReflectionSystemPrompt() {
        return """
            Summarize execution results in 2-3 sentences. What worked? Any issues?
            """;
    }
    
    /**
     * Build reflection prompt - OPTIMIZED FOR SPEED
     */
    private String buildReflectionPrompt(List<ExecutionStep> steps) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Steps executed:\n");
        
        // Only include key info for speed
        for (ExecutionStep step : steps) {
            String status = step.getSuccess() ? "✓" : "✗";
            prompt.append(status).append(" Step ").append(step.getPlanStep().getStepNumber())
                  .append(": ").append(step.getPlanStep().getDescription()).append("\n");
            
            // Only include result summary if it's critical
            if (!step.getSuccess() && step.getError() != null) {
                prompt.append("  Error: ").append(step.getError()).append("\n");
            }
        }
        
        prompt.append("\nSummarize: What happened? (2-3 sentences)");
        
        return prompt.toString();
    }
    
    /**
     * Synthesize final answer from plan, steps, and reflection
     */
    private String synthesizeResponse(
            Plan plan,
            List<ExecutionStep> steps,
            String reflection,
            String sessionId) {
        
        String synthesisPrompt = buildSynthesisPrompt(plan, steps, reflection);
        
        // Add session tracking and RAG filtering for better context
        String response = agenticChatClient.prompt()
            .system(buildSynthesisSystemPrompt())
            .user(synthesisPrompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
            .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION,
                    llmFilterAdvisorService.generateFilterExpression(plan.getGoal())))
            .call()
            .content();
        
        return response.trim();
    }
    
    /**
     * Quick reflection without LLM call (for showReasoning=true)
     */
    private String buildQuickReflection(List<ExecutionStep> steps) {
        int successCount = (int) steps.stream().filter(ExecutionStep::getSuccess).count();
        int failCount = steps.size() - successCount;
        return String.format("Executed %d steps: %d succeeded, %d failed", 
                steps.size(), successCount, failCount);
    }
    
    /**
     * System prompt for synthesis
     */
    private String buildSynthesisSystemPrompt() {
        return """
            Generate a concise, direct answer to the user's question using the execution results.
            Be clear and actionable. No verbose explanations.
            """;
    }
    
    /**
     * Build synthesis prompt - OPTIMIZED FOR SPEED
     */
    private String buildSynthesisPrompt(Plan plan, List<ExecutionStep> steps, String reflection) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Question: ").append(plan.getGoal()).append("\n\n");
        
        prompt.append("Results:\n");
        // Only include key successful results (limit to first 2-3 for speed)
        int count = 0;
        for (ExecutionStep step : steps) {
            if (step.getSuccess() && step.getResult() != null && count < 3) {
                String summary = summarizeForSynthesis(step.getResult());
                // Limit summary length
                if (summary.length() > 300) {
                    summary = summary.substring(0, 300) + "...";
                }
                prompt.append("- ").append(summary).append("\n");
                count++;
            }
        }
        
        // Skip reflection for speed if null
        if (reflection != null && !reflection.isEmpty()) {
            prompt.append("\nNote: ").append(reflection);
        }
        
        prompt.append("\n\nProvide a clear, concise answer.");
        
        return prompt.toString();
    }
    
    /**
     * Build synthesis summary
     */
    private String buildSynthesis(List<ExecutionStep> steps) {
        int successCount = (int) steps.stream().filter(ExecutionStep::getSuccess).count();
        return String.format("Successfully executed %d out of %d steps", 
                successCount, steps.size());
    }
    
    /**
     * Build reflection query for RAG filtering
     */
    private String buildReflectionQuery(List<ExecutionStep> steps) {
        // Build a query string from execution steps for RAG filtering
        if (steps.isEmpty()) {
            return "execution results";
        }
        StringBuilder query = new StringBuilder("execution results: ");
        for (ExecutionStep step : steps) {
            query.append(step.getPlanStep().getToolName()).append(" ");
        }
        return query.toString().trim();
    }
    
    /**
     * Summarize result for reflection context
     */
    private String summarizeForReflection(ToolResult result) {
        if (result == null || result.getFormattedResult() == null) {
            return "No result";
        }
        
        String resultStr = result.getFormattedResult();
        // Limit length for context
        if (resultStr.length() > 300) {
            return resultStr.substring(0, 300) + "...";
        }
        return resultStr;
    }
    
    /**
     * Summarize result for synthesis context
     */
    private String summarizeForSynthesis(ToolResult result) {
        if (result == null || result.getFormattedResult() == null) {
            return "No result";
        }
        
        String resultStr = result.getFormattedResult();
        // Limit length for context
        if (resultStr.length() > 500) {
            return resultStr.substring(0, 500) + "...";
        }
        return resultStr;
    }
}

