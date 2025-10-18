package com.devassist.controller;


import com.devassist.model.AgentRequest;
import com.devassist.model.AiResponse;
import com.devassist.repository.RedisChatMemoryRepository;
import com.devassist.service.ChatService;
import com.devassist.service.PromptBuilderService;
import com.devassist.tools.SqlTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ChatController - main entrypoints for DevAssist AI interactions.
 *
 * POST /ai/chat       - send a message (body: AgentRequest). Returns AiResponse and X-Conversation-Id header.
 * GET  /ai/debug/memory/{conversationId} - inspect raw messages stored in Redis for the conversation.
 */
@RestController
@RequestMapping("/ai-test")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    ChatService chatService;

    /**
     * Primary chat endpoint.
     * Accepts an AgentRequest (role, task/message, optional sessionId). Returns structured AiResponse.
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AgentRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "anon-" + UUID.randomUUID();
            log.debug("Generated ephemeral sessionId={}", sessionId);
        }

        String aiResponse = chatService.sendMessage(sessionId, request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Conversation-Id", sessionId);

        return ResponseEntity.ok().headers(headers).body(aiResponse);
    }


    /**
     * Debug endpoint: returns raw JSON entries stored in Redis for the given conversationId.
     * Useful to inspect what messages are persisted by RedisChatMemoryRepository.
     */
    @GetMapping("/debug/memory/{conversationId}")
    public ResponseEntity<?> debugMemory(@PathVariable String conversationId) {
        try {
            List<String> raw = chatService.readChatHisotry(conversationId);
            return ResponseEntity.ok(raw);
        } catch (Exception ex) {
            log.error("Failed to read memory for conversationId={}: {}", conversationId, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Failed to read memory: " + ex.getMessage());
        }
    }



    /**
     * SQL Tool Endpoint
     * AI can query database logs
     */
    @PostMapping("/chat/sql")
    public ResponseEntity<?> chatSql(@RequestBody AgentRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "anon-" + UUID.randomUUID();
            log.debug("Generated ephemeral sessionId={}", sessionId);
        }

        String aiResponse = chatService.sendSqlToolMessage(sessionId, request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Conversation-Id", sessionId);

        return ResponseEntity.ok().headers(headers).body(aiResponse);
    }

    /**
     * FileSystem Tool Endpoint
     * AI can read files, list directories, get file info
     */
    @PostMapping("/chat/filesystem")
    public ResponseEntity<?> chatFileSystem(@RequestBody AgentRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "anon-" + UUID.randomUUID();
            log.debug("Generated ephemeral sessionId={}", sessionId);
        }

        String aiResponse = chatService.sendFileSystemToolMessage(sessionId, request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Conversation-Id", sessionId);

        return ResponseEntity.ok().headers(headers).body(aiResponse);
    }

    /**
     * HTTP Tool Endpoint
     * AI can make external API calls
     */
    @PostMapping("/chat/http")
    public ResponseEntity<?> chatHttp(@RequestBody AgentRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "anon-" + UUID.randomUUID();
            log.debug("Generated ephemeral sessionId={}", sessionId);
        }

        String aiResponse = chatService.sendHttpToolMessage(sessionId, request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Conversation-Id", sessionId);

        return ResponseEntity.ok().headers(headers).body(aiResponse);
    }

    /**
     * Multi-Tool Endpoint (ALL TOOLS)
     * AI can use SQL, FileSystem, and HTTP tools together
     * Demonstrates TOOL ORCHESTRATION and CHAINING
     */
    @PostMapping("/chat/multi-tool")
    public ResponseEntity<?> chatMultiTool(@RequestBody AgentRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "anon-" + UUID.randomUUID();
            log.debug("Generated ephemeral sessionId={}", sessionId);
        }

        String aiResponse = chatService.sendMultiToolMessage(sessionId, request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Conversation-Id", sessionId);

        return ResponseEntity.ok().headers(headers).body(aiResponse);
    }
}
