package com.devassist.service;

import com.devassist.model.Plan;
import com.devassist.model.PlanStep;
import com.devassist.service.LLMFilterAdvisorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlanningEngine - Creates step-by-step plans from user tasks
 * 
 * Uses LLM to break complex tasks into executable steps,
 * then parses the output into structured Plan objects.
 */
@Service
@Log4j2
public class PlanningEngine {
    
    private final ChatClient agenticChatClient; // ChatClient WITHOUT tools
    private final LLMFilterAdvisorService llmFilterAdvisorService;
    
    public PlanningEngine(
            @Qualifier("agenticChatClient") ChatClient agenticChatClient,
            LLMFilterAdvisorService llmFilterAdvisorService) {
        this.agenticChatClient = agenticChatClient;
        this.llmFilterAdvisorService = llmFilterAdvisorService;
    }
    
    /**
     * Creates a plan from user task
     */
    public Plan createPlan(String task, String sessionId) {
        log.info("📋 Planning: Creating plan for task: {}", task);
        
        // Build planning prompt
        String planningPrompt = buildPlanningPrompt(task);
        
        // Call LLM for planning (using ChatClient without tools)
        // Add session tracking and RAG filtering
        String planText = agenticChatClient.prompt()
            .system(buildPlanningSystemPrompt())
            .user(planningPrompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
            .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, 
                    llmFilterAdvisorService.generateFilterExpression(task)))
            .call()
            .content();
        
        log.debug("📋 Planning: Raw plan output:\n{}", planText);
        
        // Parse plan text into structured Plan object
        Plan plan = parsePlan(planText, task);
        
        log.info("📋 Planning: Parsed plan with {} steps", plan.getSteps().size());
        
        return plan;
    }
    
    /**
     * System prompt for planning agent - OPTIMIZED FOR SPEED
     */
    private String buildPlanningSystemPrompt() {
        return """
            Fast planning agent. Break into 2-4 concise steps. Prefer fewer steps.
            
            Tools: sql_execute, read_file, list_files, file_info, http_get, http_post, check_url
            
            Format:
            Step 1: tool_name - what to do
            Step 2: tool_name - what to do
            
            Rules:
            - Max 4 steps (prefer 2-3)
            - One action per step
            - Include key parameters in description
            - No verbose explanations
            """;
    }
    
    /**
     * User prompt for planning - OPTIMIZED FOR SPEED
     */
    private String buildPlanningPrompt(String task) {
        return String.format("""
            Plan this task in 2-5 steps:
            
            %s
            
            Output steps only, no thinking process.
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
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );
        
        Matcher matcher = pattern.matcher(planText);
        
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
            Pattern sqlPattern = Pattern.compile("(SELECT.*?LIMIT|SELECT.*?WHERE.*?LIMIT|SELECT.*?)(?:\\s|$)", 
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher sqlMatcher = sqlPattern.matcher(description);
            if (sqlMatcher.find()) {
                String query = sqlMatcher.group(1).trim();
                // Try to extract LIMIT value
                Pattern limitPattern = Pattern.compile("LIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
                Matcher limitMatcher = limitPattern.matcher(description);
                if (limitMatcher.find()) {
                    params.put("maxRows", Integer.parseInt(limitMatcher.group(1)));
                }
                params.put("query", query);
            } else if (description.contains("SELECT") || description.contains("select")) {
                // Fallback: extract basic query structure
                params.put("query", extractSqlQuery(description));
            }
        } else if (toolName.equals("read_file")) {
            // Extract file path
            String filePath = extractFilePath(description);
            if (filePath != null) {
                params.put("filePath", filePath);
            }
        } else if (toolName.equals("list_files")) {
            // Extract directory path
            String dirPath = extractDirectoryPath(description);
            if (dirPath != null) {
                params.put("directoryPath", dirPath);
            }
        } else if (toolName.equals("http_get") || toolName.equals("check_url")) {
            // Extract URL
            String url = extractUrl(description);
            if (url != null) {
                params.put("url", url);
            }
        }
        // More tool-specific parsing can be added here
        
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
        // Basic extraction - try to find SELECT statement
        Pattern pattern = Pattern.compile("(SELECT.*?FROM.*?)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1).trim() + " LIMIT 10";
        }
        // Fallback generic query
        return "SELECT * FROM app_logs LIMIT 10";
    }
    
    private String extractFilePath(String description) {
        // Try to find file path patterns (e.g., .java, .properties, etc.)
        Pattern pattern = Pattern.compile("([\\w/]+\\.[a-z]+|[\\w/]+/[\\w/]+\\.[a-z]+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractDirectoryPath(String description) {
        // Try to find directory path
        Pattern pattern = Pattern.compile("(src|src/[\\w/]+|/[\\w/]+/?)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractUrl(String description) {
        // Try to find URL pattern
        Pattern pattern = Pattern.compile("(https?://[\\w.\\-/:]+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}

