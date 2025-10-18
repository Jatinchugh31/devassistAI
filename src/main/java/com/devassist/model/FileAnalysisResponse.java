package com.devassist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FileAnalysisResponse - Structured response for file upload and analysis
 * 
 * This provides a clean, well-formatted JSON response that's easy to read in Postman
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAnalysisResponse {
    
    @JsonProperty("status")
    private String status; // "success" or "error"
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("fileInfo")
    private FileInfo fileInfo;
    
    @JsonProperty("analysis")
    private Analysis analysis;
    
    @JsonProperty("error")
    private ErrorInfo error;
    
    /**
     * File information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        @JsonProperty("fileName")
        private String fileName;
        
        @JsonProperty("fileSize")
        private Long fileSize;
        
        @JsonProperty("fileSizeFormatted")
        private String fileSizeFormatted;
        
        @JsonProperty("fileType")
        private String fileType;
        
        @JsonProperty("numberOfLines")
        private Integer numberOfLines;
    }
    
    /**
     * Analysis results
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Analysis {
        @JsonProperty("summary")
        private String summary;
        
        @JsonProperty("keyFindings")
        private List<String> keyFindings;
        
        @JsonProperty("recommendations")
        private List<String> recommendations;
        
        @JsonProperty("fullResponse")
        private String fullResponse;
        
        @JsonProperty("processingTimeMs")
        private Long processingTimeMs;
    }
    
    /**
     * Error information (if any)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("details")
        private String details;
    }
    
    /**
     * Helper method to format file size
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
}

