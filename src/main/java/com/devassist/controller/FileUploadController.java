package com.devassist.controller;

import com.devassist.model.AgentRequest;
import com.devassist.model.FileAnalysisResponse;
import com.devassist.service.ChatService;
import com.devassist.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileUploadController - Handle file uploads and AI analysis
 * 
 * Endpoints:
 * - POST /ai-test/upload - Upload file and analyze with AI
 */
@RestController
@RequestMapping("/ai-test")
@RequiredArgsConstructor
@Log4j2
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final ChatService chatService;

    /**
     * Upload a file and analyze it with AI
     * 
     * @param file The uploaded file
     * @param task What to do with the file (e.g., "Analyze this log file", "Summarize this document")
     * @param sessionId Optional session ID for conversation continuity
     * @return AI's analysis of the file
     */
    @PostMapping("/upload")
    public ResponseEntity<FileAnalysisResponse> uploadAndAnalyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "task", defaultValue = "Analyze this file and tell me what it contains") String task,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        
        log.info("File upload received: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        long startTime = System.currentTimeMillis();
        
        try {
            // Generate session ID if not provided
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = "upload-" + UUID.randomUUID();
            }
            
            // Save file temporarily and get path
            String tempFilePath = fileUploadService.saveTemporaryFile(file);
            log.info("File saved temporarily at: {}", tempFilePath);
            
            // Read file content
            String fileContent = fileUploadService.readFileContent(tempFilePath);
            
            // Build enhanced task with file context
            String enhancedTask = buildEnhancedTask(task, file.getOriginalFilename(), fileContent);
            
            // Create request for AI
            AgentRequest request = new AgentRequest();
            request.setTask(enhancedTask);
            request.setSessionId(sessionId);
            
            // Send to AI for analysis
            String aiResponse = chatService.sendMessage(sessionId, request);
            
            // Clean up temporary file
            fileUploadService.deleteTemporaryFile(tempFilePath);
            log.info("Temporary file deleted: {}", tempFilePath);
            
            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Parse AI response to extract structured information
            AnalysisResult analysisResult = parseAiResponse(aiResponse);
            
            // Build file info
            FileAnalysisResponse.FileInfo fileInfo = FileAnalysisResponse.FileInfo.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileSizeFormatted(FileAnalysisResponse.formatFileSize(file.getSize()))
                    .fileType(getFileType(file.getOriginalFilename()))
                    .numberOfLines(countLines(fileContent))
                    .build();
            
            // Build analysis
            FileAnalysisResponse.Analysis analysis = FileAnalysisResponse.Analysis.builder()
                    .summary(analysisResult.summary)
                    .keyFindings(analysisResult.keyFindings)
                    .recommendations(analysisResult.recommendations)
                    .fullResponse(aiResponse)
                    .processingTimeMs(processingTime)
                    .build();
            
            // Build final response
            FileAnalysisResponse response = FileAnalysisResponse.builder()
                    .status("success")
                    .timestamp(LocalDateTime.now())
                    .sessionId(sessionId)
                    .fileInfo(fileInfo)
                    .analysis(analysis)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Conversation-Id", sessionId);
            
            return ResponseEntity.ok().headers(headers).body(response);
            
        } catch (Exception e) {
            log.error("Error processing uploaded file: {}", e.getMessage(), e);
            
            FileAnalysisResponse errorResponse = FileAnalysisResponse.builder()
                    .status("error")
                    .timestamp(LocalDateTime.now())
                    .sessionId(sessionId)
                    .error(FileAnalysisResponse.ErrorInfo.builder()
                            .code("FILE_PROCESSING_ERROR")
                            .message("Failed to process file")
                            .details(e.getMessage())
                            .build())
                    .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Build enhanced task with file context
     */
    private String buildEnhancedTask(String userTask, String fileName, String fileContent) {
        // Truncate content if too large (max 10,000 characters)
        String truncatedContent = fileContent;
        boolean wasTruncated = false;
        
        if (fileContent.length() > 10000) {
            truncatedContent = fileContent.substring(0, 10000);
            wasTruncated = true;
        }
        
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("User uploaded a file named: ").append(fileName).append("\n\n");
        enhanced.append("File content:\n");
        enhanced.append("```\n");
        enhanced.append(truncatedContent);
        enhanced.append("\n```\n\n");
        
        if (wasTruncated) {
            enhanced.append("(Note: File content was truncated to 10,000 characters)\n\n");
        }
        
        enhanced.append("User's request: ").append(userTask);
        
        return enhanced.toString();
    }

    /**
     * Upload multiple files and analyze together
     */
    @PostMapping("/upload/multiple")
    public ResponseEntity<?> uploadMultipleAndAnalyze(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "task", defaultValue = "Analyze these files") String task,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        
        log.info("Multiple files upload received: {} files", files.length);
        
        try {
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = "upload-multi-" + UUID.randomUUID();
            }
            
            StringBuilder combinedContent = new StringBuilder();
            combinedContent.append("User uploaded ").append(files.length).append(" files:\n\n");
            
            for (MultipartFile file : files) {
                String tempFilePath = fileUploadService.saveTemporaryFile(file);
                String fileContent = fileUploadService.readFileContent(tempFilePath);
                
                combinedContent.append("=== File: ").append(file.getOriginalFilename()).append(" ===\n");
                combinedContent.append(fileContent.length() > 5000 
                    ? fileContent.substring(0, 5000) + "\n...(truncated)" 
                    : fileContent);
                combinedContent.append("\n\n");
                
                fileUploadService.deleteTemporaryFile(tempFilePath);
            }
            
            combinedContent.append("User's request: ").append(task);
            
            AgentRequest request = new AgentRequest();
            request.setTask(combinedContent.toString());
            request.setSessionId(sessionId);
            
            String aiResponse = chatService.sendMessage(sessionId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileCount", files.length);
            response.put("analysis", aiResponse);
            response.put("sessionId", sessionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Conversation-Id", sessionId);
            
            return ResponseEntity.ok().headers(headers).body(response);
            
        } catch (Exception e) {
            log.error("Error processing uploaded files: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process files");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Helper class to hold parsed analysis results
     */
    private static class AnalysisResult {
        String summary;
        List<String> keyFindings;
        List<String> recommendations;
    }
    
    /**
     * Parse AI response to extract structured information
     * This tries to identify key sections in the AI's response
     */
    private AnalysisResult parseAiResponse(String aiResponse) {
        AnalysisResult result = new AnalysisResult();
        result.keyFindings = new ArrayList<>();
        result.recommendations = new ArrayList<>();
        
        // Try to extract summary (first paragraph or first 200 chars)
        String[] lines = aiResponse.split("\n");
        if (lines.length > 0) {
            result.summary = lines[0];
            if (result.summary.length() > 200) {
                result.summary = result.summary.substring(0, 200) + "...";
            }
        } else {
            result.summary = aiResponse.length() > 200 
                ? aiResponse.substring(0, 200) + "..." 
                : aiResponse;
        }
        
        // Try to extract key findings (lines with numbers, bullets, or "ERROR", "WARNING", etc.)
        Pattern findingPattern = Pattern.compile("^[\\d\\-\\*•]\\s+(.+)$|.*(ERROR|WARN|FAIL|ISSUE|PROBLEM).*", 
                Pattern.CASE_INSENSITIVE);
        for (String line : lines) {
            Matcher matcher = findingPattern.matcher(line.trim());
            if (matcher.find() && !line.trim().isEmpty()) {
                result.keyFindings.add(line.trim());
                if (result.keyFindings.size() >= 5) break; // Limit to 5 findings
            }
        }
        
        // Try to extract recommendations (lines with "should", "recommend", "suggest", etc.)
        Pattern recommendPattern = Pattern.compile(".*(should|recommend|suggest|consider|try|fix).*", 
                Pattern.CASE_INSENSITIVE);
        for (String line : lines) {
            Matcher matcher = recommendPattern.matcher(line.trim());
            if (matcher.find() && !line.trim().isEmpty() && !result.recommendations.contains(line.trim())) {
                result.recommendations.add(line.trim());
                if (result.recommendations.size() >= 3) break; // Limit to 3 recommendations
            }
        }
        
        return result;
    }
    
    /**
     * Get file type based on extension
     */
    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        switch (extension) {
            case "java": return "Java Source Code";
            case "log": return "Log File";
            case "txt": return "Text File";
            case "json": return "JSON Data";
            case "xml": return "XML Document";
            case "properties": return "Properties File";
            case "yml":
            case "yaml": return "YAML Configuration";
            case "sql": return "SQL Script";
            case "md": return "Markdown Document";
            case "html": return "HTML Document";
            case "css": return "CSS Stylesheet";
            case "js": return "JavaScript";
            case "ts": return "TypeScript";
            default: return extension.toUpperCase() + " File";
        }
    }
    
    /**
     * Count number of lines in content
     */
    private int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return content.split("\n").length;
    }
}

