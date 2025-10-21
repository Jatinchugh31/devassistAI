package com.devassist.controller;

import com.devassist.model.AiResponse;
import com.devassist.model.ChatRequest;
import com.devassist.service.DevAssistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * DevAssist Controller - The main API endpoint for DevAssist AI
 * 
 * This controller provides the primary interface for DevAssist AI capabilities:
 * 🔍 RAG (Retrieval-Augmented Generation) - Code analysis and explanations
 * 🛠️ Tools Integration - SQL execution, file operations, HTTP requests  
 * 📁 File Analysis - Upload and analyze files with AI
 * 💬 Natural Chat - General conversation and developer assistance
 * 🧠 Smart Context - Automatic context enhancement for better responses
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "DevAssist AI", description = "Intelligent developer assistant with RAG, Tools, File Analysis, and Natural Chat capabilities")
public class DevAssistController {

    private final DevAssistService devAssistService;

    /**
     * Text-only chat endpoint
     * POST /chat
     */
    @PostMapping(consumes = "application/json")
    @Operation(summary = "Text chat endpoint",
            description = "Process text-only queries using RAG, Tools, or general chat.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Chat request with message and optional session ID",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatRequest.class),
                            examples = {
                                    @ExampleObject(name = "RAG Example", value = "{\"task\": \"What is SqlTool?\", \"sessionId\": \"chat-1\"}"),
                                    @ExampleObject(name = "SQL Tool Example", value = "{\"task\": \"Execute SQL: SELECT * FROM app_logs LIMIT 3\", \"sessionId\": \"chat-2\"}"),
                                    @ExampleObject(name = "Hybrid Example", value = "{\"task\": \"What is SqlTool and show me its code?\", \"sessionId\": \"chat-3\"}")
                            }
                    )),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully processed chat request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AiResponse.class),
                                    examples = @ExampleObject(value = "{\"summary\": \"Response from AI...\", \"role\": \"UNIFIED\"}"))),
                    @ApiResponse(responseCode = "500", description = "Failed to process chat request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AiResponse.class),
                                    examples = @ExampleObject(value = "{\"summary\": \"Error processing request\", \"role\": \"ERROR\"}")))
            })
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        log.info("🤖 Unified Chat: Processing text request '{}'", request.getTask());
        
        try {
            String response = devAssistService.processDeveloperQuery(request.getTask(), request.getSessionId());

            
            log.info("✅ Unified Chat: Generated response");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Unified Chat: Failed to process request", e);
            

            
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /**
     * File upload endpoint (with optional text)
     * POST /chat/upload
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "File upload endpoint",
            description = "Upload files with optional text for analysis. Supports file-only or text+file combinations.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Multipart form with file and optional task",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "object"),
                            examples = {
                                    @ExampleObject(name = "File Only", value = "Form data with file field"),
                                    @ExampleObject(name = "Text + File", value = "Form data with task, file, and sessionId fields")
                            }
                    )),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully processed file upload",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AiResponse.class),
                                    examples = @ExampleObject(value = "{\"summary\": \"File analysis results...\", \"role\": \"UNIFIED\"}"))),
                    @ApiResponse(responseCode = "500", description = "Failed to process file upload",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AiResponse.class),
                                    examples = @ExampleObject(value = "{\"summary\": \"Error processing file\", \"role\": \"ERROR\"}")))
            })
    public ResponseEntity<AiResponse> upload(
            @RequestParam(value = "task", required = false) String task,
            @RequestParam(value = "sessionId", defaultValue = "file-session") String sessionId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("📁 Unified Chat: Processing file upload - task: '{}', file: '{}', session: '{}'", 
                task, file.getOriginalFilename(), sessionId);
        
        try {
            String response;
            
            if (task != null && !task.trim().isEmpty()) {
                // Both text and file
                response = devAssistService.processQueryWithFile(task, file, sessionId);
            } else {
                // File only
                response = devAssistService.analyzeFile(file, sessionId);
            }
            
            AiResponse aiResponse = new AiResponse();
            aiResponse.setSummary(response);
            aiResponse.setRole("UNIFIED");
            
            log.info("✅ Unified Chat: Generated response for file upload");
            return ResponseEntity.ok(aiResponse);
            
        } catch (Exception e) {
            log.error("❌ Unified Chat: Failed to process file upload", e);
            
            AiResponse errorResponse = new AiResponse();
            errorResponse.setSummary("Error processing file: " + e.getMessage());
            errorResponse.setRole("ERROR");
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    /**
     * System status endpoint
     * GET /chat/status
     */
    @GetMapping("/status")
    @Operation(summary = "Get system status",
            description = "Returns the current status and capabilities of the unified chat system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "System status retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = "{\"status\": \"success\", \"message\": \"Unified Chat System is running\", \"features\": {\"rag\": \"Code analysis\", \"tools\": \"SQL, FileSystem, HTTP\", \"general\": \"Basic chat\"}}")))
            })
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("📊 Getting system status");
        
        Map<String, Object> status = devAssistService.getSystemCapabilities();
        return ResponseEntity.ok(status);
    }
}