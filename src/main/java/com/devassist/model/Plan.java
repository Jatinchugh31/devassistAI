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
public class Plan {
    
    /**
     * Original task/goal
     */
    private String goal;
    
    /**
     * List of planned steps
     */
    private List<PlanStep> steps;
    
    /**
     * Estimated complexity (1-10)
     */
    private Integer complexity;
}


