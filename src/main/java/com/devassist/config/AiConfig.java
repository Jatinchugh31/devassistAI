package com.devassist.config;

import com.devassist.advisor.LogAdvisor;
import com.devassist.model.RedisChatMemory;
import com.devassist.repository.RedisChatMemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory(RedisChatMemoryRepository repo, ObjectMapper mapper) {
        // RedisChatMemory constructor: RedisChatMemory(RedisChatMemoryRepository repo, ObjectMapper mapper)
        return new RedisChatMemory(repo, mapper);
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory, LogAdvisor logAdvisor) {
        // Build a ChatClient with multiple advisors:
        // 1. LogAdvisor (order=0) - Logs all requests/responses
        // 2. PromptChatMemoryAdvisor (order=10) - Manages conversation history
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                    logAdvisor,  // Executes first (order=0)
                    PromptChatMemoryAdvisor.builder(chatMemory).build()  // Executes second (order=10)
                )
                .build();
    }
}

