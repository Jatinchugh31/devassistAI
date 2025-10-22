package com.devassist.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPooled;

/**
 * Configurable RAG Configuration
 * 
 * This configuration allows switching between different vector stores and RAG approaches
 * based on application properties.
 * 
 * Supported Vector Stores:
 * - SIMPLE: SimpleVectorStore (in-memory, slower)
 * - REDIS: RedisVectorStore (persistent, faster)
 * 
 * Supported RAG Approaches:
 * - QUESTION_ANSWER_ADVISOR: Spring AI built-in RAG
 * - VECTOR_STORE_DOCUMENT_RETRIEVER: Advanced retrieval with more control
 * - CUSTOM_PIPELINE: Custom RAG pipeline (future implementation)
 */
@Configuration
@RequiredArgsConstructor
@Log4j2
public class RagConfig {

    private final RagProperties ragProperties;
    private final EmbeddingModel embeddingModel;

    /**
     * Simple Vector Store Bean (when devassist.rag.vector-store.type=SIMPLE)
     */
    @Bean
    @ConditionalOnProperty(name = "devassist.rag.vector-store.type", havingValue = "SIMPLE")
    public VectorStore simpleVectorStore() {
        log.info("🔧 RAG: Configuring Simple Vector Store");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * Redis Vector Store Bean (when devassist.rag.vector-store.type=REDIS)
     */
    @Bean
    @ConditionalOnProperty(name = "devassist.rag.vector-store.type", havingValue = "REDIS")
    @Primary
    public VectorStore redisVectorStore() {
        log.info("🔧 RAG: Configuring Redis Vector Store");
        
        // Create JedisPooled connection
        JedisPooled jedisPooled = new JedisPooled("localhost", 6379);
        
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("devassist-index")
                .prefix("devassist:embedding:")
                .initializeSchema(true)
                .metadataFields(
                // textual fields: filePath might be more useful as TEXT (full text)
                RedisVectorStore.MetadataField.text("filePath"),
                // className, methodName and codeType are usually exact tokens; Tag is fine
                RedisVectorStore.MetadataField.tag("className"),
                RedisVectorStore.MetadataField.tag("methodName"),
                RedisVectorStore.MetadataField.tag("codeType"),
                // roles and tags are arrays in JSON — index the array elements as TAGs
                RedisVectorStore.MetadataField.tag("roles"),   // must be stored as JSON array in docs
                RedisVectorStore.MetadataField.tag("tags"),    // must be stored as JSON array in docs
                // boolean flags: store 'true'/'false' as TAG values (easy to query)
                RedisVectorStore.MetadataField.tag("isController"),
                RedisVectorStore.MetadataField.tag("isService"),
                RedisVectorStore.MetadataField.tag("isRepository"),
                // optional: project metadata for multi-project setups
                RedisVectorStore.MetadataField.tag("project")
        )
                .build();
    }

    /**
     * QuestionAnswerAdvisor Bean (when devassist.rag.approach.type=QUESTION_ANSWER_ADVISOR)
     */
    @Bean
    @ConditionalOnProperty(name = "devassist.rag.approach.type", havingValue = "QUESTION_ANSWER_ADVISOR")
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        log.info("🔧 RAG: Configuring QuestionAnswerAdvisor with {} vector store", 
                ragProperties.getVectorStore().getType());
        
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(org.springframework.ai.vectorstore.SearchRequest.builder()
                        .topK(8)
                        .similarityThreshold(0.0)
                        .build())
                .build();
    }

    /**
     * VectorStoreDocumentRetriever Bean (when devassist.rag.approach.type=VECTOR_STORE_DOCUMENT_RETRIEVER)
     * TODO: Implement advanced retrieval approach
     */
    @Bean
    @ConditionalOnProperty(name = "devassist.rag.approach.type", havingValue = "VECTOR_STORE_DOCUMENT_RETRIEVER")
    public Advisor vectorStoreDocumentRetriever(VectorStore vectorStore) {
        log.info("🔧 RAG: Configuring VectorStoreDocumentRetriever with {} vector store", 
                ragProperties.getVectorStore().getType());
        
        // TODO: Implement VectorStoreDocumentRetriever
        log.warn("⚠️ RAG: VectorStoreDocumentRetriever not yet implemented, falling back to QuestionAnswerAdvisor");
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();

    }

    /**
     * Custom Pipeline Bean (when devassist.rag.approach.type=CUSTOM_PIPELINE)
     * TODO: Implement custom RAG pipeline
     */
    @Bean
    @ConditionalOnProperty(name = "devassist.rag.approach.type", havingValue = "CUSTOM_PIPELINE")
    public Object customRagPipeline(VectorStore vectorStore) {
        log.info("🔧 RAG: Configuring Custom RAG Pipeline with {} vector store", 
                ragProperties.getVectorStore().getType());
        
        // TODO: Implement custom RAG pipeline
        log.warn("⚠️ RAG: Custom Pipeline not yet implemented, falling back to QuestionAnswerAdvisor");
        return questionAnswerAdvisor(vectorStore);
    }
}