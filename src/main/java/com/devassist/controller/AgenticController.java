package com.devassist.controller;

import com.devassist.model.AgenticRequest;
import com.devassist.model.AgenticResponse;
import com.devassist.service.AgenticService;
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

/**
 * AgenticController - EXPERIMENTAL/LEARNING ONLY
 * 
 * ⚠️ DEPRECATED: This endpoint is for learning/research purposes only.
 * ⚠️ Use `/chat` endpoint for production - it's 10x faster (~30s vs 5+ min).
 * 
 * Provides autonomous agent capabilities with:
 * - Planning: Break tasks into executable steps
 * - Reasoning: Think before acting (ReAct pattern)
 * - Execution: Execute tools with reasoning trail
 * - Reflection: Analyze results and learn
 * - Synthesis: Generate comprehensive answers
 * 
 * Performance: ~5+ minutes per query (vs ~30s for /chat)
 * Resource Usage: 8+ LLM calls per query (vs 1 for /chat)
 * 
 * See AGENTIC_LEARNINGS.md for performance comparison and learnings.
 */
@RestController
@RequestMapping("/chat/agentic")
@RequiredArgsConstructor
@Log4j2
@Deprecated
@Tag(name = "Agentic AI (Experimental)", description = "⚠️ EXPERIMENTAL: Learning/research only. Use /chat for production.")
public class AgenticController {
    
    private final AgenticService agenticService;
    
    /**
     * Agentic chat endpoint
     * POST /chat/agentic
     */
    @PostMapping(consumes = "application/json")
    @Operation(
        summary = "Agentic chat endpoint",
        description = "Process queries using agentic AI patterns with planning, reasoning, and execution. " +
                     "The agent will plan multi-step tasks, reason about each step, execute tools, " +
                     "reflect on results, and synthesize a comprehensive answer.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Agentic request with task and optional configuration",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AgenticRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Simple Task",
                        value = "{\"task\": \"Find errors in logs and show me the code\", \"sessionId\": \"agent-1\"}"
                    ),
                    @ExampleObject(
                        name = "With Options",
                        value = "{\"task\": \"Investigate system health\", \"sessionId\": \"agent-2\", " +
                               "\"options\": {\"showReasoning\": true, \"maxSteps\": 5, \"mode\": \"react\"}}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully processed agentic request",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AgenticResponse.class),
                    examples = @ExampleObject(
                        value = "{\"response\": \"Final answer...\", " +
                               "\"reasoning\": {\"plan\": {...}, \"reflection\": \"...\", \"synthesis\": \"...\"}, " +
                               "\"executionSteps\": [...], \"sessionId\": \"agent-1\", \"executionTime\": 2.5, \"totalSteps\": 3}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Failed to process agentic request",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AgenticResponse.class),
                    examples = @ExampleObject(
                        value = "{\"response\": \"Error: ...\", \"sessionId\": \"agent-1\", \"totalSteps\": 0, \"executionTime\": 0.0}"
                    )
                )
            )
        }
    )
    public ResponseEntity<AgenticResponse> agenticChat(
            @RequestBody AgenticRequest request) {
        
        log.info("🤖 AgenticController: Received request for session '{}'", 
                request.getSessionId());
        
        try {
            // Validate request
            if (request.getTask() == null || request.getTask().trim().isEmpty()) {
                throw new IllegalArgumentException("Task cannot be empty");
            }
            
            // Set default sessionId if not provided
            String sessionId = request.getSessionId() != null && !request.getSessionId().trim().isEmpty() ? 
                request.getSessionId() : 
                "agentic-" + System.currentTimeMillis();
            
            // Set default options if not provided
            if (request.getOptions() == null) {
                request.setOptions(new com.devassist.model.AgenticOptions());
            }
            
            // Process agentic query
            AgenticResponse response = agenticService.processAgenticQuery(
                request, 
                sessionId
            );
            
            log.info("✅ AgenticController: Generated response with {} steps in {}s", 
                    response.getTotalSteps(), response.getExecutionTime());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ AgenticController: Error processing request", e);
            
            // Return error response
            AgenticResponse errorResponse = AgenticResponse.builder()
                .response("Error: " + e.getMessage())
                .sessionId(request.getSessionId() != null ? request.getSessionId() : "unknown")
                .totalSteps(0)
                .executionTime(0.0)
                .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}


