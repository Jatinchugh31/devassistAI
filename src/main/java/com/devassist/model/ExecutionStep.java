package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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


