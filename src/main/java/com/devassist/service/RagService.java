package com.devassist.service;

import com.devassist.model.CodeDocument;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG Service - Spring AI Built-in Approach
 *
 * This implements Spring AI's built-in RAG approach:
 * 1. QuestionAnswerAdvisor handles vector search automatically
 * 2. PromptTemplate with placeholders for context
 * 3. Single ChatClient call with advisor
 * 4. Spring AI manages the entire RAG flow
 */
@Service
@Log4j2
public class RagService {
    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private DocumentLoaderService documentLoaderService;


    private volatile boolean isPopulated = false;

    /**
     * Load all project files into vector store (RAG step 1)
     * Performance optimized with filtering and batching
     */
    public void loadDocumentsIntoVectorStore() {
        log.info("🚀 RAG: Loading project documents into vector store (performance optimized)");

        try {
            List<CodeDocument> codeDocuments = documentLoaderService.loadAllProjectFiles();

            log.info("📄 RAG: Loaded {} code chunks from manual loader", codeDocuments.size());

            // Performance optimization 1: Filter relevant documents
            List<CodeDocument> filteredDocs = filterRelevantDocuments(codeDocuments);
            log.info("📄 RAG: Filtered to {} relevant documents (removed {})",
                    filteredDocs.size(), codeDocuments.size() - filteredDocs.size());

            // Thread-safe documents collection for parallel processing
            List<Document> documents = Collections.synchronizedList(new ArrayList<>());

            // Parallel conversion to Documents (safe because `documents` is synchronized)
            filteredDocs.parallelStream().forEach(codeDoc -> {
                try {
                    log.info("Tags found" + codeDoc.getTags() + codeDoc.getFilePath());
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("id", safeString(codeDoc.getId()));
                    meta.put("filePath", safeString(codeDoc.getFilePath()));
                    meta.put("repo", safeString(codeDoc.getRepo()));
                    meta.put("branch", safeString(codeDoc.getBranch()));
                    meta.put("className", safeString(codeDoc.getClassName()));
                    meta.put("methodName", codeDoc.getMethodName() != null ? codeDoc.getMethodName() : "");
                    meta.put("methodSignature", safeString(codeDoc.getMethodSignature()));
                    meta.put("codeType", safeString(codeDoc.getCodeType()));
                    meta.put("lineRange", safeString(codeDoc.getLineRange()));
                    meta.put("lang", safeString(codeDoc.getFileExtension()));
                    // tags (legacy)
                    meta.put("tags",  Optional.ofNullable(codeDoc.getTags()).orElse(Collections.emptyList()));
                    // roles (preferred)
                    meta.put("roles", Optional.ofNullable(codeDoc.getRoles()).orElse(Collections.emptyList()));
                    // annotations if present
                    meta.put("annotations", String.join(",", Optional.ofNullable(codeDoc.getAnnotations()).orElse(Collections.emptyList())));

                    // boolean convenience flags
                    boolean isController = Optional.ofNullable(codeDoc.getRoles()).orElse(Collections.emptyList())
                            .stream().anyMatch(t -> t.equalsIgnoreCase("controller") || t.equalsIgnoreCase("restcontroller"));
                    boolean isService = Optional.ofNullable(codeDoc.getRoles()).orElse(Collections.emptyList())
                            .stream().anyMatch(t -> t.equalsIgnoreCase("service"));
                    boolean isRepository = Optional.ofNullable(codeDoc.getRoles()).orElse(Collections.emptyList())
                            .stream().anyMatch(t -> t.equalsIgnoreCase("repository"));

                    meta.put("isController", Boolean.toString(isController));
                    meta.put("isService", Boolean.toString(isService));
                    meta.put("isRepository", Boolean.toString(isRepository));

                    // sizes / counts / priority
                    meta.put("contentSize", codeDoc.getContent() != null ? codeDoc.getContent().length() : 0);
                    meta.put("tokenCount", estimateTokenCount(codeDoc.getContent()));
                    meta.put("createdAt", codeDoc.getCreatedAt() != null ? codeDoc.getCreatedAt().toString() : LocalDateTime.now().toString());
                    meta.put("priority", getDocumentPriority(codeDoc));
                    meta.put("description", codeDoc.getDescription());

                    Document doc = Document.builder()
                            .text(codeDoc.getContent() != null ? codeDoc.getContent() : "")
                            .metadata(meta)
                            .build();

                    documents.add(doc);
                } catch (Exception ex) {
                    log.error("Failed to convert CodeDocument to Document for file: {}", codeDoc.getFilePath(), ex);
                }
            });

            log.info("📄 RAG: Converting {} documents to vector embeddings (batch processing)", documents.size());

            // Performance optimization 3: Add documents in batches
            int batchSize = 500; // Process 500 documents at a time
            for (int i = 0; i < documents.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, endIndex);

                log.info("📄 RAG: Processing batch {}/{} (documents {}-{})",
                        (i / batchSize) + 1, (documents.size() + batchSize - 1) / batchSize, i + 1, endIndex);

                vectorStore.add(batch);
            }

            isPopulated = true;
            log.info("✅ RAG: Successfully loaded {} project code chunks into vector store", documents.size());

        } catch (Exception e) {
            log.error("❌ RAG: Failed to load project files into vector store", e);
            throw new RuntimeException("Failed to load project files into vector store", e);
        }
    }

    /**
     * Filter documents to keep only the most relevant ones
     */
    private List<CodeDocument> filterRelevantDocuments(List<CodeDocument> documents) {
        return documents.stream()
                .filter(doc -> {
                    if (doc.getFilePath() != null && doc.getFilePath().endsWith(".java")) return true;
                    if (doc.getFilePath() != null && (doc.getFilePath().endsWith(".properties")
                            || doc.getFilePath().endsWith(".yml")
                            || doc.getFilePath().endsWith(".yaml"))) return true;
                    if (doc.getFilePath() != null && (doc.getFilePath().endsWith("build.gradle") || doc.getFilePath().endsWith("pom.xml")))
                        return true;


                    return true;
                })
                .sorted((a, b) -> Integer.compare(getDocumentPriority(b), getDocumentPriority(a)))
                .collect(Collectors.toList());
    }

    /**
     * Get document priority for sorting (higher = more important)
     */
    private int getDocumentPriority(CodeDocument doc) {
        String filePath = Optional.ofNullable(doc.getFilePath()).orElse("");
        if (filePath.endsWith(".java")) {
            if (filePath.contains("controller") || filePath.contains("service")
                    || filePath.contains("tool") || filePath.contains("config")) {
                return 90;
            }
            return 80;
        }
        if (filePath.endsWith(".properties") || filePath.endsWith(".yml")) {
            return 100;
        }
        if (filePath.endsWith("build.gradle")) {
            return 100;
        }
        return 20;
    }





    /* ===================== Helpers ===================== */

    private static String safeString(String s) {
        return s == null ? "" : s;
    }

    /**
     * Rough estimate of token count (conservative). Replace with an actual tokenizer if you need exactness.
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chars = text.length();
        double charEstimate = Math.ceil(chars / 4.0);
        String trimmed = text.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        double wordEstimate = Math.ceil(words * 1.3);
        int estimate = (int) Math.max(1, Math.max(charEstimate, wordEstimate));
        final int MAX_TOKENS_SANE = 20000;
        return Math.min(estimate, MAX_TOKENS_SANE);
    }
}
