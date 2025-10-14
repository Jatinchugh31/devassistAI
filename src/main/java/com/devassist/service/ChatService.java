package com.devassist.service;

import com.devassist.model.AgentRequest;
import com.devassist.repository.RedisChatMemoryRepository;
import com.devassist.tools.SqlTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatService {

    private final SqlTool sqlTool;
    private final ChatClient chatClient;
    private final PromptBuilderService promptBuilderService;
    private final RedisChatMemoryRepository memoryRepository;

    public String sendMessage(String sessionId, AgentRequest request) {
        String systemInstruction = promptBuilderService.buildSystemInstruction(request.getRole());
        final String finalSessionId = sessionId;
        try {
            log.info("Starting chat request for conversationId={}, role={}", finalSessionId, request.getRole());

            return chatClient.prompt()
                    .system(systemInstruction)
                    .user(request.getTask())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
                    .call()
                    .content();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> readChatHisotry(String conversationId) {
        return memoryRepository.readAll(conversationId);
    }

    public String sendSqlToolMessage(String sessionId, AgentRequest request) {
        // Always use SQL role for SQL tool endpoint, with explicit table schema guidance
        String systemInstruction = """
            You are SqlAgent: an expert SQL performance engineer and log analyst.
            Focus on safety and optimization. Use READ-ONLY SQL when executing.
            
            IMPORTANT DATABASE SCHEMA:
            - Table name: 'app_logs' (plural, with 's' at the end)
            - Available columns: id, created_at, level, service, host, thread, request_id, user_id, logger, 
              exception_type, message, stack_trace, context, resolved, tags
            
            When querying logs, ALWAYS use 'app_logs' as the table name (not 'app_log').
            
            Your job is to:
            1. Query the app_logs table using the sql_execute tool
            2. Analyze the results
            3. Explain the root cause of errors
            4. Suggest solutions
            """;
        
        final String finalSessionId = sessionId;

        log.info("Starting SQL tool chat request for conversationId={}", finalSessionId);

        return chatClient.prompt()
                .system(systemInstruction)
                .user(request.getTask())
                .tools(sqlTool)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
                .call()
                .content();


    }
}
