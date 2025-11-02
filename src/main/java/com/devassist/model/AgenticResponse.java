package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticResponse {
    
    /**
     * Final user-facing answer
     */
    private String response;
    
    /**
     * Complete reasoning trail
     */
    private ReasoningTrail reasoning;
    
    /**
     * List of all execution steps
     */
    private List<ExecutionStep> executionSteps;
    
    /**
     * Session ID
     */
    private String sessionId;
    
    /**
     * Total execution time in seconds
     */
    private Double executionTime;
    
    /**
     * Total number of steps executed
     */
    private Integer totalSteps;
}


