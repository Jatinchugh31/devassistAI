package com.devassist.controller;


import com.devassist.model.AgentRequest;
import com.devassist.model.AiResponse;
import com.devassist.repository.RedisChatMemoryRepository;
import com.devassist.service.PromptBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
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

    private final ChatClient chatClient;
    private final PromptBuilderService promptBuilderService;
    private final RedisChatMemoryRepository memoryRepository;

    public ChatController(ChatClient chatClient,
                          PromptBuilderService promptBuilderService,
                          RedisChatMemoryRepository memoryRepository) {
        this.chatClient = chatClient;
        this.promptBuilderService = promptBuilderService;
        this.memoryRepository = memoryRepository;
    }

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

        String systemInstruction = promptBuilderService.buildSystemInstruction(request.getRole());
        final String finalSessionId = sessionId;
        try {
            log.info("Starting chat request for conversationId={}, role={}", finalSessionId, request.getRole());
            
            String aiResponse = chatClient.prompt()
                    .system(systemInstruction)
                    .user(request.getTask())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
                    .call()
                    .content();
            
            log.info("Chat response received for conversationId={}", finalSessionId);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Conversation-Id", sessionId);

            return ResponseEntity.ok().headers(headers).body(aiResponse);
        } catch (Exception ex) {
            log.error("AI call failed for conversationId={}: {}", sessionId, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("AI call failed: " + ex.getMessage());
        }
    }

    /**
     * Debug endpoint: returns raw JSON entries stored in Redis for the given conversationId.
     * Useful to inspect what messages are persisted by RedisChatMemoryRepository.
     */
    @GetMapping("/debug/memory/{conversationId}")
    public ResponseEntity<?> debugMemory(@PathVariable String conversationId) {
        try {
            List<String> raw = memoryRepository.readAll(conversationId);
            return ResponseEntity.ok(raw);
        } catch (Exception ex) {
            log.error("Failed to read memory for conversationId={}: {}", conversationId, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Failed to read memory: " + ex.getMessage());
        }
    }
}
