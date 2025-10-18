package com.devassist.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FileSystemTool: Multi-method tool for file operations
 * 
 * Demonstrates:
 * - Multiple @Tool methods in one class
 * - Each method = separate tool for AI
 * - Type-safe request/response objects
 * - Safe file access with validation
 */
@Component
@Log4j2
public class FileSystemTool {

    private final String baseDirectory;
    private final int maxFileSize = 1024 * 1024; // 1MB limit

    public FileSystemTool() {
        // Set base directory to project root for safety
        this.baseDirectory = System.getProperty("user.dir");
        log.info("FileSystemTool initialized with baseDirectory: {}", baseDirectory);
    }

    // ==================== TOOL 1: READ FILE ====================
    
    public record ReadFileRequest(
        @JsonProperty(value = "filePath", required = true)
        @JsonPropertyDescription("Relative path to the file (e.g., 'src/main/java/MyClass.java')")
        String filePath
    ) {}

    public record FileContent(
        String path,
        String content,
        long sizeBytes,
        int lineCount,
        String message
    ) {}

    @Tool(
        name = "read_file",
        description = "Read the contents of a file. Provide a relative path from the project root. " +
                     "Example: 'src/main/java/com/devassist/DevassistApplication.java'. " +
                     "Returns file content, size, and line count. Max file size: 1MB."
    )
    public FileContent readFile(ReadFileRequest request) {
        log.info("read_file tool called with request: {}", request);
        
        if (request == null || request.filePath() == null || request.filePath().isBlank()) {
            log.error("read_file received null or empty request");
            return new FileContent(
                "unknown",
                null,
                0,
                0,
                "Error: filePath is required"
            );
        }
        
        log.info("read_file processing path: {}", request.filePath());
        
        try {
            // Validate and resolve path
            Path fullPath = validateAndResolvePath(request.filePath());
            
            // Check file size
            long size = Files.size(fullPath);
            if (size > maxFileSize) {
                return new FileContent(
                    request.filePath(),
                    null,
                    size,
                    0,
                    "File too large: " + size + " bytes (max: " + maxFileSize + " bytes)"
                );
            }
            
            // Read content
            String content = Files.readString(fullPath);
            int lineCount = content.split("\n").length;
            
            log.info("Successfully read file: {} ({} bytes, {} lines)", request.filePath(), size, lineCount);
            
            return new FileContent(
                request.filePath(),
                content,
                size,
                lineCount,
                "File read successfully"
            );
            
        } catch (IOException e) {
            log.error("Error reading file: {}", request.filePath(), e);
            return new FileContent(
                request.filePath(),
                null,
                0,
                0,
                "Error: " + e.getMessage()
            );
        }
    }

    // ==================== TOOL 2: LIST FILES ====================
    
    public record ListFilesRequest(
        @JsonProperty(value = "directoryPath", required = true)
        @JsonPropertyDescription("Relative directory path (e.g., 'src/main/java' or '.' for root)")
        String directoryPath,
        
        @JsonProperty(value = "extension", required = false)
        @JsonPropertyDescription("Optional file extension filter (e.g., '.java', '.properties')")
        String extension
    ) {}

    public record FileItem(
        String name,
        String path,
        boolean isDirectory,
        long sizeBytes,
        String lastModified
    ) {}

    public record FileList(
        String directory,
        int fileCount,
        int directoryCount,
        List<FileItem> items,
        String message
    ) {}

