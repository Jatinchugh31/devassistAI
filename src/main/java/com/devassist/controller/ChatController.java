package com.devassist.controller;

import com.devassist.model.AgentRequest;
import com.devassist.model.AiResponse;
import com.devassist.service.PromptBuilderService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;
    private final PromptBuilderService promptBuilderService;

    public ChatController(ChatClient chatClient, PromptBuilderService promptBuilderService) {
        this.chatClient = chatClient;
        this.promptBuilderService = promptBuilderService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AgentRequest request) {
        try {
            var sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = "anon-" + UUID.randomUUID();
            }

            String systemInstruction = promptBuilderService.buildSystemInstruction(request.getRole());
            final  String finaSessionId = sessionId;
            // Use conversationId so MessageChatMemoryAdvisor injects history automatically
            String aiResponse = chatClient.prompt()
                    .advisors(advisorSpec -> {advisorSpec.param(ChatMemory.CONVERSATION_ID,finaSessionId);})
                    .system(systemInstruction)
                    .user(request.getTask())
                    .call()
                    .content();

            // return aiResponse plus sessionId so client can continue session
            return ResponseEntity.ok().header("X-Conversation-Id", sessionId).body(aiResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("AI call failed: " + e.getMessage());
        }
    }
}
