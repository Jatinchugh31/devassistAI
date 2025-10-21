package com.devassist.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request object for chat interactions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * The role/persona for the AI
     * Example: "sql" or "general"
     */
    private String role;
    
    /**
     * The task or question to ask
     * Example: "Find the last error in app_logs table"
     */
    private String task;
    
    /**
     * Optional session ID for tracking conversations
     * Example: "chat-session-1"
     */
    private String sessionId;
}

