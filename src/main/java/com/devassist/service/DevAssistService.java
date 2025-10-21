package com.devassist.service;

import com.devassist.constant.RoleType;
import com.devassist.model.AgentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * DevAssist Service - The core AI assistant service
 * 
 * This is the main service that powers DevAssist AI with intelligent capabilities:
 * 1. 🔍 RAG (Retrieval-Augmented Generation) - Code analysis and explanations
 * 2. 🛠️ Tools Integration - SQL execution, file operations, HTTP requests
 * 3. 📁 File Analysis - Upload and analyze files with AI
 * 4. 💬 Natural Chat - General conversation and assistance
 * 5. 🧠 Smart Context - Automatically enhances queries with relevant codebase context
 * 
 * The service intelligently combines these capabilities to provide comprehensive
 * developer assistance through a single, unified interface.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class DevAssistService {

    private final ChatModel chatModel;
    private final ChatService chatService; // For tool-based responses

    /**
     * Process user message with intelligent AI assistance
     * 
     * This method handles all types of developer queries by letting the AI decide:
     * - Whether to use RAG context (automatically available)
     * - Whether to use tools (SQL, file system, HTTP)
     * - Whether to combine both approaches
     * - Whether to use general chat
     * 
     * @param message The user's question or request
     * @param sessionId Session identifier for conversation tracking
     * @return AI-generated response with appropriate context and tool usage
     */
    public String processDeveloperQuery(String message, String sessionId) {
        log.info("🤖 DevAssist: Processing developer query '{}' with session '{}'", message, sessionId);
        
        try {
            // Simple approach: Let the AI decide everything
            // RAG context and Tools are both available to the AI
            // The AI will intelligently choose what to use
            AgentRequest agentRequest = new AgentRequest();
            agentRequest.setTask(message);
            agentRequest.setRole(RoleType.GENERAL);
            
            return chatService.sendMessage(sessionId, agentRequest);
            
        } catch (Exception e) {
            log.error("❌ DevAssist: Error processing developer query", e);
            return "I encountered an error while processing your request. Please try again.";
        }
    }

    /**
     * Analyze uploaded file with AI assistance
     * 
     * @param file The uploaded file to analyze
     * @param sessionId Session identifier for conversation tracking
     * @return AI analysis of the file content
     */
    public String analyzeFile(MultipartFile file, String sessionId) {
        log.info("📁 DevAssist: Analyzing file '{}' with session '{}'", file.getOriginalFilename(), sessionId);
        
        try {
            // Read file content
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            // Create analysis request
            String analysisRequest = String.format(
                "Please analyze this file: %s\n\nFile content:\n%s\n\nProvide:\n1. File type and purpose\n2. Key findings\n3. Any issues or recommendations",
                file.getOriginalFilename(),
                content
            );
            
            // Use the DevAssist service to analyze
            return processDeveloperQuery(analysisRequest, sessionId);
            
        } catch (IOException e) {
            log.error("❌ DevAssist: Error reading file", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    /**
     * Process developer query with file context
     * 
     * @param message The developer's question or request
     * @param file The uploaded file to include in context
     * @param sessionId Session identifier for conversation tracking
     * @return AI response combining the query and file analysis
     */
    public String processQueryWithFile(String message, MultipartFile file, String sessionId) {
        log.info("📁 DevAssist: Processing query + file - '{}' + '{}'", message, file.getOriginalFilename());
        
        try {
            // Read file content
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            // Combine message with file content
            String combinedRequest = String.format(
                "%s\n\nFile: %s\nFile content:\n%s",
                message,
                file.getOriginalFilename(),
                content
            );
            
            // Use the DevAssist service to process
            return processDeveloperQuery(combinedRequest, sessionId);
            
        } catch (IOException e) {
            log.error("❌ DevAssist: Error reading file", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    /**
     * Get DevAssist system capabilities and status
     * 
     * @return Map containing system information, capabilities, and endpoints
     */
    public Map<String, Object> getSystemCapabilities() {
        return Map.of(
            "status", "success",
            "message", "DevAssist AI is running and ready to help!",
            "service", "DevAssist Core Service",
            "description", "Intelligent developer assistant with RAG, Tools, File Analysis, and Natural Chat",
            "capabilities", Map.of(
                "rag", "🔍 Code analysis and explanations with codebase context",
                "tools", "🛠️ SQL execution, File System operations, HTTP requests", 
                "fileAnalysis", "📁 Upload and analyze files with AI",
                "naturalChat", "💬 General conversation and developer assistance",
                "smartContext", "🧠 Automatic context enhancement for better responses"
            ),
            "endpoints", Map.of(
                "chat", "/chat - Text-based queries and assistance",
                "fileUpload", "/chat/upload - File upload and analysis",
                "status", "/chat/status - System capabilities and status"
            ),
            "sessionId", "devassist-system"
        );
    }
}
