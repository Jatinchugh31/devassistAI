package com.devassist.service;

import com.devassist.model.ToolResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ToolExecutor - Executes tools via ChatClient (model-driven)
 * 
 * Since tools are @Component beans, they're auto-registered globally with Spring AI.
 * We use ChatClient WITHOUT registering tools again - tools are already available!
 * The model will naturally call the appropriate tool.
 */
@Service
@Log4j2
public class ToolExecutor {
    
    private final ChatClient chatClient; // ChatClient WITHOUT tool registration (tools auto-discovered)
    
    // Use @Primary ChatClient - tools are already globally registered as @Component beans
    // We don't need to register them again - Spring AI auto-discovers them!
    public ToolExecutor(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * Execute a step - let the LLM decide which tool to call and how!
     * Fully autonomous execution based on step description.
     */
    public ToolResult executeTool(
            String toolName,
            Map<String, Object> parameters,
            String sessionId) {
        
        // Accept step description directly instead
        return executeStep(parameters, sessionId);
    }
    
    /**
     * Execute a step from description - LLM decides everything!
     */
    public ToolResult executeStep(
            Map<String, Object> stepContext,
            String sessionId) {
        
        log.info("🔧 ToolExecutor: Executing step - LLM will decide tool and parameters");
        
        try {
            // Build natural step description from context
            String stepDescription = buildNaturalDescription(stepContext);
            
            // Use ChatClient - tools are already registered globally
            // LLM will autonomously decide which tool to call and with what parameters
            String result = chatClient.prompt()
                .system("""
                    Execute the step. Use appropriate tool. Return ONLY raw result, no explanations.
                    """)
                .user(stepDescription)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
            
            // Parse result - LLM decided which tool was used
            return parseToolResult("auto-detected", result);
            
        } catch (Exception e) {
            log.error("❌ ToolExecutor: Failed to execute step", e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build natural description from step context - let LLM interpret it
     */
    private String buildNaturalDescription(Map<String, Object> context) {
        // Use description if available - LLM will decide what to do
        if (context.containsKey("description")) {
            String description = context.get("description").toString();
            
            // Add context hints if available (but LLM decides)
            StringBuilder fullDesc = new StringBuilder();
            fullDesc.append(description);
            
            // Optionally add suggested tool as hint (LLM can override)
            if (context.containsKey("suggestedTool")) {
                fullDesc.append("\n\n(Suggested tool: ").append(context.get("suggestedTool")).append(")");
            }
            
            return fullDesc.toString();
        }
        
        // Fallback: let LLM figure it out from context
        return "Execute this task: " + context.toString();
    }
    
    /**
     * Parse tool result into ToolResult object
     * Result is a String from ChatClient response
     */
    private ToolResult parseToolResult(String toolName, String result) {
        // Result is already a string from ChatClient
        String formattedResult = formatResult(result);
        
        return ToolResult.builder()
            .toolName(toolName)
            .result(result)  // Keep as string
            .formattedResult(formattedResult)
            .build();
    }
    
    /**
     * Format result for display
     */
    private String formatResult(String result) {
        // Basic formatting - can be enhanced
        if (result == null) {
            return "No result";
        }
        if (result.length() > 500) {
            return result.substring(0, 500) + "... (truncated)";
        }
        return result;
    }
}

