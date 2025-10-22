package com.devassist.config;

import com.devassist.advisor.LogAdvisor;
import com.devassist.model.RedisChatMemory;
import com.devassist.repository.RedisChatMemoryRepository;
import com.devassist.tools.FileSystemTool;
import com.devassist.tools.HttpTool;
import com.devassist.tools.SqlTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory(RedisChatMemoryRepository repo, ObjectMapper mapper) {
        return new RedisChatMemory(repo, mapper);
    }

    @Bean
    @ConditionalOnProperty(name = "devassist.rag.enabled", havingValue = "true")
    @ConditionalOnProperty(name = "devassist.rag.approach.type", havingValue = "QUESTION_ANSWER_ADVISOR")
    public ChatClient chatClientWithRag(ChatModel chatModel, ChatMemory chatMemory, LogAdvisor logAdvisor,
                                        SqlTool sqlTool, FileSystemTool fileSystemTool, HttpTool httpTool,
                                        VectorStore vectorStore) {
        // Build a ChatClient with RAG + Tools + Memory:
        // 1. LogAdvisor (order=0) - Logs all requests/responses
        // 2. QuestionAnswerAdvisor (RAG) - Provides codebase context automatically
        // 3. PromptChatMemoryAdvisor (order=10) - Manages conversation history
        // 4. Tools - SQL, FileSystem, HTTP tools for AI to use

        QuestionAnswerAdvisor questionAnswerAdvisor1 = QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(50).similarityThreshold(0.6).build())
                .build();
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        logAdvisor,  // Executes first (order=0)
                        questionAnswerAdvisor1,  // RAG context (order=5)
                        PromptChatMemoryAdvisor.builder(chatMemory).build()  // Executes last (order=10)
                )
                .defaultTools(sqlTool, fileSystemTool, httpTool)  // Register all tools
                .build();
    }


    @Bean
    @ConditionalOnProperty(name = "devassist.rag.enabled", havingValue = "true")
    @ConditionalOnProperty(name = "devassist.rag.approach.type", havingValue = "VECTOR_STORE_DOCUMENT_RETRIEVER")
    public ChatClient chatClientWithRagWithRetrieval(ChatModel chatModel, ChatMemory chatMemory, LogAdvisor logAdvisor,
                                                     SqlTool sqlTool, FileSystemTool fileSystemTool, HttpTool httpTool,
                                                     VectorStore vectorStore) {


        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.65)
                        .vectorStore(vectorStore)
                        .topK(50)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .promptTemplate(new PromptTemplate("""
                                Context information is below.
                                
                                ---------------------
                                {context}
                                ---------------------
                                
                                Given the context information and no prior knowledge, answer the query.
                                
                                Follow these rules:
                                
                                1. If the answer is not in the context, just say that you don't know.
                                2. Avoid statements like "Based on the context..." or "The provided information...".
                                3. If Question or query is genric Like some Theory/concept tutorial then you can skip context and Answer
                                Query: {query}
                                
                                Answer:
                                """)
                        )
                        .allowEmptyContext(true)
                        .build())
                .build();

        // Build a ChatClient with RAG + Tools + Memory:
        // 1. LogAdvisor (order=0) - Logs all requests/responses
        // 2. QuestionAnswerAdvisor (RAG) - Provides codebase context automatically
        // 3. PromptChatMemoryAdvisor (order=10) - Manages conversation history
        // 4. Tools - SQL, FileSystem, HTTP tools for AI to use
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        logAdvisor,  // Executes first (order=0)
                        retrievalAugmentationAdvisor,  // RAG context (order=5)
                        PromptChatMemoryAdvisor.builder(chatMemory).build()  // Executes last (order=10)
                )
                .defaultTools(sqlTool, fileSystemTool, httpTool)  // Register all tools
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "devassist.rag.enabled", havingValue = "false", matchIfMissing = false)
    public ChatClient chatClientWithoutRag(ChatModel chatModel, ChatMemory chatMemory, LogAdvisor logAdvisor,
                                           SqlTool sqlTool, FileSystemTool fileSystemTool, HttpTool httpTool) {
        // Build a ChatClient with Tools + Memory (no RAG):
        // 1. LogAdvisor (order=0) - Logs all requests/responses
        // 2. PromptChatMemoryAdvisor (order=10) - Manages conversation history
        // 3. Tools - SQL, FileSystem, HTTP tools for AI to use
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        logAdvisor,  // Executes first (order=0)
                        PromptChatMemoryAdvisor.builder(chatMemory).build()  // Executes second (order=10)
                )
                .defaultTools(sqlTool, fileSystemTool, httpTool)  // Register all tools
                .build();
    }
}