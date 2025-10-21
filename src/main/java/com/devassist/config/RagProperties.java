package com.devassist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RAG Configuration Properties
 * 
 * This class defines all configurable properties for the RAG system,
 * allowing users to choose between different vector stores and RAG approaches.
 */
@Configuration
@ConfigurationProperties(prefix = "devassist.rag")
@Data
public class RagProperties {

    /**
     * Whether RAG is enabled or disabled
     */
    private boolean enabled = true;

    /**
     * Vector Store Configuration
     */
    private VectorStore vectorStore = new VectorStore();

    /**
     * RAG Approach Configuration
     */
    private Approach approach = new Approach();

    @Data
    public static class VectorStore {
        /**
         * Type of vector store to use
         * Options: SIMPLE, REDIS
         */
        private Type type = Type.REDIS;

        public enum Type {
            SIMPLE, REDIS
        }
    }

    @Data
    public static class Approach {
        /**
         * Type of RAG approach to use
         * Options: QUESTION_ANSWER_ADVISOR, VECTOR_STORE_DOCUMENT_RETRIEVER, CUSTOM_PIPELINE
         */
        private Type type = Type.QUESTION_ANSWER_ADVISOR;

        public enum Type {
            QUESTION_ANSWER_ADVISOR,
            VECTOR_STORE_DOCUMENT_RETRIEVER,
            CUSTOM_PIPELINE
        }
    }
}

