package com.devassist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeDocument - Represents a piece of code for RAG processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeDocument {

    /** Unique identifier for this document chunk */
    private String id;

    /** File path where this code came from (relative to repo root) */
    private String filePath;

    /** Optional repository name (useful in multi-repo setups) */
    private String repo;

    /** Optional branch name */
    private String branch;

    /** Class name (extracted from file) */
    private String className;

    /** Method name (if this chunk contains a method) */
    private String methodName;

    /** Full method signature (return type + name + params) */
    private String methodSignature;

    /** The actual code content */
    private String content;

    /** Line numbers this chunk covers (e.g., "10-25") */
    private String lineRange;

    /** Type of code (class, method, field, imports, config, etc.) */
    private String codeType;

    /** Detected annotations (AST-extracted) e.g. RestController, Service */
    @Builder.Default
    private List<String> annotations = new ArrayList<>();

    /** Normalized role tags: controller, service, repository, constant, endpoint, config, etc. */
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    /** Generic tags/keywords (legacy + extra heuristics) */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /** Fast boolean convenience flags derived from roles/annotations */
    @Builder.Default
    private Boolean isController = false;
    @Builder.Default
    private Boolean isService = false;
    @Builder.Default
    private Boolean isRepository = false;

    /** When this document was created / indexed */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Size of the content in characters */
    @Builder.Default
    private int contentSize = 0;

    /** Estimated token count for LLM prompts (helpful to cap prompt length) */
    @Builder.Default
    private int tokenCount = 0;

    /** Precomputed importance/priority for retrieval/ranking */
    @Builder.Default
    private int priority = 0;

    /**
     * Helper to get a short description
     */
    public String getDescription() {
        if (methodName != null && !methodName.isEmpty()) {
            return className + "." + methodName + " (" + lineRange + ")";
        }
        return (className != null ? className : filePath) + " (" + lineRange + ")";
    }

    /** Helper to check if this is a method chunk */
    public boolean isMethod() {
        return methodName != null && !methodName.isEmpty();
    }

    /** Helper to get file extension */
    public String getFileExtension() {
        if (filePath == null) return "";
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(lastDot + 1) : "";
    }
}
