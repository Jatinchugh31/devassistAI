package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReasoningTrail {
    
    /**
     * Original plan generated before execution
     */
    private Plan plan;
    
    /**
     * Reflection after execution
     */
    private String reflection;
    
    /**
     * Synthesis summary
     */
    private String synthesis;
}


