package com.devassist.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    
    /**
     * Tool name that was executed
     */
    private String toolName;
    
    /**
     * Raw result from tool (as string/object)
     */
    private Object result;
    
    /**
     * Formatted result for display
     */
    private String formattedResult;
}


