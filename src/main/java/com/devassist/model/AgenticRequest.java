package com.devassist.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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