    @Tool(
        name = "list_files",
        description = "List all files and directories in a given path. " +
                     "Provide relative path from project root (use '.' for root directory). " +
                     "Optionally filter by extension (e.g., '.java'). " +
                     "Returns list of files with metadata (name, size, type, last modified)."
    )
    public FileList listFiles(ListFilesRequest request) {
        log.info("list_files tool called with request: {}", request);
        
        if (request == null || request.directoryPath() == null || request.directoryPath().isBlank()) {
            log.error("list_files received null or empty request");
            return new FileList(
                "unknown",
                0, 0,
                List.of(),
                "Error: directoryPath is required"
            );
        }
        
        log.info("list_files processing directory: {}, extension: {}", 
                 request.directoryPath(), request.extension());
        
        try {
            // Validate and resolve path
            Path fullPath = validateAndResolvePath(request.directoryPath());
            
            if (!Files.isDirectory(fullPath)) {
                return new FileList(
                    request.directoryPath(),
                    0, 0,
                    List.of(),
                    "Error: Not a directory"
                );
            }
            
            // List files
            File[] files = fullPath.toFile().listFiles();
            if (files == null) {
                return new FileList(
                    request.directoryPath(),
                    0, 0,
                    List.of(),
                    "Error: Cannot read directory"
                );
            }
            
            // Filter and map to FileItem
            List<FileItem> items = Arrays.stream(files)
                .filter(f -> request.extension() == null || 
                            f.isDirectory() || 
                            f.getName().endsWith(request.extension()))
                .map(f -> {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(
                            f.toPath(), 
                            BasicFileAttributes.class
                        );
                        return new FileItem(
                            f.getName(),
                            request.directoryPath() + "/" + f.getName(),
                            f.isDirectory(),
                            f.length(),
                            Instant.ofEpochMilli(f.lastModified()).toString()
                        );
                    } catch (IOException e) {
                        return new FileItem(
                            f.getName(),
                            request.directoryPath() + "/" + f.getName(),
                            f.isDirectory(),
                            0,
                            "unknown"
                        );
                    }
                })
                .collect(Collectors.toList());
            
            int fileCount = (int) items.stream().filter(i -> !i.isDirectory()).count();
            int dirCount = (int) items.stream().filter(FileItem::isDirectory).count();
            
            log.info("Listed {} files and {} directories in {}", fileCount, dirCount, request.directoryPath());
            
            return new FileList(
                request.directoryPath(),
                fileCount,
                dirCount,
                items,
                "Listed successfully"
            );
            
        } catch (Exception e) {
            log.error("Error listing directory: {}", request.directoryPath(), e);
            return new FileList(
                request.directoryPath(),
                0, 0,
                List.of(),
                "Error: " + e.getMessage()
            );
        }
    }

    // ==================== TOOL 3: FILE INFO ====================
    
    public record FileInfoRequest(
        @JsonProperty(value = "path", required = true)
        @JsonPropertyDescription("Relative path to file or directory")
        String path
    ) {}

    public record FileInfo(
        String path,
        boolean exists,
        boolean isFile,
        boolean isDirectory,
        long sizeBytes,
        String created,
        String lastModified,
        String lastAccessed,
        boolean readable,
        boolean writable,
        String message
    ) {}

    @Tool(
        name = "file_info",
        description = "Get detailed metadata about a file or directory. " +
                     "Provide relative path from project root. " +
                     "Returns: existence, type, size, timestamps, permissions."
    )
    public FileInfo getFileInfo(FileInfoRequest request) {
        log.info("file_info tool called with path: {}", request.path());
        
        try {
            Path fullPath = validateAndResolvePath(request.path());
            
            if (!Files.exists(fullPath)) {
                return new FileInfo(
                    request.path(),
                    false, false, false,
                    0,
                    null, null, null,
                    false, false,
                    "File or directory does not exist"
                );
            }
            
            BasicFileAttributes attrs = Files.readAttributes(fullPath, BasicFileAttributes.class);
            
            FileInfo info = new FileInfo(
                request.path(),
                true,
                Files.isRegularFile(fullPath),
                Files.isDirectory(fullPath),
                attrs.size(),
                attrs.creationTime().toInstant().toString(),
                attrs.lastModifiedTime().toInstant().toString(),
                attrs.lastAccessTime().toInstant().toString(),
                Files.isReadable(fullPath),
                Files.isWritable(fullPath),
                "Info retrieved successfully"
            );
            
            log.info("Retrieved info for: {} (exists={}, isFile={}, size={})", 
                     request.path(), info.exists(), info.isFile(), info.sizeBytes());
            
            return info;
            
        } catch (Exception e) {
            log.error("Error getting file info: {}", request.path(), e);
            return new FileInfo(
                request.path(),
                false, false, false,
                0,
                null, null, null,
                false, false,
                "Error: " + e.getMessage()
            );
        }
    }

    // ==================== HELPER METHODS ====================
    
    private Path validateAndResolvePath(String relativePath) throws IOException {
        // Resolve relative path against base directory
        Path fullPath = Paths.get(baseDirectory, relativePath).normalize();
        
        // Security check: ensure path is within base directory
        if (!fullPath.startsWith(baseDirectory)) {
            throw new SecurityException("Access denied: path outside project directory");
        }
        
        return fullPath;
    }
}

