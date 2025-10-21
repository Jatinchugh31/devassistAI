package com.devassist.rag.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request object for asking questions using RAG
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagQuestionRequest {
    
    /**
     * The question to ask about the codebase
     * Example: "What is SqlTool?"
     */
    private String question;
    
    /**
     * Optional session ID for tracking conversations
     * Example: "rag-session-1"
     */
    private String sessionId;
}

