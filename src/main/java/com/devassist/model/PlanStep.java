package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanStep {
    
    /**
     * Step number (1, 2, 3, ...)
     */
    private Integer stepNumber;
    
    /**
     * Tool name to execute
     * Examples: "sql_execute", "read_file", "list_files"
     */
    private String toolName;
    
    /**
     * Description of what this step does
     */
    private String description;
    
    /**
     * Parameters for tool execution
     * Example: {"query": "SELECT * FROM app_logs"}
     */
    private Map<String, Object> parameters;
    
    /**
     * Dependencies (which steps must complete first)
     */
    private List<Integer> dependsOn;
}


