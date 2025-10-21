package com.devassist.advisor;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LogAdvisor - Comprehensive logging advisor for all AI interactions
 * 
 * Logs:
 * - User prompts
 * - System instructions
 * - Tool calls
 * - AI responses
 * - Token usage
 * - Response time
 * - Conversation ID
 * 
 * Execution Order: 0 (runs first, before all other advisors)
 */
@Component
@Log4j2
public class LogAdvisor implements CallAdvisor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 1; // Execute first (before all other advisors)
    }



    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        
        logRequest(timestamp, chatClientRequest);

        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        long duration = System.currentTimeMillis() - startTime;
        logResponse(timestamp, chatClientResponse, duration);
        
        return chatClientResponse;
    }

    private void logRequest(String timestamp, ChatClientRequest request) {
        log.info("╔═══════════════════════════════════════════════════════════════════════════════");
        log.info("║ 🤖 AI REQUEST");
        log.info("╠═══════════════════════════════════════════════════════════════════════════════");
        log.info("║ ⏰ Timestamp: {}", timestamp);
        log.info("║ 📋 Request: {}", request);
        log.info("╚═══════════════════════════════════════════════════════════════════════════════");
    }

    private void logResponse(String timestamp, ChatClientResponse response, long duration) {
        log.info("╔═══════════════════════════════════════════════════════════════════════════════");
        log.info("║ ✅ AI RESPONSE");
        log.info("╠═══════════════════════════════════════════════════════════════════════════════");
        log.info("║ ⏰ Timestamp: {}", timestamp);
        log.info("║ ⏱️  Duration: {} ms", duration);
        log.info("║ 📋 Response: {}", response);
        
        // Try to log token usage if available
        try {
            if (response.chatResponse() != null && response.chatResponse().getMetadata() != null) {
                var metadata = response.chatResponse().getMetadata();
                if (metadata.getUsage() != null) {
                    var usage = metadata.getUsage();
                    log.info("║ 🎯 Token Usage:");
                    log.info("║    • Prompt Tokens: {}", usage.getPromptTokens());
                    log.info("║    • Completion Tokens: {}", usage.getCompletionTokens());
                    log.info("║    • Total Tokens: {}", usage.getTotalTokens());
                    
                    // Calculate approximate cost (GPT-4 pricing)
                    double cost = calculateCost(usage.getPromptTokens(), usage.getCompletionTokens());
                    log.info("║    • Estimated Cost: ${}", String.format("%.4f", cost));
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract token usage: {}", e.getMessage());
        }
        
        log.info("╚═══════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Calculate approximate cost based on GPT-4 pricing
     * Input: $0.03 per 1K tokens
     * Output: $0.06 per 1K tokens
     */
    private double calculateCost(long promptTokens, long completionTokens) {
        double inputCost = (promptTokens / 1000.0) * 0.03;
        double outputCost = (completionTokens / 1000.0) * 0.06;
        return inputCost + outputCost;
    }
}

