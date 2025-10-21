package com.devassist.service;

import com.devassist.model.AgentRequest;
import com.devassist.rag.service.LLMFilterAdvisorService;
import com.devassist.repository.RedisChatMemoryRepository;
import com.devassist.tools.FileSystemTool;
import com.devassist.tools.HttpTool;
import com.devassist.tools.SqlTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Chat Service - Core service for AI interactions with tools
 * 
 * This service provides the foundation for all AI interactions.
 * It's used by UnifiedChatService to handle the actual AI calls.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatService {

    private final SqlTool sqlTool;
    private final FileSystemTool fileSystemTool;
    private final HttpTool httpTool;
    private final ChatClient chatClient;
    private final PromptBuilderService promptBuilderService;
    private final RedisChatMemoryRepository memoryRepository;
    private final LLMFilterAdvisorService llmFilterAdvisorService;

    /**
     * Main method for sending messages to AI with tools
     */
    public String sendMessage(String sessionId, AgentRequest request) {
        String systemInstruction = promptBuilderService.buildSystemInstruction(request.getRole());
        final String finalSessionId = sessionId;
        try {
            log.info("Starting chat request for conversationId={}, role={}", finalSessionId, request.getRole());

            return chatClient.prompt()
                    .system(systemInstruction)
                    .user(request.getTask())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
                    .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION,llmFilterAdvisorService.generateFilterExpression(request.getTask())))
                    .call()
                    .content();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get chat history for a conversation
     */
    public List<String> readChatHistory(String conversationId) {
        return memoryRepository.readAll(conversationId);
    }
}