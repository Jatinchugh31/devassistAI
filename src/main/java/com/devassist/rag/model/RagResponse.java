package com.devassist.rag.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Response object for RAG operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagResponse {
    
    /**
     * Status of the operation
     * Example: "success" or "error"
     */
    private String status;
    
    /**
     * Human-readable message
     * Example: "Documents loaded successfully!"
     */
    private String message;
    
    /**
     * Description of what happened
     * Example: "Java files converted to vectors using OpenAI embeddings"
     */
    private String description;
    
    /**
     * The question that was asked (for ask endpoint)
     * Example: "What is SqlTool?"
     */
    private String question;
    
    /**
     * The AI-generated answer (for ask endpoint)
     * Example: "SqlTool is a Spring AI tool that allows..."
     */
    private String answer;
    
    /**
     * Session ID for tracking
     * Example: "rag-session-1"
     */
    private String sessionId;
    
    /**
     * Additional metadata
     */
    private Object metadata;
}

