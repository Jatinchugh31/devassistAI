package com.devassist.config;

import com.devassist.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * RAG Startup Configuration
 * 
 * This component automatically loads project documents into the vector store
 * when the application starts, enabling RAG functionality for all AI interactions.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class RagStartupConfig {

    private final RagService ragService;

    @Value("${devassist.rag.update.metaData}")
    boolean updateDocuments;
    /**
     * Load documents into vector store when application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadDocumentsOnStartup() {
        log.info("🚀 RAG Startup: Loading project documents into vector store...");
        
        try {
            if (updateDocuments) {
                ragService.loadDocumentsIntoVectorStore();
            }
            log.info("✅ RAG Startup: Successfully loaded documents. RAG is now active!");
            
        } catch (Exception e) {
            log.error("❌ RAG Startup: Failed to load documents. RAG will not be available.", e);
            // Don't fail the application startup - RAG is optional
        }
    }
}

