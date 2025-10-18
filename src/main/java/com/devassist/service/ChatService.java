package com.devassist.service;

import com.devassist.model.AgentRequest;
import com.devassist.repository.RedisChatMemoryRepository;
import com.devassist.tools.FileSystemTool;
import com.devassist.tools.HttpTool;
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
    private final FileSystemTool fileSystemTool;
    private final HttpTool httpTool;
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

    /**
     * FileSystem Tool Endpoint
     * AI can read files, list directories, get file info
     */
    public String sendFileSystemToolMessage(String sessionId, AgentRequest request) {
        String systemInstruction = """
            You are FileSystemAgent: an expert code analyzer and file system navigator.
            
            You have access to file system tools:
            - read_file: Read file contents (max 1MB)
            - list_files: List directory contents with optional extension filter
            - file_info: Get detailed file metadata
            
            Your job is to:
            1. Navigate the project structure
            2. Read and analyze code files
            3. Find specific files or patterns
            4. Provide insights about code organization
            5. Answer questions about file contents
            
            Always use relative paths from project root (e.g., 'src/main/java/com/devassist/DevassistApplication.java').
            """;
        
        final String finalSessionId = sessionId;
        log.info("Starting FileSystem tool chat request for conversationId={}", finalSessionId);

        return chatClient.prompt()
                .system(systemInstruction)
                .user(request.getTask())
                .tools(fileSystemTool)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
                .call()
                .content();
    }

    /**
     * HTTP Tool Endpoint
     * AI can make external API calls
     */
    public String sendHttpToolMessage(String sessionId, AgentRequest request) {
        String systemInstruction = """
            You are ApiAgent: an expert at interacting with external APIs and web services.
            
            You have access to HTTP tools:
            - http_get: Make GET requests to fetch data
            - http_post: Make POST requests to submit data
            - check_url: Check if a URL is reachable
            
            Your job is to:
            1. Make HTTP requests to external APIs
            2. Parse and analyze API responses
            3. Check service health and availability
            4. Integrate external data into responses
            
            Always handle errors gracefully and explain API responses clearly.
            """;
        
        final String finalSessionId = sessionId;
        log.info("Starting HTTP tool chat request for conversationId={}", finalSessionId);

        return chatClient.prompt()
                .system(systemInstruction)
                .user(request.getTask())
                .tools(httpTool)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
                .call()
                .content();
    }

    /**
     * Multi-Tool Endpoint (ALL TOOLS)
     * AI can use SQL, FileSystem, and HTTP tools together
     * This demonstrates TOOL ORCHESTRATION
     */
    public String sendMultiToolMessage(String sessionId, AgentRequest request) {
        String systemInstruction = """
            You are DevAssist: a comprehensive developer assistant with access to multiple tools.
            
            Available Tools:
            
            SQL Tools:
            - sql_execute: Query the app_logs database
            
            FileSystem Tools:
            - read_file: Read file contents
            - list_files: List directory contents
            - file_info: Get file metadata
            
            HTTP Tools:
            - http_get: Fetch data from external APIs
            - http_post: Submit data to external APIs
            - check_url: Check URL reachability
            
            Your job is to:
            1. Analyze the user's request
            2. Decide which tool(s) to use
            3. Chain multiple tools if needed
            4. Provide comprehensive answers
            
            Examples of multi-tool scenarios:
            - "Find errors in logs and show me the related code file" → sql_execute + read_file
            - "Check if our API is up and query recent logs" → check_url + sql_execute
            - "List all Java files and analyze the main application class" → list_files + read_file
            
            Be smart about tool selection and chaining!
            """;
        
        final String finalSessionId = sessionId;
        log.info("Starting Multi-tool chat request for conversationId={}", finalSessionId);

        return chatClient.prompt()
                .system(systemInstruction)
                .user(request.getTask())
                .tools(sqlTool, fileSystemTool, httpTool)  // ← ALL TOOLS!
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalSessionId))
                .call()
                .content();
    }
}
