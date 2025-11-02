package com.devassist.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenticOptions {
    
    /**
     * Show reasoning trail in response (default: false for cleaner output)
     * Set to true to see plan, reflection, and execution steps
     */
    private Boolean showReasoning = false;
    
    /**
     * Maximum number of execution steps (default: 5 for speed)
     * Prevents infinite loops
     */
    private Integer maxSteps = 5;
    
    /**
     * Agentic mode: 
     * - "direct": Skip planning, use ChatClient directly (fast, like /chat endpoint)
     * - "react": Plan then execute with reasoning (slower but more structured)
     * - "plan-act-reflect": Full agentic with planning, execution, reflection
     * Default: "direct" for speed
     */
    private String mode = "direct";
}

